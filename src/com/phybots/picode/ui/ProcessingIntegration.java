package com.phybots.picode.ui;

import processing.app.Base;
import processing.app.Platform;
import processing.app.Preferences;
import processing.app.RunnerListener;
import processing.app.SketchCode;

public class ProcessingIntegration implements RunnerListener {

  public static Platform getPlatform() {
    return Base.getPlatform();
  }

  public static void init() {
    Base.initPlatform();
    Preferences.init();
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

  public void statusEdit(String string, String string2) {
    // TODO Auto-generated method stub
    
  }

  public void headerRebuild() {
    // TODO Auto-generated method stub
    
  }

  public void baseHandleClose(PicodeFrame editor, boolean b) {
    // TODO Auto-generated method stub
    
  }

  public void headerRepaint() {
    // TODO Auto-generated method stub
    
  }

  public String getText() {
    // TODO Auto-generated method stub
    return null;
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