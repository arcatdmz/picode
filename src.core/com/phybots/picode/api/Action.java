package com.phybots.picode.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.phybots.service.ServiceAbstractImpl;

public class Action {
	private Poser robot;
	private List<ActionElement> elements;
	private ActionService actionService;

	Action(Poser robot) {
		this.robot = robot;
		elements = new ArrayList<ActionElement>();
	}
	
	public Action pose(Pose pose) {
		ActionElement e = new ActionElement();
		e.pose = pose;
		elements.add(e);
		return this;
	}
	
	public Action stay(long wait) {
		ActionElement e = new ActionElement();
		e.wait = wait;
		elements.add(e);
		return this;
	}
	
	public void play() {
		actionService = new ActionService(this);
		actionService.start();
	}
	
	public boolean isPlaying() {
		return actionService != null &&
				actionService.isStarted();
	}
	
	private static class ActionElement {
		Pose pose;
		long wait;
	}
	
	private static class ActionService extends ServiceAbstractImpl {
		private static final long serialVersionUID = 22694340261932745L;
		private Action action;
		private int index;
		private ActionElement currentElement;
		private long lastTime;

		public ActionService(Action action) {
			this.action = action;
			this.index = 0;
		}
		
		@Override
		protected void onStart() {
			next();
		}

		public void run() {
			if (currentElement.pose != null) {
				if (!action.robot.isActing()) {
					next();
				}
			} else {
				long currentTime = new Date().getTime();
				if (currentTime - lastTime > currentElement.wait) {
					next();
				}
			}
		}
		
		private void next() {
			if (index >= action.elements.size()) {
				this.stop();
				return;
			}
			this.currentElement = action.elements.get(index ++);
			lastTime = new Date().getTime();
			if (currentElement.pose != null) {
				action.robot.setPose(currentElement.pose);
			}
		}
	}
}
