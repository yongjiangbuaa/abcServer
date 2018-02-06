package com.geng.core.serialization;

/**
 * Author: shushenglin
 * Date:   16/2/2 15:35
 */


import com.geng.core.data.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface ISFSDataSerializer {
    byte[] object2binary(ISFSObject var1);

    byte[] array2binary(ISFSArray var1);

    ISFSObject binary2object(byte[] var1);

    ISFSArray binary2array(byte[] var1);

    String object2json(Map<String, Object> var1);

    String array2json(List<Object> var1);

    ISFSObject json2object(String var1);

    ISFSArray json2array(String var1);

    ISFSObject pojo2sfs(Object var1);

    Object sfs2pojo(ISFSObject var1);

    SFSObject resultSet2object(ResultSet var1) throws SQLException;

    SFSArray resultSet2array(ResultSet var1) throws SQLException;

    void flattenArray(List<Object> array, SFSArray sfsArray);
}
