package com.phybots.picode.builder;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.phybots.picode.ProcessingIntegration;
import com.phybots.picode.ui.PicodeFrame;

import processing.app.Base;
import processing.app.Preferences;
import processing.app.RunnerListener;
import processing.app.SketchException;
import processing.app.exec.StreamRedirectThread;
import processing.core.PApplet;
import processing.mode.java.runner.MessageConsumer;
import processing.mode.java.runner.MessageSiphon;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.ExceptionEvent;

/**
 * Copied from {@link processing.mode.java.runner.Runner}
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Launcher implements MessageConsumer {

  private Builder builder;
  
  public Launcher(Builder builder) {
    this.builder = builder;
    this.listener = builder.getPicodeMain().getPintegration();
  }

  private PicodeFrame getPicodeFrame() {
    return builder.getPicodeMain() != null ?
      builder.getPicodeMain().getFrame() : null;
  }
  
  private ProcessingIntegration getPintegration() {
    return builder.getPicodeMain() != null ?
      builder.getPicodeMain().getPintegration() : null;
  }

  private SketchException placeException(String message, String filename,
                                         int lineNumber) {
    // TODO [Enhancement] Exception visualization.
    return null;
  }

  private boolean presenting;

  // Object that listens for error messages or exceptions.
  protected RunnerListener listener;

  // Running remote VM
  protected VirtualMachine vm;

  // Thread transferring remote error stream to our error stream
  protected Thread errThread = null;

  // Thread transferring remote output stream to our output stream
  protected Thread outThread = null;

  // Mode for tracing the Trace program (default= 0 off)
  protected int debugTraceMode = 0;

  //  Do we want to watch assignments to fields
  protected boolean watchFields = false;

  // Class patterns for which we don't want events
  protected String[] excludes = {
      "java.*", "javax.*", "sun.*", "com.sun.*",
      "apple.*",
      "processing.*"
  };

  protected SketchException exception;
  //private PrintStream leechErr;

//  public void launch(String appletClassName, boolean presenting) {
//    this.appletClassName = appletClassName;
  public void launch(boolean presenting) {
    this.presenting = presenting;

    // all params have to be stored as separate items,
    // so a growable array needs to be used. i.e. -Xms128m -Xmx1024m
    // will throw an error if it's shoved into a single array element
    //Vector params = new Vector();

    // get around Apple's Java 1.5 bugs
    //params.add("/System/Library/Frameworks/JavaVM.framework/Versions/1.4.2/Commands/java");
    //params.add("java");
    //System.out.println("0");

    String[] machineParamList = getMachineParams();
    String[] sketchParamList = getSketchParams();

    vm = launchVirtualMachine(machineParamList, sketchParamList);
    if (vm != null) {
      generateTrace(null);
      //redirectStreams(vm);

//      try {
//        generateTrace(new PrintWriter("/Users/fry/Desktop/output.txt"));
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
    }
    builder.getPicodeMain().stopSketch();
  }

  /**
   * <dl>
   * <dt>In:</dt>
   * <dd>mainClassName, javaLibraryPath, classPath</dd>
   * </dl>
   */
  protected String[] getMachineParams() {
    ArrayList<String> params = new ArrayList<String>();

    //params.add("-Xint"); // interpreted mode
    //params.add("-Xprof");  // profiler
    //params.add("-Xaprof");  // allocation profiler
    //params.add("-Xrunhprof:cpu=samples");  // old-style profiler

    // P5TODO change this to use run.args = true, run.args.0, run.args.1, etc.
    // so that spaces can be included in the arg names
    String options = Preferences.get("run.options");
    if (options.length() > 0) {
      String pieces[] = PApplet.split(options, ' ');
      for (int i = 0; i < pieces.length; i++) {
        String p = pieces[i].trim();
        if (p.length() > 0) {
          params.add(p);
        }
      }
    }

//    params.add("-Djava.ext.dirs=nuffing");

    if (Preferences.getBoolean("run.options.memory")) {
      params.add("-Xms" + Preferences.get("run.options.memory.initial") + "m");
      params.add("-Xmx" + Preferences.get("run.options.memory.maximum") + "m");
    }

    if (Base.isMacOS()) {
      params.add("-Xdock:name=" + builder.getMainClassName());
//      params.add("-Dcom.apple.mrj.application.apple.menu.about.name=" +
//                 sketch.getMainClassName());
    }
    // sketch.libraryPath might be ""
    // librariesClassPath will always have sep char prepended
    params.add("-Djava.library.path=" +
               builder.getJavaLibraryPath() +
               File.pathSeparator +
               System.getProperty("java.library.path"));

    params.add("-cp");
    params.add(builder.getClassPath());
//    params.add(sketch.getClassPath() +
//        File.pathSeparator +
//        Base.librariesClassPath);

    // enable assertions - http://dev.processing.org/bugs/show_bug.cgi?id=1188
    params.add("-ea");
    //PApplet.println(PApplet.split(sketch.classPath, ':'));

    String outgoing[] = new String[params.size()];
    params.toArray(outgoing);

//    PApplet.println(outgoing);
//    PApplet.println(PApplet.split(outgoing[0], ":"));
//    PApplet.println();
//    PApplet.println("class path");
//    PApplet.println(PApplet.split(outgoing[2], ":"));

    return outgoing;
    //return (String[]) params.toArray();

//  System.out.println("sketch class path");
//  PApplet.println(PApplet.split(sketch.classPath, ';'));
//  System.out.println();
//  System.out.println("libraries class path");
//  PApplet.println(PApplet.split(Base.librariesClassPath, ';'));
//  System.out.println();
  }

  /**
   * <dl>
   * <dt>In:</dt>
   * <dd>foundMain, mainClassName, javaLibraryPath, classPath</dd>
   * </dl>
   */
  protected String[] getSketchParams() {
    ArrayList<String> params = new ArrayList<String>();

    // It's dangerous to add your own main() to your code,
    // but if you've done it, we'll respect your right to hang yourself.
    // http://dev.processing.org/bugs/show_bug.cgi?id=1446
    if (builder.isFoundMain()) {
      params.add(builder.getMainClassName());

    } else {
      params.add("processing.core.PApplet");

      // get the stored device index (starts at 0)
      int runDisplay = Preferences.getInteger("run.display");

      // If there was a saved location (this guy has been run more than once)
      // then the location will be set to the last position of the sketch window.
      // This will be passed to the PApplet runner using something like
      // --location=30,20
      // Otherwise, the editor location will be passed, and the applet will
      // figure out where to place itself based on the editor location.
      // --editor-location=150,20
      if (getPicodeFrame() != null) {  // if running processing-cmd, don't do placement
        GraphicsDevice editorDevice =
          getPicodeFrame().getGraphicsConfiguration().getDevice();
        GraphicsEnvironment ge =
          GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();

        // Make sure the display set in Preferences actually exists
        GraphicsDevice runDevice = editorDevice;
        if (runDisplay >= 0 && runDisplay < devices.length) {
          runDevice = devices[runDisplay];
        } else {
          runDevice = editorDevice;
          for (int i = 0; i < devices.length; i++) {
            if (devices[i] == runDevice) {
              runDisplay = i;
              break;
              // Don't set the pref, might be a temporary thing. Users can
              // open/close Preferences to reset the device themselves.
//              Preferences.setInteger("run.display", runDisplay);
            }
          }
        }

        Point windowLocation = getPintegration().getSketchLocation();
//        if (windowLocation != null) {
//          // could check to make sure the sketch location is on the device
//          // that's specified in Preferences, but that's going to be annoying
//          // if you move a sketch to another window, then it keeps jumping
//          // back to the specified window.
////          Rectangle screenRect =
////            runDevice.getDefaultConfiguration().getBounds();
//        }
        if (windowLocation == null) {
          if (editorDevice == runDevice) {
            // If sketches are to be shown on the same display as the editor,
            // provide the editor location so the sketch's main() can place it.
            Point editorLocation = getPicodeFrame().getLocation();
            params.add(PApplet.ARGS_EDITOR_LOCATION + "=" +
                       editorLocation.x + "," + editorLocation.y);
          } else {
            // The sketch's main() will set a location centered on the new
            // display. It has to happen in main() because the width/height
            // of the sketch are not known here.
//             Set a location centered on the other display
//            Rectangle screenRect =
//              runDevice.getDefaultConfiguration().getBounds();
//            int runX =
//            params.add(PApplet.ARGS_LOCATION + "=" + runX + "," + runY);
          }
        } else {
          params.add(PApplet.ARGS_LOCATION + "=" +
                     windowLocation.x + "," + windowLocation.y);
        }
        params.add(PApplet.ARGS_EXTERNAL);
      }

      params.add(PApplet.ARGS_DISPLAY + "=" + runDisplay);
      params.add(PApplet.ARGS_SKETCH_FOLDER + "=" +
                 builder.getSketchPath());

      if (presenting) {
        params.add(PApplet.ARGS_FULL_SCREEN);
//        if (Preferences.getBoolean("run.present.exclusive")) {
//          params.add(PApplet.ARGS_EXCLUSIVE);
//        }
        params.add(PApplet.ARGS_STOP_COLOR + "=" +
                   Preferences.get("run.present.stop.color"));
        params.add(PApplet.ARGS_BGCOLOR + "=" +
                   Preferences.get("run.present.bgcolor"));
      }

      params.add(builder.getMainClassName());
    }

//    String outgoing[] = new String[params.size()];
//    params.toArray(outgoing);
//    return outgoing;
    return params.toArray(new String[0]);
  }


  protected VirtualMachine launchVirtualMachine(String[] vmParams,
                                                String[] classParams) {
    //vm = launchTarget(sb.toString());
    LaunchingConnector connector = (LaunchingConnector)
      findConnector("com.sun.jdi.RawCommandLineLaunch");
    //PApplet.println(connector);  // gets the defaults

    //Map arguments = connectorArguments(connector, mainArgs);
    Map arguments = connector.defaultArguments();

    Connector.Argument commandArg =
      (Connector.Argument)arguments.get("command");
    // Using localhost instead of 127.0.0.1 sometimes causes a
    // "Transport Error 202" error message when trying to run.
    // http://dev.processing.org/bugs/show_bug.cgi?id=895
    String addr = "127.0.0.1:" + (8000 + (int) (Math.random() * 1000));
    //String addr = "localhost:" + (8000 + (int) (Math.random() * 1000));
    //String addr = "" + (8000 + (int) (Math.random() * 1000));

    String commandArgs =
      "java -Xrunjdwp:transport=dt_socket,address=" + addr + ",suspend=y ";
    if (Base.isWindows()) {
      commandArgs =
        "java -Xrunjdwp:transport=dt_shmem,address=" + addr + ",suspend=y ";
    } else if (Base.isMacOS()) {
      // Decided to just set this to 1.6 only, because otherwise it's gonna
      // be a shitshow if folks are getting Apple's 1.6 with 32-bit and
      // Oracle's 1.7 when run in 64-bit mode. ("Why does my sketch suck in
      // 64-bit? Why is retina broken?)
      // The --request flag will prompt to install Apple's 1.6 JVM if none is
      // available. We're specifying 1.6 so that we can get support for both
      // 32- and 64-bit, because Oracle won't be releasing Java 1.7 in 32-bit.
      // Helpfully, the --request flag is not present on Mac OS X 10.6
      // (luckily it is also not needed, because 1.6 is installed by default)
      // but it requires an additional workaround to not use that flag,
      // otherwise will see an error about an unsupported option. The flag is
      // available with 10.7 and 10.8, the only other supported versions of
      // OS X at this point, because we require 10.6.8 and higher. That also
      // means we don't need to check for any other OS versions, unless
      // is a douchebag and modifies Info.plist to get around the restriction.
      addr = "" + (8000 + (int) (Math.random() * 1000));
      commandArgs =
        "/usr/libexec/java_home " +
        (System.getProperty("os.version").startsWith("10.6") ? "" : "--request ") +
        "--version 1.6 " +
        "--exec java " +
        "-d" + Base.getNativeBits() + " " +
        "-Xrunjdwp:transport=dt_socket,address=" + addr + ",suspend=y ";
    }

    for (int i = 0; i < vmParams.length; i++) {
      commandArgs = addArgument(commandArgs, vmParams[i], ' ');
    }
    if (classParams != null) {
      for (int i = 0; i < classParams.length; i++) {
        commandArgs = addArgument(commandArgs, classParams[i], ' ');
      }
    }
    commandArg.setValue(commandArgs);

    Connector.Argument addressArg =
      (Connector.Argument)arguments.get("address");
    addressArg.setValue(addr);

    //PApplet.println(connector);  // prints the current
    //com.sun.tools.jdi.AbstractLauncher al;
    //com.sun.tools.jdi.RawCommandLineLauncher rcll;

    //System.out.println(PApplet.javaVersion);
    // http://java.sun.com/j2se/1.5.0/docs/guide/jpda/conninv.html#sunlaunch
    try {
      return connector.launch(arguments);
    } catch (IOException exc) {
      throw new Error("Unable to launch target VM: " + exc);
    } catch (IllegalConnectorArgumentsException exc) {
      throw new Error("Internal error: " + exc);
    } catch (VMStartException exc) {
      Process p = exc.process();
      //System.out.println(p);
      String[] errorStrings = PApplet.loadStrings(p.getErrorStream());
      /*String[] inputStrings =*/ PApplet.loadStrings(p.getInputStream());

      if (errorStrings != null && errorStrings.length > 1) {
        if (errorStrings[0].indexOf("Invalid maximum heap size") != -1) {
          Base.showWarning("Way Too High",
                           "Please lower the value for \u201Cmaximum available memory\u201D in the\n" +
                           "Preferences window. For more information, read Help \u2192 Troubleshooting.",
                           exc);
        } else {
          PApplet.println(errorStrings);
          System.err.println("Using startup command:");
          PApplet.println(arguments);
        }
      } else {
        exc.printStackTrace();
        System.err.println("Could not run the sketch (Target VM failed to initialize).");
        if (Preferences.getBoolean("run.options.memory")) {
          // Only mention this if they've even altered the memory setup
          System.err.println("Make sure that you haven't set the maximum available memory too high.");
        }
        System.err.println("For more information, read revisions.txt and Help \u2192 Troubleshooting.");
      }
      // changing this to separate editor and listener [091124]
      //if (editor != null) {
      listener.statusError("Could not run the sketch.");
      //}
      return null;
    }
  }


  private static boolean hasWhitespace(String string) {
    int length = string.length();
    for (int i = 0; i < length; i++) {
      if (Character.isWhitespace(string.charAt(i))) {
        return true;
      }
    }
    return false;
  }


  private static String addArgument(String string, String argument, char sep) {
    if (hasWhitespace(argument) || argument.indexOf(',') != -1) {
      // Quotes were stripped out for this argument, add 'em back.
      StringBuffer buffer = new StringBuffer(string);
      buffer.append('"');
      for (int i = 0; i < argument.length(); i++) {
        char c = argument.charAt(i);
        if (c == '"') {
          buffer.append('\\');
        }
        buffer.append(c);
      }
      buffer.append('\"');
      buffer.append(sep);
      return buffer.toString();
    } else {
      return string + argument + String.valueOf(sep);
    }
  }


