using Jp.Digitalmuseum.Kinect;
using Microsoft.Kinect;
using Microsoft.Speech.AudioFormat;
using Microsoft.Speech.Recognition;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading;
using Thrift.Collections;

namespace ConsoleSkeletonServer
{
    class KinectServiceHandler : KinectService.Iface
    {
        #region Private State
        private SpeechRecognitionEngine sre;
        private KinectAudioSource kinectAudioSource;
        private THashSet<string> recognizedPhrases;
        private HashSet<string> keywords;
        private byte[] imageData;
        private Skeleton[] skeletonData;
        private Frame frame;
        #endregion Private State

        #region Thrift server implementation
        public KinectServiceHandler()
        {
            KinectStart();
            InitializeSpeechRecognition(Kinect);
        }

        public void addKeyword(string text)
        {
            keywords.Add(text);
            UpdateGrammar();
        }

        public void removeKeyword(string text)
        {
            keywords.Remove(text);
            UpdateGrammar();
        }

        private void UpdateGrammar() {
            GrammarBuilder gb = new GrammarBuilder();

            // This is needed to ensure that it will work on machines with any culture, not just en-us.
            gb.Culture = sre.RecognizerInfo.Culture;

            string[] keywordsArray = keywords.ToArray();
            gb.Append(new Choices(keywordsArray));

            sre.LoadGrammarAsync(new Grammar(gb));
        }

        public THashSet<string> getKeywords()
        {
            var keywords = new THashSet<string>();
            foreach (string keyword in this.keywords) {
                keywords.Add(keyword);
            }
            return keywords;
        }

        private int currentAngle = int.MaxValue;
        public void setAngle(int angle)
        {
            new Thread(() =>
            {
                lock (Kinect) {
                    currentAngle = Kinect.ElevationAngle;
                    Kinect.ElevationAngle = angle;
                    currentAngle = int.MaxValue;
                }
            }).Start();
        }

        public int getAngle()
        {
            if (currentAngle != int.MaxValue) {
                return currentAngle;
            }
            return Kinect.ElevationAngle;
        }

        public Frame getFrame()
        {
            recognizedPhrases.Clear();
            return frame;
        }

        public void stop()
        {
            if (Shutdown != null)
            {
                Shutdown();
            }
        }

        public delegate void ShutdownAction();
        public ShutdownAction Shutdown { internal set; get; }
        #endregion Thrift server implementation


        #region Kinect discovery + setup

        // Kinect enabled apps should customize each message and replace the technique of displaying the message.
        private void ShowStatus(ErrorCondition errorCondition)
        {
            string statusMessage;
            switch (errorCondition)
            {
                case ErrorCondition.None:
                    statusMessage = null;
                    break;
                case ErrorCondition.NoKinect:
                    statusMessage = Properties.Resources.NoKinectError;
                    break;
                case ErrorCondition.NoPower:
                    statusMessage = Properties.Resources.NoPowerError;
                    break;
                case ErrorCondition.NoSpeech:
                    statusMessage = Properties.Resources.NoSpeechError;
                    break;
                case ErrorCondition.NotReady:
                    statusMessage = Properties.Resources.NotReady;
                    break;
                case ErrorCondition.Initializing:
                    statusMessage = Properties.Resources.Initializing;
                    break;
                case ErrorCondition.KinectAppConflict:
                    statusMessage = Properties.Resources.KinectAppConflict;
                    break;
                case ErrorCondition.InsufficientBandwidth:
                    statusMessage = Properties.Resources.InsufficientBandwidth;
                    break;
                default:
                    statusMessage = errorCondition.ToString();
                    break;
            }
            Console.WriteLine(statusMessage);
            currentErrorCondition = errorCondition;
        }

        // Kinect enabled apps should customize which Kinect services it initializes here.
        private KinectSensor InitializeKinectServices(KinectSensor sensor)
        {
            // Application should enable all streams first.
            sensor.ColorStream.Enable(ColorImageFormat.RgbResolution640x480Fps30);
            sensor.DepthStream.Enable(DepthImageFormat.Resolution320x240Fps30);
            imageData = new byte[640*480*4];

            // sensor.SkeletonStream.TransformSmooth = true;
            sensor.AllFramesReady += new EventHandler<AllFramesReadyEventArgs>(AllFramesReady);
            sensor.SkeletonStream.Enable();

            try
            {
                sensor.Start();
            }
            catch (IOException)
            {
                ShowStatus(ErrorCondition.KinectAppConflict);
                return null;
            }
            return sensor;
        }

