package com.phybots.picode.api;

public interface PicodeInterface {

	public void onAddPoser(Poser poser);

	public void onRemovePoser(Poser poser);

	public void onCurrentPoserChange(Poser poser);

	public void onAddPose(Pose pose);

	public void onRemovePose(Pose pose);
}
