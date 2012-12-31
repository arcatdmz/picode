using System;
using System.Diagnostics;
using System.Windows;
using Microsoft.Kinect;

namespace Microsoft.Samples.Kinect.WpfViewers
{
    /// <summary>
    /// Interaction logic for KinectColorViewer.xaml
    /// </summary>
    public partial class KinectColorViewer : ImageViewer
    {
        public KinectColorViewer()
        {
            InitializeComponent();
        }

        #region Kinect discovery + setup
        public override KinectSensor Kinect
        {
            get
            {
                return _Kinect;
            }
            set
            {
                if (_Kinect != null)
                {
                    _Kinect.ColorFrameReady -= ColorImageReady;
                }

                _Kinect = value;
                if (_Kinect != null && _Kinect.Status == KinectStatus.Connected)
                {
                    ResetFrameRateCounters();

                    if (Kinect.ColorStream.Format == ColorImageFormat.RawYuvResolution640x480Fps15)
                    {
                        throw new NotImplementedException("RawYuv conversion is not yet implemented.");
                        //KinectSDK TODO: support conversion from RawYuv, and have specific event handler that does it.
                        //_Kinect.ColorFrameReady += new EventHandler<ImageFrameReadyEventArgs>(RawYuvColorImageReady);
                    }
                    else
                    {
                        _Kinect.ColorFrameReady += ColorImageReady;
                    }
                }
            }
        }

        private KinectSensor _Kinect;

        #endregion

        #region Kinect ColorImage processing
        void ColorImageReady(object sender, ColorImageFrameReadyEventArgs e)
        {
            using (ColorImageFrame imageFrame = e.OpenColorImageFrame())
            {
                if (imageFrame != null)
                {
                    // We need to initialize if colorDataNeedsInitialization is true or if the format has changed.
                    colorDataNeedsInitialization |= (lastImageFormat != imageFrame.Format);

                    if (colorDataNeedsInitialization)
                    {
                        lastImageFormat = imageFrame.Format;
                        pixelData = new byte[imageFrame.PixelDataLength];
                    }

                    imageFrame.CopyPixelDataTo(pixelData);

                    //An interopBitmap is a WPF construct that enables resetting the Bits of the image.
                    //This is more efficient than doing a BitmapSource.Create call every frame.
                    if (colorDataNeedsInitialization)
                    {
                        kinectColorImage.Visibility = Visibility.Visible;
                        imageHelper = new InteropBitmapHelper(imageFrame.Width, imageFrame.Height, pixelData);
                        kinectColorImage.Source = imageHelper.InteropBitmap;
                    }
                    else
                    {
                        imageHelper.UpdateBits(pixelData);
                    }

                    // If we succeeded, we can mark the color data as successfully initialized
                    colorDataNeedsInitialization = false;
                    lastImageFormat = imageFrame.Format;

                    UpdateFrameRate();
                }
            }
        }

        private bool colorDataNeedsInitialization = true;
        private ColorImageFormat lastImageFormat;
        private byte[] pixelData;
        public InteropBitmapHelper imageHelper { get; private set; }
        #endregion Kinect ColorImage processing
    }
}
