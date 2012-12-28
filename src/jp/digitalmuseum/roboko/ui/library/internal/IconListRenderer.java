package jp.digitalmuseum.roboko.ui.library.internal;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;

public class IconListRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = -4868792171164183339L;
	private IconProvider iconProvider;

	public IconListRenderer(IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
				index, isSelected, cellHasFocus);
		Icon icon = iconProvider.getIcon(value);
		label.setIcon(icon);
		label.setText(iconProvider.getName(value));
		return label;
	}
}
