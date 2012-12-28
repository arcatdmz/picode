package jp.digitalmuseum.roboko.core;

import jp.digitalmuseum.roboko.RobokoMain;

/**
 * Just for people who want to persist that they're not robots ;)
 * 
 * @author Jun Kato
 */
public class Human extends Robot {

	public Human() {
		super(new com.phybots.entity.Human(""));
	}

	public Human(RobokoMain robokoMain) {
		super(robokoMain, new com.phybots.entity.Human(""));
	}

}
