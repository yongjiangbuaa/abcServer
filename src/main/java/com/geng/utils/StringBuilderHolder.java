package com.geng.utils;

/**
 * Author: shushenglin
 * Date:   16/2/24 11:22
 */
public class StringBuilderHolder {

	private final StringBuilder sb;

	public StringBuilderHolder(int capacity) {
		sb = new StringBuilder(capacity);
	}

	/**
	 * 重置StringBuilder内部的writerIndex, 而char[]保留不动.
	 */
	public StringBuilder getStringBuilder() {
		sb.setLength(0);
		return sb;
	}
}
