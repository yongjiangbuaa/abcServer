package com.geng.core.serialization;

import com.geng.core.data.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
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

public class DefaultSFSDataSerializerV2 implements ISFSDataSerializer {
    private static final String CLASS_MARKER_KEY = "$C";
    private static final String CLASS_FIELDS_KEY = "$F";
    private static final String FIELD_NAME_KEY = "N";
    private static final String FIELD_VALUE_KEY = "V";
    private static DefaultSFSDataSerializerV2 instance = new DefaultSFSDataSerializerV2();
    private static int BUFFER_CHUNK_SIZE = 1024;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final PooledByteBufAllocator alloc = new PooledByteBufAllocator();

    public static DefaultSFSDataSerializerV2 getInstance() {
        return instance;
    }

    private DefaultSFSDataSerializerV2() {
    }

    public int getUnsignedByte(byte b) {
        return 255 & b;
    }

    public String array2json(List<Object> array) {
        return JSONArray.fromObject(array).toString();
    }

    public ISFSArray binary2array(byte[] data) {
        ISFSArray result = null;
        if (data.length < 3) {
            throw new IllegalStateException("Can\'t decode an SFSArray. Byte data is insufficient. Size: " + data.length + " bytes");
        } else {
            ByteBuf buffer = null;
            try {
                buffer = alloc.buffer(data.length);
                buffer.writeBytes(data);
                result = this.decodeSFSArray(buffer);
            }finally {
                if(buffer != null){
                    buffer.release();
                }
            }
        }
        return result;
    }

