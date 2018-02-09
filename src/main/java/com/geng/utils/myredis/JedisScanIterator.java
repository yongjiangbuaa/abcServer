package com.geng.utils.myredis;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.Iterator;

/**
 * Jedis command SCAN helper.
 * @author XuZiHui
 *
 */
public class JedisScanIterator implements Iterator<String> {
    private Jedis jedis;
    private String patten;
    private ScanParams params;

    private ScanResult<String> _scanResult;
    Iterator<String> _keys;
    String _nextCursor;

    public JedisScanIterator(Jedis jedis, String patten) {
        this.jedis = jedis;
        this.patten = patten;
        this.params = new ScanParams();
        params.match(this.patten);
        params.count(1000);

        _scanResult = this.jedis.scan("0", params);
        _keys = _scanResult.getResult().iterator();
        _nextCursor = _scanResult.getStringCursor();
    }

    @Override
    public boolean hasNext() {
        if (_keys.hasNext()) {
            return true;
        }
        while (!"0".equals(this._nextCursor)) {
            _scanResult = jedis.scan(_nextCursor, params);
            _nextCursor = _scanResult.getStringCursor();
            _keys = _scanResult.getResult().iterator();
            if (_keys.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String next() {
        if (_keys.hasNext()) {
            return _keys.next();
        }

        while (!"0".equals(this._nextCursor)) {
            _scanResult = jedis.scan(_nextCursor, params);
            _nextCursor = _scanResult.getStringCursor();
            _keys = _scanResult.getResult().iterator();
            if (_keys.hasNext()) {
                break;
            }
        }
        return this._keys.next();
    }

}