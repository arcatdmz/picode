package com.phybots.picode;

import java.awt.EventQueue;
import java.awt.Point;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.phybots.picode.ui.PicodeFrame;
import com.phybots.picode.ui.editor.PicodeEditor;

import processing.app.Base;
import processing.app.PicodeSketch;
import processing.app.Platform;
import processing.app.Preferences;
import processing.app.RunnerListener;
import processing.app.SketchCode;
import processing.app.SketchException;

public class ProcessingIntegration implements RunnerListener {
  private PicodeMain picodeMain;

  private Point sketchWindowLocation;

  public static Platform getPlatform() {
    return Base.getPlatform();
  }

  public static void init() {
    Base.initPlatform();
    Preferences.init();
  }
  
//  public void statusError(SketchException se) {
//    PicodeSketch sketch = picodeMain.getSketch();
//    statusError(getErrorString(sketch, se));
//  }

  public static String getErrorString(PicodeSketch sketch, SketchException se) {
    StringBuilder sb = new StringBuilder();
    sb.append(se.getCodeIndex() < 0 ?
        "-" :
        sketch.getCode(se.getCodeIndex()).getFileName());
    if (se.getCodeLine() >= 0) {
      sb.append(", L");
      sb.append(se.getCodeLine() + 1);
      if (se.getCodeColumn() >= 0) {
        sb.append(":");
        sb.append(se.getCodeColumn() + 1);
      }
    }
    sb.append(" ");
    sb.append(se.getMessage());
    return sb.toString();
  }
  
  public ProcessingIntegration(PicodeMain picodeMain) {
    this.picodeMain = picodeMain;
  }

  public int getSelectionStart() {
    // TODO [Enhancement] Code area selection.
    return 0;
  }

  public int getSelectionStop() {
    // TODO [Enhancement] Code area selection.
    return 0;
  }

  /**
   * Get view state to be restored in the future by {@link #setCode(SketchCode)}.
   * 
   * @return Vertical scroll position
   */
  public int getScrollPosition() {
    PicodeEditor editor = picodeMain.getFrame().getCurrentEditor();
    if (editor == null) {
      return 0;
    }
    return editor.getOuterScrollPane().getViewport().getViewPosition().y;
  }

  /**
   * Corresponds to {@link processing.app.Editor#getText()}. Set view state.
   * 
   * Switch between tabs, this swaps out the Document object
   * that's currently being manipulated.
   */
  public void setCode(SketchCode current) {
    PicodeEditor editor = picodeMain.getFrame().getCurrentEditor();
    if (editor == null) {
      return;
    }
    editor.getOuterScrollPane().getViewport().setViewPosition(
        new Point(0, current.getScrollPosition()));
  }

  public void statusEdit(String message, String defaultValue) {
    Object result = JOptionPane.showInputDialog(
      picodeMain.getFrame(), message, null,
      JOptionPane.QUESTION_MESSAGE, null, null, defaultValue);
    if (result == null) {
      return;
    }
    String name = result.toString();
    picodeMain.getSketch().nameCode(name);
  }

  /**
   * Corresponds to {@link processing.app.Editor#getText()}.
   * 
   * Get the contents of the current buffer. Used by the Sketch class.
   */
  public String getText() {
    // This actually does nothing.
    // (In Picode, text buffer is always synchronized with the editor state.)
    return picodeMain.getSketch().getCurrentCode().getProgram();
  }

  public void removeRecent() {
    // TODO [Enhancement] Recent sketch list.
    
  }

  public void addRecent() {
    // TODO [Enhancement] Recent sketch list.
    
  }

  /**
   * Copied from {@link processing.app.Editor#setSketchLocation(Point)}.
   * 
   * Set the location of the sketch run window. Used by Runner to update the
   * Editor about window drag events while the sketch is running.
   */
  public void setSketchLocation(Point p) {
    sketchWindowLocation = p;
  }


  /**
   * Copied from {@link processing.app.Editor#getSketchLocation()}.
   * 
   * Get the last location of the sketch's run window. Used by Runner to make
   * the window show up in the same location as when it was last closed.
   */
  public Point getSketchLocation() {
    return sketchWindowLocation;
  }

