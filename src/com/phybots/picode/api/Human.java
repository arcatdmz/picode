package com.phybots.picode.api;

import com.phybots.picode.PicodeMain;

public class Human extends Poser {

	public Human() {
		super();
	}

	public Human(PicodeMain picodeMain) {
		super(picodeMain);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
	  return "Human (Kinect)";
	}

}
