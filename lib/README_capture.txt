capture - a webcam capture package for Windows/Mac/Linux
Copyright (C) 2010 Jun KATO

version 1.0.3
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

Webcam capture package, or simply "capture", is a simple
package for capturing real-time images using webcams.
It uses
- DirectShow for Windows
- QuickTime for Mac OSX
- Java Media Framework (Video4Linux) for Linux
to capture images.

"capture" is distributed under MPL 1.1/GPL 2.0/LGPL 2.1 triple
license. Please read LICENSE.txt for the detail.
You can get the source code by visiting its official site.

This toolkit works well with "napkit" which can detect
position and rotation of ARToolKit markers in captured images.
Please see http://mr.digitalmuseum.jp/ for details.


=== How to use ===

== Windows

To use this toolkit on Windows (in other words, when capturing
images with DirectShow), you will need "DirectShow for Java",
a wrapper library of DirectShow for Java developed by humatic.
Note that you are assumed to agree with its license when you
use its function.
The library and its license documents are available at:
http://www.humatic.de/htools/dsj.htm

When you get dsj.jar and dsj.dll in the library,
please place it under the directory where Java VM can find.

== Mac OSX (or Windows with WinVDIG)

To use this toolkit on Mac OSX (in other words, when capturing
images with QuickTime), you will need "QuickTime for Java",
a wrapper library of QuickTime for Java developed by Apple.

The library is installed by default in the case of Mac OSX,
so nothing is needed for using this library.
When you use Windows, you must install QuickTime Player and
WinVDIG before using this function.

To learn about WinVDIG, please ask Google:
http://www.google.co.jp/search?q=winvdig

== Linux (or to whom loves Java Media Framework)

To use this toolkit on Linux (in other words, when capturing
images with Java Media Framework), you will need
"Java Media Framework" installed on your computer.

To learn about Java Media Framework, please see:
http://java.sun.com/javase/technologies/desktop/media/jmf/

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
http://phybots.com/
arc (at) digitalmuseum.jp
