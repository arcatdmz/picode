package com.phybots.picode.api;

import javax.swing.Icon;
import com.phybots.picode.api.TypeBasedPoseLibrary;
import com.phybots.picode.ui.list.IconListModel;
import com.phybots.picode.ui.list.IconProvider;

public class TypeBasedPoseLibrary extends IconListModel<Pose> implements IconProvider {
	private static final long serialVersionUID = 4052983716336989888L;
	private PoserTypeInfo poserType;

	public TypeBasedPoseLibrary(PoserTypeInfo poserType) {
		this.poserType = poserType;
	}

	public PoserTypeInfo getPoserType() {
		return poserType;
	}

	public Pose get(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof String) {
			return get((String) object);
		} else if (object instanceof Pose) {
			return (Pose) object;
		}
		return null;
	}

	public Icon getIcon(Object object) {
		Pose pose = get(object);
		return pose != null ? pose.getIcon() : null;
	}

	public String getName(Object object) {
		Pose pose = get(object);
		return pose != null ? pose.getName() : null;
	}

}
