package com.phybots.picode.api;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.CameraManager;
import com.phybots.utils.ClassUtils;

@SuppressWarnings("unchecked")
public class PoserLibrary {

	private static PoserLibrary instance;

	public static PoserLibrary getInstance() {
		if (instance == null) {
			instance = new PoserLibrary();
		}
		return instance;
	}

	private static Set<PoserTypeInfo> poserTypeInfos;

	static {
		Set<Class<? extends Poser>> poserClasses = getPoserClasses();
		poserTypeInfos = new HashSet<PoserTypeInfo>();
		for (Class<? extends Poser> poserClass : poserClasses) {

			PoserTypeInfo poserType = new PoserTypeInfo();

			poserType.typeName = poserClass.getSimpleName();

			try {
				poserType.constructor = poserClass.getConstructor();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			if (PoserWithConnector.class.isAssignableFrom(poserClass)) {
				poserType.supportsConnector = true;
			}

			try {
				Method method = poserClass.getMethod("getCameraClass");
				poserType.defaultCameraClass = (Class<? extends Camera>) method.invoke(null);
				poserType.defaultCameraConstructor = poserType.defaultCameraClass.getConstructor();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			try {
				Method method = poserClass.getMethod("getPoseClass");
				poserType.poseClass = (Class<? extends Pose>) method.invoke(null);
				poserType.poseConstructor = poserType.poseClass.getConstructor();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			poserTypeInfos.add(poserType);
		}
	}

	private PicodeInterface ide;
	private CameraManager cameraManager;
	private List<Poser> posers;
	private Poser currentPoser;

	private PoserLibrary() {
		cameraManager = new CameraManager();
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

	public static PoserTypeInfo getTypeInfo(Poser poser) {
		if (poser == null) {
			return null;
		}
		return getTypeInfo(poser.getClass().getSimpleName());
	}

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

	public void attachIDE(PicodeInterface ide) {
		this.ide = ide;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	public Poser newPoserInstance(PoserInfo poserInfo) {
		Poser poser;
		try {
			poser = poserInfo.type.constructor.newInstance();
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
		if (ide != null) {
			ide.onAddPoser(poser);
			setCurrentPoser(poser);
		}
	}

	void removePoser(Poser poser) {
		if (!posers.remove(poser)) {
			return;
		}
		poser.dispose();
		if (ide != null) {
			ide.onRemovePoser(poser);
		}
	}

	public void setCurrentPoser(Poser poser) {
		if (ide != null
				&& poser != this.currentPoser) {
			ide.onCurrentPoserChange(poser);
			this.currentPoser = poser;
		}
	}

	public Poser getCurrentPoser() {
		return ide == null ?
				null : currentPoser;
	}

}
