package com.phybots.picode.ui.editor;

public class Decoration implements Comparable<Decoration> {
	private int offset;
	private int length;
	private Type type;
	private Object option;

	public Decoration(int offset, int length, Type type, Object option) {
		this.offset = offset;
		this.length = length;
		this.type = type;
		this.option = option;
	}

	public Decoration(int startIndex, int length, Type type) {
		this(startIndex, length, type, null);
	}

	public int compareTo(Decoration decoration) {
		return offset - decoration.offset;
	}

	public int getOffset() {
		return offset;
	}

	public int getLenth() {
		return length;
	}

	public Type getType() {
		return type;
	}

	public Object getOption() {
		return option;
	}

	public static enum Type {
		ERROR, KEYWORD, COMMENT, POSE
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		sb.append(" from:");
		sb.append(getOffset());
		sb.append(" length:");
		sb.append(getLenth());
		sb.append(" option:");
		sb.append(getOption());
		return sb.toString();
	}
}
