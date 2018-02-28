package com.geng.gameengine;

import com.geng.exceptions.ExceptionMonitorType;
import com.geng.utils.COKLoggerFactory;
import com.geng.utils.Constants;
import com.geng.utils.properties.PropertyFileReader;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by LiYongJun on 2015/8/19 19:27
 */
public class GeoIpService {
    private static Logger LOGGER = LoggerFactory.getLogger(GeoIpService.class);
    private static FileInputStream database = null;
    private static DatabaseReader reader = null;
    private static final String filePath = PropertyFileReader.getItem("GeoIP2_Country_File");
    private static long databaseFileModifyTime = -1;

    static {
        try {
            database = new FileInputStream(filePath);
            reader = new DatabaseReader.Builder(database).build();
            File file = new File(filePath);
            if(!file.exists()){
                COKLoggerFactory.monitorException("ip2country error: file not exists", ExceptionMonitorType.GEOIP, COKLoggerFactory.ExceptionOwner.GWP);
            }else{
                databaseFileModifyTime = file.lastModified();
            }
        } catch (Exception e) {
            COKLoggerFactory.monitorException("ip2country error", ExceptionMonitorType.GEOIP, COKLoggerFactory.ExceptionOwner.LYJ, e);
        } finally {
            if (database != null) {
                try {
                    database.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void reloadCountryCode(){
        File file = new File(filePath);
        if(!file.exists()){
            COKLoggerFactory.monitorException("ip2country error: file not exists", ExceptionMonitorType.GEOIP, COKLoggerFactory.ExceptionOwner.GWP);
            return;
        }

        if(file.lastModified() == databaseFileModifyTime){
            return;
        }

        databaseFileModifyTime = file.lastModified();
        FileInputStream newDatabase = null;
        DatabaseReader oldReader = null;
        try{
            newDatabase = new FileInputStream(filePath);
            DatabaseReader newReader = new DatabaseReader.Builder(newDatabase).build();
            oldReader = reader;
            reader = newReader;
        }catch (Exception e){
            COKLoggerFactory.monitorException("ip2country error: reload file", ExceptionMonitorType.GEOIP, COKLoggerFactory.ExceptionOwner.GWP, e);
        }finally {
            if(newDatabase != null){
                try{
                    newDatabase.close();
                }catch (Exception e){
                    COKLoggerFactory.monitorException("ip2country error: close inputStream", ExceptionMonitorType.GEOIP, COKLoggerFactory.ExceptionOwner.GWP, e);
                }
            }
            if(oldReader != null){
                try{
                    oldReader.close();
                }catch (Exception e){
                    COKLoggerFactory.monitorException("ip2country error: close reader", ExceptionMonitorType.GEOIP, COKLoggerFactory.ExceptionOwner.GWP, e);
                }
            }
        }
    }

    public static String getCountryCode(String ip) {
        String countryCode = null;
        if (reader != null) {
            try {
                InetAddress ipAddress = InetAddress.getByName(ip);
                CountryResponse response = reader.country(ipAddress);

                Country country = response.getCountry();
                countryCode = country.getIsoCode();
            } catch (Exception e) {
                LOGGER.error("GeoIP DatabaseReader not recognized ip:{}", ip);
            }
        } else {
            LOGGER.info("GeoIP DatabaseReader is null");
        }
        return countryCode;
    }

    public static void shutDown() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (database != null) {
            try {
                database.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("GeoIpService reader has been closed");
    }


}
