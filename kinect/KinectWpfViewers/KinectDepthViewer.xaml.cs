using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using Microsoft.Kinect;

namespace Microsoft.Samples.Kinect.WpfViewers
{
    /// <summary>
    /// Interaction logic for KinectColorViewer.xaml
    /// </summary>
    public partial class KinectDepthViewer : ImageViewer
    {
        #region ctor
        public KinectDepthViewer()
        {
            InitializeComponent();
        }
        #endregion

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
                    _Kinect.DepthFrameReady -= new EventHandler<DepthImageFrameReadyEventArgs>(DepthImageReady);
                }

                _Kinect = value;
                if (_Kinect != null && _Kinect.Status == KinectStatus.Connected)
                {
                    ResetFrameRateCounters();

                    _Kinect.DepthFrameReady += new EventHandler<DepthImageFrameReadyEventArgs>(DepthImageReady);
                }
            }
        }

        private KinectSensor _Kinect;

        #endregion

        #region DepthImage Processing
        private void DepthImageReady(object sender, DepthImageFrameReadyEventArgs e)
        {
            using (DepthImageFrame imageFrame = e.OpenDepthImageFrame())
            {
                if (imageFrame != null)
                {
                    // We need to initialize if colorDataNeedsInitialization is true or if the format has changed.
                    depthDataNeedsInitialization |= (lastImageFormat != imageFrame.Format);

                    if (depthDataNeedsInitialization)
                    {
                        pixelData = new short[imageFrame.PixelDataLength];
                        depthFrame32 = new byte[imageFrame.Width * imageFrame.Height * 4];
                    }

                    imageFrame.CopyPixelDataTo(pixelData);

                    byte[] convertedDepthBits = convertDepthFrame(pixelData, ((KinectSensor)sender).DepthStream.TooFarDepth);

                    //An interopBitmap is a WPF construct that enables resetting the Bits of the image.
                    //This is more efficient than doing a BitmapSource.Create call every frame.
                    if (depthDataNeedsInitialization)
                    {
                        imageHelper = new InteropBitmapHelper(imageFrame.Width, imageFrame.Height, convertedDepthBits);
                        kinectDepthImage.Source = imageHelper.InteropBitmap;
                    }
                    else
                    {
                        imageHelper.UpdateBits(convertedDepthBits);
                    }

                    // If we succeeded, we can mark the depth data as successfully initialized
                    depthDataNeedsInitialization = false;
                    lastImageFormat = imageFrame.Format;

                    UpdateFrameRate();
                }
            }
        }

        // Converts a 16-bit grayscale depth frame which includes player indexes into a 32-bit frame
        // that displays different players in different colors
        byte[] convertDepthFrame(short[] depthFrame, int tooFarDepth )
        {
            for (int i16 = 0, i32 = 0; i16 < depthFrame.Length && i32 < depthFrame32.Length; i16++, i32 += 4)
            {
                int player = depthFrame[i16] & DepthImageFrame.PlayerIndexBitmask;
                int realDepth = depthFrame[i16] >> DepthImageFrame.PlayerIndexBitmaskWidth;

                // transform 13-bit depth information into an 8-bit intensity appropriate
                // for display (we disregard information in most significant bit)
                byte intensity = (byte)(255 - (255 * realDepth / 0x0fff));

                depthFrame32[i32 + RedIndex] = 0;
                depthFrame32[i32 + GreenIndex] = 0;
                depthFrame32[i32 + BlueIndex] = 0;

                // choose different display colors based on player
                switch (player)
                {
                    case 0:
                        if (realDepth == 0) //white 
                        {
                            depthFrame32[i32 + RedIndex] = 255;
                            depthFrame32[i32 + GreenIndex] = 255;
                            depthFrame32[i32 + BlueIndex] = 255;
                        }
                        else if (realDepth == tooFarDepth) //dark purple
                        {
                            depthFrame32[i32 + RedIndex] = 66;
                            depthFrame32[i32 + GreenIndex] = 0;
                            depthFrame32[i32 + BlueIndex] = 66;
                        }
                        else
                        {
                            depthFrame32[i32 + RedIndex] = (byte)(intensity / 2);
                            depthFrame32[i32 + GreenIndex] = (byte)(intensity / 2);
                            depthFrame32[i32 + BlueIndex] = (byte)(intensity / 2);
                        }
                        break;
                    case 1:
                        depthFrame32[i32 + RedIndex] = intensity;
                        break;
                    case 2:
                        depthFrame32[i32 + GreenIndex] = intensity;
                        break;
                    case 3:
                        depthFrame32[i32 + RedIndex] = (byte)(intensity / 4);
                        depthFrame32[i32 + GreenIndex] = (byte)(intensity);
                        depthFrame32[i32 + BlueIndex] = (byte)(intensity);
                        break;
                    case 4:
                        depthFrame32[i32 + RedIndex] = (byte)(intensity);
                        depthFrame32[i32 + GreenIndex] = (byte)(intensity);
                        depthFrame32[i32 + BlueIndex] = (byte)(intensity / 4);
                        break;
                    case 5:
                        depthFrame32[i32 + RedIndex] = (byte)(intensity);
                        depthFrame32[i32 + GreenIndex] = (byte)(intensity / 4);
                        depthFrame32[i32 + BlueIndex] = (byte)(intensity);
                        break;
                    case 6:
                        depthFrame32[i32 + RedIndex] = (byte)(intensity / 2);
                        depthFrame32[i32 + GreenIndex] = (byte)(intensity / 2);
                        depthFrame32[i32 + BlueIndex] = (byte)(intensity);
                        break;
                    case 7:
                        depthFrame32[i32 + RedIndex] = (byte)(255 - intensity);
                        depthFrame32[i32 + GreenIndex] = (byte)(255 - intensity);
                        depthFrame32[i32 + BlueIndex] = (byte)(255 - intensity);
                        break;
                }
            }
            return depthFrame32;
        }
        #endregion DepthImage Processing

        #region Private State
        // We want to control how depth data gets converted into false-color data
        // for more intuitive visualization, so we keep 32-bit color frame buffer versions of
        // these, to be updated whenever we receive and process a 16-bit frame.
        private const int RedIndex = 2;
        private const int GreenIndex = 1;
        private const int BlueIndex = 0;

        private bool depthDataNeedsInitialization = true;
        private DepthImageFormat lastImageFormat;
        private short[] pixelData;
        private byte[] depthFrame32;
        private InteropBitmapHelper imageHelper;

        #endregion Private State
    }
}
