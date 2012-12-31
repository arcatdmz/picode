using System;
using System.Diagnostics;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Threading;
using Microsoft.Kinect;

namespace Microsoft.Samples.Kinect.WpfViewers
{
    /// <summary>
    /// Interaction logic for KinectSettings.xaml
    /// </summary>
    internal partial class KinectSettings : UserControl
    {
        public KinectSettings(KinectDiagnosticViewer diagViewer)
        {
            DiagViewer = diagViewer;
            InitializeComponent();
            debounce.Tick += debounce_Elapsed;
        }

        public KinectDiagnosticViewer DiagViewer { get; set; }

        private KinectSensor _Kinect;

        public KinectSensor Kinect
        {
            get { return _Kinect; }
            set { _Kinect = value; }
        }

        #region UI Event Handlers

        internal void PopulateComboBoxesWithFormatChoices()
        {
            foreach (ColorImageFormat colorImageFormat in Enum.GetValues(typeof (ColorImageFormat)))
            {
                if (colorImageFormat == ColorImageFormat.RawYuvResolution640x480Fps15)
                {
                    //don't add RawYuv to combobox..
                    //That colorImageFormat works, but needs YUV->RGB conversion code which this sample doesn't have yet.
                }
                else
                {
                    colorFormats.Items.Add(colorImageFormat);
                }
            }

            foreach (DepthImageFormat depthImageFormat in Enum.GetValues(typeof (DepthImageFormat)))
            {
                depthFormats.Items.Add(depthImageFormat);
            }

            foreach (TrackingMode trackingMode in Enum.GetValues(typeof (TrackingMode)))
            {
                trackingModes.Items.Add(trackingMode);
            }

            foreach (DepthRange depthRange in Enum.GetValues(typeof (DepthRange)))
            {
                depthRanges.Items.Add(depthRange);
            }
            depthRanges.SelectedIndex = 0;
        }

        private void colorFormats_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            ComboBox comboBox = sender as ComboBox;
            if (Kinect != null && Kinect.Status == KinectStatus.Connected && comboBox.SelectedItem != null)
            {
                if (Kinect.ColorStream.IsEnabled)
                {
                    Kinect.ColorStream.Enable((ColorImageFormat) colorFormats.SelectedItem);
                }
            }
        }

