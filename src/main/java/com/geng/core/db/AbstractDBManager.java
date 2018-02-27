package com.geng.core.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: shushenglin
 * Date:   16/1/8 09:40
 */
public abstract class AbstractDBManager implements IDBManager {
    private static final AtomicInteger autoId = new AtomicInteger();
    protected boolean active = false;
    protected final DBConfig config;
    protected final String name;
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public AbstractDBManager(DBConfig config) {
        this.config = config;
        this.name = "DBManager-" + autoId.incrementAndGet();
    }

    public void init(Object o) {
        if (this.config == null) {
            throw new IllegalStateException("DBManager was not configured! Please make sure to pass a non-null DBConfig to the constructor.");
        }
    }

    public void destroy(Object o) {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        throw new UnsupportedOperationException("Sorry, operation is not supported in this class. The name is auto-generated.");
    }

    public void handleMessage(Object arg0) {
        throw new UnsupportedOperationException("Sorry, operation is not supported in this class.");
    }

    public DBConfig getConfig() {
        return this.config;
    }

    public boolean isActive() {
        return this.active;
    }
}
