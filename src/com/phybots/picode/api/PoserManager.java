package com.phybots.picode.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.camera.CameraManager;
import com.phybots.utils.ClassUtils;

public class PoserManager {

	private static PoserManager instance;
	private static Set<PoserTypeInfo> poserTypeInfos;

	public static PoserManager getInstance() {
		if (instance == null) {
			instance = new PoserManager();
		}
		return instance;
	}

	static {
		Set<Class<? extends Poser>> poserClasses = getPoserClasses();
		poserTypeInfos = new HashSet<PoserTypeInfo>();
		for (Class<? extends Poser> poserClass : poserClasses) {

			PoserTypeInfo poserTypeInfo = new PoserTypeInfo();

			poserTypeInfo.typeName = poserClass.getSimpleName();

			try {
				poserTypeInfo.constructor = poserClass.getConstructor(PicodeMain.class);
			} catch (Exception e) {
				continue;
			}

			if (PoserWithConnector.class.isAssignableFrom(poserClass)) {
				poserTypeInfo.supportsConnector = true;
			}
			
			poserTypeInfos.add(poserTypeInfo);
		}
	}

	private PicodeMain picodeMain;
	private GlobalPoseLibrary globalPoseLibrary;
	private CameraManager cameraManager;
	private List<Poser> posers;

	private PoserManager() {
		cameraManager = new CameraManager();
		globalPoseLibrary = new GlobalPoseLibrary();
		posers = new ArrayList<Poser>();
	}

	public static Set<PoserTypeInfo> getTypeInfos() {
		return new HashSet<PoserTypeInfo>(poserTypeInfos);
	}

	public static PoserTypeInfo getTypeInfo(String string) {
		for (PoserTypeInfo pti : poserTypeInfos) {
			if (pti.typeName.equals(string)) {
				return pti;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static Set<Class<? extends Poser>> getPoserClasses() {
		Set<Class<? extends Poser>> classSet = new HashSet<Class<? extends Poser>>();

		List<Class<?>> classObjects =
			ClassUtils.getClasses("com.phybots.picode.api");
		for (Class<?> classObject : classObjects) {

			if (classSet.contains(classObject)) {
				continue;
			}

			if (!Poser.class.isAssignableFrom(classObject)) {
				continue;
			}

			int mod = classObject.getModifiers();
			if (Modifier.isAbstract(mod) ||
					Modifier.isInterface(mod)) {
				continue;
			}

			classSet.add((Class<? extends Poser>) classObject);
		}
		return classSet;
	}

	public void setIDE(PicodeMain picodeMain) {
		this.picodeMain = picodeMain;
	}

	public GlobalPoseLibrary getPoseLibrary() {
		return globalPoseLibrary;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	public Poser newPoserInstance(PoserInfo poserInfo) {
		Poser poser;
		try {
			poser = poserInfo.type.constructor.newInstance(picodeMain);
		} catch (Exception e) {
			return null;
		}
		poser.setName(poserInfo.name);
		if (poserInfo.type.supportsConnector) {
			((PoserWithConnector) poser).setConnector(poserInfo.connector);
		}
		return poser;
	}

	public List<Poser> getPosers() {
		return new ArrayList<Poser>(posers);
	}

	public Poser findPoser(String poserIdentifier) {
		for (Poser poser : posers) {
			if (poser.getIdentifier().equals(poserIdentifier)) {
				return poser;
			}
		}
		return null;
	}

	void addPoser(Poser poser) {
		posers.add(poser);
		if (picodeMain != null) {
			picodeMain.getFrame();
			//
		}
	}

	void removePoser(Poser poser) {
		if (!posers.remove(poser)) {
			return;
		}
		poser.dispose();
		if (picodeMain != null) {
			//
		}
	}

	public void setCurrentPoser(Poser poser) {
		if (picodeMain == null) {
			return;
		}
	}

	public Poser getCurrentPoser() {
		if (picodeMain == null) {
			return null;
		}
		return null;
	}

	public static class PoserInfo {
		public PoserTypeInfo type;
		public String connector;
		public String name;
	}

	public static class PoserTypeInfo {
		public String typeName;
		public boolean supportsConnector;
		public Constructor<? extends Poser> constructor;
	}

}
