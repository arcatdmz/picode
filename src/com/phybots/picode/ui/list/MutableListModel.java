package com.phybots.picode.ui.list;

import javax.swing.ListModel;

public interface MutableListModel<T> extends ListModel<T> {
    public boolean isCellEditable(int index);
    public void setElementAt(T value, int index);
}
