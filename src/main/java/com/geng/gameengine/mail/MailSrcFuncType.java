package com.geng.gameengine.mail;

/**
 * Created by qinbinbin on 2016/12/29
 * 用作邮件的srcType 和 gold_cost_type
 */

//禁止 变化值!! 导致统计不对应,尤其后台管理
public enum MailSrcFuncType {
    MAIL_DEFAULT(0),//默认
    BUG_FIX(1), //bug补偿,后台管理用的多
    ADMIN_GM(2), //漏单补发 --后台管理
    MAIL_BATCH(3),
    ADMIN_GM_GROUPMAIL(4), //后台管理---群体邮件
    ADMIN_GM_PERSONMAIL(5), //后台管理---个人邮件
    Resource(6),//资源战
    PERSON_CHAT(7),//个人聊天
    ALLIANCEPERSON_CHAT(8),//联盟聊天
    NORMAL(9),//通用,未用
    SendMailEvent(10),
    EXCHANGE(11),//礼包购买确认
    DragonBattleFightTop(12),//巨龙战场
    EXPEDITION(13),//远征
    CROSS_THRONE(14),//跨服王座
    KINGDOM(15),
    PAY_ALLIANCE(16),//购买礼包-给联盟奖
    MONSTER_SIEGE(17),//杀怪活动
    ALLIANCE_SCORE(18),//联盟积分  AllianceActivityManager
    PERSON_SCORE(19),//个人积分 ScoreManager
    ALLIANCE_PERSON(20),//联盟个人
    GIFT(21),//邮件送礼  暂时未用
    ARENA(22),//竞技场
    PAY_ALLIANCE_TOTAL(23),//联盟累冲
    ROSE_CROWN(24),//跨服迁城清除花冠数据补偿邮件
    MOVE_CITY_LOST(25),//迁城损失资源
    PAY_USER_BACK(26),//充值玩家回归
    CROSS_KINGDOM_FIGHT(27),//CrossKingdomFightManager
    KINGDOM_CEMETERY(28),//KingdomCemeteryManager
    BLESS(29),//祝福活动
    InviteMovePoint(30),//邀请迁城
    KickAllianceUser(31),//联盟踢人
    AllianceInvite(32),//邀请加入联盟
    RefuseAllianceApply(33),//拒绝加入联盟
    JoinAlliance(34),//加入联盟奖励
    RewardRankService(35),//RewardRankService
    HeroEmployRankService(36),//HeroEmployRankService
    FightOfKingActivity(37),//FightOfKingActivity
    RoseCrownActivity(38),//RoseCrownActivity
    PointsActivityService(39),//PointsActivityService
    AllianceScoreAct(40),//AllianceScoreAct
    ActivityRewardMailTask(41),//ActivityRewardMailTask
    newUserItem(42),//新手发迁城道具
    UserActivityService(43),
    SignInFeedService(44),
    SevenDayExchangeService(45),
    ServerSuggestionService(46),
    FACEBOOK_LIKE(47),
    ActivationService(48),
    AccountService(49),// 第一次绑定用户
    NEWYEAR_ACTIVITY(50),// 新年活动 黑土地采集
    ALLIANCE_DONATE(51),
    ROSE_CROWN_SEND(52),
    DragonBattleMailProcesser(53),
    USER_VIP(54),
    battleTeamRetreatForMoveCity(55),
    RibbonCut(56),//剪彩
    SERVER_PUSH(57),//server_push 表(后台管理)
    AllianceTeleport(58),//励精图治
    KILL_TITAN(59),
    USER_NOBILITY(60),//爵位
    FRIEND_MESSAGE(61),
    CASINO(62),//拉斯维加斯
    USER_BUST(63),//半身像
    Detect(64),//侦查
    NEWPLAYER_TASK(65),//新手7日任务  NewPlayerTaskManager
    GLAMOUR(66),//荣誉值
    ALCHEMY(67),//炼金场
    INVITEE(68),//接受邀请
    WORLDPOINT_BLACKLAND(69),//黑土地被摧毁,强制迁城
    BUILD_UPGRADE(70),//建筑升级
    Cure_Soldier(71),
    WORLD_NEW_EXPLORE(72),
    KingPresent(73),
    FIRST_PAY(74),
    GIFT_EXCHANGE(75),
    PAY_EXCHANGE_TOTAL(76),//累计充值
    CoinsFeedback(77),//金币累消
    STRONGHOLD(78),//超级要塞
    TERRITORY_FLAG(79),
    TERRITORY_TOWER(80),
    COMPENSATION(81),//WorldFightLoserCompensationService
    USER_LORD(82),
    ALLIANCE_TEAM(83),
    TreasureMapTask(84),
    WorldActivityFightHandler(85),
    TradeResource(86),
    MONSTER_BOSS(87),
    MissileFight(88),
    ResourceBattle(89),//ResourceBattleManager
    PresentGift(90),//赠送礼物
    TUTORIAL(91),
    INVITER(92),
    SendMailWebProxy(93),//
    WORLD_MONSTER_HALFORCS(94),
    WORLD_MONSTER_BOSS(95),
    WORLD_MONSTER_GUIDE(96),
    WORLD_MONSTER_PUSH(97),
    WORLD_MONSTER(98),    /*世界野外打怪*/
    DETECT_PUSH(99),//PushPseudoScoutManager
    ADMIN_OP_REFUND(100),// 后台管理  退款
    ADMIN_USER_AUDIT_PIC(101),// 后台管理  审核图片
    ADMIN_USER_AUDIT_PIC_MSG(102),// 后台管理  朋友圈审核图片
    ADMIN_USER_PIC_CONFIM(103),// 后台管理  个人图片确认
    ADMIN_BAN_WORD(104),// 后台管理  禁言警告
    ADMIN_USER_REPORTPICTURE(105),// 后台管理  审核被举报图片
    ADMIN_USER_CHANGENAME(106),// 后台管理  改名
    ADMIN_GM_FILEMAIL(107),//文本邮件 ,后台管理
    STRONGHOLD_REFRESH(108),
    KINGDOM_SKILL(109),
    WorldClearDeadAccountTask(110),
    WORLD_MARCH(111),
    USER_GENERAL(112),
    LeaderCommand(113),
    UpdatePicVer(114),
    FANTASY_FIGHT(115),//虚拟战斗
    ALLIANCE_BATTLE_LEADER(116),
    PLAYERCITY_FIGHT(117),
    ALLIANCE_BATTLE_TEAM(118),//WorldAllianceBattleTeamFightHandler
    ALLIANCE_BATTLE(119),
    WORLD_SPACE_FIGHT(120),
    USER_MIX(121),
    ABSTRACT_WORLD_FIGHT(122),//AbstractWorldFightHandler
    ANCIENT_BATTLE(123), //远古战场
    ONECLOUDPAY_ACTIVITY(124), //一元云购活动
    EXCHANGE_ACTIVITY(125), //ExchangeActivityService
    EXPLORER_MANAGER(126), //ExplorerManager
    USE_ITEM(127),//使用道具
    SOCK_ACTIVITY(128),//圣诞袜
    COLLECT_RESOURCE(129),//UserResource 类中
    CITY_MONSTER(130),//
    DAILY_ACTIVE(131),//
    DAILY_TASK(132),//
    DEBRIS_CLEAN(133),//
    DUNGEONS(134),//
    FIGHTING_MANAGER(135),//
    FRESH_RECHARGE_SERVICE(136),//
    HUNTING_MANAGER(137),//
    LOTTERY_SERVICE(138),//
    MONSTER_ACTIVITY(139),//
    MONTHLY_CARD(140),//
    ONLINE_REWARD(141),//
    PLAYER_LEVELUP(142),//
    QUICK_DNF(143),//
    SEVENDAY_EXCHANGE(144),//7日充值
    SIGNIN_SERVICE(145),//
    TASK_COMPLETE(146),//
    TASK_MANAGER(147),//
    TIME_REWARD(148),//
    TRIAL_FIELD(149),//
    USER_ACTIVITY_NEWS(150),//
    BUILD_REWARD(151),//
    USER_GOAL(152),//
    MOPUP_MONSTER(153),// 怪物扫荡
    ACHIEVEMENT_MANAGER(154),//成就
    LOTTERY_ACTIVITY(155),//
    ALLIANCEPAY_MANAGER(156),//
    Login14DaysActivity(157),//
    DRAGON_MATE(158),//
    WORLD_PVE_FIGHT(159),//
    GROWTH_PLAN(160),//成长计划
    SIGN_IN(161),
    NORMAL_EXTRA(162),
    MATERIAL_EXTRA(163),
    ROSE_EXTRA(164),
    DO_EFFECT(165),//使用道具增加效果
	ALLIANCE_BOSS(166),//联盟Boss
    SUPER_BOSS_ACTIVITY(167),//SuperBoss
    PRINCESS_SIGNIN(168),//小公主签到
    LOVER_KEEPSAKE(169), //情侣关系
    BUY_FOR_SEND_GIFT(170), //购买礼物赠送给好友
    LUCKY_CLOVER(171), //幸运草送礼
    EXPLORE_AUTO_SEND_COMPLETE_MAIL(172), //给老客户端已完成的探险任务发送奖励邮件
    PROMOTION_ARENA(173),//晋级竞技场奖励
    APRIL_FOOL_DAY(174), //愚人节活动
    CASH_REWARD(175), //赠送的碎片
    NEWS_AWARD(176), // 新闻
    EASTER_EGG(177), //复活节砸蛋
    CROSS_ARENA(178),//跨服竞技场奖励
    SEVEN_DAY(179),//新手7天
    GOLD_ACTIVITY_EXCHANGE(180),//金币活动兑换活动
    GOLD_ACTIVITY_AWARD(181),//金币活动全服奖励
    GOLD_ACTIVITY_RAK(182),//金币活动排行榜
    COMEBACK_REWARD(183), //召回奖励
    COMEBACK_LOGIN_REWARD(184), //召回登录奖励
    COMEBACK_HEAP_PAY_REWARD(185), //召回累计充值奖励
    COMEBACK_ACTIVITY(186), //用户召回活动
    WORLD_SKIN(187), //世界地表皮肤激活发奖
    GOLD_LOTTERY(188), //黄金宝箱
    BIND_SHARE_REWARD(189), //各渠道第一次分享奖励
    GOODS_RECYCLE(190), //物品回收
    FIGHT_COMPENSATION(191), //守城失败补偿
    EXCHANGE_MULTI_GOLD(192), //多倍金币卡
    DRAGON_TASK(193),//龙任务

    NEWWORLD_FIGHT(195),//异世界战报
    NEWWORLD_REWARD(196),//异世界结算奖励
    RECALL_REWARD(197),//召回奖励
    RECALL_BUY(198),//召回兑换

    GOOGLE_CODE(199),//谷歌兑换码
    DAILY_PAY(200), //每日首充
    ELSA_TREASURE(201),//艾莎公主的宝藏（小累充）

    /*
    * 新增邮件来源(新增活动发邮件),需要在上面新增类型
    * 检查代码文件中,有无邮件发送type. 不是同种类型,则需要自己添加
     */
    ;


    private int value;

    MailSrcFuncType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
//
//    public static MailSrcFuncType get(int ordinal) { //这个数值必须是连续的
//        return values()[ordinal];
//    }
    public static MailSrcFuncType getByValue(int value) {
        MailSrcFuncType[] ges = values();
        for (MailSrcFuncType ge : ges) {
            if (ge.getValue() == value) {
                return ge;
            }
        }
        throw new IllegalArgumentException("MailSrcFuncType is not exists:" + value);
    }
}