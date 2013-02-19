Picode - inline photos representing posture data
================================================

Copyright (C) 2013 Jun KATO

version 0.0.1
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

## How to use

### For Windows

Download [DirectShow for Java wrapper](http://www.humatic.de/htools/dsj.htm) and place dsj.{dll|jar} in
.\lib\phybots\library\ directory.

Then, simply double-click picode.jar or launch picode.cmd with arguments as described below:

```
# With Kinect
.\picode.cmd -type Human
# With MindstormsNXT
.\picode.cmd -type MindstormsNXT -address btspp://deadbeaf
```

### For Mac OS X

Simply double-click picode.jar or launch picode.sh with arguments as described below:

```
# With MindstormsNXT (Kinect is not supported at this moment.)
./picode.sh -type MindstormsNXT -address btspp://deadbeaf
```

### Supported hardware

- Mindstorms NXT (Windows, Mac)
- Kinect for Windows (Windows)

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
http://github.com/arcatdmz/picode
arc (at) digitalmuseum.jp