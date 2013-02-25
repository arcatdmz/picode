package com.phybots.picode.ui.list;

import java.awt.Component;

import javax.swing.CellEditor;
import javax.swing.JList;

public interface ListCellEditor<T> extends CellEditor {
	public T getCellEditorValue();
	Component getListCellEditorComponent(JList<T> list, Object value,
			boolean isSelected, int index);
}