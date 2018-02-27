package com.geng.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Created by TangJP on 2016/10/18.
 */
public final class Versions {

    public static final String VERSION_1_0_35 = "1.0.35";
    public static final String VERSION_1_0_77 = "1.0.77";
    public static final String VERSION_1_0_83 = "1.0.83";
    public static final String VERSION_1_0_85 = "1.0.85";
    public static final String VERSION_1_0_86 = "1.0.86";
    public static final String VERSION_1_0_88 = "1.0.88";
    public static final String VERSION_1_0_90 = "1.0.90";
    public static final String VERSION_1_0_91 = "1.0.91";
    public static final String VERSION_1_0_97 = "1.0.97";
    public static final String VERSION_1_0_98 = "1.0.98";
    public static final String VERSION_1_0_99 = "1.0.99";
    public static final String VERSION_1_1_1 = "1.1.1";
    public static final String VERSION_1_1_2 = "1.1.2";
    public static final String VERSION_1_1_3 = "1.1.3";
    public static final String VERSION_1_1_4 = "1.1.4";
    public static final String VERSION_1_1_5 = "1.1.5";
    public static final String VERSION_1_1_8 = "1.1.8";
    public static final String VERSION_1_1_9 = "1.1.9";
    public static final String VERSION_1_1_10 = "1.1.10";
    public static final String VERSION_1_1_11 = "1.1.11";
    public static final String VERSION_1_1_20 = "1.1.20";
    public static final String VERSION_1_2_6 = "1.2.6";
    public static final String VERSION_1_3_1 = "1.3.1";
    public static final String VERSION_1_3_4 = "1.3.4";
    public static final String VERSION_1_3_7 = "1.3.7";
    public static final String VERSION_1_4_0 = "1.4.0";
    public static final String VERSION_1_4_7 = "1.4.7";
    public static final String VERSION_1_5_7 = "1.5.7";
    public static final String VERSION_1_5_10 = "1.5.10";
    public static final String VERSION_1_5_14 = "1.5.14";
    public static final String VERSION_1_5_15 = "1.5.15";
    public static final String VERSION_1_6_0 = "1.6.0";
    public static final String VERSION_1_6_1 = "1.6.1";
    public static final String VERSION_1_6_7 = "1.6.7";
    public static final String VERSION_1_6_9 = "1.6.9";
    public static final String VERSION_1_6_12 = "1.6.12";
    public static final String VERSION_1_6_17 = "1.6.17";
    public static final String VERSION_1_6_20 = "1.6.20";
    public static final String VERSION_1_6_21 = "1.6.21";
    public static final String VERSION_1_6_22 = "1.6.22";
    public static final String VERSION_1_7_5 = "1.7.5";
    public static final String VERSION_1_7_8 = "1.7.8";
    public static final String VERSION_1_7_12 = "1.7.12";
    public static final String VERSION_1_7_18 = "1.7.18";
    public static final String VERSION_1_8_0 =  "1.8.0";
    public static final String VERSION_1_8_2 =  "1.8.2";
    public static final String VERSION_1_8_4 =  "1.8.4";
    public static final String VERSION_1_8_6 =  "1.8.6";

    public static final String VERSION_1_8_11 = "1.8.11";
    public static final String VERSION_1_8_13 = "1.8.13";
    public static final String VERSION_1_8_14 = "1.8.14";
    public static final String VERSION_1_8_15 = "1.8.15";
    public static final String VERSION_1_8_18 = "1.8.18";
    public static final String VERSION_1_8_19 = "1.8.19";
    public static final String VERSION_1_8_20 = "1.8.20";
    public static final String VERSION_1_8_21 = "1.8.21";
    public static final String VERSION_1_8_23 = "1.8.23";
    public static final String VERSION_1_8_32 = "1.8.32";
    public static final String VERSION_1_8_39 = "1.8.39";
    public static final String VERSION_1_8_45 = "1.8.45";
    public static final String VERSION_1_8_49 = "1.8.49";
    public static final String VERSION_1_8_52 = "1.8.52";
    public static final String VERSION_1_8_53 = "1.8.53";
    public static final String VERSION_1_8_58 = "1.8.58";
    public static final String VERSION_1_8_59 = "1.8.59";
    public static final String VERSION_1_8_60 = "1.8.60";
    public static final String VERSION_2_0_0  = "2.0.0" ;
    public static final String VERSION_2_1_2  = "2.1.2" ;
    public static final String VERSION_2_1_5  = "2.1.5" ;
    public static final String VERSION_2_1_6  = "2.1.6" ;
    public static final String VERSION_2_1_13 = "2.1.13" ;
    public static final String VERSION_2_1_19 = "2.1.19" ;



    /**
     * 比较版本号函数
     * ver1 < ver2  返回 小于 0
     * ver1 = ver2  返回 0
     * ver1 > ver2  返回 大于 0
     * @param ver1
     * @param ver2
     * @return
     */
    public static final int Compare(String ver1,String ver2)
    {
        if(StringUtils.isBlank(ver1))
        {
            return -1;
        }
        if(StringUtils.isBlank(ver2))
        {
            return 1;
        }

        int val= CommonUtils.compareVersion(ver1, ver2);
        if(val==0)
        {
            return 1;
        }else if(val==1)
        {
            return 0;
        }
        return -1;
    }

//    /**
//     *  getVersion() <= 玩家版本
//     * @param userProfile
//     * @return
//     */
//    public static final boolean isOlderThanAndEqualVersion(UserProfile userProfile)
//    {
//        if(userProfile==null)
//        {
//            return false;
//        }
//        return userProfile.greatThanAndEqualVersion(getVersion());
//    }
}