  /**
   * Copied from {@link processing.app.Editor#checkModified()}.
   * 
   * Check if the sketch is modified and ask user to save changes.
   * @return false if canceling the close/quit operation
   */
  public boolean checkModified() {
    PicodeSketch sketch = picodeMain.getSketch();
    if (!sketch.isModified()) return true;

    // As of Processing 1.0.10, this always happens immediately.
    // http://dev.processing.org/bugs/show_bug.cgi?id=1456

    String prompt = "Save changes to " + sketch.getName() + "?  ";

    if (!Base.isMacOS()) {
      int result =
        JOptionPane.showConfirmDialog(picodeMain.getFrame(), prompt, "Close",
                                      JOptionPane.YES_NO_CANCEL_OPTION,
                                      JOptionPane.QUESTION_MESSAGE);

      if (result == JOptionPane.YES_OPTION) {
        return handleSave(true);

      } else if (result == JOptionPane.NO_OPTION) {
        return true;  // ok to continue

      } else if (result == JOptionPane.CANCEL_OPTION ||
                 result == JOptionPane.CLOSED_OPTION) {
        return false;

      } else {
        throw new IllegalStateException();
      }

    } else {
      // This code is disabled unless Java 1.5 is being used on Mac OS X
      // because of a Java bug that prevents the initial value of the
      // dialog from being set properly (at least on my MacBook Pro).
      // The bug causes the "Don't Save" option to be the highlighted,
      // blinking, default. This sucks. But I'll tell you what doesn't
      // suck--workarounds for the Mac and Apple's snobby attitude about it!
      // I think it's nifty that they treat their developers like dirt.

      // Pane formatting adapted from the quaqua guide
      // http://www.randelshofer.ch/quaqua/guide/joptionpane.html
      JOptionPane pane =
        new JOptionPane("<html> " +
                        "<head> <style type=\"text/css\">"+
                        "b { font: 13pt \"Lucida Grande\" }"+
                        "p { font: 11pt \"Lucida Grande\"; margin-top: 8px }"+
                        "</style> </head>" +
                        "<b>Do you want to save changes to this sketch<BR>" +
                        " before closing?</b>" +
                        "<p>If you don't save, your changes will be lost.",
                        JOptionPane.QUESTION_MESSAGE);

      String[] options = new String[] {
        "Save", "Cancel", "Don't Save"
      };
      pane.setOptions(options);

      // highlight the safest option ala apple hig
      pane.setInitialValue(options[0]);

      // on macosx, setting the destructive property places this option
      // away from the others at the lefthand side
      pane.putClientProperty("Quaqua.OptionPane.destructiveOption",
                             new Integer(2));

      JDialog dialog = pane.createDialog(picodeMain.getFrame(), null);
      dialog.setVisible(true);

      Object result = pane.getValue();
      if (result == options[0]) {  // save (and close/quit)
        return handleSave(true);

      } else if (result == options[2]) {  // don't save (still close/quit)
        return true;

      } else {  // cancel?
        return false;
      }
    }
  }

  
  /**
   * Copied and modified from {@link processing.app.Editor#updateTitle()}.
   * 
   * Set the title of the PDE window based on the current sketch, i.e.
   * something like "sketch_070752a - Processing 0126"
   */
  public void updateTitle() {
    PicodeSketch sketch = picodeMain.getSketch();
    if (sketch == null) {
      picodeMain.getFrame().setTitle("Picode");
    } else {
      picodeMain.getFrame().setTitle(sketch.getName() + " | Picode");
    }

    if (sketch != null && !sketch.isUntitled()) {
      // set current file for OS X so that cmd-click in title bar works
      File sketchFile = sketch.getMainFile();
      picodeMain.getFrame().getRootPane().putClientProperty("Window.documentFile", sketchFile);
    } else {
      // per other applications, don't set this until the file has been saved
      picodeMain.getFrame().getRootPane().putClientProperty("Window.documentFile", null);
    }
  }

  
  /**
   * Copied from {@link processing.app.Editor#handleSave(boolean)}.
   * 
   * Actually handle the save command. If 'immediately' is set to false,
   * this will happen in another thread so that the message area
   * will update and the save button will stay highlighted while the
   * save is happening. If 'immediately' is true, then it will happen
   * immediately. This is used during a quit, because invokeLater()
   * won't run properly while a quit is happening. This fixes
   * <A HREF="http://dev.processing.org/bugs/show_bug.cgi?id=276">Bug 276</A>.
   */
  public boolean handleSave(boolean immediately) {
//    handleStop();  // 0136

    if (picodeMain.getSketch().isUntitled()) {
      return handleSaveAs();
      // need to get the name, user might also cancel here

    } else if (immediately) {
      handleSaveImpl();

    } else {
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            handleSaveImpl();
          }
        });
    }
    return true;
  }

  /**
   * Copied from {@link processing.app.Editor#handleSaveImpl()}.
   */
  protected void handleSaveImpl() {
    statusNotice("Saving...");
    try {
      if (picodeMain.getSketch().save()) {
        statusNotice("Done Saving.");
      } else {
        statusEmpty();
      }

    } catch (Exception e) {
      // show the error as a message in the window
      statusError(e);

      // zero out the current action,
      // so that checkModified2 will just do nothing
      //checkModifiedMode = 0;
      // this is used when another operation calls a save
    }
  }

  /**
   * Copied from {@link processing.app.Editor#handleSaveAs()}.
   */
  public boolean handleSaveAs() {
    statusNotice("Saving...");
    try {
      if (picodeMain.getSketch().saveAs()) {
        statusNotice("Done Saving.");
        // Disabling this for 0125, instead rebuild the menu inside
        // the Save As method of the Sketch object, since that's the
        // only one who knows whether something was renamed.
        //sketchbook.rebuildMenusAsync();
      } else {
        statusNotice("Save Canceled.");
        return false;
      }
    } catch (Exception e) {
      // show the error as a message in the window
      statusError(e);
    }
    return true;
  }

  // Base methods are implemented below.

  public void baseRebuildSketchbookMenus() {
	  // Do nothing.
  }

  /**
   * Close a sketch.
   */
  public void baseHandleClose(PicodeFrame editor, boolean b) {
    picodeMain.loadSketch(PicodeSketch.newInstance(picodeMain));
  }

  // ProcessingHeader methods are implemented below.

  public void headerRepaint() {
    // Do nothing.
  }

  public void headerRebuild() {
    // Do nothing.
  }

  // Runner listener methods are implemented below:
  
  /**
   * Copied from {@link processing.app.Editor#statusError(String)}.
   * 
   * Show an error in the status bar.
   */
  public void statusError(String what) {
    picodeMain.getFrame().setStatusText(what);
  }


  /**
   * Copied from {@link processing.app.Editor#statusError(Exception)}.
   * 
   * Show an exception in the editor status bar.
   */
  public void statusError(Exception e) {
    // e.printStackTrace(); // Picode: commented out.
//    if (e == null) {
//      System.err.println("Editor.statusError() was passed a null exception.");
//      return;
//    }

    if (e instanceof SketchException) {
      SketchException re = (SketchException) e;
      if (re.hasCodeIndex()) {
        picodeMain.getSketch().setCurrentCode(re.getCodeIndex());
      }
      if (re.hasCodeLine()) {
        int line = re.getCodeLine();
        // subtract one from the end so that the \n ain't included
        SketchCode code = picodeMain.getSketch().getCode(re.getCodeIndex());
        if (line >= code.getLineCount()) {
          // The error is at the end of this current chunk of code,
          // so the last line needs to be selected.
          line = code.getLineCount() - 1;
//TODO Do we really need this?
//          if (code.getLineText(line).length() == 0) {
//            // The last line may be zero length, meaning nothing to select.
//            // If so, back up one more line.
//            line--;
//          }
        }
        if (line < 0 || line >= code.getLineCount()) {
          System.err.println("Bad error line: " + line);
        } else {
// TODO [Enhancement] Code area selection.
//          textarea.select(textarea.getLineStartOffset(line),
//                          textarea.getLineStopOffset(line) - 1);
        }
      }
    }

    // Since this will catch all Exception types, spend some time figuring
    // out which kind and try to give a better error message to the user.
    String mess = e.getMessage();
    if (mess != null) {
      String javaLang = "java.lang.";
      if (mess.indexOf(javaLang) == 0) {
        mess = mess.substring(javaLang.length());
      }
      String rxString = "RuntimeException: ";
      if (mess.startsWith(rxString)) {
        mess = mess.substring(rxString.length());
      }
      statusError(mess);
    }
//    e.printStackTrace();
  }


  /**
   * Copied from {@link processing.app.Editor#statusNotice(String)}.
   * 
   * Show a notice message in the editor status bar.
   */
  public void statusNotice(String msg) {
	//TODO [Enhancement] Show notice.
	//    status.notice(msg);
  }


  /**
   * Copied from {@link processing.app.Editor#clearNotice(String)}.
   * 
   * Clear the status area.
   */
  public void clearNotice(String msg) {
	//TODO [Enhancement] Show notice.
	//    if (status.message.equals(msg)) {
	//      statusEmpty();
	//    }
  }


  /**
   * Copied from {@link processing.app.Editor#statusEmpty()}.
   * 
   * Clear the status area.
   */
  public void statusEmpty() {
    statusNotice(processing.app.Editor.EMPTY);
  }

  
  @Override
  public void startIndeterminate() {
  }

  @Override
  public void stopIndeterminate() {
  }

  @Override
  public void statusHalt() {
  }

  @Override
  public boolean isHalted() {
    return false;
  }

}