package com.phybots.picode;

import com.phybots.picode.ui.PicodeMain;

/**
 * Just for people who want to persist that they're not robots ;)
 * 
 * @author Jun Kato
 */
public class Human extends Robot {

	public Human() {
		super(new com.phybots.entity.Human(""));
	}

	public Human(PicodeMain picodeMain) {
		super(picodeMain, new com.phybots.entity.Human(""));
	}

	@Override
	public String toString() {
	  return "Human (Kinect)";
	}
}