        private void depthFormats_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            ComboBox comboBox = sender as ComboBox;
            if (Kinect != null && Kinect.Status == KinectStatus.Connected && comboBox.SelectedItem != null)
            {
                if (Kinect.DepthStream.IsEnabled)
                {
                    Kinect.DepthStream.Enable((DepthImageFormat) depthFormats.SelectedItem);
                }
            }
        }

        private void Skeletons_Checked(object sender, RoutedEventArgs e)
        {
            CheckBox checkBox = sender as CheckBox;
            if (Kinect != null && Kinect.Status == KinectStatus.Connected && checkBox.IsChecked.HasValue)
            {
                SetSkeletalTracking(checkBox.IsChecked.Value);
            }
        }

        private bool IsSkeletalViewerAvailable
        {
            get { return KinectSensor.KinectSensors.All(k => (!k.IsRunning || !k.SkeletonStream.IsEnabled)); }
        }

        private void SetSkeletalTracking(bool enable)
        {
            if (enable)
            {
                if (IsSkeletalViewerAvailable)
                {
                    Kinect.SkeletonStream.Enable();
                    trackingModes.IsEnabled = true;
                    DiagViewer.KinectSkeletonViewerOnColor.Visibility = System.Windows.Visibility.Visible;
                    DiagViewer.KinectSkeletonViewerOnDepth.Visibility = System.Windows.Visibility.Visible;
                    showSkeletons.IsChecked = true;
                }
                else
                {
                    showSkeletons.IsChecked = false;
                }
            }
            else
            {
                Kinect.SkeletonStream.Disable();
                trackingModes.IsEnabled = false;
                DiagViewer.KinectSkeletonViewerOnColor.Visibility = System.Windows.Visibility.Hidden;
                DiagViewer.KinectSkeletonViewerOnDepth.Visibility = System.Windows.Visibility.Hidden;
            }
        }

        private void trackingModes_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            ComboBox comboBox = sender as ComboBox;
            if (Kinect != null && Kinect.Status == KinectStatus.Connected && comboBox.SelectedItem != null)
            {
                TrackingMode newMode = (TrackingMode) comboBox.SelectedItem;
                Kinect.SkeletonStream.AppChoosesSkeletons = (newMode != TrackingMode.DefaultSystemTracking);
                DiagViewer.KinectSkeletonViewerOnColor.TrackingMode = newMode;
                DiagViewer.KinectSkeletonViewerOnDepth.TrackingMode = newMode;
            }
        }

        private void tooFar_Checked(object sender, EventArgs e)
        {
            CheckBox tooFarCheckBox = (CheckBox) sender;
            // Kinect.DepthStream.IsTooFarRangeEnabled = tooFarCheckBox.IsChecked.Value;
        }

        private void depthRanges_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            ComboBox comboBox = sender as ComboBox;
            if (Kinect != null && Kinect.Status == KinectStatus.Connected && comboBox.SelectedItem != null)
            {
                try
                {
                    Kinect.DepthStream.Range = (DepthRange) comboBox.SelectedItem;
                }
                catch (InvalidOperationException)
                {
                    comboBox.SelectedIndex = 0;
                    comboBox.Items.RemoveAt(1);
                    comboBox.Items.Add("-- NearMode not supported on this device. See Readme. --");
                }
                catch (InvalidCastException)
                {
                    // they chose the error string, switch back
                    comboBox.SelectedIndex = 0;
                }
            }
        }

        private void ColorStream_Enabled(object sender, RoutedEventArgs e)
        {
            CheckBox checkBox = (CheckBox) sender;
            DisplayColumnBasedOnIsChecked(checkBox, 1, 2);
            DisplayPanelBasedOnIsChecked(checkBox, DiagViewer.colorPanel);
            if (Kinect != null)
            {
                EnableColorImageStreamBasedOnIsChecked(checkBox, Kinect.ColorStream, colorFormats);
            }
        }

        private void EnableDepthImageStreamBasedOnIsChecked(CheckBox checkBox, DepthImageStream imageStream,
                                                            ComboBox depthFormats)
        {
            if (checkBox.IsChecked.HasValue && checkBox.IsChecked.Value)
            {
                imageStream.Enable((DepthImageFormat) depthFormats.SelectedItem);
            }
            else
            {
                imageStream.Disable();
            }
        }

        private void EnableColorImageStreamBasedOnIsChecked(CheckBox checkBox, ColorImageStream imageStream,
                                                            ComboBox colorFormats)
        {
            if (checkBox.IsChecked.HasValue && checkBox.IsChecked.Value)
            {
                imageStream.Enable((ColorImageFormat) colorFormats.SelectedItem);
            }
            else
            {
                imageStream.Disable();
            }
        }

        private void DepthStream_Enabled(object sender, RoutedEventArgs e)
        {
            CheckBox checkBox = (CheckBox) sender;
            DisplayColumnBasedOnIsChecked(checkBox, 2, 1);
            DisplayPanelBasedOnIsChecked(checkBox, DiagViewer.depthPanel);
            if (Kinect != null)
            {
                EnableDepthImageStreamBasedOnIsChecked(checkBox, Kinect.DepthStream, depthFormats);
            }
        }

        private void DisplayPanelBasedOnIsChecked(CheckBox checkBox, Grid panel)
        {
            //on load of XAML page, panel will be null.
            if (panel == null)
                return;

            if (checkBox.IsChecked.HasValue && checkBox.IsChecked.Value)
            {
                panel.Visibility = Visibility.Visible;
            }
            else
            {
                panel.Visibility = Visibility.Collapsed;
            }
        }

        private void DisplayColumnBasedOnIsChecked(CheckBox checkBox, int column, int stars)
        {
            if (checkBox.IsChecked.HasValue && checkBox.IsChecked.Value)
            {
                DiagViewer.LayoutRoot.ColumnDefinitions[column].Width = new GridLength(stars, GridUnitType.Star);
            }
            else
            {
                DiagViewer.LayoutRoot.ColumnDefinitions[column].Width = new GridLength(0);
            }
        }

        #endregion UI Event Handlers


        private DispatcherTimer debounce = new DispatcherTimer
                                               {IsEnabled = false, Interval = TimeSpan.FromMilliseconds(200)};

        private int lastSetSensorAngle = int.MaxValue;
        private bool userUpdate = true;

        private void ElevationAngle_Changed(object sender, RoutedPropertyChangedEventArgs<double> e)
        {
            if (userUpdate)
            {
                debounce.Stop();
                debounce.Start();
            }
        }

        internal void UpdateUIElevationAngleFromSensor()
        {
            if (Kinect != null)
            {
                userUpdate = false;

                // If it's never been set, retrieve the value.
                if (lastSetSensorAngle == int.MaxValue)
                {
                    lastSetSensorAngle = Kinect.ElevationAngle;
                }

                // Use the cache to prevent race conditions with the background thread which may 
                // be in the process of setting this value.
                ElevationAngle.Value = lastSetSensorAngle;
                userUpdate = true;
            }
        }

        private bool backgroundUpdateInProgress;

        private void debounce_Elapsed(object sender, EventArgs e)
        {
            // The time has elapsed.  We may start it again later.
            debounce.Stop();

            int angleToSet = (int) ElevationAngle.Value;

            // Is there an update in progress?
            if (backgroundUpdateInProgress)
            {
                // Try again in a few moments.
                debounce.Start();
            }
            else
            {
                backgroundUpdateInProgress = true;

                Task.Factory.StartNew(
                    () =>
                        {
                            try
                            {
                                // Check for not null and running
                                if ((Kinect != null) && (Kinect.IsRunning))
                                {
                                    // We must wait at least 1 second, and call no more frequently than 15 times every 20 seconds
                                    // So, we wait at least 1350ms afterwards before we set backgroundUpdateInProgress to false.
                                    Kinect.ElevationAngle = angleToSet;
                                    lastSetSensorAngle = angleToSet;
                                    Thread.Sleep(1350);
                                }
                            }
                            finally
                            {
                                backgroundUpdateInProgress = false;
                            }
                        }).ContinueWith(
                            results =>
                                {
                                    // This can happen if the Kinect transitions from Running to not running
                                    // after the check above but before setting the ElevationAngle.
                                    if (results.IsFaulted)
                                    {
                                        var exception = results.Exception;

                                        Debug.WriteLine(
                                            "Set Elevation Task failed with exception " +
                                            exception);
                                    }
                                });
            }
        }
    }
}
