package com.geng.gameengine;

import com.geng.utils.Constants;
import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang.StringUtils;

import java.util.Map;


public enum SwitchConstant {
    //新增开关枚举类型请增加注释
    /**
     * 装备功能开关
     */
    EquipFunctionSwitch("EquipFunctionSwitch"),

    CargoCoinSwitch("CargoCoinSwitch"),     //在线金币奖励系统

    /**
     * 成长计划功能开关
     */
    GroupthPlanSwitch("GroupthPlanSwitch"),

    /**
     * 杀怪加成功能开关
     */
    KillMonsterAddFunctionSwitch("KillMonsterAddFunctionSwitch"),
    /**
     * 城堡皮肤系统开关
     */
    CitySkinFunctionSwitch("CitySkinFunctionSwitch"),

    /**
     * 魅力值开关
     */
    GlamourSwitch("GlamourSwitch"),
/**
     * 扔臭鸡蛋开关
     */
    EggSwitch("EggSwitch"),

    /**
     * 三日特权功能开关
     */
    ThreeDayRightSwitch("ThreeDayRightSwitch"),

    /**
     * 战争道具生效开关
     */
    BattleItemEffectSwitch("BattleItemEffectSwitch"),

    /**
     * 安全资源获取开关
     */
    SafeResourceGetSwitch("SafeResourceGetSwitch"),
    /**
     * 掠夺资源等级差衰减
     */
    PlunderResourceDecaySwitch("PlunderResourceDecaySwitch"),

    /**
     * 炼金厂开关
     */
    AlchemySwitch("AlchemySwitch"),

    /**
     * 好友邀请ios开关
     */
    InviteIOSSwitch("InviteIOSSwitch"),

    /**
     * 好友邀请GP开关
     */
    InviteGPSwitch("InviteGPSwitch"),

    /**
     * 首次绑定账户ios开关
     */
    BindIOSSwitch("BindIOSSwitch"),

    /**
     * 首次绑定账户gp开关
     */
    BindGPSwitch("BindGPSwitch"),

    /**
     * 炼金房好友邀请ios开关
     */
    AlchemyIOSSwitch("AlchemyIOSSwitch"),

    /**
     * 炼金房好友邀请gp开关
     */
    AlchemyGPSwitch("AlchemyGPSwitch"),

    /**
     * 龙交配功能开关
     */
    DragonMateSwitch("DragonMateSwitch"),

    /**
     * 亲密度
     */
    FriendAmourSwitch("FriendAmourSwitch"),
	
	 /**
     * 行军特效开关
     */
    MarchEffectSwitch("marchEffect"),

    /**
     * 农民上线效果开关
     */
    FarmerMaxAddSwitch("FarmerMaxAddSwitch"),

    /**
     * 新科技开关
     */
    ScienceNewSwitch("science_new"),

    /**
     * 资源争夺战开关
     */
    ResourceBattleSwitch("resourceBattle"),
    /**
     * 资源争夺战死转伤开关
     */
    ResourceBattleTurnCureSwitch("ResourceBattleTurnCureSwitch"),

    /**
     * 英雄功能开关
     */
    HeroFunctionSwitch("HeroFunctionSwitch"),

    /**
     * 士兵防守队列开关
     */
    DefendArmySwitch("defend_army"),

    /**
     * 联盟累充开关
     */
    AlliancePayTotal("alliance_payTotal"),


    /**
     * 分城开关
     */
    SceneBuildingSwitch("sceneBuilding"),

    /**
     * 小王战行军时间固定开关
     */
    StrongholdMarchTime("stronghold_march"),

    /**
     * 分城科技开关
     */
    ScienceBranch("science_branch"),

    /**
     * 两玩家派兵援助，破罩开关
     */
    AllianceReinforceShield("allianceReinforceShield"),

    /**
     * 皮肤
     */
    Avatar("Avatar"),

    /**
     * Casino Type5 开关
     */
    CasinoTypeSwitch("casinoTypeSwitch"),

    /**
     * 王战期间,官职效果加成开关
     */
    KingFightEffectSwitch("king_fight_effect_switch"),

    /**
     * 英雄驻守功能开关
     */
    HeroGarrisonSwitch("HeroGarrisonSwitch"),
    /**
     * 小王战死转伤 开关
     */
    StrongholdBattleTurnCureSwitch("StrongholdBattleTurnCureSwitch"),

    /**
     * 评论功能开关
     */
    DiscussFunctionSwitch("DiscussFunctionSwitch"),
    /**
     *竞技场功能开关
     */
    ArenaFunctionSwitch("ArenaFunctionSwitch"),

    /**
     * 英灵殿开关
     */
    ArmySoulBuilding("armySoulBuilding"),

    /**
     * 详细属性加成显示开关
     */
    EffectDetailSwitch("effect_detail_switch"),

    /**
     * 资源周活动开关
     */
    WeekResourceMaterialSwitch("WeekResourceMaterial"),

    /**
     * 祈福周活动开关
     */
    WeekPrayActivitySwitch("WeekPrayActivity"),

    /**
     * 每日活动周活动开关
     */
    WeekDailyActivitySwitch("WeekDailyActivity"),

    /**
     * 王战结束清理占领部队开关
     */
    KingFightClearMarchSwitch("KingFightClearMarchSwitch"),

    /**
     * 玩家伤兵等级差衰减
     */
    HurtArmyDecaySwitch("HurtArmyDecaySwitch"),

    /**
     * 玩家死兵等级差衰减
     */
    DeadArmyDecaySwitch("DeadArmyDecaySwitch"),

