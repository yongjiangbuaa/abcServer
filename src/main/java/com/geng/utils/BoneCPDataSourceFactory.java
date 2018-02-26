package com.geng.utils;

import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * Created by lifangkai on 15/6/11.
 */
public class BoneCPDataSourceFactory extends UnpooledDataSourceFactory {

    public BoneCPDataSourceFactory() {
        this.dataSource = new BoneCPDataSource();
    }
}
