package com.phybots.picode.ui.library.internal;

import javax.swing.ListModel;

public interface MutableListModel<T> extends ListModel {
    public boolean isCellEditable(int index);
    public void setElementAt(T value, int index);
}
