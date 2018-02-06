package com.geng.core.serialization;

/**
 * Author: shushenglin
 * Date:   16/2/2 15:35
 */

import com.geng.core.data.*;
import net.logstash.logback.encoder.org.apache.commons.io.IOUtils;
import net.sf.json.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class DefaultSFSDataSerializer implements ISFSDataSerializer {
    private static final String CLASS_MARKER_KEY = "$C";
    private static final String CLASS_FIELDS_KEY = "$F";
    private static final String FIELD_NAME_KEY = "N";
    private static final String FIELD_VALUE_KEY = "V";
    private static DefaultSFSDataSerializer instance = new DefaultSFSDataSerializer();
    private static int BUFFER_CHUNK_SIZE = 2*1024; //huangyuanqiang
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static DefaultSFSDataSerializer getInstance() {
        return instance;
    }

    private DefaultSFSDataSerializer() {
    }

    public int getUnsignedByte(byte b) {
        return 255 & b;
    }

    public String array2json(List<Object> array) {
        return JSONArray.fromObject(array).toString();
    }

    public ISFSArray binary2array(byte[] data) {
        if (data.length < 3) {
            throw new IllegalStateException("Can\'t decode an SFSArray. Byte data is insufficient. Size: " + data.length + " bytes");
        } else {
            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            buffer.put(data);
            buffer.flip();
            return this.decodeSFSArray(buffer);
        }
    }

    private ISFSArray decodeSFSArray(ByteBuffer buffer) {
        SFSArray sfsArray = SFSArray.newInstance();
        byte headerBuffer = buffer.get();
        if (headerBuffer != SFSDataType.SFS_ARRAY.getTypeID()) {
            throw new IllegalStateException("Invalid SFSDataType. Expected: " + SFSDataType.SFS_ARRAY.getTypeID() + ", found: " + headerBuffer);
        } else {
            short size = buffer.getShort();
            if (size < 0) {
                throw new IllegalStateException("Can\'t decode SFSArray. Size is negative = " + size);
            } else {
                try {
                    for (int codecError = 0; codecError < size; ++codecError) {
                        SFSDataWrapper decodedObject = this.decodeObject(buffer);
                        if (decodedObject == null) {
                            throw new IllegalStateException("Could not decode SFSArray item at index: " + codecError);
                        }

                        sfsArray.add(decodedObject);
                    }

                    return sfsArray;
                } catch (SFSCodecException var7) {
                    throw new IllegalArgumentException(var7.getMessage());
                }
            }
        }
    }

    public ISFSObject binary2object(byte[] data) {
        if (data.length < 3) {
            throw new IllegalStateException("Can\'t decode an SFSObject. Byte data is insufficient. Size: " + data.length + " bytes");
        } else {
            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            buffer.put(data);
            buffer.flip();
            return this.decodeSFSObject(buffer);
        }
    }

    public ISFSObject binary2object(ByteBuffer buffer){
        if(buffer.remaining() < 3){
            throw new IllegalStateException("Can\'t decode an SFSObject. Byte data is insufficient. Size: " + buffer.remaining() + " bytes");
        }
        return this.decodeSFSObject(buffer);
    }

    private ISFSObject decodeSFSObject(ByteBuffer buffer) {
        SFSObject sfsObject = SFSObject.newInstance();
        byte headerBuffer = buffer.get();
        if (headerBuffer != SFSDataType.SFS_OBJECT.getTypeID()) {
            throw new IllegalStateException("Invalid SFSDataType. Expected: " + SFSDataType.SFS_OBJECT.getTypeID() + ", found: " + headerBuffer);
        } else {
            short size = buffer.getShort();
            if (size < 0) {
                throw new IllegalStateException("Can\'t decode SFSObject. Size is negative = " + size);
            } else {
                try {
                    for (int codecError = 0; codecError < size; ++codecError) {
                        short keySize = buffer.getShort();
                        if (keySize < 0 || keySize > 255) {
                            throw new IllegalStateException("Invalid SFSObject key length. Found = " + keySize);
                        }

                        byte[] keyData = new byte[keySize];
                        buffer.get(keyData, 0, keyData.length);
                        String key = new String(keyData);
                        SFSDataWrapper decodedObject = this.decodeObject(buffer);
                        if (decodedObject == null) {
                            throw new IllegalStateException("Could not decode value for key: " + keyData);
                        }

                        sfsObject.put(key, decodedObject);
                    }

                    return sfsObject;
                } catch (SFSCodecException var10) {
                    throw new IllegalArgumentException(var10.getMessage());
                }
            }
        }
    }

    public ISFSArray json2array(String jsonStr) {
        if (jsonStr.length() < 2) {
            throw new IllegalStateException("Can\'t decode SFSObject. JSON String is too short. Len: " + jsonStr.length());
        } else {
            JSONArray jsa = JSONArray.fromObject(jsonStr);
            return this.decodeSFSArray(jsa);
        }
    }

    private ISFSArray decodeSFSArray(JSONArray jsa) {
        SFSArrayLite sfsArray = SFSArrayLite.newInstance();
        Iterator iter = jsa.iterator();

        while (iter.hasNext()) {
            Object value = iter.next();
            SFSDataWrapper decodedObject = this.decodeJsonObject(value);
            if (decodedObject == null) {
                throw new IllegalStateException("(json2sfarray) Could not decode value for object: " + value);
            }

            sfsArray.add(decodedObject);
        }

        return sfsArray;
    }

    public ISFSObject json2object(String jsonStr) {
        if (jsonStr.length() < 2) {
            throw new IllegalStateException("Can\'t decode SFSObject. JSON String is too short. Len: " + jsonStr.length());
        } else {
            JSONObject jso = JSONObject.fromObject(jsonStr);
            return this.decodeSFSObject(jso);
        }
    }

    private ISFSObject decodeSFSObject(JSONObject jso) {
        SFSObject sfsObject = SFSObjectLite.newInstance();
        Iterator var4 = jso.keySet().iterator();

        while (var4.hasNext()) {
            Object key = var4.next();
            Object value = jso.get(key);
            SFSDataWrapper decodedObject = this.decodeJsonObject(value);
            if (decodedObject == null) {
                throw new IllegalStateException("(json2sfsobj) Could not decode value for key: " + key);
            }

            sfsObject.put((String) key, decodedObject);
        }

        return sfsObject;
    }

    private SFSDataWrapper decodeJsonObject(Object o) {
        if (o instanceof Integer) {
            return new SFSDataWrapper(SFSDataType.INT, o);
        } else if (o instanceof Long) {
            return new SFSDataWrapper(SFSDataType.LONG, o);
        } else if (o instanceof Double) {
            return new SFSDataWrapper(SFSDataType.DOUBLE, o);
        } else if (o instanceof Boolean) {
            return new SFSDataWrapper(SFSDataType.BOOL, o);
        } else if (o instanceof String) {
            return new SFSDataWrapper(SFSDataType.UTF_STRING, o);
        } else if (o instanceof JSONObject) {
            JSONObject jso = (JSONObject) o;
            return jso.isNullObject() ? new SFSDataWrapper(SFSDataType.NULL, (Object) null) : new SFSDataWrapper(SFSDataType.SFS_OBJECT, this.decodeSFSObject(jso));
        } else if (o instanceof JSONArray) {
            return new SFSDataWrapper(SFSDataType.SFS_ARRAY, this.decodeSFSArray((JSONArray) o));
        } else {
            throw new IllegalArgumentException(String.format("Unrecognized DataType while converting JSONObject 2 SFSObject. Object: %s, Type: %s", new Object[]{o, o == null ? "null" : o.getClass()}));
        }
    }

    public SFSObject resultSet2object(ResultSet rset) throws SQLException {
        ResultSetMetaData metaData = rset.getMetaData();
        SFSObject sfso = new SFSObject();
        if (rset.isBeforeFirst()) {
            rset.next();
        }

        for (int col = 1; col <= metaData.getColumnCount(); ++col) {
            String colName = metaData.getColumnName(col);
            int type = metaData.getColumnType(col);
            Object rawDataObj = rset.getObject(col);
            if (rawDataObj != null) {
                if (type == 0) {
                    sfso.putNull(colName);
                } else if (type == 16) {
                    sfso.putBool(colName, rset.getBoolean(col));
                } else if (type == 91) {
                    sfso.putLong(colName, rset.getDate(col).getTime());
                } else if (type != 6 && type != 3 && type != 8 && type != 7) {
                    if (type != 4 && type != -6 && type != 5) {
                        if (type != -1 && type != 12 && type != 1) {
                            if (type != -9 && type != -16 && type != -15) {
                                if (type == 93) {
                                    sfso.putLong(colName, rset.getTimestamp(col).getTime());
                                } else if (type == -5) {
                                    sfso.putLong(colName, rset.getLong(col));
                                } else if (type == -4) {
                                    byte[] blob = this.getBlobData(colName, rset.getBinaryStream(col));
                                    if (blob != null) {
                                        sfso.putByteArray(colName, blob);
                                    }
                                } else if (type == 2004) {
                                    Blob var9 = rset.getBlob(col);
                                    sfso.putByteArray(colName, var9.getBytes(0L, (int) var9.length()));
                                } else {
                                    this.logger.info("Skipping Unsupported SQL TYPE: " + type + ", Column:" + colName);
                                }
                            } else {
                                sfso.putUtfString(colName, rset.getNString(col));
                            }
                        } else {
                            sfso.putUtfString(colName, rset.getString(col));
                        }
                    } else {
                        sfso.putInt(colName, rset.getInt(col));
                    }
                } else {
                    sfso.putDouble(colName, rset.getDouble(col));
                }
            }
        }

        return sfso;
    }

    private byte[] getBlobData(String colName, InputStream stream) {
        BufferedInputStream bis = new BufferedInputStream(stream);
        byte[] bytes = null;

        try {
            bytes = new byte[bis.available()];
            bis.read(bytes);
        } catch (IOException var9) {
            this.logger.warn("SFSObject serialize error. Failed reading BLOB data for column: " + colName);
        } finally {
            IOUtils.closeQuietly(bis);
        }

        return bytes;
    }

    public SFSArray resultSet2array(ResultSet rset) throws SQLException {
        SFSArray sfsa = new SFSArray();

        while (rset.next()) {
            sfsa.addSFSObject(this.resultSet2object(rset));
        }

        return sfsa;
    }

    public byte[] object2binary(ISFSObject object) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CHUNK_SIZE);
        buffer.put((byte) SFSDataType.SFS_OBJECT.getTypeID());
        buffer.putShort((short) object.size());
        return this.obj2bin(object, buffer);
    }

    public ByteBuffer object2buffer(ISFSObject object) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CHUNK_SIZE);
        buffer.put((byte) SFSDataType.SFS_OBJECT.getTypeID());
        buffer.putShort((short) object.size());
        return this.obj2buf(object, buffer);
    }

    private ByteBuffer obj2buf(ISFSObject object, ByteBuffer buffer) {
        Set keys = object.getKeys();

        SFSDataWrapper wrapper;
        Object dataObj;
        for (Iterator result = keys.iterator(); result.hasNext(); buffer = this.encodeObject(buffer, wrapper.getTypeId(), dataObj)) {
            String pos = (String) result.next();
            wrapper = object.get(pos);
            dataObj = wrapper.getObject();
            buffer = this.encodeSFSObjectKey(buffer, pos);
        }
        buffer.flip();
        return buffer;
    }

    private byte[] obj2bin(ISFSObject object, ByteBuffer buffer) {
        Set keys = object.getKeys();

        SFSDataWrapper wrapper;
        Object dataObj;
        for (Iterator result = keys.iterator(); result.hasNext(); buffer = this.encodeObject(buffer, wrapper.getTypeId(), dataObj)) {
            String pos = (String) result.next();
            wrapper = object.get(pos);
            dataObj = wrapper.getObject();
            buffer = this.encodeSFSObjectKey(buffer, pos);
        }

        int pos1 = buffer.position();
        byte[] result1 = new byte[pos1];
        buffer.flip();
        buffer.get(result1, 0, pos1);
        return result1;
    }

    public byte[] array2binary(ISFSArray array) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CHUNK_SIZE);
        buffer.put((byte) SFSDataType.SFS_ARRAY.getTypeID());
        buffer.putShort((short) array.size());
        return this.arr2bin(array, buffer);
    }

    private byte[] arr2bin(ISFSArray array, ByteBuffer buffer) {
        SFSDataWrapper wrapper;
        Object pos;
        for (Iterator iter = array.iterator(); iter.hasNext(); buffer = this.encodeObject(buffer, wrapper.getTypeId(), pos)) {
            wrapper = (SFSDataWrapper) iter.next();
            pos = wrapper.getObject();
        }

        int pos1 = buffer.position();
        byte[] result = new byte[pos1];
        buffer.flip();
        buffer.get(result, 0, pos1);
        return result;
    }

    public String object2json(Map<String, Object> map) {
        return JSONObject.fromObject(map).toString();
    }

    public void flattenObject(Map<String, Object> map, SFSObject sfsObj) {
        Iterator it = sfsObj.iterator();

        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            String key = (String) entry.getKey();
            SFSDataWrapper value = (SFSDataWrapper) entry.getValue();
            if (value.getTypeId() == SFSDataType.SFS_OBJECT) {
                HashMap newList = new HashMap();
                map.put(key, newList);
                this.flattenObject(newList, (SFSObject) value.getObject());
            } else if (value.getTypeId() == SFSDataType.SFS_ARRAY) {
                ArrayList newList1 = new ArrayList();
                map.put(key, newList1);
                this.flattenArray(newList1, (SFSArray) value.getObject());
            } else {
                map.put(key, value.getObject());
            }
        }

    }

    public void flattenArray(List<Object> array, SFSArray sfsArray) {
        Iterator it = sfsArray.iterator();

        while (it.hasNext()) {
            SFSDataWrapper value = (SFSDataWrapper) it.next();
            if (value.getTypeId() == SFSDataType.SFS_OBJECT) {
                HashMap newList = new HashMap();
                array.add(newList);
                this.flattenObject(newList, (SFSObject) value.getObject());
            } else if (value.getTypeId() == SFSDataType.SFS_ARRAY) {
                ArrayList newList1 = new ArrayList();
                array.add(newList1);
                this.flattenArray(newList1, (SFSArray) value.getObject());
            } else {
                array.add(value.getObject());
            }
        }

    }

    private SFSDataWrapper decodeObject(ByteBuffer buffer) throws SFSCodecException {
        SFSDataWrapper decodedObject = null;
        byte headerByte = buffer.get();
        if (headerByte == SFSDataType.NULL.getTypeID()) {
            decodedObject = this.binDecode_NULL(buffer);
        } else if (headerByte == SFSDataType.BOOL.getTypeID()) {
            decodedObject = this.binDecode_BOOL(buffer);
        } else if (headerByte == SFSDataType.BOOL_ARRAY.getTypeID()) {
            decodedObject = this.binDecode_BOOL_ARRAY(buffer);
        } else if (headerByte == SFSDataType.BYTE.getTypeID()) {
            decodedObject = this.binDecode_BYTE(buffer);
        } else if (headerByte == SFSDataType.BYTE_ARRAY.getTypeID()) {
            decodedObject = this.binDecode_BYTE_ARRAY(buffer);
        } else if (headerByte == SFSDataType.SHORT.getTypeID()) {
            decodedObject = this.binDecode_SHORT(buffer);
        } else if (headerByte == SFSDataType.SHORT_ARRAY.getTypeID()) {
            decodedObject = this.binDecode_SHORT_ARRAY(buffer);
        } else if (headerByte == SFSDataType.INT.getTypeID()) {
            decodedObject = this.binDecode_INT(buffer);
        } else if (headerByte == SFSDataType.INT_ARRAY.getTypeID()) {
            decodedObject = this.binDecode_INT_ARRAY(buffer);
        } else if (headerByte == SFSDataType.LONG.getTypeID()) {
            decodedObject = this.binDecode_LONG(buffer);
        } else if (headerByte == SFSDataType.LONG_ARRAY.getTypeID()) {
            decodedObject = this.binDecode_LONG_ARRAY(buffer);
        } else if (headerByte == SFSDataType.FLOAT.getTypeID()) {
            decodedObject = this.binDecode_FLOAT(buffer);
        } else if (headerByte == SFSDataType.FLOAT_ARRAY.getTypeID()) {
            decodedObject = this.binDecode_FLOAT_ARRAY(buffer);
        } else if (headerByte == SFSDataType.DOUBLE.getTypeID()) {
            decodedObject = this.binDecode_DOUBLE(buffer);
        } else if (headerByte == SFSDataType.DOUBLE_ARRAY.getTypeID()) {
            decodedObject = this.binDecode_DOUBLE_ARRAY(buffer);
        } else if (headerByte == SFSDataType.UTF_STRING.getTypeID()) {
            decodedObject = this.binDecode_UTF_STRING(buffer);
        } else if (headerByte == SFSDataType.UTF_STRING_ARRAY.getTypeID()) {
            decodedObject = this.binDecode_UTF_STRING_ARRAY(buffer);
        } else if (headerByte == SFSDataType.SFS_ARRAY.getTypeID()) {
            buffer.position(buffer.position() - 1);
            decodedObject = new SFSDataWrapper(SFSDataType.SFS_ARRAY, this.decodeSFSArray(buffer));
        } else {
            if (headerByte != SFSDataType.SFS_OBJECT.getTypeID()) {
                throw new SFSCodecException("Unknow SFSDataType ID: " + headerByte);
            }

            buffer.position(buffer.position() - 1);
            ISFSObject sfsObj = this.decodeSFSObject(buffer);
            SFSDataType type = SFSDataType.SFS_OBJECT;
            Object finalSfsObj = sfsObj;
            if (sfsObj.containsKey(CLASS_MARKER_KEY) && sfsObj.containsKey(CLASS_FIELDS_KEY)) {
                type = SFSDataType.CLASS;
                finalSfsObj = this.sfs2pojo(sfsObj);
            }

            decodedObject = new SFSDataWrapper(type, finalSfsObj);
        }

        return decodedObject;
    }

    private ByteBuffer encodeObject(ByteBuffer buffer, SFSDataType typeId, Object object) {
        switch (typeId) {
            case NULL:
                buffer = this.binEncode_NULL(buffer);
                break;
            case BOOL:
                buffer = this.binEncode_BOOL(buffer, (Boolean) object);
                break;
            case BYTE:
                buffer = this.binEncode_BYTE(buffer, (Byte) object);
                break;
            case SHORT:
                buffer = this.binEncode_SHORT(buffer, (Short) object);
                break;
            case INT:
                buffer = this.binEncode_INT(buffer, (Integer) object);
                break;
            case LONG:
                buffer = this.binEncode_LONG(buffer, (Long) object);
                break;
            case FLOAT:
                buffer = this.binEncode_FLOAT(buffer, (Float) object);
                break;
            case DOUBLE:
                buffer = this.binEncode_DOUBLE(buffer, (Double) object);
                break;
            case UTF_STRING:
                buffer = this.binEncode_UTF_STRING(buffer, (String) object);
                break;
            case BOOL_ARRAY:
                buffer = this.binEncode_BOOL_ARRAY(buffer, (Collection) object);
                break;
            case BYTE_ARRAY:
                buffer = this.binEncode_BYTE_ARRAY(buffer, (byte[]) object);
                break;
            case SHORT_ARRAY:
                buffer = this.binEncode_SHORT_ARRAY(buffer, (Collection) object);
                break;
            case INT_ARRAY:
                buffer = this.binEncode_INT_ARRAY(buffer, (Collection) object);
                break;
            case LONG_ARRAY:
                buffer = this.binEncode_LONG_ARRAY(buffer, (Collection) object);
                break;
            case FLOAT_ARRAY:
                buffer = this.binEncode_FLOAT_ARRAY(buffer, (Collection) object);
                break;
            case DOUBLE_ARRAY:
                buffer = this.binEncode_DOUBLE_ARRAY(buffer, (Collection) object);
                break;
            case UTF_STRING_ARRAY:
                buffer = this.binEncode_UTF_STRING_ARRAY(buffer, (Collection) object);
                break;
            case SFS_ARRAY:
                buffer = this.addData(buffer, this.array2binary((SFSArray) object));
                break;
            case SFS_OBJECT:
                buffer = this.addData(buffer, this.object2binary((SFSObject) object));
                break;
            case CLASS:
                buffer = this.addData(buffer, this.object2binary(this.pojo2sfs(object)));
                break;
            default:
                throw new IllegalArgumentException("Unrecognized type in SFSObject serialization: " + typeId);
        }

        return buffer;
    }

    private SFSDataWrapper binDecode_NULL(ByteBuffer buffer) {
        return new SFSDataWrapper(SFSDataType.NULL, (Object) null);
    }

    private SFSDataWrapper binDecode_BOOL(ByteBuffer buffer) throws SFSCodecException {
        byte boolByte = buffer.get();
        Boolean bool = null;
        if (boolByte == 0) {
            bool = new Boolean(false);
        } else {
            if (boolByte != 1) {
                throw new SFSCodecException("Error decoding Bool type. Illegal value: " + bool);
            }

            bool = new Boolean(true);
        }

        return new SFSDataWrapper(SFSDataType.BOOL, bool);
    }

    private SFSDataWrapper binDecode_BYTE(ByteBuffer buffer) {
        byte boolByte = buffer.get();
        return new SFSDataWrapper(SFSDataType.BYTE, Byte.valueOf(boolByte));
    }

    private SFSDataWrapper binDecode_SHORT(ByteBuffer buffer) {
        short shortValue = buffer.getShort();
        return new SFSDataWrapper(SFSDataType.SHORT, Short.valueOf(shortValue));
    }

    private SFSDataWrapper binDecode_INT(ByteBuffer buffer) {
        int intValue = buffer.getInt();
        return new SFSDataWrapper(SFSDataType.INT, Integer.valueOf(intValue));
    }

    private SFSDataWrapper binDecode_LONG(ByteBuffer buffer) {
        long longValue = buffer.getLong();
        return new SFSDataWrapper(SFSDataType.LONG, Long.valueOf(longValue));
    }

    private SFSDataWrapper binDecode_FLOAT(ByteBuffer buffer) {
        float floatValue = buffer.getFloat();
        return new SFSDataWrapper(SFSDataType.FLOAT, Float.valueOf(floatValue));
    }

    private SFSDataWrapper binDecode_DOUBLE(ByteBuffer buffer) {
        double doubleValue = buffer.getDouble();
        return new SFSDataWrapper(SFSDataType.DOUBLE, Double.valueOf(doubleValue));
    }

    private SFSDataWrapper binDecode_UTF_STRING(ByteBuffer buffer) throws SFSCodecException {
        short strLen = buffer.getShort();
        if (strLen < 0) {
            throw new SFSCodecException("Error decoding UtfString. Negative size: " + strLen);
        } else {
            byte[] strData = new byte[strLen];
            buffer.get(strData, 0, strLen);
            String decodedString = new String(strData);
            return new SFSDataWrapper(SFSDataType.UTF_STRING, decodedString);
        }
    }

    private SFSDataWrapper binDecode_BOOL_ARRAY(ByteBuffer buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            byte boolData = buffer.get();
            if (boolData == 0) {
                array.add(Boolean.valueOf(false));
            } else {
                if (boolData != 1) {
                    throw new SFSCodecException("Error decoding BoolArray. Invalid bool value: " + boolData);
                }

                array.add(Boolean.valueOf(true));
            }
        }

        return new SFSDataWrapper(SFSDataType.BOOL_ARRAY, array);
    }

    private SFSDataWrapper binDecode_BYTE_ARRAY(ByteBuffer buffer) throws SFSCodecException {
        int arraySize = buffer.getInt();
        if (arraySize < 0) {
            throw new SFSCodecException("Error decoding typed array size. Negative size: " + arraySize);
        } else {
            byte[] byteData = new byte[arraySize];
            buffer.get(byteData, 0, arraySize);
            return new SFSDataWrapper(SFSDataType.BYTE_ARRAY, byteData);
        }
    }

    private SFSDataWrapper binDecode_SHORT_ARRAY(ByteBuffer buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            short shortValue = buffer.getShort();
            array.add(Short.valueOf(shortValue));
        }

        return new SFSDataWrapper(SFSDataType.SHORT_ARRAY, array);
    }

    private SFSDataWrapper binDecode_INT_ARRAY(ByteBuffer buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            int intValue = buffer.getInt();
            array.add(Integer.valueOf(intValue));
        }

        return new SFSDataWrapper(SFSDataType.INT_ARRAY, array);
    }

    private SFSDataWrapper binDecode_LONG_ARRAY(ByteBuffer buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            long longValue = buffer.getLong();
            array.add(Long.valueOf(longValue));
        }

        return new SFSDataWrapper(SFSDataType.LONG_ARRAY, array);
    }

    private SFSDataWrapper binDecode_FLOAT_ARRAY(ByteBuffer buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            float floatValue = buffer.getFloat();
            array.add(Float.valueOf(floatValue));
        }

        return new SFSDataWrapper(SFSDataType.FLOAT_ARRAY, array);
    }

    private SFSDataWrapper binDecode_DOUBLE_ARRAY(ByteBuffer buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            double doubleValue = buffer.getDouble();
            array.add(Double.valueOf(doubleValue));
        }

        return new SFSDataWrapper(SFSDataType.DOUBLE_ARRAY, array);
    }

    private SFSDataWrapper binDecode_UTF_STRING_ARRAY(ByteBuffer buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            short strLen = buffer.getShort();
            if (strLen < 0) {
                throw new SFSCodecException("Error decoding UtfStringArray element. Element has negative size: " + strLen);
            }

            byte[] strData = new byte[strLen];
            buffer.get(strData, 0, strLen);
            array.add(new String(strData));
        }

        return new SFSDataWrapper(SFSDataType.UTF_STRING_ARRAY, array);
    }

    private short getTypeArraySize(ByteBuffer buffer) throws SFSCodecException {
        short arraySize = buffer.getShort();
        if (arraySize < 0) {
            throw new SFSCodecException("Error decoding typed array size. Negative size: " + arraySize);
        } else {
            return arraySize;
        }
    }

    private ByteBuffer binEncode_NULL(ByteBuffer buffer) {
        return this.addData(buffer, new byte[1]);
    }

    private ByteBuffer binEncode_BOOL(ByteBuffer buffer, Boolean value) {
        byte[] data = new byte[]{(byte) SFSDataType.BOOL.getTypeID(), (byte) (value.booleanValue() ? 1 : 0)};
        return this.addData(buffer, data);
    }

    private ByteBuffer binEncode_BYTE(ByteBuffer buffer, Byte value) {
        byte[] data = new byte[]{(byte) SFSDataType.BYTE.getTypeID(), value.byteValue()};
        return this.addData(buffer, data);
    }

    private ByteBuffer binEncode_SHORT(ByteBuffer buffer, Short value) {
        ByteBuffer buf = ByteBuffer.allocate(3);
        buf.put((byte) SFSDataType.SHORT.getTypeID());
        buf.putShort(value.shortValue());
        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_INT(ByteBuffer buffer, Integer value) {
        ByteBuffer buf = ByteBuffer.allocate(5);
        buf.put((byte) SFSDataType.INT.getTypeID());
        buf.putInt(value.intValue());
        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_LONG(ByteBuffer buffer, Long value) {
        ByteBuffer buf = ByteBuffer.allocate(9);
        buf.put((byte) SFSDataType.LONG.getTypeID());
        buf.putLong(value.longValue());
        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_FLOAT(ByteBuffer buffer, Float value) {
        ByteBuffer buf = ByteBuffer.allocate(5);
        buf.put((byte) SFSDataType.FLOAT.getTypeID());
        buf.putFloat(value.floatValue());
        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_DOUBLE(ByteBuffer buffer, Double value) {
        ByteBuffer buf = ByteBuffer.allocate(9);
        buf.put((byte) SFSDataType.DOUBLE.getTypeID());
        buf.putDouble(value.doubleValue());
        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_UTF_STRING(ByteBuffer buffer, String value) {
        byte[] stringBytes = value.getBytes();
        ByteBuffer buf = ByteBuffer.allocate(3 + stringBytes.length);
        buf.put((byte) SFSDataType.UTF_STRING.getTypeID());
        buf.putShort((short) stringBytes.length);
        buf.put(stringBytes);
        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_BOOL_ARRAY(ByteBuffer buffer, Collection<Boolean> value) {
        ByteBuffer buf = ByteBuffer.allocate(3 + value.size());
        buf.put((byte) SFSDataType.BOOL_ARRAY.getTypeID());
        buf.putShort((short) value.size());
        Iterator var5 = value.iterator();

        while (var5.hasNext()) {
            boolean b = ((Boolean) var5.next()).booleanValue();
            buf.put((byte) (b ? 1 : 0));
        }

        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_BYTE_ARRAY(ByteBuffer buffer, byte[] value) {
        ByteBuffer buf = ByteBuffer.allocate(5 + value.length);
        buf.put((byte) SFSDataType.BYTE_ARRAY.getTypeID());
        buf.putInt(value.length);
        buf.put(value);
        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_SHORT_ARRAY(ByteBuffer buffer, Collection<Short> value) {
        ByteBuffer buf = ByteBuffer.allocate(3 + 2 * value.size());
        buf.put((byte) SFSDataType.SHORT_ARRAY.getTypeID());
        buf.putShort((short) value.size());
        Iterator var5 = value.iterator();

        while (var5.hasNext()) {
            short item = ((Short) var5.next()).shortValue();
            buf.putShort(item);
        }

        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_INT_ARRAY(ByteBuffer buffer, Collection<Integer> value) {
        ByteBuffer buf = ByteBuffer.allocate(3 + 4 * value.size());
        buf.put((byte) SFSDataType.INT_ARRAY.getTypeID());
        buf.putShort((short) value.size());
        Iterator var5 = value.iterator();

        while (var5.hasNext()) {
            int item = ((Integer) var5.next()).intValue();
            buf.putInt(item);
        }

        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_LONG_ARRAY(ByteBuffer buffer, Collection<Long> value) {
        ByteBuffer buf = ByteBuffer.allocate(3 + 8 * value.size());
        buf.put((byte) SFSDataType.LONG_ARRAY.getTypeID());
        buf.putShort((short) value.size());
        Iterator var6 = value.iterator();

        while (var6.hasNext()) {
            long item = ((Long) var6.next()).longValue();
            buf.putLong(item);
        }

        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_FLOAT_ARRAY(ByteBuffer buffer, Collection<Float> value) {
        ByteBuffer buf = ByteBuffer.allocate(3 + 4 * value.size());
        buf.put((byte) SFSDataType.FLOAT_ARRAY.getTypeID());
        buf.putShort((short) value.size());
        Iterator var5 = value.iterator();

        while (var5.hasNext()) {
            float item = ((Float) var5.next()).floatValue();
            buf.putFloat(item);
        }

        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_DOUBLE_ARRAY(ByteBuffer buffer, Collection<Double> value) {
        ByteBuffer buf = ByteBuffer.allocate(3 + 8 * value.size());
        buf.put((byte) SFSDataType.DOUBLE_ARRAY.getTypeID());
        buf.putShort((short) value.size());
        Iterator var6 = value.iterator();

        while (var6.hasNext()) {
            double item = ((Double) var6.next()).doubleValue();
            buf.putDouble(item);
        }

        return this.addData(buffer, buf.array());
    }

    private ByteBuffer binEncode_UTF_STRING_ARRAY(ByteBuffer buffer, Collection<String> value) {
        int stringDataLen = 0;
        byte[][] binStrings = new byte[value.size()][];
        int count = 0;

        byte[] binStr;
        for (Iterator binItem = value.iterator(); binItem.hasNext(); stringDataLen += 2 + binStr.length) {
            String buf = (String) binItem.next();
            binStr = buf.getBytes();
            binStrings[count++] = binStr;
        }

        ByteBuffer var11 = ByteBuffer.allocate(3 + stringDataLen);
        var11.put((byte) SFSDataType.UTF_STRING_ARRAY.getTypeID());
        var11.putShort((short) value.size());
        byte[][] var10 = binStrings;
        int var9 = binStrings.length;

        for (int var13 = 0; var13 < var9; ++var13) {
            byte[] var12 = var10[var13];
            var11.putShort((short) var12.length);
            var11.put(var12);
        }

        return this.addData(buffer, var11.array());
    }

    private ByteBuffer encodeSFSObjectKey(ByteBuffer buffer, String value) {
        ByteBuffer buf = ByteBuffer.allocate(2 + value.length());
        buf.putShort((short) value.length());
        buf.put(value.getBytes());
        return this.addData(buffer, buf.array());
    }

    private ByteBuffer addData(ByteBuffer buffer, byte[] newData) {
        if (buffer.remaining() < newData.length) {
            int newSize = BUFFER_CHUNK_SIZE;
            if (newSize < newData.length) {
                newSize = newData.length;
            }

            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + newSize);
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
        }

        buffer.put(newData);
        return buffer;
    }

    public ISFSObject pojo2sfs(Object pojo) {
        SFSObject sfsObj = SFSObject.newInstance();

        try {
            this.convertPojo(pojo, sfsObj);
            return sfsObj;
        } catch (Exception var4) {
            throw new SFSRuntimeException(var4);
        }
    }

    private void convertPojo(Object pojo, ISFSObject sfsObj) throws Exception {
        Class pojoClazz = pojo.getClass();
        String classFullName = pojoClazz.getCanonicalName();
        if (classFullName == null) {
            throw new IllegalArgumentException("Anonymous classes cannot be serialized!");
        } else if (!(pojo instanceof SerializableSFSType)) {
            throw new IllegalStateException("Cannot serialize object: " + pojo + ", type: " + classFullName + " -- It doesn\'t implement the SerializableSFSType interface");
        } else {
            SFSArray fieldList = SFSArray.newInstance();
            sfsObj.putUtfString(CLASS_MARKER_KEY, classFullName);
            sfsObj.putSFSArray(CLASS_FIELDS_KEY, fieldList);
            Field[] var9;
            int var8 = (var9 = pojoClazz.getDeclaredFields()).length;

            for (int var7 = 0; var7 < var8; ++var7) {
                Field field = var9[var7];

                try {
                    int err = field.getModifiers();
                    if (!Modifier.isTransient(err) && !Modifier.isStatic(err)) {
                        String fieldName = field.getName();
                        Object fieldValue = null;
                        if (Modifier.isPublic(err)) {
                            fieldValue = field.get(pojo);
                        } else {
                            fieldValue = this.readValueFromGetter(fieldName, field.getType().getSimpleName(), pojo);
                        }

                        SFSObject fieldDescriptor = SFSObject.newInstance();
                        fieldDescriptor.putUtfString(FIELD_NAME_KEY, fieldName);
                        fieldDescriptor.put(FIELD_VALUE_KEY, this.wrapPojoField(fieldValue));
                        fieldList.addSFSObject(fieldDescriptor);
                    }
                } catch (NoSuchMethodException var14) {
                    this.logger.info("-- No public getter -- Serializer skipping private field: " + field.getName() + ", from class: " + pojoClazz);
                    var14.printStackTrace();
                }
            }

        }
    }

    private Object readValueFromGetter(String fieldName, String type, Object pojo) throws Exception {
        Object value = null;
        boolean isBool = type.equalsIgnoreCase("boolean");
        String getterName = isBool ? "is" + StringUtils.capitalize(fieldName) : "get" + StringUtils.capitalize(fieldName);
        Method getterMethod = pojo.getClass().getMethod(getterName, new Class[0]);
        value = getterMethod.invoke(pojo, new Object[0]);
        return value;
    }

    private SFSDataWrapper wrapPojoField(Object value) {
        if (value == null) {
            return new SFSDataWrapper(SFSDataType.NULL, (Object) null);
        } else {
            SFSDataWrapper wrapper = null;
            if (value instanceof Boolean) {
                wrapper = new SFSDataWrapper(SFSDataType.BOOL, value);
            } else if (value instanceof Byte) {
                wrapper = new SFSDataWrapper(SFSDataType.BYTE, value);
            } else if (value instanceof Short) {
                wrapper = new SFSDataWrapper(SFSDataType.SHORT, value);
            } else if (value instanceof Integer) {
                wrapper = new SFSDataWrapper(SFSDataType.INT, value);
            } else if (value instanceof Long) {
                wrapper = new SFSDataWrapper(SFSDataType.LONG, value);
            } else if (value instanceof Float) {
                wrapper = new SFSDataWrapper(SFSDataType.FLOAT, value);
            } else if (value instanceof Double) {
                wrapper = new SFSDataWrapper(SFSDataType.DOUBLE, value);
            } else if (value instanceof String) {
                wrapper = new SFSDataWrapper(SFSDataType.UTF_STRING, value);
            } else if (value.getClass().isArray()) {
                wrapper = new SFSDataWrapper(SFSDataType.SFS_ARRAY, this.unrollArray((Object[]) value));
            } else if (value instanceof Collection) {
                wrapper = new SFSDataWrapper(SFSDataType.SFS_ARRAY, this.unrollCollection((Collection) value));
            } else if (value instanceof Map) {
                wrapper = new SFSDataWrapper(SFSDataType.SFS_OBJECT, this.unrollMap((Map) value));
            } else if (value instanceof SerializableSFSType) {
                wrapper = new SFSDataWrapper(SFSDataType.SFS_OBJECT, this.pojo2sfs(value));
            }

            return wrapper;
        }
    }

    private ISFSArray unrollArray(Object[] arr) {
        SFSArray array = SFSArray.newInstance();
        Object[] var6 = arr;
        int var5 = arr.length;

        for (int var4 = 0; var4 < var5; ++var4) {
            Object item = var6[var4];
            array.add(this.wrapPojoField(item));
        }

        return array;
    }

    private ISFSArray unrollCollection(Collection collection) {
        SFSArray array = SFSArray.newInstance();
        Iterator var4 = collection.iterator();

        while (var4.hasNext()) {
            Object item = var4.next();
            array.add(this.wrapPojoField(item));
        }

        return array;
    }

    private ISFSObject unrollMap(Map map) {
        SFSObject sfsObj = SFSObject.newInstance();
        Set entries = map.entrySet();
        Iterator iter = entries.iterator();

        while (iter.hasNext()) {
            Entry item = (Entry) iter.next();
            Object key = item.getKey();
            if (key instanceof String) {
                sfsObj.put((String) key, this.wrapPojoField(item.getValue()));
            }
        }

        return sfsObj;
    }

    public Object sfs2pojo(ISFSObject sfsObj) {
        Object pojo = null;
        if (!sfsObj.containsKey(CLASS_MARKER_KEY) && !sfsObj.containsKey(CLASS_FIELDS_KEY)) {
            throw new SFSRuntimeException("The SFSObject passed does not represent any serialized class.");
        } else {
            try {
                String e = sfsObj.getUtfString(CLASS_MARKER_KEY);
                Class theClass = Class.forName(e);
                pojo = theClass.newInstance();
                if (!(pojo instanceof SerializableSFSType)) {
                    throw new IllegalStateException("Cannot deserialize object: " + pojo + ", type: " + e + " -- It doesn\'t implement the SerializableSFSType interface");
                } else {
                    this.convertSFSObject(sfsObj.getSFSArray(CLASS_FIELDS_KEY), pojo);
                    return pojo;
                }
            } catch (Exception var5) {
                throw new SFSRuntimeException(var5);
            }
        }
    }

    private void convertSFSObject(ISFSArray fieldList, Object pojo) throws Exception {
        for (int j = 0; j < fieldList.size(); ++j) {
            ISFSObject fieldDescriptor = fieldList.getSFSObject(j);
            String fieldName = fieldDescriptor.getUtfString(FIELD_NAME_KEY);
            Object fieldValue = this.unwrapPojoField(fieldDescriptor.get(FIELD_VALUE_KEY));
            this.setObjectField(pojo, fieldName, fieldValue);
        }

    }

    private void setObjectField(Object pojo, String fieldName, Object fieldValue) throws Exception {
        Class pojoClass = pojo.getClass();
        Field field = pojoClass.getDeclaredField(fieldName);
        int fieldModifier = field.getModifiers();
        if (!Modifier.isTransient(fieldModifier)) {
            boolean isArray = field.getType().isArray();
            Collection collection;
            if (isArray) {
                if (!(fieldValue instanceof Collection)) {
                    throw new SFSRuntimeException("Problem during SFSObject => POJO conversion. Found array field in POJO: " + fieldName + ", but data is not a Collection!");
                }

                collection = (Collection) fieldValue;
                Object[] fieldValue1 = collection.toArray();
                int fieldClass = collection.size();
                Object typedArray = Array.newInstance(field.getType().getComponentType(), fieldClass);
                System.arraycopy(fieldValue1, 0, typedArray, 0, fieldClass);
                fieldValue = typedArray;
            } else if (fieldValue instanceof Collection) {
                collection = (Collection) fieldValue;
                String fieldClass1 = field.getType().getSimpleName();
                if (fieldClass1.equals("ArrayList") || fieldClass1.equals("List")) {
                    fieldValue = new ArrayList(collection);
                }

                if (fieldClass1.equals("CopyOnWriteArrayList")) {
                    fieldValue = new CopyOnWriteArrayList(collection);
                } else if (fieldClass1.equals("LinkedList")) {
                    fieldValue = new LinkedList(collection);
                } else if (fieldClass1.equals("Vector")) {
                    fieldValue = new Vector(collection);
                } else if (!fieldClass1.equals("Set") && !fieldClass1.equals("HashSet")) {
                    if (fieldClass1.equals("LinkedHashSet")) {
                        fieldValue = new LinkedHashSet(collection);
                    } else if (fieldClass1.equals("TreeSet")) {
                        fieldValue = new TreeSet(collection);
                    } else if (fieldClass1.equals("CopyOnWriteArraySet")) {
                        fieldValue = new CopyOnWriteArraySet(collection);
                    } else if (!fieldClass1.equals("Queue") && !fieldClass1.equals("PriorityQueue")) {
                        if (!fieldClass1.equals("BlockingQueue") && !fieldClass1.equals("LinkedBlockingQueue")) {
                            if (fieldClass1.equals("PriorityBlockingQueue")) {
                                fieldValue = new PriorityBlockingQueue(collection);
                            } else if (fieldClass1.equals("ConcurrentLinkedQueue")) {
                                fieldValue = new ConcurrentLinkedQueue(collection);
                            } else if (fieldClass1.equals("DelayQueue")) {
                                fieldValue = new DelayQueue(collection);
                            } else if (!fieldClass1.equals("Deque") && !fieldClass1.equals("ArrayDeque")) {
                                if (fieldClass1.equals("LinkedBlockingDeque")) {
                                    fieldValue = new LinkedBlockingDeque(collection);
                                }
                            } else {
                                fieldValue = new ArrayDeque(collection);
                            }
                        } else {
                            fieldValue = new LinkedBlockingQueue(collection);
                        }
                    } else {
                        fieldValue = new PriorityQueue(collection);
                    }
                } else {
                    fieldValue = new HashSet(collection);
                }
            }

            if (Modifier.isPublic(fieldModifier)) {
                field.set(pojo, fieldValue);
            } else {
                this.writeValueFromSetter(field, pojo, fieldValue);
            }

        }
    }

    private void writeValueFromSetter(Field field, Object pojo, Object fieldValue) throws Exception {
        String setterName = "set" + StringUtils.capitalize(field.getName());

        try {
            Method setterMethod = pojo.getClass().getMethod(setterName, new Class[]{field.getType()});
            setterMethod.invoke(pojo, new Object[]{fieldValue});
        } catch (NoSuchMethodException var7) {
            this.logger.info("-- No public setter -- Serializer skipping private field: " + field.getName() + ", from class: " + pojo.getClass().getName());
        }

    }

    private Object unwrapPojoField(SFSDataWrapper wrapper) {
        Object obj = null;
        SFSDataType type = wrapper.getTypeId();
        if (type.getTypeID() <= SFSDataType.UTF_STRING.getTypeID()) {
            obj = wrapper.getObject();
        } else if (type == SFSDataType.SFS_ARRAY) {
            obj = this.rebuildArray((ISFSArray) wrapper.getObject());
        } else if (type == SFSDataType.SFS_OBJECT) {
            obj = this.rebuildMap((ISFSObject) wrapper.getObject());
        } else if (type == SFSDataType.CLASS) {
            obj = wrapper.getObject();
        }

        return obj;
    }

    private Object rebuildArray(ISFSArray sfsArray) {
        ArrayList collection = new ArrayList();
        Iterator iter = sfsArray.iterator();

        while (iter.hasNext()) {
            Object item = this.unwrapPojoField((SFSDataWrapper) iter.next());
            collection.add(item);
        }

        return collection;
    }

    private Object rebuildMap(ISFSObject sfsObj) {
        HashMap map = new HashMap();
        Iterator var4 = sfsObj.getKeys().iterator();

        while (var4.hasNext()) {
            String key = (String) var4.next();
            SFSDataWrapper wrapper = sfsObj.get(key);
            map.put(key, this.unwrapPojoField(wrapper));
        }

        return map;
    }

}