    /**
     * 新年收集活动
     */
    BuffActivitySwitch("buffActivity"),

    /**
     * 新年定时邮件活动
     */
    TimeActivityMailSwitch("timeActivityMail"),
    /**
     * 飞弹开关
     */
    MissileSwitch("MissileSwitch"),

    /**
     * 黑骑士开关
     */
    MonsterSiege("MonsterSiege"),

    /**
     * 符石开关
     */
    RuneSwitch("RuneSwitch"),
    /**
     * 士兵升级不同服开关
     */
    ArmyUpgradeSwitch("PromotionSwitch"),

    /**
     * 联盟关系开关
     */
    AllianceRelation("alliance_relation"),

    /**
     * 联盟BOSS
     */
    AllianceBossSwitch("AllianceBoss"),

    /**
     * 资源商城市点查找功能开关
     */
    ResourceBusinessScoutSwitch("ResourceBusinessScoutSwitch"),
    /*
     *    超级Boss开关
     */
    SuperBossSwitch("SuperBossSwitch"),

    /**
     * 小公主签到开关
     */
    PrincessSwitch("PrincessSwitch"),

    /**
     * 竞技积分晋级活动开关
     */
    ArenaScoreActivitySwitch("PromoFunctionSwitch"),
    /**
     *跨服竞技场活动开关
     */
    ArenaCrossRankSwitch("RankFunctionSwitch"),
    /*
     * 神战开关
     */
    DeityWarSwitch("deity_war"),

    LoverKeepsake("LoverKeepsake"), // 情侣信物

    EndlessEquip("endlessEquipment"), //无尽装备


    LuckyClover("LuckyClover"), //幸运草

    PuzzleSwitch("PuzzleSwitch"), //拼图

    ClownItemSwitch("ClownItemSwitch"), //小丑鬼脸道具

    ChinaUserMailSwitch("ChinaUserMailSwitch"), //中国渠道新手邮件开关

    UserSevenDaySwitch("UserSevenDaySwitch"),//新手七日登陆奖励
    /**
     * 荣耀建筑功能开关
     */
    SheenBuildingSwitch("SheenBuildingSwitch"),

    GoldActivitySwitch("GoldActivitySwitch"),//金币积分活动

    AnniversarySwitch("AnniversarySwitch"), //一周年使用物品，开出特定物品推送

    NationalArmyAvatarSwitch("NationalArmyAvatarSwitch"),//国家士兵换肤
    ClanBuildingSwitch("territory"),//新联盟堡垒

    HideSoldiersSwitch("HideSoldiersSwitch"), //藏兵所开关

    BindSwitch("BindSwitch"), //账户绑定邮件奖励开关

    CDResetSwitch("CDResetSwitch"), //领主技能重置开关

    WorldRankSwitch("WorldRankSwitch"),//全服排行功能开关

    DefendFailCompensationSwitch("DefendFailCompensationSwitch"), //防守失败战损补偿

    StarEquip("star_npc"),//星辰装备

    /**
     * 龙进阶和羁绊功能开关
     */
    DragonEvolutionSwitch("DragonEvolutionSwitch"),
    PowerLogSwitch("PowerLogSwitch"),
    //大兵装备
    DeityEquip("DeityEquip"),

    GodAchievement("GodAchievement"),

    NewWorldSwitch("NewWorldSwitch"),

    /**
     *符石ID屏蔽开关
     */
    RuneIdForbidSwitch("RuneIdForbidSwitch"),

    /**
     *手机消息通知开关
     */
    MessageSendSwitch("MessageSendSwitch"),

    SecondSheenBuildingSwitch("SecondSheenBuildingSwitch"), //浮空建筑荣耀单独开关

    //大兵全体出战
    GodAllFight("GodAllFight"),

    //资源商注册防刷
    ResourceDealerRegisterFilter("ResourceDealerRegisterFilter"),
    StarRepay("StarRepay"),
    HeroDraw("HeroDraw"), //英雄抽取
    LOCK("Lock"), //密码锁开关
    FastEntrance("FastEntrance");//概览



    private String switchName;

    private SwitchConstant(String switchName){
        this.switchName = switchName;
    }

    public boolean isSwitchOpen(){
        int serverId = Constants.SERVER_ID;
        boolean switchOpen = true;
        boolean testOpen =false;
        GameConfigManager sysConfig = new GameConfigManager("sys_config");
        Map<String,String> switchInfo = sysConfig.getItem(switchName);

        if(switchInfo == null || switchInfo.size() <= 0){
            return false;
        }
        //开关关闭直接返回false
        if(!StringUtils.equals(String.valueOf(1),switchInfo.get("functionSwitchOpen"))) {
           return false;
        }

        if(!switchInfo.containsKey("IsTestOpen") || StringUtils.equals(switchInfo.get("IsTestOpen"), String.valueOf(0))){
            testOpen = true;
        }else if(StringUtils.equals(switchInfo.get("IsTestOpen"), String.valueOf(1))){
            Map<String,String> openRegular = sysConfig.getItem(switchInfo.get("OpenRegular"));
            if(openRegular == null){
                testOpen = true;
            }else {
                String[] openArr = StringUtils.split(openRegular.get("List"), '|');
                if (openArr != null && openArr.length > 0) {
                    for (int i = 0; i < openArr.length; i++) {
                        String[] regularBlock = StringUtils.split(openArr[i], '-');
                        if (Integer.parseInt(regularBlock[0]) <= serverId && serverId <= Integer.parseInt(regularBlock[1])) {
                            testOpen = true;
                            break;
                        }
                    }
                }
            }

        }
        return switchOpen && testOpen;

    }

}
