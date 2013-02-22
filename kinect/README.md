kinect-thrift-server
================================================================
Copyright (C) 2013 Jun KATO
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

A pair of simple Kinect server (C#) and client (Java).

## How to use

### Kinect server (C#)

Before you run the program, [Kinect for Windows SDK] [1] and
[Microsoft Speech Platform SDK v11] [2] need to be installed
on your Windows machine.

When you're ready, just execute [ConsoleKinectServer.exe] [3].
You may add `-port PORT_NUMBER` parameter to change the port.

### Kinect client (Java)

Just double-click [KinectThriftClient-runnable.jar] [4].

You can also use its features from your Java application when
you include [KinectThriftClient.jar] [5] in the classpath.

  [1]: http://www.microsoft.com/en-us/kinectforwindows/develop/developer-downloads.aspx "Kinect for Windows SDK"
  [2]: http://www.microsoft.com/en-us/download/details.aspx?id=27226 "Microsoft Speech Platform SDK v11"
  [3]: https://github.com/arcatdmz/kinect-thrift-server/blob/master/csharp/ConsoleKinectServer/bin/Release/ConsoleKinectServer.exe "ConsoleKinectServer.exe"
  [4]: https://github.com/arcatdmz/kinect-thrift-server/blob/master/java/KinectThriftClient-runnable.jar "KinectThriftClient-runnable.jar"
  [5]: https://github.com/arcatdmz/kinect-thrift-server/blob/master/java/KinectThriftClient.jar "KinectThriftClient.jar"

## License

This work is licensed under Apache License, Version 2.0.

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
http://github.com/arcatdmz/kinect-thrift-server | i@junkato.jp