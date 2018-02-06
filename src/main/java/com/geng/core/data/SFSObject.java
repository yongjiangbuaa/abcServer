package com.geng.core.data;

/**
 * Author: shushenglin
 * Date:   16/2/2 15:27
 */


import com.geng.core.serialization.DefaultObjectDumpFormatter;
import com.geng.core.serialization.DefaultSFSDataSerializer;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class SFSObject implements ISFSObject, Serializable {
    private Map<String, SFSDataWrapper> dataHolder = new ConcurrentHashMap<>();

    public static SFSObject newFromObject(Object o) {
        return (SFSObject) DefaultSFSDataSerializer.getInstance().pojo2sfs(o);
    }

    public static SFSObject newFromBinaryData(byte[] bytes) {
        return (SFSObject)DefaultSFSDataSerializer.getInstance().binary2object(bytes);
    }

    public static ISFSObject newFromJsonData(String jsonStr) {
        return DefaultSFSDataSerializer.getInstance().json2object(jsonStr);
    }

    public static SFSObject newFromResultSet(ResultSet rset) throws SQLException {
        return DefaultSFSDataSerializer.getInstance().resultSet2object(rset);
    }

    public static SFSObject newInstance() {
        return new SFSObject();
    }

    public SFSObject() {
    }

    public Iterator<Entry<String, SFSDataWrapper>> iterator() {
        return this.dataHolder.entrySet().iterator();
    }

    public boolean containsKey(String key) {
        return this.dataHolder.containsKey(key);
    }

    public boolean removeElement(String key) {
        return this.dataHolder.remove(key) != null;
    }

    public int size() {
        return this.dataHolder.size();
    }

    public byte[] toBinary() {
        return DefaultSFSDataSerializer.getInstance().object2binary(this);
    }

    public String toJson() {
        return DefaultSFSDataSerializer.getInstance().object2json(this.flatten());
    }

    public String getDump() {
        return this.size() == 0?"[ Empty SFSObject ]":DefaultObjectDumpFormatter.prettyPrintDump(this.dump());
    }

    public String getDump(boolean noFormat) {
        return !noFormat?this.dump():this.getDump();
    }

    private String dump() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');

        for(Iterator var3 = this.getKeys().iterator(); var3.hasNext(); buffer.append(';')) {
            String key = (String)var3.next();
            SFSDataWrapper wrapper = this.get(key);
            buffer.append("(").append(wrapper.getTypeId().name().toLowerCase()).append(") ").append(key).append(": ");
            if(wrapper.getTypeId() == SFSDataType.SFS_OBJECT) {
                buffer.append(((SFSObject)wrapper.getObject()).getDump(false));
            } else if(wrapper.getTypeId() == SFSDataType.SFS_ARRAY) {
                buffer.append(((SFSArray)wrapper.getObject()).getDump(false));
            } else if(wrapper.getTypeId() == SFSDataType.BYTE_ARRAY) {
                buffer.append(DefaultObjectDumpFormatter.prettyPrintByteArray((byte[])wrapper.getObject()));
            } else if(wrapper.getTypeId() == SFSDataType.CLASS) {
                buffer.append(wrapper.getObject().getClass().getName());
            } else {
                buffer.append(wrapper.getObject());
            }
        }

        buffer.append('}');
        return buffer.toString();
    }

    public String getHexDump() {
        return ByteUtils.fullHexDump(this.toBinary());
    }

    public boolean isNull(String key) {
        SFSDataWrapper wrapper = this.dataHolder.get(key);
        return wrapper == null?false:wrapper.getTypeId() == SFSDataType.NULL;
    }

    public SFSDataWrapper get(String key) {
        return this.dataHolder.get(key);
    }

    public Boolean getBool(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Boolean)o.getObject();
    }

    public Collection<Boolean> getBoolArray(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Collection)o.getObject();
    }

    public Byte getByte(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Byte)o.getObject();
    }

    public byte[] getByteArray(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(byte[])o.getObject();
    }

    public Double getDouble(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Double)o.getObject();
    }

    public Collection<Double> getDoubleArray(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Collection)o.getObject();
    }

    public Float getFloat(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Float)o.getObject();
    }

    public Collection<Float> getFloatArray(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Collection)o.getObject();
    }

    public Integer getInt(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Integer)o.getObject();
    }

    public Collection<Integer> getIntArray(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Collection)o.getObject();
    }

    public Set<String> getKeys() {
        return this.dataHolder.keySet();
    }

    public Long getLong(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Long)o.getObject();
    }

    public Collection<Long> getLongArray(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Collection)o.getObject();
    }

    public ISFSArray getSFSArray(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(ISFSArray)o.getObject();
    }

    public ISFSObject getSFSObject(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(ISFSObject)o.getObject();
    }

    public Short getShort(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Short)o.getObject();
    }

    public Collection<Short> getShortArray(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Collection)o.getObject();
    }

    public Integer getUnsignedByte(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:Integer.valueOf(DefaultSFSDataSerializer.getInstance().getUnsignedByte(((Byte)o.getObject()).byteValue()));
    }

    public Collection<Integer> getUnsignedByteArray(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        if(o == null) {
            return null;
        } else {
            DefaultSFSDataSerializer serializer = DefaultSFSDataSerializer.getInstance();
            ArrayList intCollection = new ArrayList();
            byte[] var8;
            int var7 = (var8 = (byte[])o.getObject()).length;

            for(int var6 = 0; var6 < var7; ++var6) {
                byte b = var8[var6];
                intCollection.add(Integer.valueOf(serializer.getUnsignedByte(b)));
            }

            return intCollection;
        }
    }

    public String getUtfString(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(String)o.getObject();
    }

    public Collection<String> getUtfStringArray(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:(Collection)o.getObject();
    }

    public Object getClass(String key) {
        SFSDataWrapper o = this.dataHolder.get(key);
        return o == null?null:o.getObject();
    }

    public void putBool(String key, boolean value) {
        this.putObj(key, Boolean.valueOf(value), SFSDataType.BOOL);
    }

    public void putBoolArray(String key, Collection<Boolean> value) {
        this.putObj(key, value, SFSDataType.BOOL_ARRAY);
    }

    public void putByte(String key, byte value) {
        this.putObj(key, Byte.valueOf(value), SFSDataType.BYTE);
    }

    public void putByteArray(String key, byte[] value) {
        this.putObj(key, value, SFSDataType.BYTE_ARRAY);
    }

    public void putDouble(String key, double value) {
        this.putObj(key, Double.valueOf(value), SFSDataType.DOUBLE);
    }

    public void putDoubleArray(String key, Collection<Double> value) {
        this.putObj(key, value, SFSDataType.DOUBLE_ARRAY);
    }

    public void putFloat(String key, float value) {
        this.putObj(key, Float.valueOf(value), SFSDataType.FLOAT);
    }

    public void putFloatArray(String key, Collection<Float> value) {
        this.putObj(key, value, SFSDataType.FLOAT_ARRAY);
    }

    public void putInt(String key, int value) {
        this.putObj(key, Integer.valueOf(value), SFSDataType.INT);
    }

    public void putIntArray(String key, Collection<Integer> value) {
        this.putObj(key, value, SFSDataType.INT_ARRAY);
    }

    public void putLong(String key, long value) {
        this.putObj(key, Long.valueOf(value), SFSDataType.LONG);
    }

    public void putLongArray(String key, Collection<Long> value) {
        this.putObj(key, value, SFSDataType.LONG_ARRAY);
    }

    public void putNull(String key) {
        this.dataHolder.put(key, new SFSDataWrapper(SFSDataType.NULL, (Object)null));
    }

    public void putSFSArray(String key, ISFSArray value) {
        this.putObj(key, value, SFSDataType.SFS_ARRAY);
    }

    public void putSFSObject(String key, ISFSObject value) {
        this.putObj(key, value, SFSDataType.SFS_OBJECT);
    }

    public void putShort(String key, short value) {
        this.putObj(key, Short.valueOf(value), SFSDataType.SHORT);
    }

    public void putShortArray(String key, Collection<Short> value) {
        this.putObj(key, value, SFSDataType.SHORT_ARRAY);
    }

    public void putUtfString(String key, String value) {
        this.putObj(key, value, SFSDataType.UTF_STRING);
    }

    public void putUtfStringArray(String key, Collection<String> value) {
        this.putObj(key, value, SFSDataType.UTF_STRING_ARRAY);
    }

    public void put(String key, SFSDataWrapper wrappedObject) {
        this.putObj(key, wrappedObject, (SFSDataType)null);
    }

    public void putClass(String key, Object o) {
        this.putObj(key, o, SFSDataType.CLASS);
    }

    public String toString() {
        return "[SFSObject, size: " + this.size() + "]";
    }

    private void putObj(String key, Object value, SFSDataType typeId) {
        if(key == null) {
            throw new IllegalArgumentException("SFSObject requires a non-null key for a \'put\' operation!");
        } else if(key.length() > 255) {
            throw new IllegalArgumentException("SFSObject keys must be less than 255 characters!");
        } else if(value == null) {
            throw new IllegalArgumentException("SFSObject requires a non-null value! If you need to add a null use the putNull() method.");
        } else {
            if(value instanceof SFSDataWrapper) {
                this.dataHolder.put(key, (SFSDataWrapper)value);
            } else {
                this.dataHolder.put(key, new SFSDataWrapper(typeId, value));
            }

        }
    }

    private Map<String, Object> flatten() {
        HashMap map = new HashMap();
        DefaultSFSDataSerializer.getInstance().flattenObject(map, this);
        return map;
    }
}

