package com.geng.utils.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 为了省去烦人的XXX.valueOf操作, 
 * 缓存起来这些需要转型的值, 在提升时间性能的同时，也可以减轻一部分GC负担。
 * 
 * @author jiangmin.wu
 *
 */
@SuppressWarnings("serial")
public class ConfigMap extends HashMap<String, String> {
	private static Map<String, Integer> intCache = new ConcurrentHashMap<>();
	private static Map<String, Long> longCache = new ConcurrentHashMap<>();
	private static Map<String, Float> floatCache = new ConcurrentHashMap<>();
	private static Map<String, Double> doubleCache = new ConcurrentHashMap<>();

	public static final ConfigMap EMPTY_MAP = new EmptyConfigMap();

	public ConfigMap() {
		super();
	}

	public ConfigMap(Map<String, String> m) {
		super(m);
	}

	public Long getLong(String key) {
		String strVal = get(key);
		if (strVal == null){
			return null;
		}
		Long longVal = longCache.get(strVal);
		if (longVal == null) {
			longVal = Long.valueOf(strVal);
			longCache.put(strVal, longVal);
		}
		return longVal;
	}

	public Integer getInt(String key, int defaultValue) {
		if (!containsKey(key)){
			 return defaultValue;
		}
		return getInt(key);
	}

	public Integer getInt(String key) {
		String strVal = get(key);
		if (strVal == null) {
			return null;
		}
		Integer intVal = intCache.get(strVal);
		if (intVal == null) {
			intVal = Integer.valueOf(strVal);
			intCache.put(strVal, intVal);
		}
		return intVal;
	}

	public Float getFloat(String key) {
		String strVal = get(key);
		if (strVal == null) {
			return null;
		}
		Float val = floatCache.get(strVal);
		if (val == null) {
			val = Float.valueOf(strVal);
			floatCache.put(strVal, val);
		}
		return val;
	}

	public Double getDouble(String key) {
		String strVal = get(key);
		if (strVal == null) {
			return null;
		}
		Double val = doubleCache.get(strVal);
		if (val == null) {
			val = Double.valueOf(strVal);
			doubleCache.put(strVal, val);
		}
		return val;
	}

	private static class EmptyConfigMap extends ConfigMap{
		public EmptyConfigMap() {
			super();
		}

		@Override
		public String put(String key, String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAll(Map<? extends String, ? extends String> m) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String remove(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String putIfAbsent(String key, String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object key, Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean replace(String key, String oldValue, String newValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String replace(String key, String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String merge(String key, String value, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String computeIfAbsent(String key, Function<? super String, ? extends String> mappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String computeIfPresent(String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String compute(String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
			throw new UnsupportedOperationException();
		}
	}
}
