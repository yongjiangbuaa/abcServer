package com.geng.utils;


/**
 * Author: shushenglin
 * Date:   2016/12/15 11:49
 */
public class ThreadLocalStringBuilder extends ThreadLocal<StringBuilderHolder> {
	private int initCapacity = 16;

	public ThreadLocalStringBuilder(int initCapacity) {
		this.initCapacity = initCapacity;
	}

	public StringBuilder getStringBuilder(){
		return get().getStringBuilder();
	}

	@Override
	protected StringBuilderHolder initialValue() {
		return new StringBuilderHolder(initCapacity);
	}
}
