package com.phybots.picode.ui;

import javax.swing.JOptionPane;

import processing.app.Base;
import processing.app.Platform;
import processing.app.Preferences;
import processing.app.RunnerListener;
import processing.app.SketchCode;
import processing.app.SketchException;

public class ProcessingIntegration implements RunnerListener {
  private PicodeMain picodeMain;

  public static Platform getPlatform() {
    return Base.getPlatform();
  }

  public static void init() {
    Base.initPlatform();
    Preferences.init();
  }
  
  public ProcessingIntegration(PicodeMain picodeMain) {
    this.picodeMain = picodeMain;
  }

  public void statusNotice(String string) {
    // TODO 自動生成されたメソッド・スタブ

  }

  public int getSelectionStart() {
    // TODO 自動生成されたメソッド・スタブ
    return 0;
  }

  public int getSelectionStop() {
    // TODO 自動生成されたメソッド・スタブ
    return 0;
  }

  public int getScrollPosition() {
    // TODO 自動生成されたメソッド・スタブ
    return 0;
  }

  public void setCode(SketchCode current) {
    // TODO 自動生成されたメソッド・スタブ

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

  public void headerRebuild() {
    picodeMain.getFrame().updateTabs();
  }

  public void baseHandleClose(PicodeFrame editor, boolean b) {
    // TODO Auto-generated method stub
    
  }

  public void headerRepaint() {
    // TODO Auto-generated method stub
    
  }

  /**
   * Corresponds to {@link processing.app.Editor#getText()}.
   */
  public String getText() {
    // This actually does nothing.
    // (In Picode, text buffer is always synchronized with the editor state.)
    return picodeMain.getSketch().getCurrentCode().getProgram();
  }

  public void removeRecent() {
    // TODO Auto-generated method stub
    
  }

  public void addRecent() {
    // TODO Auto-generated method stub
    
  }

  public void updateTitle() {
    // TODO Auto-generated method stub
    
  }

  public void baseRebuildSketchbookMenus() {
    // TODO Auto-generated method stub
    
  }

  // Runner listener methods are implemented below:
  
  @Override
  public void statusError(String message) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void statusError(Exception exception) {
    // TODO Auto-generated method stub
    
  }

  public void statusError(SketchException se) {
    picodeMain.getFrame().setStatusText(
      PicodeMain.getErrorString(
        picodeMain.getSketch(), se));
  }

  @Override
  public void startIndeterminate() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void stopIndeterminate() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void statusHalt() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isHalted() {
    // TODO Auto-generated method stub
    return false;
  }
}