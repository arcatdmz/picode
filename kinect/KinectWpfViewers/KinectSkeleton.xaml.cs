using System.Windows;
using System.Windows.Controls;

namespace Microsoft.Samples.Kinect.WpfViewers
{
    /// <summary>
    /// Interaction logic for KinectSkeleton.xaml
    /// </summary>
    public partial class KinectSkeleton : Canvas
    {
        public Point CenterPoint
        {
            get { return new Point(Canvas.GetLeft(centerPosition), Canvas.GetTop(centerPosition)); }
            set
            {
                Canvas.SetLeft(centerPosition, value.X);
                Canvas.SetTop(centerPosition, value.Y);
            }
        }

        public bool ShowCenter
        {
            get { return centerPosition.Visibility == Visibility.Visible; }
            set
            {
                if (value)
                {
                    //Hide all other children (joints/bones)
                    foreach (FrameworkElement child in Children)
                    {
                        child.Visibility = Visibility.Collapsed;
                    }
                    centerPosition.Visibility = Visibility.Visible;
                }
                else
                {
                    //Show all other children (joints/bones)
                    foreach (FrameworkElement child in Children)
                    {
                        child.Visibility = Visibility.Visible;
                    }
                    centerPosition.Visibility = Visibility.Collapsed;
                }
            }
        }

        public KinectSkeleton()
        {
            InitializeComponent();
        }
    }
}
