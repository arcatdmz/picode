package com.phybots.picode.api;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.phybots.picode.camera.Camera;
import com.phybots.picode.camera.CameraManager;
import com.phybots.picode.ui.camera.CameraFrame;
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
				Method method = poserClass.getMethod("getSecondaryCameraClass");
				poserType.secondaryCameraClass = (Class<? extends Camera>) method.invoke(null);
				if (poserType.secondaryCameraClass != null) {
					poserType.secondaryCameraConstructor =
							poserType.secondaryCameraClass.getConstructor();
				}
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
	private List<Poser> posers;
	private Poser currentPoser;
	private CameraManager cameraManager;
	private CameraFrame cameraFrame;

	private PoserLibrary() {
		cameraManager = new CameraManager();
		posers = new ArrayList<Poser>();
	}

	public static Poser newInstance(String identifier) {
		String[] keys = identifier.split(Poser.identifierSeparator);
		PoserTypeInfo typeInfo = getTypeInfo(keys[0]);
		try {
			Poser poser = typeInfo.constructor.newInstance();
			if (poser instanceof PoserWithConnector) {
				((PoserWithConnector) poser).setConnector(keys[1]);
			}
			return poser;
		} catch (Exception e) {
			return null;
		}
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
			ClassUtils.getClasses(Poser.class.getName().substring(0,
					Poser.class.getName().lastIndexOf(".")));
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

	public void showCameraFrame(boolean isVisible) {
		if (cameraFrame == null) {
			cameraFrame = new CameraFrame();
			cameraFrame.setPoser(currentPoser);
		}
		cameraFrame.setVisible(isVisible);
	}

	public Poser newPoserInstance(PoserInfo poserInfo) {
		Poser poser;
		try {
			poser = poserInfo.type.constructor.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		poser.setName(poserInfo.name);
		if (poserInfo.type.supportsConnector) {
			PoserWithConnector p = (PoserWithConnector) poser;
			p.setConnector(poserInfo.connector);
			p.connect();
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
		}
		setCurrentPoser(poser);
	}

	void removePoser(Poser poser) {
		if (!posers.remove(poser)) {
			return;
		}
		poser.dispose();
		if (ide != null) {
			ide.onRemovePoser(poser);
		}
		setCurrentPoser(null);
	}

	public void setCurrentPoser(Poser poser) {
		if (ide != null
				&& poser != this.currentPoser) {
			ide.onCurrentPoserChange(poser);
		}
		this.currentPoser = poser;
		if (cameraFrame != null) {
			cameraFrame.setPoser(currentPoser);
		}
	}

	public Poser getCurrentPoser() {
		return currentPoser;
	}

	public void dispose() {
		if (cameraFrame != null) {
			cameraFrame.dispose();
			cameraFrame = null;
		}
		cameraManager.dispose();
	}

}
