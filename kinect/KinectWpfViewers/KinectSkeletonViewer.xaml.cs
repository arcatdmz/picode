using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Shapes;
using Microsoft.Kinect;

namespace Microsoft.Samples.Kinect.WpfViewers
{
    public enum TrackingMode
    {
        DefaultSystemTracking,
        Closest_1Player,
        Closest_2Player,
        Sticky_1Player,
        Sticky_2Player,
        MostActive_1Player,
        MostActive_2Player
    }

    /// <summary>
    /// Interaction logic for KinectSkeletonViewer.xaml
    /// </summary>
    public partial class KinectSkeletonViewer : UserControl, INotifyPropertyChanged
    {
        public KinectSkeletonViewer()
        {
            InitializeComponent();
            ShowJoints = true;
            ShowBones = true;
            ShowCenter = true;
        }

        public bool ShowBones { get; set; }
        public bool ShowJoints { get; set; }
        public bool ShowCenter { get; set; }
        public ImageType ImageType { get; set; }

        #region Kinect discovery + setup
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
                    _Kinect.AllFramesReady -= new EventHandler<AllFramesReadyEventArgs>(_Kinect_AllFramesReady);
                }
                _Kinect = value;
                if (_Kinect != null && _Kinect.Status == KinectStatus.Connected)
                {
                    _Kinect.AllFramesReady += new EventHandler<AllFramesReadyEventArgs>(_Kinect_AllFramesReady);
                }
            }
        }

        public TrackingMode TrackingMode { get; set; }

        private KinectSensor _Kinect;
        #endregion

        #region Skeleton Processing
        private void _Kinect_AllFramesReady(object sender, AllFramesReadyEventArgs e)
        {
            // Have we already been "shut down" by the user of this viewer?
            if (this.Kinect == null)
            {
                return;
            }

            bool haveSkeletonData = false;

            using (SkeletonFrame skeletonFrame = e.OpenSkeletonFrame())
            {
                if (skeletonFrame != null)
                {
                    if (skeletonCanvases == null)
                    {
                        CreateListOfSkeletonCanvases();
                    }

                    if ((skeletonData == null) || (skeletonData.Length != skeletonFrame.SkeletonArrayLength))
                    {
                        skeletonData = new Skeleton[skeletonFrame.SkeletonArrayLength];
                    }

                    skeletonFrame.CopySkeletonDataTo(skeletonData);

                    haveSkeletonData = true;
                }
            }

            if (haveSkeletonData)
            {
                using (DepthImageFrame depthImageFrame = e.OpenDepthImageFrame())
                {
                    if (depthImageFrame != null)
                    {
                        int trackedSkeletons = 0;

                        foreach (Skeleton skeleton in skeletonData)
                        {
                            KinectSkeleton skeletonCanvas = skeletonCanvases[trackedSkeletons++];

                            switch (skeleton.TrackingState)
                            {
                                case SkeletonTrackingState.Tracked:
                                    skeletonCanvas.ShowCenter = false;
                                    if (skeletonCanvases != null)
                                    {
                                        skeletonCanvas.Visibility = ShowJoints || ShowBones
                                                                        ? Visibility.Visible
                                                                        : Visibility.Hidden;
                                        if (ShowBones)
                                        {
                                            adjustLocationOfBones(skeleton, skeletonCanvas, depthImageFrame);
                                        }
                                        if (ShowJoints)
                                        {
                                            adjustLocationOfJoints(skeleton, skeletonCanvas, depthImageFrame);
                                        }
                                    }
                                    break;
                                case SkeletonTrackingState.PositionOnly:
                                    skeletonCanvas.ShowCenter = true;
                                    skeletonCanvas.Visibility = ShowCenter ? Visibility.Visible : Visibility.Hidden;
                                    skeletonCanvas.CenterPoint = getPosition2DLocation(skeleton.Position, skeletonCanvas, depthImageFrame);
                                    break;
                                default:
                                    // When people leave the scene, hide their skeletons
                                    skeletonCanvas.Visibility = Visibility.Hidden;
                                    break;
                            }
                        }

                        //when people leave the scene, hide their skeleton.
                        switch (trackedSkeletons)
                        {
                            case 0:
                                skeletonCanvas1.Visibility = System.Windows.Visibility.Hidden;
                                skeletonCanvas2.Visibility = System.Windows.Visibility.Hidden;
                                break;
                            case 1:
                                skeletonCanvas2.Visibility = System.Windows.Visibility.Hidden;
                                break;
                        }

                        if (ImageType == ImageType.Depth)
                        {
                            ChooseTrackedSkeletons(skeletonData);
                        }
                    }
                }
            }
        }

        private void CreateListOfSkeletonCanvases()
        {
            skeletonCanvases = new List<KinectSkeleton>();
            skeletonCanvases.Add(skeletonCanvas1);
            skeletonCanvases.Add(skeletonCanvas2);
            skeletonCanvases.Add(skeletonCanvas3);
            skeletonCanvases.Add(skeletonCanvas4);
            skeletonCanvases.Add(skeletonCanvas5);
            skeletonCanvases.Add(skeletonCanvas6);
        }

        private void adjustLocationOfBones(Skeleton skeletonData, KinectSkeleton skeletonCanvas, DepthImageFrame depthFrame)
        {
            //the 3rd param are the child id of the bone PolyLines in skeletonCanvas.
            //Draw Core Bones
            adjustBoneRendering(skeletonData.Joints, skeletonCanvas, depthFrame, 0, JointType.Head, JointType.ShoulderCenter, JointType.Spine, JointType.HipCenter);
            adjustBoneRendering(skeletonData.Joints, skeletonCanvas, depthFrame, 1, JointType.HipLeft, JointType.HipCenter, JointType.HipRight);
            adjustBoneRendering(skeletonData.Joints, skeletonCanvas, depthFrame, 2, JointType.ShoulderLeft, JointType.ShoulderCenter, JointType.ShoulderRight);
            //Draw Arms
            adjustBoneRendering(skeletonData.Joints, skeletonCanvas, depthFrame, 3, JointType.ShoulderLeft, JointType.ElbowLeft, JointType.WristLeft, JointType.HandLeft);
            adjustBoneRendering(skeletonData.Joints, skeletonCanvas, depthFrame, 4, JointType.ShoulderRight, JointType.ElbowRight, JointType.WristRight, JointType.HandRight);
            //Draw Legs
            adjustBoneRendering(skeletonData.Joints, skeletonCanvas, depthFrame, 5, JointType.HipLeft, JointType.KneeLeft, JointType.AnkleLeft, JointType.FootLeft);
            adjustBoneRendering(skeletonData.Joints, skeletonCanvas, depthFrame, 6, JointType.HipRight, JointType.KneeRight, JointType.AnkleRight, JointType.FootRight);
        }

        private void adjustLocationOfJoints(Skeleton skeletonData, KinectSkeleton skeletonCanvas, DepthImageFrame depthFrame)
        {
            int jointIndex = 0;
            foreach (Joint joint in skeletonData.Joints)
            {
                Point jointPos = getJoint2DLocation(joint, skeletonCanvas, depthFrame);
                Line jointLine = (Line)skeletonCanvas.Children[7 + jointIndex++]; // 7 - is the first Line element in the skeletonCanvas.
                jointLine.X1 = jointPos.X - JointAdjustment;
                jointLine.X2 = jointLine.X1 + JointWidth;
                jointLine.Y1 = jointLine.Y2 = jointPos.Y;
                switch (joint.TrackingState)
                {
                    case JointTrackingState.Tracked:
                        jointLine.Stroke = Brushes.Green;
                        break;
                    case JointTrackingState.Inferred:
                        jointLine.Stroke = Brushes.Yellow;
                        break;
                    case JointTrackingState.NotTracked:
                        jointLine.Stroke = Brushes.Red;
                        break;
                }
            }
        }

        private void adjustBoneRendering(JointCollection joints, KinectSkeleton skeletonCanvas, DepthImageFrame depthFrame, int canvasChildIndex, params JointType[] ids)
        {
            PointCollection points = new PointCollection(ids.Length);
            for (int i = 0; i < ids.Length; ++i)
            {
                if (joints[ids[i]].TrackingState != JointTrackingState.NotTracked)
                {
                    points.Add(getJoint2DLocation(joints[ids[i]], skeletonCanvas, depthFrame));
                }
            }
            Polyline polyline = (Polyline)skeletonCanvas.Children[canvasChildIndex];
            polyline.Points = points;
            switch (joints[ids[ids.Length - 1]].TrackingState)
            {
                case JointTrackingState.Tracked:
                    polyline.Stroke = Brushes.Green;
                    break;
                case JointTrackingState.Inferred:
                    polyline.Stroke = Brushes.Yellow;
                    break;
                case JointTrackingState.NotTracked:
                    //no rendering
                    break;
            }
        }

        private Point getJoint2DLocation(Joint joint, Canvas skeletonCanvas, DepthImageFrame depthFrame)
        {
            return getPosition2DLocation(joint.Position, skeletonCanvas, depthFrame);
        }

        private Point getPosition2DLocation(SkeletonPoint position, Canvas skeletonCanvas, DepthImageFrame depthFrame)
        {
            DepthImagePoint depthPoint = depthFrame.MapFromSkeletonPoint(position);

            switch (ImageType)
            {
                case ImageType.Color:
                    ColorImagePoint colorPoint = depthFrame.MapToColorImagePoint(depthPoint.X, depthPoint.Y, Kinect.ColorStream.Format);

                    // map back to skeleton.Width & skeleton.Height
                    return new Point(
                        (int)(skeletonCanvas.ActualWidth * colorPoint.X / Kinect.ColorStream.FrameWidth),
                        (int)(skeletonCanvas.ActualHeight * colorPoint.Y / Kinect.ColorStream.FrameHeight)
                    );
                case ImageType.Depth:
                    return new Point(
                        (int)(skeletonCanvas.ActualWidth * depthPoint.X / depthFrame.Width),
                        (int)(skeletonCanvas.ActualHeight * depthPoint.Y / depthFrame.Height)
                    );
                default:
                    throw new ArgumentOutOfRangeException("ImageType was a not expected value: " + ImageType.ToString());
            }
        }

        #region Choose Tracked Skeletons - Will be broken out later
        private void ChooseTrackedSkeletons(Skeleton[] skeletonData)
        {
            switch (TrackingMode)
            {
                case TrackingMode.Closest_1Player:
                    ChooseClosestSkeletons(skeletonData, 1);
                    break;
                case TrackingMode.Closest_2Player:
                    ChooseClosestSkeletons(skeletonData, 2);
                    break;
                case TrackingMode.Sticky_1Player:
                    ChooseOldestSkeletons(skeletonData, 1);
                    break;
                case TrackingMode.Sticky_2Player:
                    ChooseOldestSkeletons(skeletonData, 2);
                    break;
                case TrackingMode.MostActive_1Player:
                    ChooseMostActiveSkeletons(skeletonData, 1);
                    break;
                case TrackingMode.MostActive_2Player:
                    ChooseMostActiveSkeletons(skeletonData, 2);
                    break;
            }
        }

        private void ChooseClosestSkeletons(Skeleton[] skeletonData, int count)
        {
            SortedList<float, int> depthSorted = new SortedList<float, int>();

            foreach (Skeleton s in skeletonData)
            {
                if (s.TrackingState != SkeletonTrackingState.NotTracked)
                {
                    float zVal = s.Position.Z;
                    while (depthSorted.ContainsKey(zVal))
                    {
                        zVal += 0.0001f;
                    }
                    depthSorted.Add(zVal, s.TrackingId);
                }
            }

            ChooseSkeletonsFromList(depthSorted.Values, count);
        }

        private List<int> activeList = new List<int>();

        private void ChooseOldestSkeletons(Skeleton[] skeletonData, int count)
        {
            List<int> newList = new List<int>();
            foreach (Skeleton s in skeletonData)
            {
                if (s.TrackingState != SkeletonTrackingState.NotTracked)
                {
                    newList.Add(s.TrackingId);
                }
            }

            // Remove all elements from the active list that are not currently present
            activeList.RemoveAll(k => !newList.Contains(k));

            // Add all elements that aren't already in the activeList
            activeList.AddRange(newList.FindAll(k => !activeList.Contains(k)));

            ChooseSkeletonsFromList(activeList, count);
        }

        private const float ActivityFalloff = 0.98f;

        private class ActivityWatcher : IComparable<ActivityWatcher>
        {
            internal float activityLevel;
            internal int trackingId;
            internal bool updated;
            internal SkeletonPoint previousPosition;
            internal SkeletonPoint previousDelta;

            internal ActivityWatcher(Skeleton s)
            {
                activityLevel = 0.0f;
                trackingId = s.TrackingId;
                updated = true;
                previousPosition = s.Position;
                previousDelta = new SkeletonPoint();
            }

            internal void NewPass()
            {
                updated = false;
            }

            internal void Update(Skeleton s)
            {
                SkeletonPoint newPosition = s.Position;
                SkeletonPoint newDelta = new SkeletonPoint();
                newDelta.X = newPosition.X - previousPosition.X;
                newDelta.Y = newPosition.Y - previousPosition.Y;
                newDelta.Z = newPosition.Z - previousPosition.Z;

                SkeletonPoint deltaV = new SkeletonPoint();
                deltaV.X = newDelta.X - previousDelta.X;
                deltaV.Y = newDelta.Y - previousDelta.Y;
                deltaV.Z = newDelta.Z - previousDelta.Z;

                previousPosition = newPosition;
                previousDelta = newDelta;

                float deltaVLengthSquared = deltaV.X * deltaV.X + deltaV.Y * deltaV.Y + deltaV.Z * deltaV.Z;
                float deltaVLength = (float)Math.Sqrt(deltaVLengthSquared);

                activityLevel = activityLevel * ActivityFalloff;
                activityLevel += deltaVLength;

                updated = true;
            }

            public int CompareTo(ActivityWatcher other)
            {
                if (activityLevel == other.activityLevel)
                {
                    return 0;
                }
                else if (activityLevel > other.activityLevel)
                {
                    return -1;
                }
                else
                {
                    return 1;
                }
            }
        }

        private List<ActivityWatcher> recentActivity = new List<ActivityWatcher>();

        private void ChooseMostActiveSkeletons(Skeleton[] skeletonData, int count)
        {
            foreach (ActivityWatcher watcher in recentActivity)
            {
                watcher.NewPass();
            }

            foreach (Skeleton s in skeletonData)
            {
                if (s.TrackingState != SkeletonTrackingState.NotTracked)
                {
                    ActivityWatcher watcher = recentActivity.Find(w => w.trackingId == s.TrackingId);
                    if (watcher != null)
                    {
                        watcher.Update(s);
                    }
                    else
                    {
                        recentActivity.Add(new ActivityWatcher(s));
                    }
                }
            }

            // Remove any skeletons that are gone
            recentActivity.RemoveAll(aw => !aw.updated);

            recentActivity.Sort();
            ChooseSkeletonsFromList(recentActivity.ConvertAll<int>(f => f.trackingId), count);
        }

        private void ChooseSkeletonsFromList(IList<int> list, int max)
        {
            int argCount = Math.Min(list.Count, max);

            if (argCount == 0)
            {
                Kinect.SkeletonStream.ChooseSkeletons();
            }
            if (argCount == 1)
            {
                Kinect.SkeletonStream.ChooseSkeletons(list[0]);
            }
            if (argCount >= 2)
            {
                Kinect.SkeletonStream.ChooseSkeletons(list[0], list[1]);
            }
        }
        #endregion Choose Tracked Skeletons

        #endregion Skeleton Processing

        #region INotifyPropertyChanged
        public event PropertyChangedEventHandler PropertyChanged;

        protected void NotifyPropertyChanged(String info)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(info));
            }
        }
        #endregion INotifyPropertyChanged

        #region Private state
        private List<KinectSkeleton> skeletonCanvases;
        private const int JointWidth = 6;
        private const int JointAdjustment = 3;
        private Skeleton[] skeletonData;
        #endregion Private state
    }

    public enum ImageType
    {
        Color,
        Depth,
    }
}
