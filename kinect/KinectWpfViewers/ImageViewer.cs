using System;
using System.ComponentModel;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using Microsoft.Kinect;

namespace Microsoft.Samples.Kinect.WpfViewers
{
    public abstract class ImageViewer : UserControl, INotifyPropertyChanged
    {
        public abstract KinectSensor Kinect { get; set;}

        #region FlipHorizontally + HorizontalScaleTransform
        public bool FlipHorizontally
        {
            get { return _FlipHorizontally; }
            set
            {
                if (_FlipHorizontally != value)
                {
                    _FlipHorizontally = value;
                    NotifyPropertyChanged("FlipHorizontally");
                    _HorizontalScaleTransform = new ScaleTransform() { ScaleX = _FlipHorizontally ? -1 : 1 };
                    NotifyPropertyChanged("HorizontalScaleTransform");
                }
            }
        }
        public ScaleTransform HorizontalScaleTransform
        {
            get
            {
                return _HorizontalScaleTransform; 
            }
        }
        #endregion

        public Stretch Stretch
        {
            get { return (Stretch)GetValue(StretchProperty); }
            set { SetValue(StretchProperty, value); }
        }

        public static readonly DependencyProperty StretchProperty =
            DependencyProperty.Register("Stretch", typeof(Stretch), typeof(KinectColorViewer), new UIPropertyMetadata(Stretch.Uniform));

        #region FrameRate property + helpers
        public bool CollectFrameRate
        {
            get { return _CollectFrameRate; }
            set
            {
                if (value != _CollectFrameRate)
                {
                    _CollectFrameRate = value;
                    NotifyPropertyChanged("CollectFrameRate");
                }
            }
        }

        public int FrameRate
        {
            get { return _FrameRate; }
            private set
            {
                if (_FrameRate != value)
                {
                    _FrameRate = value;
                    NotifyPropertyChanged("FrameRate");
                }
            }
        }

        protected void ResetFrameRateCounters()
        {
            if (CollectFrameRate)
            {
                lastTime = DateTime.MaxValue;
                totalFrames = 0;
                lastFrames = 0;
            }
        }

        protected void UpdateFrameRate()
        {
            if (CollectFrameRate)
            {
                ++totalFrames;

                DateTime cur = DateTime.Now;
                if (lastTime == DateTime.MaxValue || cur.Subtract(lastTime) > TimeSpan.FromSeconds(1))
                {
                    FrameRate = totalFrames - lastFrames;
                    lastFrames = totalFrames;
                    lastTime = cur;
                }
            }
        }
        #endregion FrameRate property + helpers

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

        #region Private State
        private bool _FlipHorizontally = false;
        private ScaleTransform _HorizontalScaleTransform;

        private int _FrameRate = -1;
        private bool _CollectFrameRate = false;
        protected int totalFrames;
        protected int lastFrames;
        protected DateTime lastTime = DateTime.MaxValue;
        #endregion Private State
    }
}
