package com.geng.core;

import com.geng.utils.Constants;
import com.geng.utils.LoggerUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 14-4-10.
 */
public class ConcurrentLock {
    private static ConcurrentLock instance;
    private static final String LOCK_PREFIX = "COK" + Constants.SERVER_ID;
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentLock.class);
    private ConcurrentHashMap<String, Object> lock;

    public enum LockType {
		AlliancePoint("donate")						//更改联盟积分
		,ALLIANCE_HELP("allHelp")
        ,SAME_ACCOUNT_LOGIN("same_account_login")
		,SEND_SERVER_MAIL("server_mail")
        ,SEND_SERVER_PUSH("server_push")    // huangyuanqiang
		,RECEIVE_MAIL_REWARD("mail_reward")
        ,CREATE_POS_BUILDING("building_pos")
        ,MINE_FRIEND_HELP("mine_friend_help")
		, ALLIANCE_MEMBER_CHANGE("alliance_member_change")
        , ALLIANCE_LEADER_CHANGE("alliance_leader_change")
        , INVITATION_COUNT("invitation_count")
        , INVITATION_GOLD("invitation_gold")
        , UPGRADE_SCIENCE("upgrade_science")
        , USER_SOCK("user_sock")
        , ONLINE_REWARD("online_reward")
        , HOT_ITEM("hot_item")
        , MATERIAL_DROP("material_drop")
        , EQUIP("equip")
        , ACTIVATION_CODE("activation_code")
        , TREASURE("treasure")
        , SEND_KING_PRESENT("send_king_present")
        , QUEUE("queue_reset")
        , GET_FREE_QUEUE("get_free_queue")
	    , MAIL_STATUS("mail_status")
        , USER_LOGIN_IN("user_login")
        , KILL_EVENT_MAIL("kill_event_mail")
        , RECEIVE_SERVER_MAIL("receive_server_mail")
	    , DELETE_MAIL("delete_mail")
        //, CHECK_MAIL_NUM("check_mail_num")
        , USER_ACTIVITY_NEW("user_activity_new")
        , RECALL_WORLD_MARCH_ARMY("recall_world_march_army")
        , LOTTERY("lottery")
        , CARGO_REWARD("cargo_reward")
        , CARGO_COIN_REWARD("cargo_coin_reward")
        , GET_LOGIN_INFO("get_login_info")
        , PROP_MAKE("prop_make")
        , BATTLE_TEAM_RETREAT("battle_team_retreat")
        , TOWER_NEW_CONDITION("tower_new_condition")
        , TREASURE_MAP("treasure_map")
        , BADWORDS("bad_word")
        , CROSS_MOVE("cross_move")
        , USE_WORLD_POINT("use_world_point")
        , USER_HOSPITAL_UPDATE("user_hospital_update")
        , DRAGON_BATTLE_ALLIANCE("dragon_battle_alliance")
        , MonthLyCardsReward("monthly_cards_reward")
        , CARGO_PLAY("cargo_play")
        , VISIT_FRIEND("visit_friend")
        , KILL_TITAN("kill_Tian")
        , DRIFT_BOLLLE("drift_bottle")
        , USER_NOBILITY_EXP("user_nobility_exp")
        , USER_NOBILITY_EXPLOIT("user_nobility_exploit")
        , FIGHTING_REWARD("fighting_reward")
        , FACEBOOK_LIKE_REWARD("facebook_like_reward")
        , GET_FREE_FARMER("get_free_farmer")//查询农民信息时使用此锁
        , FARMER("user_farmer")//更改农民状态时需使用此锁
		, EXPEDITION_TAX("expedition_tax")
        , VIP_GIFT("vip_gift")//领取vip奖励
        , BUILDING_RIBBONCUT_REWARD("building_ribboncut_reward")//建筑剪彩之后领取奖励
        , DEBRIS_CLEAN_REWARD("debris_clean_reward")//杂物清理领奖
        , HUNTING_REWARD("hunting_reward")//狩猎
        , EXPLORER_NPC("explorer_npc")//探险家
        , DAILY_ACTIVE("daily_active")//每日活跃
        ,DUNGEONS("dungeons")//地下城
        ,GLAMOUR("glamour")//魅力值
        ,EGG("egg")//臭鸡蛋
        ,ALCHEMY("alchemy")//炼金厂
        ,FRIEND_AMOUR("friend_amour")// 亲密度
        ,DRAGON_MATE("dragon_mate")//龙交配
        ,ALLIANCE_PAY_TOTAL("alliance_payTotal")//联盟累充
        ,USER_GLORY("user_glory") // 荣誉值
        ,USER_DRAGON_PRO("user_dragon_pro") // 龙元素
        ,ARENA_BATTLE("arena_battle") //竞技场战斗锁
        ,PROMOTION_ARENA_BATTLE("promotion_arena_battle")//晋级竞技场战斗锁
        ,DAILY_THRESHOLD("daily_threshold") // 每日上限
        ,ARMY_ENHANCE("army_enhance") // 兵种强化
        ,ALLIANCE_BOSS("alliance_boss") //联盟boss
        ,ALLIANCE_RELATION("alliance_relation") //联盟关系
        ,SUPER_BOSS("super_boss_rank_cache") //世界boss排名缓存
        ,EQUIP_COLLECTION("equip_collection") // 装备集合
        ,LOVER_KEEPSAKE("lover_keepsake") //情侣手信
        ,LUCKY_CLOVER("user_lucky_clover") //用户幸运草
        ,ONECLOUD_PAY("onecloud_pay") //一元云购
        ,WORLD_SKIN_STORAGE("world_skin_storage") //国家皮肤装饰
        ,NEW_WORLD_RESOURCE("newworld_resource") //异世界资源
        ;

        private LockType(String code) {
            lockCode = code;
        }

        public String getLockCode() {
            return lockCode;
        }

        private String lockCode;
    }

    public static class LockValue {
        String key;

        public LockValue(LockType type, String index) {
            if (StringUtils.isBlank(index)) {
                key = type.getLockCode();
            } else {
                key = type.getLockCode() + "_" + index;
            }
            key = key + "_" + Constants.SERVER_ID;
        }

        public String getKey() {
            return key;
        }
    }

    private ConcurrentLock() {
        this.lock = new ConcurrentHashMap<>();
    }

    private static class LazyHolder {
        final private static ConcurrentLock INSTANCE = new ConcurrentLock();
    }

    public static ConcurrentLock getInstance() {
        return LazyHolder.INSTANCE;
    }

    public boolean lock(LockValue key) {
        Object value = lock.putIfAbsent(key.getKey(), new Object());
        if (value == null) {
            return true;
        }
        return false;
    }

    /**
     * 重载方法，方便使用
     *
     * @param lockType
     * @param index
     * @return
     */
    public LockValue lock(LockType lockType, String index) {
        LockValue lockKey = new LockValue(lockType, index);
        lock.putIfAbsent(lockKey.getKey(), new Object());
        return lockKey;
    }

    public boolean isLock(LockValue key) {
        return lock.containsKey(key.getKey());
    }

    /**
     * 带时间的锁
     *
     * @param lockType
     * @param uid
     * @param expiredTime 秒
     * @return
     */
    public boolean lockWithExpiredTime(LockType lockType, String uid, long expiredTime) {
        String key = getKey(lockType, uid);
        Long time = Long.valueOf(System.currentTimeMillis() + expiredTime * 1000);
        Object value = lock.putIfAbsent(key, time);
        if (value == null) {
            return true;
        } else if (value != null && value instanceof Long && System.currentTimeMillis() >= (long) value) {
            synchronized (value) {
                Object tmpValue = lock.get(key);
                if (tmpValue != null && tmpValue instanceof Long && System.currentTimeMillis() < (long) tmpValue) {
                    return false;
                }
                lock.replace(key, time);
                return true;
            }
        } else if (value != null && value instanceof Long && System.currentTimeMillis() < (long) value) {
            return false;
        }
        return false;
    }

    public void unExpiredTimeLock(LockType lockType, String uid) {
        lock.remove(getKey(lockType, uid));
    }

    private static String getKey(LockType lockType, String uid) {
        StringBuilder sb = new StringBuilder(LOCK_PREFIX).append(lockType.getLockCode());
        if (!StringUtils.isBlank(uid)) {
            sb.append(uid);
        }
        return sb.toString();
    }

    public Object getSyncObj(LockValue key) {
        return lock.get(key.getKey());
    }

    public void unLock(LockValue key) {
        try {
            lock.remove(key.getKey());
        } catch (Exception e) {
            LoggerUtil.getInstance().recordException(e);
        }
    }

    /**
     * 获取锁如果失败间隔onceWaitTime时间再次获取，总共重新尝试loopTime次
     * @param key
     * @param loopTime
     * @param onceWaitTime
     * @return
     */
    public boolean lockWaitTime(LockValue key, int loopTime, long onceWaitTime){
        Object value = lock.putIfAbsent(key.getKey(),new Object());
        if(value == null) {
            logger.info("wait get lock {} direct success", key.getKey());
            return true;
        }

        for(int i = 0; i < loopTime; ++i){
            try {
                Thread.currentThread().sleep(onceWaitTime);
                Object tmpValue = lock.putIfAbsent(key.getKey(),new Object());
                if(tmpValue == null) {
                    logger.info("wait get lock {} success with loop {}", key.getKey(), i);
                    return true;
                }
            }catch (Exception e){
                logger.info("wait get lock {} fail in loop {} by exception", key.getKey(), i);
                return false;
            }
        }

        logger.info("wait get lock {} fail over time", key.getKey());
        return false;
    }
}
