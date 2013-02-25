package com.phybots.picode.ui.list;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;

public abstract class IconListModel<T> extends DefaultListModel<T> implements MutableListModel<T>, IconProvider {
	private static final long serialVersionUID = -4583426804317292958L;
	private Map<String, T> database = new HashMap<String, T>();

	public T get(String key) {
		return database.get(key);
	}

	@Override
	public void add(int index, T object) {
		super.add(index, object);
		database.put(getName(object), object);
	}

	@Override
	public T remove(int index) {
		T object = super.remove(index);
		if (object != null) {
			database.remove(getName(object));
		}
		return object;
	}

	@Override
	public T set(int index, T object) {
		T oldObject = super.set(index, object);
		database.remove(getName(oldObject));
		database.put(getName(object), object);
		return oldObject;
	}

	@Override
	public void addElement(T object) {
		super.addElement(object);
		database.put(getName(object), object);
	}

	@Override
	public void insertElementAt(T object, int index) {
		super.insertElementAt(object, index);
		database.put(getName(object), object);
	}

	@Override
	public void removeAllElements() {
		super.removeAllElements();
		database.clear();
	}

	@Override
	public boolean removeElement(Object object) {
		if (super.removeElement(object)) {
			database.remove(getName(object));
			return true;
		}
		return false;
	}

	@Override
	public void removeElementAt(int index) {
		Object object = getElementAt(index);
		super.removeElementAt(index);
		database.remove(getName(object));
	}

	@Override
	public void removeRange(int fromIndex, int toIndex) {
		for (int i = fromIndex; i < toIndex; i ++) {
			Object object = getElementAt(i);
			database.remove(getName(object));
		}
		super.removeRange(fromIndex, toIndex);
	}

	@Override
	public void setElementAt(T object, int index) {
		Object oldObject = getElementAt(index);
		super.setElementAt(object, index);
		database.remove(getName(oldObject));
		database.put(getName(object), object);
	}

	@Override
	public boolean contains(Object object) {
		if (object == null) {
			return false;
		}
		if (object instanceof String) {
			return database.containsKey(object);
		}
		return super.contains(object);
	}

	public boolean isCellEditable(int index) {
		return true;
	}
}
