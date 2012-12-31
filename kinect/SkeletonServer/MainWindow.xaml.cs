using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Microsoft.Kinect;
using Microsoft.Speech.AudioFormat;
using Microsoft.Speech.Recognition;
using System.IO;
using SkeletonServer.Properties;
using Microsoft.Samples.Kinect.WpfViewers;
using System.Net.Sockets;
using System.Threading;
using System.Net;
using System.Globalization;

namespace SkeletonServer
{

    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {

        #region Private State
        private SpeechRecognitionEngine sre;
        private KinectAudioSource kinectAudioSource;
        private string recognizedPhrase;

        private Skeleton[] skeletonData;

        private TcpListener server;
        private Thread serverThread;
        private Encoding encoding = Encoding.UTF8;
        private readonly object lockObject = new object();
        private HashSet<SkeletonClient> clients = new HashSet<SkeletonClient>();
        #endregion Private State

        #region ctor + Window Events

        public MainWindow(StartupEventArgs e)
        {
            InitializeComponent();
            InitializeSpeechRecognition();
            PopulateComboBoxWithFormatChoices();
            RestoreWindowState();

            // Check command line parameters.
            bool isAutomatedServer = false;
            foreach (string s in e.Args)
            {
                if (s == "/AutomatedServer")
                {
                    isAutomatedServer = true;
                }
            }

            // Start the server automatically?
            if (isAutomatedServer)
            {
                streamingCheckBox.IsChecked = true;
                streamingCheckBox.IsEnabled = false;
                serverPortBox.IsEnabled = false;
                applyButton.IsEnabled = false;
                Connect();
            }

            AddListeners();
        }

        private void PopulateComboBoxWithFormatChoices()
        {
            foreach (TrackingMode trackingMode in Enum.GetValues(typeof(TrackingMode)))
            {
                trackingModes.Items.Add(trackingMode);
            }
        }

        private void RestoreWindowState()
        {
            // Restore window state to that last used
            Settings settings = Properties.Settings.Default;
            Rect bounds = settings.WindowRect;
            if (bounds.Right == bounds.Left)
            {
                CenterWindowOnScreen();
            }
            else
            {
                this.Top = bounds.Top;
                this.Left = bounds.Left;
                this.Height = bounds.Height;
                this.Width = bounds.Width;
            }
            this.WindowState = (WindowState)settings.WindowState;

            // Restore configuration form
            /*
            ipBox1.Text = settings.ip1.ToString();
            ipBox2.Text = settings.ip2.ToString();
            ipBox3.Text = settings.ip3.ToString();
            ipBox4.Text = settings.ip4.ToString();
             */
            serverPortBox.Text = settings.ServerPort.ToString();
            trackingModes.SelectedIndex = settings.TrackingMode;
        }

        private void AddListeners()
        {
            trackingModes.SelectionChanged += new SelectionChangedEventHandler(trackingModes_SelectionChanged);
            streamingCheckBox.Checked += new RoutedEventHandler(streamingCheckBox_Checked);
            streamingCheckBox.Unchecked += new RoutedEventHandler(streamingCheckBox_Unchecked);
            applyButton.Click += new RoutedEventHandler(applyButton_Click);
            captureButton.Click += new RoutedEventHandler(captureButton_Click);
        }

        void captureButton_Click(object sender, RoutedEventArgs e)
        {
            recognizedPhrase = "Capture";
        }

        private void streamingCheckBox_Checked(object sender, RoutedEventArgs e)
        {
            Connect();
        }

        private void streamingCheckBox_Unchecked(object sender, RoutedEventArgs e)
        {
            Disconnect();
        }

        private void applyButton_Click(object sender, RoutedEventArgs e)
        {
            lock (lockObject)
            {
                Disconnect();
                Connect();
            }
        }

        void trackingModes_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            skeletonViewer.TrackingMode = (TrackingMode)trackingModes.SelectedValue;
        }

        private void Window_Loaded(object sender, EventArgs e)
        {
            KinectStart();
        }