    private ISFSArray decodeSFSArray(ByteBuf buffer) {
        SFSArray sfsArray = SFSArray.newInstance();

            byte headerBuffer = buffer.readByte();
            if (headerBuffer != SFSDataType.SFS_ARRAY.getTypeID()) {
                throw new IllegalStateException("Invalid SFSDataType. Expected: " + SFSDataType.SFS_ARRAY.getTypeID() + ", found: " + headerBuffer);
            } else {
                short size = buffer.getShort(buffer.readerIndex());
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
        ISFSObject result = null;
        if (data.length < 3) {
            throw new IllegalStateException("Can\'t decode an SFSObject. Byte data is insufficient. Size: " + data.length + " bytes");
        } else {
            ByteBuf buffer = null;
            try {
                buffer = alloc.buffer(data.length);
                buffer.writeBytes(data);
                result = this.decodeSFSObject(buffer);
            }finally{
                if(buffer != null){
                    buffer.release();
                }
            }


        }
        return result;
    }

    public ISFSObject binary2object(ByteBuffer buffer){
        if(buffer.remaining() < 3){
            throw new IllegalStateException("Can\'t decode an SFSObject. Byte data is insufficient. Size: " + buffer.remaining() + " bytes");
        }
        return this.decodeSFSObject(Unpooled.wrappedBuffer(buffer)); //unpooled bytebuf 所以不需要手动release
    }

    private ISFSObject decodeSFSObject(ByteBuf buffer) {
        SFSObject sfsObject = SFSObject.newInstance();

            byte headerBuffer = buffer.readByte();
            if (headerBuffer != SFSDataType.SFS_OBJECT.getTypeID()) {
                throw new IllegalStateException("Invalid SFSDataType. Expected: " + SFSDataType.SFS_OBJECT.getTypeID() + ", found: " + headerBuffer);
            } else {
                short size = buffer.readShort();
                if (size < 0) {
                    throw new IllegalStateException("Can\'t decode SFSObject. Size is negative = " + size);
                } else {
                    try {
                        for (int codecError = 0; codecError < size; ++codecError) {
                            short keySize = buffer.readShort();
                            if (keySize < 0 || keySize > 255) {
                                throw new IllegalStateException("Invalid SFSObject key length. Found = " + keySize);
                            }

                            byte[] keyData = new byte[keySize];
                            buffer.readBytes(keyData, 0, keyData.length);
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
        ByteBuf buffer = alloc.buffer(BUFFER_CHUNK_SIZE);
        byte[] result = null;
        try {
            buffer.writeByte((byte) SFSDataType.SFS_OBJECT.getTypeID());
            buffer.writeShort((short) object.size());
            result = this.obj2bin(object, buffer);
        }finally {
            if(buffer != null){
                buffer.release();
            }
        }
        return result;
    }

    public ByteBuffer object2buffer(ISFSObject object) {
        ByteBuf buffer = alloc.buffer(BUFFER_CHUNK_SIZE);
        ByteBuffer result = null;
        try {
            buffer.writeByte((byte) SFSDataType.SFS_OBJECT.getTypeID());
            buffer.writeShort((short) object.size());
            result = this.obj2buf(object, buffer);
        }finally{
            if(buffer != null){
                buffer.release();
            }
        }
        return result;
    }
    private  ByteBuffer copyToNioBuffer(ByteBuf buffer) {
        if (buffer.isDirect()) {
            return buffer.nioBuffer();
        }
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return ByteBuffer.wrap(bytes);
    }
    private ByteBuffer obj2buf(ISFSObject object, ByteBuf buffer) {
        Set keys = object.getKeys();
        SFSDataWrapper wrapper;
        Object dataObj;
        for (Iterator result = keys.iterator(); result.hasNext(); buffer = this.encodeObject(buffer, wrapper.getTypeId(), dataObj)) {
            String pos = (String) result.next();
            wrapper = object.get(pos);
            dataObj = wrapper.getObject();
            buffer = this.encodeSFSObjectKey(buffer, pos);
        }
        return copyToNioBuffer(buffer);
    }

    private byte[] obj2bin(ISFSObject object, ByteBuf buffer) {
        Set keys = object.getKeys();
        SFSDataWrapper wrapper;
        Object dataObj;
        for (Iterator result = keys.iterator(); result.hasNext(); buffer = this.encodeObject(buffer, wrapper.getTypeId(), dataObj)) {
            String pos = (String) result.next();
            wrapper = object.get(pos);
            dataObj = wrapper.getObject();
            buffer = this.encodeSFSObjectKey(buffer, pos);
        }
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }

    public byte[] array2binary(ISFSArray array) {
        ByteBuf buffer = alloc.buffer(BUFFER_CHUNK_SIZE);
        byte[] result = null;
        try {
            buffer.writeByte((byte) SFSDataType.SFS_ARRAY.getTypeID());
            buffer.writeShort((short) array.size());
            result = this.arr2bin(array, buffer);
        }finally {
            if(buffer != null){
                buffer.release();
            }
        }
        return result;
    }

    private byte[] arr2bin(ISFSArray array, ByteBuf buffer) {
        SFSDataWrapper wrapper;
        Object pos;
        for (Iterator iter = array.iterator(); iter.hasNext(); buffer = this.encodeObject(buffer, wrapper.getTypeId(), pos)) {
            wrapper = (SFSDataWrapper) iter.next();
            pos = wrapper.getObject();
        }
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
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

    private SFSDataWrapper decodeObject(ByteBuf buffer) throws SFSCodecException {
        SFSDataWrapper decodedObject = null;
            byte headerByte = buffer.readByte();
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
                buffer.readerIndex(buffer.readerIndex()-1);
                decodedObject = new SFSDataWrapper(SFSDataType.SFS_ARRAY, this.decodeSFSArray(buffer));
            } else {
                if (headerByte != SFSDataType.SFS_OBJECT.getTypeID()) {
                    throw new SFSCodecException("Unknow SFSDataType ID: " + headerByte);
                }

                buffer.readerIndex(buffer.readerIndex()-1);
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

    private ByteBuf encodeObject(ByteBuf buffer, SFSDataType typeId, Object object) {
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

    private SFSDataWrapper binDecode_NULL(ByteBuf buffer) {
        return new SFSDataWrapper(SFSDataType.NULL, (Object) null);
    }

    private SFSDataWrapper binDecode_BOOL(ByteBuf buffer) throws SFSCodecException {
        byte boolByte = buffer.readByte();
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

    private SFSDataWrapper binDecode_BYTE(ByteBuf buffer) {
        byte boolByte = buffer.readByte();
        return new SFSDataWrapper(SFSDataType.BYTE, Byte.valueOf(boolByte));
    }

    private SFSDataWrapper binDecode_SHORT(ByteBuf buffer) {
        short shortValue = buffer.readShort();
        return new SFSDataWrapper(SFSDataType.SHORT, Short.valueOf(shortValue));
    }

    private SFSDataWrapper binDecode_INT(ByteBuf buffer) {
        int intValue = buffer.readInt();
        return new SFSDataWrapper(SFSDataType.INT, Integer.valueOf(intValue));
    }

    private SFSDataWrapper binDecode_LONG(ByteBuf buffer) {
        long longValue = buffer.readLong();
        return new SFSDataWrapper(SFSDataType.LONG, Long.valueOf(longValue));
    }

    private SFSDataWrapper binDecode_FLOAT(ByteBuf buffer) {
        float floatValue = buffer.readFloat();
        return new SFSDataWrapper(SFSDataType.FLOAT, Float.valueOf(floatValue));
    }

    private SFSDataWrapper binDecode_DOUBLE(ByteBuf buffer) {
        double doubleValue = buffer.readDouble();
        return new SFSDataWrapper(SFSDataType.DOUBLE, Double.valueOf(doubleValue));
    }

    private SFSDataWrapper binDecode_UTF_STRING(ByteBuf buffer) throws SFSCodecException {
        short strLen = buffer.readShort();
        if (strLen < 0) {
            throw new SFSCodecException("Error decoding UtfString. Negative size: " + strLen);
        } else {
            byte[] strData = new byte[strLen];
            buffer.readBytes(strData, 0, strLen);
            String decodedString = new String(strData);
            return new SFSDataWrapper(SFSDataType.UTF_STRING, decodedString);
        }
    }

    private SFSDataWrapper binDecode_BOOL_ARRAY(ByteBuf buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            byte boolData = buffer.readByte();
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

    private SFSDataWrapper binDecode_BYTE_ARRAY(ByteBuf buffer) throws SFSCodecException {
        int arraySize = buffer.readInt();
        if (arraySize < 0) {
            throw new SFSCodecException("Error decoding typed array size. Negative size: " + arraySize);
        } else {
            byte[] byteData = new byte[arraySize];
            buffer.readBytes(byteData, 0, arraySize);
            return new SFSDataWrapper(SFSDataType.BYTE_ARRAY, byteData);
        }
    }

    private SFSDataWrapper binDecode_SHORT_ARRAY(ByteBuf buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            short shortValue = buffer.readShort();
            array.add(Short.valueOf(shortValue));
        }

        return new SFSDataWrapper(SFSDataType.SHORT_ARRAY, array);
    }

    private SFSDataWrapper binDecode_INT_ARRAY(ByteBuf buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            int intValue = buffer.readInt();
            array.add(Integer.valueOf(intValue));
        }

        return new SFSDataWrapper(SFSDataType.INT_ARRAY, array);
    }

    private SFSDataWrapper binDecode_LONG_ARRAY(ByteBuf buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            long longValue = buffer.readLong();
            array.add(Long.valueOf(longValue));
        }

        return new SFSDataWrapper(SFSDataType.LONG_ARRAY, array);
    }

    private SFSDataWrapper binDecode_FLOAT_ARRAY(ByteBuf buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            float floatValue = buffer.readFloat();
            array.add(Float.valueOf(floatValue));
        }

        return new SFSDataWrapper(SFSDataType.FLOAT_ARRAY, array);
    }

    private SFSDataWrapper binDecode_DOUBLE_ARRAY(ByteBuf buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            double doubleValue = buffer.readDouble();
            array.add(Double.valueOf(doubleValue));
        }

        return new SFSDataWrapper(SFSDataType.DOUBLE_ARRAY, array);
    }

    private SFSDataWrapper binDecode_UTF_STRING_ARRAY(ByteBuf buffer) throws SFSCodecException {
        short arraySize = this.getTypeArraySize(buffer);
        ArrayList array = new ArrayList();

        for (int j = 0; j < arraySize; ++j) {
            short strLen = buffer.readShort();
            if (strLen < 0) {
                throw new SFSCodecException("Error decoding UtfStringArray element. Element has negative size: " + strLen);
            }

            byte[] strData = new byte[strLen];
            buffer.readBytes(strData, 0, strLen);
            array.add(new String(strData));
        }

        return new SFSDataWrapper(SFSDataType.UTF_STRING_ARRAY, array);
    }

    private short getTypeArraySize(ByteBuf buffer) throws SFSCodecException {
        short arraySize = buffer.readShort();
        if (arraySize < 0) {
            throw new SFSCodecException("Error decoding typed array size. Negative size: " + arraySize);
        } else {
            return arraySize;
        }
    }

    private ByteBuf binEncode_NULL(ByteBuf buffer) {
        buffer.writeBytes(new byte[1]);
        return buffer;
    }

    private ByteBuf binEncode_BOOL(ByteBuf buffer, Boolean value) {
        byte[] data = new byte[]{(byte) SFSDataType.BOOL.getTypeID(), (byte) (value.booleanValue() ? 1 : 0)};
        buffer.writeBytes(data);
        return buffer;
    }

    private ByteBuf binEncode_BYTE(ByteBuf buffer, Byte value) {
        byte[] data = new byte[]{(byte) SFSDataType.BYTE.getTypeID(), value.byteValue()};
        buffer.writeBytes(data);
        return buffer;
    }

    private ByteBuf binEncode_SHORT(ByteBuf buffer, Short value) {
        buffer.writeByte((byte) SFSDataType.SHORT.getTypeID());
        buffer.writeShort(value.shortValue());
        return buffer;
    }

    private ByteBuf binEncode_INT(ByteBuf buffer, Integer value) {
        buffer.writeByte((byte) SFSDataType.INT.getTypeID());
        buffer.writeInt(value.intValue());
        return buffer;
    }

    private ByteBuf binEncode_LONG(ByteBuf buffer, Long value) {
        buffer.writeByte((byte) SFSDataType.LONG.getTypeID());
        buffer.writeLong(value.longValue());
        return buffer;
    }

    private ByteBuf binEncode_FLOAT(ByteBuf buffer, Float value) {
        buffer.writeByte((byte) SFSDataType.FLOAT.getTypeID());
        buffer.writeFloat(value.floatValue());
        return buffer;
    }

    private ByteBuf binEncode_DOUBLE(ByteBuf buffer, Double value) {
        buffer.writeByte((byte) SFSDataType.DOUBLE.getTypeID());
        buffer.writeDouble(value.doubleValue());
        return buffer;
    }

    private ByteBuf binEncode_UTF_STRING(ByteBuf buffer, String value) {
        byte[] stringBytes = value.getBytes();
        buffer.writeByte((byte) SFSDataType.UTF_STRING.getTypeID());
        buffer.writeShort((short) stringBytes.length);
        buffer.writeBytes(stringBytes);
        return buffer;
    }

    private ByteBuf binEncode_BOOL_ARRAY(ByteBuf buffer, Collection<Boolean> value) {
        buffer.writeByte(SFSDataType.BOOL_ARRAY.getTypeID());
        buffer.writeShort(value.size());
        Iterator var5 = value.iterator();
        while (var5.hasNext()) {
            boolean b = ((Boolean) var5.next()).booleanValue();
            buffer.writeByte((byte) (b ? 1 : 0));
        }
        return buffer;
    }

    private ByteBuf binEncode_BYTE_ARRAY(ByteBuf buffer, byte[] value) {
        buffer.writeByte(SFSDataType.BYTE_ARRAY.getTypeID());
        buffer.writeInt(value.length);
        buffer.writeBytes(value);
        return buffer;
    }

    private ByteBuf binEncode_SHORT_ARRAY(ByteBuf buffer, Collection<Short> value) {
        buffer.writeByte(SFSDataType.SHORT_ARRAY.getTypeID());
        buffer.writeShort(value.size());
        Iterator var5 = value.iterator();
        while (var5.hasNext()) {
            short item = ((Short) var5.next()).shortValue();
            buffer.writeShort(item);
        }
        return buffer;
    }

    private ByteBuf binEncode_INT_ARRAY(ByteBuf buffer, Collection<Integer> value) {
        buffer.writeByte((byte) SFSDataType.INT_ARRAY.getTypeID());
        buffer.writeShort(value.size());
        Iterator var5 = value.iterator();

        while (var5.hasNext()) {
            int item = ((Integer) var5.next()).intValue();
            buffer.writeInt(item);
        }
        return buffer;
    }

    private ByteBuf binEncode_LONG_ARRAY(ByteBuf buffer, Collection<Long> value) {
        buffer.writeByte((byte) SFSDataType.LONG_ARRAY.getTypeID());
        buffer.writeShort(value.size());
        Iterator var6 = value.iterator();

        while (var6.hasNext()) {
            long item = ((Long) var6.next()).longValue();
            buffer.writeLong(item);
        }
        return buffer;
    }

    private ByteBuf binEncode_FLOAT_ARRAY(ByteBuf buffer, Collection<Float> value) {
        buffer.writeShort((byte) SFSDataType.FLOAT_ARRAY.getTypeID());
        buffer.writeShort(value.size());
        Iterator var5 = value.iterator();

        while (var5.hasNext()) {
            float item = ((Float) var5.next()).floatValue();
            buffer.writeFloat(item);
        }
        return buffer;
    }

    private ByteBuf binEncode_DOUBLE_ARRAY(ByteBuf buffer, Collection<Double> value) {
        buffer.writeByte((byte) SFSDataType.DOUBLE_ARRAY.getTypeID());
        buffer.writeShort(value.size());
        Iterator var6 = value.iterator();

        while (var6.hasNext()) {
            double item = ((Double) var6.next()).doubleValue();
            buffer.writeDouble(item);
        }
        return buffer;
    }

    private ByteBuf binEncode_UTF_STRING_ARRAY(ByteBuf buffer, Collection<String> value) {
        int stringDataLen = 0;
        byte[][] binStrings = new byte[value.size()][];
        int count = 0;

        byte[] binStr;
        for (Iterator binItem = value.iterator(); binItem.hasNext(); stringDataLen += 2 + binStr.length) {
            String buf = (String) binItem.next();
            binStr = buf.getBytes();
            binStrings[count++] = binStr;
        }

        buffer.writeByte((byte) SFSDataType.UTF_STRING_ARRAY.getTypeID());
        buffer.writeShort(value.size());
        byte[][] var10 = binStrings;
        int var9 = binStrings.length;

        for (int var13 = 0; var13 < var9; ++var13) {
            byte[] var12 = var10[var13];
            buffer.writeShort(var12.length);
            buffer.writeBytes(var12);
        }

        return buffer;
    }

    private ByteBuf encodeSFSObjectKey(ByteBuf buffer, String value) {
        buffer.writeShort((short) value.length());
        buffer.writeBytes(value.getBytes());
        return buffer;
    }

    private ByteBuf addData(ByteBuf buffer, byte[] newData) {
        buffer.writeBytes(newData);
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