//  /**
//   * Redirect a VMs output and error streams to System.out and System.err
//   *
//   * @param vm the VM
//   */
//  protected void redirectStreams(VirtualMachine vm) {
//    MessageSiphon ms = new MessageSiphon(vm.process().getErrorStream(), this);
//    errThread = ms.getThread();
//    outThread = new StreamRedirectThread("VM output reader", vm.process().getInputStream(), System.out);
//    errThread.start();
//    outThread.start();
//  }


  /**
   * Generate the trace.
   * Enable events, start thread to display events,
   * start threads to forward remote error and output streams,
   * resume the remote VM, wait for the final event, and shutdown.
   */
  protected void generateTrace(PrintWriter writer) {
    vm.setDebugTraceMode(debugTraceMode);

    EventThread eventThread = null;
    //if (writer != null) {
    eventThread = new EventThread(this, writer);
    eventThread.start();
    eventThread.setEventRequests(watchFields);
    //}

    //redirectOutput();

    Process process = vm.process();

//  processInput = new SystemOutSiphon(process.getInputStream());
//  processError = new MessageSiphon(process.getErrorStream(), this);

    // Copy target's output and error to our output and error.
//    errThread = new StreamRedirectThread("error reader",
//        process.getErrorStream(),
//        System.err);
    MessageSiphon ms = new MessageSiphon(process.getErrorStream(), this);
    errThread = ms.getThread();

    outThread = new StreamRedirectThread("output reader",
        process.getInputStream(),
        System.out);

    errThread.start();
    outThread.start();

    vm.resume();
    //System.out.println("done with resume");

    // Shutdown begins when event thread terminates
    try {
      if (eventThread != null) eventThread.join();
//      System.out.println("in here");
      // Bug #852 tracked to this next line in the code.
      // http://dev.processing.org/bugs/show_bug.cgi?id=852
      errThread.join(); // Make sure output is forwarded
//      System.out.println("and then");
      outThread.join(); // before we exit
//      System.out.println("out of it");

      // At this point, disable the run button.
      // This happens when the sketch is exited by hitting ESC,
      // or the user manually closes the sketch window.
      // P5TODO this should be handled better, should it not?
      /*
      if (editor != null) {
        editor.deactivateRun();
      }
      */
    } catch (InterruptedException exc) {
      // we don't interrupt
    }
    //System.out.println("and leaving");
    if (writer != null) writer.close();
  }


  protected Connector findConnector(String connectorName) {
    List connectors = Bootstrap.virtualMachineManager().allConnectors();
//    List connectors =
//      org.eclipse.jdi.Bootstrap.virtualMachineManager().allConnectors();

    // debug: code to list available connectors
//    Iterator iter2 = connectors.iterator();
//    while (iter2.hasNext()) {
//      Connector connector = (Connector)iter2.next();
//      System.out.println("connector name is " + connector.name());
//    }

    for (Object c : connectors) {
      Connector connector = (Connector) c;
//      System.out.println(connector.name());
//    }
//    Iterator iter = connectors.iterator();
//    while (iter.hasNext()) {
//      Connector connector = (Connector)iter.next();
      if (connector.name().equals(connectorName)) {
        return connector;
      }
    }
    throw new Error("No connector");
  }


  public void exception(ExceptionEvent event) {
//    System.out.println(event);
    ObjectReference or = event.exception();
    ReferenceType rt = or.referenceType();
    String exceptionName = rt.name();
    //Field messageField = Throwable.class.getField("detailMessage");
    Field messageField = rt.fieldByName("detailMessage");
//    System.out.println("field " + messageField);
    Value messageValue = or.getValue(messageField);
//    System.out.println("mess val " + messageValue);

    //"java.lang.ArrayIndexOutOfBoundsException"
    int last = exceptionName.lastIndexOf('.');
    String message = exceptionName.substring(last + 1);
    if (messageValue != null) {
      String messageStr = messageValue.toString();
      if (messageStr.startsWith("\"")) {
        messageStr = messageStr.substring(1, messageStr.length() - 1);
      }
      message += ": " + messageStr;
    }
//    System.out.println("mess type " + messageValue.type());
    //StringReference messageReference = (StringReference) messageValue.type();

//    System.out.println(or.referenceType().fields());
//    if (name.startsWith("java.lang.")) {
//      name = name.substring(10);
    if (!handleCommonErrors(exceptionName, message, listener)) {
      reportException(message, or, event.thread());
    }
    /*
    if (editor != null) {
      editor.deactivateRun();
    }
    */
  }


  public static boolean handleCommonErrors(final String exceptionClass,
                                           final String message,
                                           final RunnerListener listener) {
    if (exceptionClass.equals("java.lang.OutOfMemoryError")) {
      if (message.contains("exceeds VM budget")) {
        // P5TODO this is a kludge for Android, since there's no memory preference
        listener.statusError("OutOfMemoryError: This code attempts to use more memory than available.");
        System.err.println("An OutOfMemoryError means that your code is either using up too much memory");
        System.err.println("because of a bug (e.g. creating an array that's too large, or unintentionally");
        System.err.println("loading thousands of images), or simply that it's trying to use more memory");
        System.err.println("than what is supported by the current device.");
      } else {
        listener.statusError("OutOfMemoryError: You may need to increase the memory setting in Preferences.");
        System.err.println("An OutOfMemoryError means that your code is either using up too much memory");
        System.err.println("because of a bug (e.g. creating an array that's too large, or unintentionally");
        System.err.println("loading thousands of images), or that your sketch may need more memory to run.");
        System.err.println("If your sketch uses a lot of memory (for instance if it loads a lot of data files)");
        System.err.println("you can increase the memory available to your sketch using the Preferences window.");
      }

    } else if (exceptionClass.equals("java.lang.StackOverflowError")) {
      listener.statusError("StackOverflowError: This sketch is attempting too much recursion.");
      System.err.println("A StackOverflowError means that you have a bug that's causing a function");
      System.err.println("to be called recursively (it's calling itself and going in circles),");
      System.err.println("or you're intentionally calling a recursive function too much,");
      System.err.println("and your code should be rewritten in a more efficient manner.");

    } else if (exceptionClass.equals("java.lang.UnsupportedClassVersionError")) {
      listener.statusError("UnsupportedClassVersionError: A library is using code compiled with an unsupported version of Java.");
      System.err.println("This version of Processing only supports libraries and JAR files compiled for Java 1.6 or earlier.");
      System.err.println("A library used by this sketch was compiled for Java 1.7 or later, ");
      System.err.println("and needs to be recompiled to be compatible with Java 1.6.");

    } else if (exceptionClass.equals("java.lang.NoSuchMethodError") ||
               exceptionClass.equals("java.lang.NoSuchFieldError")) {
      listener.statusError(exceptionClass.substring(10) + ": " +
                           "You may be using a library that's incompatible " +
                           "with this version of Processing.");
    } else {
      return false;
    }
    return true;
  }


  // P5TODO This may be called more than one time per error in the VM,
  // presumably because exceptions might be wrapped inside others,
  // and this will fire for both.
  protected void reportException(String message, ObjectReference or, ThreadReference thread) {
    listener.statusError(findException(message, or, thread));
  }


  /**
   * Move through a list of stack frames, searching for references to code
   * found in the current sketch. Return with a RunnerException that contains
   * the location of the error, or if nothing is found, just return with a
   * RunnerException that wraps the error message itself.
   */
  SketchException findException(String message, ObjectReference or, ThreadReference thread) {
    try {
      // use to dump the stack for debugging
//      for (StackFrame frame : thread.frames()) {
//        System.out.println("frame: " + frame);
//      }

      List<StackFrame> frames = thread.frames();
      for (StackFrame frame : frames) {
        try {
          Location location = frame.location();
          String filename = null;
          filename = location.sourceName();
          int lineNumber = location.lineNumber() - 1;
          SketchException rex =
            placeException(message, filename, lineNumber);
          if (rex != null) {
            return rex;
          }
        } catch (AbsentInformationException e) {
          // Any of the thread.blah() methods can throw an AbsentInformationEx
          // if that bit of data is missing. If so, just write out the error
          // message to the console.
          //e.printStackTrace();  // not useful
          exception = new SketchException(message);
          exception.hideStackTrace();
          listener.statusError(exception);
        }
      }
    } catch (IncompatibleThreadStateException e) {
      // This shouldn't happen, but if it does, print the exception in case
      // it's something that needs to be debugged separately.
      e.printStackTrace();
    }
    //// before giving up, try to extract from the throwable object itself
    //// since sometimes exceptions are re-thrown from a different context
    try {
      //// assume object reference is Throwable, get stack trace
      Method method = ((ClassType) or.referenceType()).concreteMethodByName("getStackTrace", "()[Ljava/lang/StackTraceElement;");
      ArrayReference result = (ArrayReference) or.invokeMethod(thread, method, new ArrayList<Value>(), ObjectReference.INVOKE_SINGLE_THREADED);
      //// iterate through stack frames and pull filename and line number for each
      for (Value val: result.getValues()) {
        ObjectReference ref = (ObjectReference)val;
        method = ((ClassType) ref.referenceType()).concreteMethodByName("getFileName", "()Ljava/lang/String;");
        StringReference strref = (StringReference) ref.invokeMethod(thread, method, new ArrayList<Value>(), ObjectReference.INVOKE_SINGLE_THREADED);
        String filename = strref.value();
        method = ((ClassType) ref.referenceType()).concreteMethodByName("getLineNumber", "()I");
        IntegerValue intval = (IntegerValue) ref.invokeMethod(thread, method, new ArrayList<Value>(), ObjectReference.INVOKE_SINGLE_THREADED);
        int lineNumber = intval.intValue() - 1;
        SketchException rex =
          placeException(message, filename, lineNumber);
        if (rex != null) {
          return rex;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    // Give up, nothing found inside the pile of stack frames
    SketchException rex = new SketchException(message);
    // exception is being created /here/, so stack trace is not useful
    rex.hideStackTrace();
    return rex;
  }

  public void close() {
    // P5TODO make sure stop() has already been called to exit the sketch

    // P5TODO actually kill off the vm here
    if (vm != null) {
      try {
        vm.exit(0);

      } catch (com.sun.jdi.VMDisconnectedException vmde) {
        // if the vm has disconnected on its own, ignore message
        //System.out.println("harmless disconnect " + vmde.getMessage());
        // P5TODO shouldn't need to do this, need to do more cleanup
      }
      vm = null;
    }
  }


  // made synchronized for rev 87
  // attempted to remove synchronized for 0136 to fix bug #775 (no luck tho)
  // http://dev.processing.org/bugs/show_bug.cgi?id=775
  synchronized public void message(String s) {
    //System.out.println("M" + s.length() + ":" + s.trim()); // + "MMM" + s.length());

    // this eats the CRLFs on the lines.. oops.. do it later
    //if (s.trim().length() == 0) return;

    // this is PApplet sending a message (via System.out.println)
    // that signals that the applet has been quit.
    if (s.indexOf(PApplet.EXTERNAL_STOP) == 0) {
      //System.out.println("external: quit");
      /*
      if (editor != null) {
//        editor.internalCloseRunner();  // [091124]
//        editor.handleStop();  // prior to 0192
        editor.internalCloseRunner();  // 0192
      }
      */
      return;
    }

    // this is the PApplet sending us a message that the applet
    // is being moved to a new window location
    if (s.indexOf(PApplet.EXTERNAL_MOVE) == 0) {
      String nums = s.substring(s.indexOf(' ') + 1).trim();
      int space = nums.indexOf(' ');
      int left = Integer.parseInt(nums.substring(0, space));
      int top = Integer.parseInt(nums.substring(space + 1));
      // this is only fired when connected to an editor
      getPintegration().setSketchLocation(new Point(left, top));
      //System.out.println("external: move to " + left + " " + top);
      return;
    }

    // these are used for debugging, in case there are concerns
    // that some errors aren't coming through properly
//    if (s.length() > 2) {
//      System.err.println(newMessage);
//      System.err.println("message " + s.length() + ":" + s);
//    }

    // always shove out the mesage, since it might not fall under
    // the same setup as we're expecting
    System.err.print(s);
    //System.err.println("[" + s.length() + "] " + s);
    System.err.flush();
  }
}