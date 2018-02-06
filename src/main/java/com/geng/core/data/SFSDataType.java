package com.geng.core.data;

/**
 * Author: shushenglin
 * Date:   16/2/2 15:28
 */
public enum SFSDataType {
    NULL(0),
    BOOL(1),
    BYTE(2),
    SHORT(3),
    INT(4),
    LONG(5),
    FLOAT(6),
    DOUBLE(7),
    UTF_STRING(8),
    BOOL_ARRAY(9),
    BYTE_ARRAY(10),
    SHORT_ARRAY(11),
    INT_ARRAY(12),
    LONG_ARRAY(13),
    FLOAT_ARRAY(14),
    DOUBLE_ARRAY(15),
    UTF_STRING_ARRAY(16),
    SFS_ARRAY(17),
    SFS_OBJECT(18),
    CLASS(19);

    private int typeID;

    private SFSDataType(int typeID) {
        this.typeID = typeID;
    }

    public static SFSDataType fromTypeId(int typeId) {
        SFSDataType[] var4;
        int var3 = (var4 = values()).length;

        for(int var2 = 0; var2 < var3; ++var2) {
            SFSDataType item = var4[var2];
            if(item.getTypeID() == typeId) {
                return item;
            }
        }

        throw new IllegalArgumentException("Unknown typeId for SFSDataType");
    }

    public static SFSDataType fromClass(Class clazz) {
        return null;
    }

    public int getTypeID() {
        return this.typeID;
    }
}