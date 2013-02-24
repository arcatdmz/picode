package com.phybots.picode;

import java.util.ArrayList;
import java.util.List;

import com.phybots.picode.api.Poser;

public class RobotManager {
	List<Poser> robots;

	public List<Poser> getRobots() {
		return new ArrayList<Poser>(robots);
	}

	public void addRobot(Poser robot) {
		robots.add(robot);
		// picodeFrame.updateRobotList();
	}

	public void removeRobot(Poser robot) {
		if (robots.remove(robot)) {
			robot.dispose();
			// picodeFrame.updateRobotList();
		}
	}

	public Poser getCurrentPoser() {
		// TODO Auto-generated method stub
		return null;
	}

}
