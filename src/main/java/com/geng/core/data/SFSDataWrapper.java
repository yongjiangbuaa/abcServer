package com.geng.core.data;

import java.io.Serializable;

/**
 * Author: shushenglin
 * Date:   16/2/2 15:28
 */

public class SFSDataWrapper implements Serializable {
    private SFSDataType typeId;
    private Object object;

    public SFSDataWrapper(SFSDataType typeId, Object object) {
        this.typeId = typeId;
        this.object = object;
    }

    public SFSDataType getTypeId() {
        return this.typeId;
    }

    public Object getObject() {
        return this.object;
    }
}