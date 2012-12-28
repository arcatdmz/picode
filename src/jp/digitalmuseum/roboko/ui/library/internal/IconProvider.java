package jp.digitalmuseum.roboko.ui.library.internal;

import javax.swing.Icon;

public interface IconProvider {
	public Icon getIcon(Object key);
	public String getName(Object key);
}