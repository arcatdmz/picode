using System;
using System.Runtime.InteropServices;
using System.Windows.Interop;
using System.Windows.Media;

namespace Microsoft.Samples.Kinect.WpfViewers
{

    /// <summary>
    /// InteropBitmapHelper is a helper class meant to ease the scenario of using an interopBitmap.
    /// InteropBitmaps are a construct in WPF which allow update of the image bits in an efficient way.
    /// </summary>
    public class InteropBitmapHelper
    {
        #region ctors
        public InteropBitmapHelper(int width, int height, byte[] pixelData)
            : this(width, height, pixelData, PixelFormats.Bgr32.BitsPerPixel, PixelFormats.Bgr32)
        {
        }

        public InteropBitmapHelper(int width, int height, byte[] pixelData, int bitsPerPixel, PixelFormat pixelFormat)
        {
            Initialize(width, height, pixelData, bitsPerPixel, pixelFormat);
        }

        public InteropBitmapHelper(int width, int height, int[] pixelData)
            : this(width, height, pixelData, sizeof(int) * 8, PixelFormats.Bgra32)  //We've enabled "A" via BGRA32 here.
        {
        }

        public InteropBitmapHelper(int width, int height, int[] pixelData, int bitsPerPixel, PixelFormat pixelFormat)
        {
            Initialize(width, height, pixelData, bitsPerPixel, pixelFormat);
        }

        private void Initialize(int width, int height, Array pixelData, int bitsPerPixel, PixelFormat pixelFormat)
        {
            _Width = width;
            _Height = height;
            _PixelFormat = pixelFormat;

            int stride = width * ((bitsPerPixel + 7) / 8);
            _ColorByteCount = (uint)(height * stride);
            var colorFileMapping = CreateFileMapping(new IntPtr(-1), IntPtr.Zero, 0x04, 0, _ColorByteCount, null);
            _ImageBits = MapViewOfFile(colorFileMapping, 0xF001F, 0, 0, _ColorByteCount);
            byte[] bytes = pixelData as byte[];
            if (bytes != null)
            {
                Marshal.Copy(bytes, 0, _ImageBits, (int)_ColorByteCount);
            }
            else
            {
                int[] ints = pixelData as int[];
                if (ints != null)
                {
                    Marshal.Copy(ints, 0, _ImageBits, (int)_ColorByteCount / sizeof(int));
                }
            }
            InteropBitmap = Imaging.CreateBitmapSourceFromMemorySection(colorFileMapping, width, height, _PixelFormat, stride, 0) as InteropBitmap;
        }
        #endregion


        #region public API
        public void UpdateBits(byte[] pixelData)
        {
            Marshal.Copy(pixelData, 0, _ImageBits, (int)_ColorByteCount);
            InteropBitmap.Invalidate();
        }

        public void UpdateBits(int[] pixelData)
        {
            Marshal.Copy(pixelData, 0, _ImageBits, ((int)_ColorByteCount)/sizeof(int));
            InteropBitmap.Invalidate();
        }

        public InteropBitmap InteropBitmap { get; private set; }
        #endregion


        #region private state
        private int _Width;
        private int _Height;
        private PixelFormat _PixelFormat;
        private IntPtr _ImageBits;
        private uint _ColorByteCount;
        #endregion


        #region DllImports
        [DllImport("kernel32.dll", SetLastError = true)]
        static extern IntPtr CreateFileMapping(IntPtr hFile,
                                               IntPtr lpFileMappingAttributes,
                                              uint flProtect,
                                              uint dwMaximumSizeHigh,
                                              uint dwMaximumSizeLow,
                                              string lpName);
        [DllImport("kernel32.dll", SetLastError = true)]
        static extern IntPtr MapViewOfFile(IntPtr hFileMappingObject,
                                          uint dwDesiredAccess,
                                          uint dwFileOffsetHigh,
                                          uint dwFileOffsetLow,
                                          uint dwNumberOfBytesToMap);
        #endregion


    }
}
