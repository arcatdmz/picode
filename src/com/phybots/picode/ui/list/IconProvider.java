package com.phybots.picode.ui.list;

import javax.swing.Icon;

public interface IconProvider {
	public Icon getIcon(Object key);
	public String getName(Object key);
}