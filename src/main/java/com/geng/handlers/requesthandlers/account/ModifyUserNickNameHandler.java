/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月9日 下午2:52:59
 */
package com.geng.handlers.requesthandlers.account;

import com.geng.core.GameEngine;
import com.geng.exceptions.COKException;
import com.geng.exceptions.GameException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.gameengine.ItemManager;
import com.geng.gameengine.UserService;
//import com.geng.gameengine.alliance.AllianceTeleportTaskManager;
//import com.geng.gameengine.chat.FilterBadWorldService;
import com.geng.gameengine.cross.SharedUserService;
//import com.geng.gameengine.world.core.WorldPoint;
import com.geng.handlers.IRequestHandler;
//import com.geng.puredb.dao.UserProfileDao;
//import com.geng.puredb.model.ModifyUserNickNameLog;
//import com.geng.puredb.model.UserArmy;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.*;
import com.geng.utils.xml.GameConfigManager;
//import com.geng.core.User;
import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
//import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 绑定账号
 */
public class ModifyUserNickNameHandler implements IRequestHandler{

    public static final String ID = "user.modify.nickName";


    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {

    }

    @Override
    public void handle(String deviceId, UserProfile userProfile, String data, StringBuilder sb) throws GameException {
        //UserProfile userProfile = GameEngine.getInstance().getUserProfile(user);
//        boolean useOldBadWords = FilterBadWorldService.isFilterBadWorld(userProfile);//对中国玩家屏蔽敏感字
        String oldname = userProfile.getName();
        ISFSObject params = StringUtils.isNotBlank(data) ? SFSObject.newFromJsonData(data) : SFSObject.newInstance();
        if(!params.containsKey("nickName"))
            throw new GameException(GameExceptionCode.INVALID_OPT,"params invalid!!");
        String nickName = params.getUtfString("nickName");
//        if((useOldBadWords && CommonUtils.containsBadWords(nickName)) ||
//                (!useOldBadWords && CommonUtils.nicknamecontainsBadWords(nickName))){
        if(CommonUtils.nicknamecontainsBadWords(nickName)){
            throw new COKException(GameExceptionCode.ACCOUNT_ALREADY_USERNAME_BADWORDS, "nick name contains badwords");
        }
        if (StringUtils.isBlank(nickName)) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "nick name is empty or null");
        } else if (nickName.trim().startsWith("(")) {
            throw new COKException(GameExceptionCode.NAME_INVALID, "nick name is empty or null");
        }
        nickName = nickName.trim();
        int nameLength = CommonUtils.getNameLength(nickName);
        if (nameLength < 3 || nameLength > 16) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "nick name is too short");
        } else if (!CommonUtils.checkName(nickName)) {
            throw new COKException(GameExceptionCode.NAME_JSON_CHARACTER, "contains { or }");
        }

        //将该名字暂时放入redis中，防止并发修改成同样的名字
        //rs.hsetnx返回如果是0说明该名字正在有人修改，抛出already exists错误.
/**        int stat = ServerCommunicationService.lock(ServerCommunicationService.USER_NAME, nickName);
        if (stat == 0) {
            throw new COKException(GameExceptionCode.ACCOUNT_ALREADY_USERNAME, "user name already exists.");
        }**/
        ISFSObject retObj = new SFSObject();
        try {
            ISFSArray nameArr = UserProfile.selectNameByName(nickName);
            if (nameArr.size() != 0) {
                throw new COKException(GameExceptionCode.ACCOUNT_ALREADY_USERNAME, "user name already exists.");
            }
//            String goodsId = "200021"; //领主改名
//            boolean costItem = true;
            /*try {
                ItemManager.decItem(userProfile, goodsId, 1, LoggerUtil.GoodsUseType.MODIFY_NAME, true);
            } catch (COKException notEnoughException) {
                costItem = false;
                int goldCost = Integer.parseInt(new GameConfigManager("goods").getItem(goodsId).get("price"));
                long goldRet = userProfile.decrAllGold(LoggerUtil.GoldCostType.CHANGE_NAME, goldCost, Integer.parseInt(goodsId), 0, null);
                userProfile.update(false);
                retObj.putLong("gold", goldRet);
            }*/
            try {
                String oldNickName = userProfile.getName();
                userProfile.setName(nickName);
                userProfile.incrChNameCount();
                UserService.changeNewName(userProfile, oldNickName, nickName);
//                WorldPoint userWorldPoint = userProfile.getUserWorld().getUserWorldPoint();//更新内存中worldpoint的ownerName
//                userWorldPoint.setOwnerName(nickName);
//                List<WorldPoint> points = WorldPoint.selectAllPointByUID(userProfile.getUid());
                /*if (points == null || points.size() == 0) {
                    LoggerUtil.getInstance().logBySFS(ExtensionLogLevel.WARN, "not found user point by userID " + userProfile.getUid());
                } else {
                    for (WorldPoint worldPoint : points) {
                        worldPoint.setOwnerName(nickName);
                        worldPoint.update();
                    }
                }*/
//                ModifyUserNickNameLog modifyUserNickNameLog =new ModifyUserNickNameLog(userProfile.getUid(),oldname,nickName);
//                modifyUserNickNameLog.insert();
//                userProfile.getAllianceTeleportTaskManager().triggerTask(AllianceTeleportTaskManager.TriggerType.LORDNAME, 0);
            }catch (COKException e){
                /*if(costItem){
                    ItemManager.addItem(userProfile,goodsId, 1, 0, LoggerUtil.GoodsGetType.SESSION_ROLLBACK);
                }else{
                    userProfile.incrAllGold(LoggerUtil.GoldCostType.MODIFY_USERNAME, Integer.parseInt(new GameConfigManager("goods").getItem(goodsId).get("price")), 0);
                    userProfile.update(false);
                }*/
                throw e;
            }
            userProfile.setLastUpdateTime();
            userProfile.update();
            //组织返回数据
            sb.append(com.geng.service.UserService.fillAll(userProfile).toJson());
        } finally {
//            ServerCommunicationService.unlock(ServerCommunicationService.USER_NAME, nickName);
        }

    }
}