        //Kinect enabled apps should uninitialize all Kinect services that were initialized in InitializeKinectServices() here.
        private void UninitializeKinectServices(KinectSensor sensor)
        {
            sensor.Stop();

            sensor.AllFramesReady -= new EventHandler<AllFramesReadyEventArgs>(AllFramesReady);

            StopSpeechRecognition();
        }

        #region Most apps won't modify this code
        private void KinectStart()
        {
            KinectDiscovery();
            if (Kinect == null)
            {
                if (KinectSensor.KinectSensors.Count == 0)
                {
                    ShowStatus(ErrorCondition.NoKinect);
                }
                else
                {
                    KinectStatus sensorStatus = KinectSensor.KinectSensors[0].Status;
                    switch (sensorStatus)
                    {
                        case KinectStatus.InsufficientBandwidth:
                            ShowStatus(ErrorCondition.InsufficientBandwidth);
                            break;
                        case KinectStatus.NotPowered:
                            ShowStatus(ErrorCondition.NoPower);
                            break;
                        default:
                            ShowStatus(ErrorCondition.NotReady);
                            break;
                    }
                }
            }
        }

        private void KinectStop()
        {
            if (Kinect != null)
            {
                Kinect = null;
            }
        }

        private bool IsKinectStarted
        {
            get { return Kinect != null; }
        }

        private void KinectDiscovery()
        {
            //listen to any status change for Kinects
            KinectSensor.KinectSensors.StatusChanged += new EventHandler<StatusChangedEventArgs>(Kinects_StatusChanged);

            //loop through all the Kinects attached to this PC, and start the first that is connected without an error.
            foreach (KinectSensor kinectSensor in KinectSensor.KinectSensors)
            {
                if (kinectSensor.Status == KinectStatus.Connected)
                {
                    if (Kinect == null)
                    {
                        Kinect = kinectSensor;
                        return;
                    }
                }
            }
        }

        private void Kinects_StatusChanged(object sender, StatusChangedEventArgs e)
        {
            switch (e.Status)
            {
                case KinectStatus.Initializing:
                    ShowStatus(ErrorCondition.Initializing);
                    break;
                case KinectStatus.Connected:
                    if (Kinect == null)
                    {
                        Kinect = e.Sensor; //if KinectSensor.Init() fails due to an AppDeviceConflict, this property will be null after return.
                        ShowStatus(ErrorCondition.None);
                    }
                    break;
                case KinectStatus.Disconnected:
                case KinectStatus.DeviceNotGenuine:
                    if (Kinect == e.Sensor)
                    {
                        Kinect = null;
                    }
                    break;
                case KinectStatus.NotReady:
                    if (Kinect == null)
                    {
                        ShowStatus(ErrorCondition.NotReady);
                    }
                    break;
                case KinectStatus.NotPowered:
                    if (Kinect == e.Sensor)
                    {
                        Kinect = null;
                        ShowStatus(ErrorCondition.NoPower);
                    }
                    break;
                case KinectStatus.InsufficientBandwidth:
                    if (Kinect == e.Sensor)
                    {
                        Kinect = null;
                        ShowStatus(ErrorCondition.InsufficientBandwidth);
                    }
                    break;
                default:
                    throw new Exception("Unhandled Status: " + e.Status);
            }
            if (Kinect == null)
            {
                ShowStatus(ErrorCondition.NoKinect);
            }
        }

        public KinectSensor Kinect
        {
            get
            {
                return _Kinect;
            }
            set
            {
                if (_Kinect != null)
                {
                    UninitializeKinectServices(_Kinect);
                }
                _Kinect = value;
                if (_Kinect != null)
                {
                    _Kinect = InitializeKinectServices(_Kinect);
                }
            }
        }

        private KinectSensor _Kinect;
        private ErrorCondition currentErrorCondition;

        internal enum ErrorCondition
        {
            None,
            Initializing,
            NoPower,
            NoKinect,
            NoSpeech,
            NotReady,
            KinectAppConflict,
            InsufficientBandwidth,
        }
        #endregion Most apps won't modify this code

        #endregion Kinect discovery + setup

        #region Speech recognition
        private void InitializeSpeechRecognition(KinectSensor sensor)
        {
            sre = new SpeechRecognitionEngine(GetKinectRecognizer());
            keywords = new HashSet<string>();
            keywords.Add("Capture");

            UpdateGrammar();

            StartSpeechRecognition(sensor.AudioSource);
        }

