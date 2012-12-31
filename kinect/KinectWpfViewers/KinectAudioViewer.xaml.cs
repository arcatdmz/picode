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

namespace Microsoft.Samples.Kinect.WpfViewers
{
    /// <summary>
    /// Interaction logic for KinectAudioViewer.xaml
    /// </summary>
    public partial class KinectAudioViewer : UserControl
    {
        double _angle;
        double _soundSourceAngle;
        KinectSensor _kinect;

        public KinectAudioViewer()
        {
            InitializeComponent();
            MarkWidth = 0.05;
            SoundSourceWidth = 0.05;
            BeamAngleInDegrees = 0;
            BeamDisplayText = null;
            SoundSourceAngleInDegrees = 0;
            SoundSourceDisplayText = null;
        }

        /// <summary>
        /// String overlayed on beam indicator
        /// </summary>
        public string BeamDisplayText
        {
            get 
            {
                return txtDisplayBeam.Text;
            }
            set
            {
                txtDisplayBeam.Text = value;
            }
        }

        /// <summary>
        /// String overlayed on sound source indicator
        /// </summary>
        public string SoundSourceDisplayText
        {
            get
            {
                return txtDisplaySource.Text;
            }
            set
            {
                txtDisplaySource.Text = value;
            }
        }

        /// <summary>
        /// Width of the beam mark, in the 0-0.5 range
        /// </summary>
        public double MarkWidth { get; set; }

        /// <summary>
        /// Audio beam angle, in degrees
        /// </summary>
        public double BeamAngleInDegrees
        {
            set
            {
                // save RAW sensor value
                _angle = value;

                // Angle is in Degrees, so map the MinBeamAngle..MaxBeamAngle range to 0..1
                // and clamp
                double gradientOffset = (value / (KinectAudioSource.MaxBeamAngle - KinectAudioSource.MinBeamAngle)) + 0.5;
                if ( gradientOffset > 1.0 ) gradientOffset = 1.0;
                if ( gradientOffset < 0.0 ) gradientOffset = 0.0;

                // Move the gradient stops together
                gsPre.Offset = Math.Max(gradientOffset - MarkWidth, 0);
                gsIt.Offset = gradientOffset;
                gsPos.Offset = Math.Min(gradientOffset + MarkWidth, 1);
            }
            get
            {
                return _angle;
            }
        }

        /// <summary>
        /// Width of the sound source mark, in the 0-0.5 range
        /// </summary>
        public double SoundSourceWidth { get; set; }

        /// <summary>
        /// Sound direction angle, in degrees
        /// </summary>
        public double SoundSourceAngleInDegrees
        {
            set
            {
                // save RAW sensor value
                _soundSourceAngle = value;

                // Angle is in Degrees, so map the MinSoundSourceAngle..MaxSoundSourceAngle range to 0..1
                // and clamp
                double gradientOffset = (value / (KinectAudioSource.MaxSoundSourceAngle - KinectAudioSource.MinSoundSourceAngle) ) + 0.5;
                if (gradientOffset > 1.0) gradientOffset = 1.0;
                if (gradientOffset < 0.0) gradientOffset = 0.0;

                // Move the gradient stops together
                gsPreS.Offset = Math.Max(gradientOffset - SoundSourceWidth, 0);
                gsItS.Offset = gradientOffset;
                gsPosS.Offset = Math.Min(gradientOffset + SoundSourceWidth, 1);
            }
            get
            {
                return _soundSourceAngle;
            }
        }

        /// <summary>
        /// Sensor
        /// </summary>
        public KinectSensor Kinect
        {
            get 
            {
                return _kinect;
            }
            set
            {
                if (_kinect != null && _kinect.AudioSource != null)
                { 
                    // remove old handlers
                    _kinect.AudioSource.BeamAngleChanged -= new EventHandler<BeamAngleChangedEventArgs>(AudioSource_BeamChanged);
                    _kinect.AudioSource.SoundSourceAngleChanged -= new EventHandler<SoundSourceAngleChangedEventArgs>(AudioSource_SoundSourceAngleChanged);
                }

                _kinect = value;

                if (_kinect != null && _kinect.AudioSource != null)
                { 
                    // add new handlers
                    _kinect.AudioSource.BeamAngleChanged += new EventHandler<BeamAngleChangedEventArgs>(AudioSource_BeamChanged);
                    _kinect.AudioSource.SoundSourceAngleChanged +=new EventHandler<SoundSourceAngleChangedEventArgs>(AudioSource_SoundSourceAngleChanged);
                }
            }
        }

        void AudioSource_SoundSourceAngleChanged(object sender, SoundSourceAngleChangedEventArgs e)
        {
            // Set width of mark based on confidence
            SoundSourceWidth = Math.Max( ((1 - e.ConfidenceLevel) / 2), 0.02);

            // Move indicator
            SoundSourceAngleInDegrees = e.Angle;

            // Update text
            SoundSourceDisplayText = " Sound source angle = " + SoundSourceAngleInDegrees.ToString("0.00") + " deg  Confidence level=" + e.ConfidenceLevel.ToString("0.00");
        }

        void AudioSource_BeamChanged(object sender, BeamAngleChangedEventArgs e)
        {
            // Move our indicator
            BeamAngleInDegrees = e.Angle;

            // Update Text
            BeamDisplayText = " Audio beam angle = " + BeamAngleInDegrees.ToString("0.00") + " deg";
        }

    }

}
