package com.geng.gameengine;

import com.geng.core.data.*;
import com.geng.exceptions.ExceptionMonitorType;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.COKLoggerFactory;
import com.geng.utils.CommonUtils;
import com.geng.utils.SFSMysql;
import com.geng.utils.Versions;
import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 留存组需要的各种开关
 * Created by Administrator on 2015/6/11.
 */
public class SwitchService {

    public static void getLoginInfo(ISFSObject outData, UserProfile userProfile){
        try{
            ISFSArray switchesList = SFSMysql.getInstance().query("select * from switches");
            //"1.6.1"
//            if(switchesList != null && userProfile.isOlderThanVersion(Versions.VERSION_1_6_1)){
//                for(int i=0;i<switchesList.size();i++){
//                    if(CommonUtils.isSameStr(switchesList.getSFSObject(i).getUtfString("name"), "UI_QuestRwd")){
//                        switchesList.removeElementAt(i);
//                        break;
//                    }
//                }
//            }
            if(switchesList == null || switchesList.size() == 0){
                outData.putSFSArray("switches", new SFSArray());
            }
            outData.putSFSArray("switches", switchesList);
        }catch (Exception e){
            COKLoggerFactory.monitorException("switchservice exceptions", ExceptionMonitorType.LOGIN, COKLoggerFactory.ExceptionOwner.ZC, e);
        }
    }

    /**
     * 版署开关是否打开
     * @return
     */
    public static boolean isPublishSwithOn(){
        String isPublish = new GameConfigManager("sys_config").getItem("publish").get("switch");
        return StringUtils.equals("1", isPublish);
    }

    /**
     * 新在线奖励+今日特价开关
     * @param appVersion
     * @return
     */
    public static boolean isNewCargoSwitchOn(String appVersion){
        Map<String, String> uiControlMap = new GameConfigManager("pay_servercontrol").getItem("69032");
        //CommonUtils.compareVersion(appVersion, uiControlMap.get("k1")) != 2
        if(Versions.Compare(appVersion, uiControlMap.get("k1")) >= 0 && CommonUtils.isValidServerId(uiControlMap.get("k2"))){
            return true;
        }

        return false;
    }

    /**
     * pay_servercontrol的各种开关
     * @param appVersion
     * @param itemId
     * @return
     */
    public static boolean isSwitchOn(String appVersion, String itemId){
        Map<String, String> uiControlMap = new GameConfigManager("pay_servercontrol").getItem(itemId);
        if(uiControlMap == null || uiControlMap.size() == 0) {
            return false;
        }
        //CommonUtils.compareVersion(appVersion, uiControlMap.get("k1")) != 2
        if(Versions.Compare(appVersion, uiControlMap.get("k1"))>=0 && CommonUtils.isValidServerId(uiControlMap.get("k2"))){
            return true;
        }

        return false;
    }

    /**
     * pay_servercontrol的各种开关
     * @param appVersion 版本 建筑等级对应k1
     * @param itemId serverID 建筑等级对应k2
     * @param  buildingLevel 建筑等级对应k3
     * @return
     */
    public static boolean isSwitchOnWithBuildingLevel(String appVersion, String itemId, int buildingLevel){
        Map<String, String> uiControlMap = new GameConfigManager("pay_servercontrol").getItem(itemId);
        if(uiControlMap == null || uiControlMap.size() == 0) {
            return false;
        }
        //CommonUtils.compareVersion(appVersion, uiControlMap.get("k1")) != 2
        if(Versions.Compare(appVersion, uiControlMap.get("k1"))>=0 && CommonUtils.isValidServerId(uiControlMap.get("k2"))
                && buildingLevel >= Integer.valueOf(uiControlMap.get("k3"))){
            return true;
        }

        return false;
    }
}