        private static RecognizerInfo GetKinectRecognizer()
        {
            Func<RecognizerInfo, bool> matchingFunc = r =>
            {
                string value;
                r.AdditionalInfo.TryGetValue("Kinect", out value);
                return "True".Equals(value, StringComparison.InvariantCultureIgnoreCase) && "en-US".Equals(r.Culture.Name, StringComparison.InvariantCultureIgnoreCase);
            };
            return SpeechRecognitionEngine.InstalledRecognizers().Where(matchingFunc).FirstOrDefault();
        }

        public void StartSpeechRecognition(KinectAudioSource kinectSource)
        {
            kinectAudioSource = kinectSource;
            kinectAudioSource.AutomaticGainControlEnabled = false;
            kinectAudioSource.BeamAngleMode = BeamAngleMode.Adaptive;

            Stream kinectStream = kinectAudioSource.Start();
            sre.SetInputToAudioStream(kinectStream,
                new SpeechAudioFormatInfo(
                    EncodingFormat.Pcm, 16000, 16, 1, 32000, 2, null));
            sre.RecognizeAsync(RecognizeMode.Multiple);
            sre.SpeechRecognized += new EventHandler<SpeechRecognizedEventArgs>(sre_SpeechRecognized);

            recognizedPhrases = new THashSet<string>();
        }

        void sre_SpeechRecognized(object sender, SpeechRecognizedEventArgs e)
        {
            recognizedPhrases.Add(e.Result.Text);
        }

        public void StopSpeechRecognition()
        {
            if (sre != null)
            {
                kinectAudioSource.Stop();
                sre.RecognizeAsyncCancel();
                sre.RecognizeAsyncStop();
            }
        }
        #endregion Speech recognition

        #region Kinect Skeleton processing
        void AllFramesReady(object sender, AllFramesReadyEventArgs e)
        {
            // Construct data.
            Frame frame = new Frame();
            using (ColorImageFrame colorImageFrame = e.OpenColorImageFrame())
            using (SkeletonFrame skeletonFrame = e.OpenSkeletonFrame())
            using (DepthImageFrame depthImageFrame = e.OpenDepthImageFrame())
            {
                colorImageFrame.CopyPixelDataTo(imageData);
                frame.Image = new List<byte>(imageData);
                /*
                if (skeletonFrame != null && depthImageFrame != null)
                {
                    if ((skeletonData == null) || (skeletonData.Length != skeletonFrame.SkeletonArrayLength))
                    {
                        skeletonData = new Skeleton[skeletonFrame.SkeletonArrayLength];
                    }
                    skeletonFrame.CopySkeletonDataTo(skeletonData);
                    foreach (Skeleton skeleton in skeletonData)
                    {
                        if (SkeletonTrackingState.Tracked == skeleton.TrackingState)
                        {
                            string header = String.Format("{0:D} {1:D}",
                                skeletonFrame.FrameNumber,
                                skeleton.TrackingId);

                            // 3D position data
                            // Body
                            sb.Append(header);
                            sb.Append(" Basic ");
                            appendPosition(sb, skeleton.Position);
                            sb.AppendLine();
                            // Joints
                            sb.Append(header);
                            sb.Append(" Joints");
                            foreach (Joint joint in skeleton.Joints)
                            {
                                sb.Append(" ");
                                appendPosition(sb, joint.Position);
                            }
                            sb.AppendLine();

                            // 2D location data
                            // Body
                            sb.Append(header);
                            sb.Append(" Basic2D ");
                            append2DLocation(sb, skeleton.Position, depthImageFrame);
                            sb.AppendLine();
                            // Joints
                            sb.Append(header);
                            sb.Append(" Joints2D");
                            foreach (Joint joint in skeleton.Joints)
                            {
                                sb.Append(" ");
                                append2DLocation(sb, joint.Position, depthImageFrame);
                            }
                            sb.AppendLine();
                        }
                    }
                }
                 */
            }
            frame.Keywords = recognizedPhrases;
            this.frame = frame;
        }

        private void appendPosition(StringBuilder sb, SkeletonPoint position)
        {
            sb.Append(String.Format("{0:F4} {1:F4} {2:F4}", position.X, position.Y, position.Z));
        }

        private void append2DLocation(StringBuilder sb, SkeletonPoint position, DepthImageFrame depthImageFrame)
        {
            if (Kinect != null)
            {
                ColorImagePoint location = getPosition2DLocation(position);
                sb.Append(String.Format("{0:D} {1:D}", location.X, location.Y));
            }
        }

        private ColorImagePoint getPosition2DLocation(SkeletonPoint position)
        {
            ColorImagePoint colorPoint = Kinect.CoordinateMapper.MapSkeletonPointToColorPoint(
                position, Kinect.ColorStream.Format);
            return colorPoint;
        }
        #endregion Kinect Skeleton processing
    }
}
