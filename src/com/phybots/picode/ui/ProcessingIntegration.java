package com.phybots.picode.ui;

import processing.app.Base;
import processing.app.Platform;
import processing.app.Preferences;

public class ProcessingIntegration {

	public static void init() {
		Base.initPlatform();
		Preferences.init();
	}

	public static Platform getPlatform() {
		return Base.getPlatform();
	}
}
