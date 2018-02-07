package com.geng.gameengine.mail;

/**
 * Created by LiYongJun on 2015/1/14 0014 18:19
 */
public enum MailType {
    Null1(false),					/*废弃00*/
    Null2(false),					/*废弃 01*/
    System(false),					/*系统栏 02*/
    Null3(false),					/*废弃 03*/
    Null4(false),					/*废弃 04*/
    Resource(true),				    /*系统栏 05*/
    Detect(false),					/*系统栏 06*/
    Fight(true),					/*系统栏 07*/  //4 => 7
    Detect_Report(true),			/*系统栏 08*/
    ENCAMP(true),					/*系统栏 09*/
    TradeResource(true), 			/*资源援助 10*/	//世界探险邮件旧格式 12
    Cure_Soldier(false),			/*系统栏 11*/
    WORLD_NEW_EXPLORE(true), 		/*系统栏 12*/	//世界探险邮件新格式 14=>12
    AllServerWithPush(false),		/*公告栏 13*/	//可回复(GM 回复个人/全服)
    MAIL_FRESHER(false), 			/*公告栏 14*/	//10 => 14
    SysNotice(false),				/*公告栏 15*/	//不可回复(XML - MAIL)
    UpNotice(false),				/*公告栏 16*/
    AllianceInvite(true),			/*个人栏 17*/
    WORLD_MONSTER(true),       	    /*世界野外打怪 18*/
    Null6(false),					/*废弃 19*/
    Alliance_ALL(false),			/*联盟邮件 20*/
    Send(false),					/*个人栏 21*/ //0 => 21
    Personal(false),				/*个人栏 22*/ //1 => 22
    ModSend(false)			        /*MOD 23*/
    ,ModPersonal(false)				/*MOD 24*/
    ,AllianceApply(true)    		/*申请加入联盟 25*/
    ,InviteMovePoint(true)          /*邀请迁城 26*/
    ,KickAllianceUser(true)         /*踢出联盟成员 27*/
    ,GIFT(false)                    /*邮件送礼 28*/
    ,GIFT_EXCHANGE(false)           /*礼包赠送 29*/
    ,MONSTER_BOSS(true)          /*世界BOSS战报 30*/
    ,CHAT_ROOM(true)          /**/
    ,MONSTER_BOSS_REWARD(true)  /*世界BOSS奖励邮件 32*/
    ,RefuseAllianceApply(true)  /*拒绝申请加入联盟 33*/
    ,GIFT_ALLIANCE(false)           /*联盟充值礼包 34*/
    ,HERO_RELEASE(false)            /*英雄释放 35*/
    ,FRIEND_VISIT(false)            /**好友访问 36*/
    ,KILL_TITAN(false)              /*击杀泰坦 37*/
    ,USER_NOBILITY(false)           /*爵位 38*/
	,FACEBOOK_LIKE(false)           /*fb_like 39*/
    ,ROSE_CROWN_SEND(false)           /*送花冠 40*/
    ,RESOURCE_COUNTRY_REMOVE(false)   /*迁城损失资源 41*/
    ,ACTIVITY_MAIL(false)           /*活动类型邮件 42*/
    ,SendMedia(false)               /*个人发送的语音消息43*/
    ,PersonalMedia(false)           /*个人接收的语音消息44*/
    ,ModSendMedia(false)            /*发送的Mod语音消息45*/
    ,ModPersonalMedia(false)       /*接收的Mod语音消息46*/
    ,PresentGift(false)       /*赠送物品47(目前物品type90用)*/
    ,MissileFight(true)           /*飞弹攻击战报 48*/
	;

    private boolean isObjectContents;

    private MailType(boolean isObjectContents) {
        this.isObjectContents = isObjectContents;
    }

    public boolean contentsIsObj () {
        return isObjectContents;
    }

    public static MailType get(int ordinal) {
        return values()[ordinal];
    }
}