        private void CenterWindowOnScreen()
        {
            double screenWidth = System.Windows.SystemParameters.PrimaryScreenWidth;
            double screenHeight = System.Windows.SystemParameters.PrimaryScreenHeight;
            double windowWidth = this.Width;
            double windowHeight = this.Height;
            this.Left = (screenWidth / 2) - (windowWidth / 2);
            this.Top = (screenHeight / 2) - (windowHeight / 2);
        }

        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            Settings settings = Properties.Settings.Default;
            settings.WindowRect = this.RestoreBounds;
            settings.WindowState = (int)this.WindowState;
            /*
            settings.ip1 = int.Parse(ipBox1.Text);
            settings.ip2 = int.Parse(ipBox2.Text);
            settings.ip3 = int.Parse(ipBox3.Text);
            settings.ip4 = int.Parse(ipBox4.Text);
             */
            settings.ServerPort = int.Parse(serverPortBox.Text);
            settings.TrackingMode = trackingModes.SelectedIndex;
            settings.Save();
        }

        private void Window_Closed(object sender, EventArgs e)
        {
            KinectStop();
            Disconnect();
        }

        #endregion ctor + Window Events

        #region Speech recognition
        private void InitializeSpeechRecognition()
        {
            sre = new SpeechRecognitionEngine(GetKinectRecognizer());

            GrammarBuilder gb = new GrammarBuilder();
            gb.Culture = sre.RecognizerInfo.Culture; //This is needed to ensure that it will work on machines with any culture, not just en-us.
            gb.Append(new Choices(new string[] { "Capture" }));

            sre.LoadGrammar(new Grammar(gb));
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
            var kinectStream = kinectAudioSource.Start();
            sre.SetInputToAudioStream(kinectStream, new SpeechAudioFormatInfo(
                                                  EncodingFormat.Pcm, 16000, 16, 1,
                                                  32000, 2, null));
            sre.RecognizeAsync(RecognizeMode.Multiple);
            sre.SpeechRecognized += new EventHandler<SpeechRecognizedEventArgs>(sre_SpeechRecognized);
        }

        void sre_SpeechRecognized(object sender, SpeechRecognizedEventArgs e)
        {
            recognizedPhrase = e.Result.Text;
        }

        public void StopSpeechRecognition()
        {
            if (sre != null)
            {
                kinectAudioSource.Stop();
                sre.RecognizeAsyncCancel();
                sre.RecognizeAsyncStop();
                // kinectAudioSource.Dispose();
            }
        }
        #endregion Speech recognition

        #region Network connection
        private void Connect()
        {
            lock (lockObject)
            {
                try
                {
                    if (server == null)
                    {
                        server = new TcpListener(IPAddress.Any, int.Parse(serverPortBox.Text));
                    }
                    if (serverThread == null)
                    {
                        serverThread = new Thread(serverThread_start);
                        serverThread.Start();
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine("Network Open Error: " + ex.Message);
                    streamingCheckBox.IsChecked = false;
                }
            }
        }

        private void Disconnect()
        {
            lock (lockObject)
            {
                // Close all connections.
                foreach (SkeletonClient client in clients)
                {
                    client.Close();
                }
                clients.Clear();

                // Stop the server thread.
                if (server != null)
                {
                    server.Stop();
                    server = null;
                }
                if (serverThread != null)
                {
                    serverThread = null;
                }
            }
        }

        private void serverThread_start()
        {
            server.Start();
            TcpClient client;
            try
            {
                while (server != null && (client = server.AcceptTcpClient()) != null)
                {
                    lock (lockObject)
                    {
                        clients.Add(new SkeletonClient(client, encoding));
                    }
                }
            }
            catch (SocketException)
            {
                // Do nothing.
            }
            Disconnect();
        }
        #endregion Network connection

        #region Kinect discovery + setup

        /* To add Kinect support, an app can:
         *   copy the entire "Kinect discovery + setup" region into your app's code file.
         *   call KinectStart() from Window loaded.
         *   call KinectStop() from Window closed.
         *   modify ShowStatus method to display app specific feedback to the user for each of the conditions.
         *   modify Initialize/UnitializeKinectServices for your app.
         */

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
                    throw new NotImplementedException("ErrorCondition." + errorCondition.ToString() + " needs a handler in ShowStatus()");
            }

            // TODO Code to show statusMessage here.

            currentErrorCondition = errorCondition;
        }

        // Kinect enabled apps should customize which Kinect services it initializes here.
        private KinectSensor InitializeKinectServices(KinectSensor sensor)
        {
            // Application should enable all streams first.
            sensor.ColorStream.Enable(ColorImageFormat.RgbResolution640x480Fps30);
            sensor.DepthStream.Enable(DepthImageFormat.Resolution320x240Fps30);

            // sensor.SkeletonStream.TransformSmooth = true;
            sensor.AllFramesReady += new EventHandler<AllFramesReadyEventArgs>(AllFramesReady);
            sensor.SkeletonStream.Enable();

            // Components are then given their KinectSensor to work with.
            colorViewer.Kinect = sensor;
            skeletonViewer.Kinect = sensor;

            try
            {
                sensor.Start();
            }
            catch (IOException)
            {
                ShowStatus(ErrorCondition.KinectAppConflict);
                return null;
            }

            // Start speech recognizer after KinectSensor.Start() is called
            StartSpeechRecognition(sensor.AudioSource);
            return sensor;
        }

        //Kinect enabled apps should uninitialize all Kinect services that were initialized in InitializeKinectServices() here.
        private void UninitializeKinectServices(KinectSensor sensor)
        {
            sensor.Stop();

            colorViewer.Kinect = null;
            skeletonViewer.Kinect = null;

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

        #region Kinect Skeleton processing
        void AllFramesReady(object sender, AllFramesReadyEventArgs e)
        {
            // Construct data.
            StringBuilder sb = new StringBuilder();
            using (SkeletonFrame skeletonFrame = e.OpenSkeletonFrame())
            using (DepthImageFrame depthImageFrame = e.OpenDepthImageFrame())
            {
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
            }

            bool sendImage = false;
            if (recognizedPhrase != null)
            {
                sb.Append("Phrase ");
                sb.AppendLine(recognizedPhrase);
                if (recognizedPhrase == "Capture")
                {
                    sendImage = true;
                }
                recognizedPhrase = null;
            }

            // Send data.
            lock (lockObject)
            {
                clients.RemoveWhere(client => !client.Connected());

                // Prepare image data if needed.
                int imageLength = 0;
                MemoryStream ms = null;
                if (sendImage)
                {
                    ms = new MemoryStream();
                    JpegBitmapEncoder jpegEncoder = new JpegBitmapEncoder();
                    jpegEncoder.Frames.Add(BitmapFrame.Create(colorViewer.imageHelper.InteropBitmap));
                    jpegEncoder.Save(ms);
                    imageLength = (int)ms.Length;
                }

                // Send data to each client.
                foreach (SkeletonClient client in clients)
                {
                    try
                    {
                        client.writer.Write(sb);
                        if (sendImage && imageLength > 0)
                        {
                            client.writer.Write("Image ");
                            client.writer.WriteLine(imageLength);
                            client.writer.Flush();
                            ms.WriteTo(client.stream);
                            client.stream.Flush();
                        }
                        else
                        {
                            client.writer.Flush();
                        }
                    }
                    catch (IOException)
                    {
                        Console.WriteLine(client.ToString());
                    }
                }

                sendImage = false;
            }
        }

        private void appendPosition(StringBuilder sb, SkeletonPoint position)
        {
            sb.Append(String.Format("{0:F4} {1:F4} {2:F4}", position.X, position.Y, position.Z));
        }

        private void append2DLocation(StringBuilder sb, SkeletonPoint position, DepthImageFrame depthImageFrame)
        {
            if (Kinect != null) {
                ColorImagePoint location = getPosition2DLocation(position, depthImageFrame);
                sb.Append(String.Format("{0:D} {1:D}", location.X, location.Y));
            }
        }

        private ColorImagePoint getPosition2DLocation(SkeletonPoint position, DepthImageFrame depthImageFrame)
        {
            DepthImagePoint depthPoint = depthImageFrame.MapFromSkeletonPoint(position);
            ColorImagePoint colorPoint = depthImageFrame.MapToColorImagePoint(depthPoint.X, depthPoint.Y, Kinect.ColorStream.Format);
            return colorPoint;
        }
        #endregion Kinect Skeleton processing
    }

    public class SkeletonClient
    {
        internal TcpClient client { get; private set; }
        internal NetworkStream stream { get; private set; }
        internal StreamWriter writer { get; private set; }

        private byte[] buffer;

        public SkeletonClient(TcpClient client, Encoding encoding)
        {
            this.client = client;
            this.stream = client.GetStream();
            this.writer = new StreamWriter(stream, encoding);
            // buffer = new byte[1024];
            // stream.BeginRead(buffer, 0, buffer.Length, new AsyncCallback(ReceiveDataCallback), stream);
        }

        private void ReceiveDataCallback(IAsyncResult ar)
        {
            NetworkStream stream = ar.AsyncState as NetworkStream;
            int length = stream.EndRead(ar);
            if (length < 0)
            {
                // Disconnected.
                return;
            }
            // TODO
        }

        internal bool Connected()
        {
            return client != null && client.Connected;
        }

        internal void Close()
        {
            writer.Close();
            client.Close();
            client = null;
            writer = null;
        }
    }
}
