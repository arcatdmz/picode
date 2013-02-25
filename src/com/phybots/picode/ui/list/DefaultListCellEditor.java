package com.phybots.picode.ui.list;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;

public class DefaultListCellEditor<T> extends DefaultCellEditor implements
		ListCellEditor<T> {
	private static final long serialVersionUID = -7679782506816900864L;

	public DefaultListCellEditor(final JCheckBox checkBox) {
		super(checkBox);
	}

	public DefaultListCellEditor(final JComboBox<T> comboBox) {
		super(comboBox);
	}

	public DefaultListCellEditor(final JTextField textField) {
		super(textField);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getCellEditorValue() {
		return (T) super.getCellEditorValue();
	}

	public Component getListCellEditorComponent(JList<T> list, Object value,
			boolean isSelected, int index) {
		delegate.setValue(value);
		return editorComponent;
	}
}