/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月4日 下午3:16:11
 */
package com.geng.core;

import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.exceptions.ExceptionMonitorType;
import com.geng.exceptions.IllegalXMLConfigException;
import com.geng.gameengine.cross.SharedUserInfo;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.*;
import com.geng.utils.properties.PropertyFileReader;
import com.geng.utils.xml.GameConfigManager;
import com.google.common.base.Optional;
import com.google.common.cache.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

import static com.geng.utils.COKLoggerFactory.userLogger;

/**
 * 游戏中心
 */
public class GameEngine {
    private static final Logger logger = LoggerFactory.getLogger(GameEngine.class);
    private static final ThreadLocalStringBuilder threadLocalStringBuilderHolder = new ThreadLocalStringBuilder(1024);
    public static int ZONE_ID = 1;
    private static GameEngine instance = new GameEngine();
//    private COKExtension cokExtension;
    private LoadingCache<String, UserProfile> userCache;
    private volatile CokZoneState serverState;
    private volatile GenericXmlApplicationContext context;
    private GenericXmlApplicationContext serverContext;
    private Object refreshContextLock = new Object();
    private long serverStopTime = 0;
    private long serverStartTime = 0;
	private Map<Integer, Integer> serverTypeMap = new HashMap<>();
    private Map<Integer, Integer> pfServerMap = new HashMap<>();//特殊渠道的服务器列表
    private boolean isNewGameEngine = false;
	private Properties configProperties = null;

    private boolean isDevTestOpen=false;

    public static String PUSH_TIP_PROTOL = "push.tip";

	private GenericXmlApplicationContext dalServerContext;

    public enum CokZoneState {
        INIT, RUNNING, SHUTDOWN
    }

    public enum ServerType {
        NORMAL //普通服
        ,TEST //测试服
        ,ANCIENT_BATTLE_FIELD //远古战场服务器
        ,DRAGON_BATTLE //巨龙战役服务器
        , ANCIENT_BATTLE_FIELD_TEST //远古战场服务器测试
        , DRAGON_BATTLE_TEST //巨龙战役服务器测试
        , INNER_TEST //内网测试服
        , DRAGON_PLAYOFF // 7 巨龙季后赛服务器
        , DRAGON_PLAYOFF_TEST // 8 巨龙季后赛测试服
    }

    private GameEngine() {
    }

    public boolean isNewGameEngine() {
        return isNewGameEngine;
    }
    public boolean isDevTestOpen() { return isDevTestOpen; }

    public void init() {
        serverState = CokZoneState.INIT;
        isNewGameEngine = this.testIsNewEngine();
        if(isNewGameEngine){
            logger.info("running under new Game Engine");
        }
        else{
//            Runtime.getRuntime().addShutdownHook(new ABCShutdownHook());
        }
//        R.Local().set(Constants.SERVER_STATE_FLAG, "3");//设置服务器为启动状态
//		setUserCache();
		initConfig();
		GameConfigManager.init();
//		ScribeLogUtil.getInstance().start();
//		PowerLogUtil.getInstance().start();//
		initDBManager();
		initServerTypeMap();
		initSpringContext();
		initGameDate();


        serverState = CokZoneState.RUNNING;
        serverStartTime = System.currentTimeMillis();
//        cokExtension.getParentZone().setProperty("proxy", new WebProxyImpl());

//		R.Local().set(Constants.SERVER_STATE_FLAG, "0");
//		R.Local().del(Constants.SERVER_STATE_START_TIME);
        logger.info("Game engine init completed");
    }

    public static void main(String[] args){
		GameEngine.getInstance().initService();
	}

    public void initService(){
		ZONE_ID = 1;

		initDALService();
		MyBatisSessionUtil.getInstance().init();
		MyBatisSessionUtil.getInstance().setSessionAutoCommitDefault(true);
	}

	private void initDALService(){
		Resource serverResource = new ClassPathResource("DALService.xml");
		dalServerContext = new GenericXmlApplicationContext(serverResource);
	}

    private boolean testIsNewEngine(){
        try {
            Class.forName("com.gengserver.COKServer");
        } catch (ClassNotFoundException e) {
            return  false;
        }
        return  true;
    }

    public void pushMsg(String pushCMD, ISFSObject paramObj, UserProfile profile){ //TODO 基于短连接的pushMsg
    }

//    public void pushMsg(String pushCMD, ISFSObject paramObj, User user) {//TODO 基于短连接的pushMsg

//    }

    public void pushMsgToRemoteUser(int server, String toUser, String pushCMD, String pushJson) {

    }

	public void pushMsgToRemoteUsers(int server, Set<String> users, String pushCMD, String pushJson) {

    }
	
    public void pushMsgToRemoteUserGroupByUids(int server, List<String> toUsers, String pushCMD, String pushJson) {


    }

    public void pushMsgToRemoteUserGroupByShareds(int server, List<SharedUserInfo> toUsers, String pushCMD, String pushJson) {


    }

    public void pushMsgToRemoteAlliance(int server, String toAlliance, String pushCMD, String pushJson) {

    }

    public void pushMsgToRemoteRoom(int server, String roomId, String pushCMD, String pushJson) {

    }

    public void pushMsgByJson(String toUser, String pushCmd, String pushJson) {

    }

    public void pushGroupMsgByJson(Set<String> users, String pushCmd, String pushJson) {

    }

    public void pushAllPlayersMsg(String pushCMD, ISFSObject pObj) {

    }

//    public Collection<User> getOnlineUsers() {
//        return cokExtension.getParentZone().getUserList();
//    }
    public void pushAllPlayersMsg(String pushCMD, ISFSObject pObj, String needVersion) {

    }

    public void pushSingleWorldAllPlayersMsg(String pushCMD, ISFSObject pObj, int worldId) {

    }
/*    public void pushGroupMsg(String msg, ISFSObject paramObj, Set<User> users) {

    }

    public void pushGroupMsg(String pushCMD, ISFSObject paramObj, List<User> users) {


    }

    public void pushGroupMsg(String pushCMD,ISFSObject paramObj,String uids){

    }
	private void pushGroupMsg(String pushCMD, ISFSObject paramObj, List<User> users, boolean isLog){

    }*/
    public void pushCommonNotice(String msgId, ISFSArray paraArray) {

    }
    public void pushGlobalCommonNotice(String msgId,ISFSArray paraArray){

    }
    public void sendPublicMessage(String msg, ISFSObject param) {

    }

    public ConcurrentMap<String,UserProfile> getUserCacheMap(){
        return  userCache.asMap();
    }


    public void addUserProfile(UserProfile userProfile) {
//        userCache.put(userProfile.getUid(), userProfile);
//        ActorRef actorRef = createActor(PlayerActor.class, userProfile.getUid());
//        userProfile.setActorRef(actorRef);
    }

    /**
     * 只获取userprofile的信息
     * @param uid
     * @return
     */
    public UserProfile getOnlyUserProfiele(String uid){
        UserProfile res = getPresentUserProfile(uid);
        if (res == null){
            res = UserProfile.getWithUid(uid);
        }

        return res;
    }

    /**
     * 获取在线用户
     *
     * @param uid
     * @return
     */
    public UserProfile getPresentUserProfile(String uid) {
        if (userCache == null) return null;
        return userCache.getIfPresent(uid);
    }
/*
    public UserProfile getPresentUserProfile(User user) {
        if (user == null || user.getProperty("uid") == null) {
            return null;
        }
        if (userCache == null) return null;
        String uid = (String) user.getProperty("uid");
        return userCache.getIfPresent(uid);
    }

    public User getUserByUid(String uid) {
        UserProfile userProfile = getPresentUserProfile(uid);
        User user = null;
        if (userProfile != null) {
            user = userProfile.getSfsUser();
        }
        return user;
    }*/

    public UserProfile getUserProfile(String uid) {
        UserProfile userProfile = null;
        try {
            if (uid != null) {
                userProfile = userCache.get(uid);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause != null) {
                LoggerUtil.getInstance().recordException(cause);
            }
            LoggerUtil.getInstance().recordException(e);
        }
        return userProfile;
    }
/*

    public UserProfile getUserProfile(User user) {
        if (user.getProperty("uid") == null) {
            return null;
        }
        UserProfile userProfile = getUserProfile((String) user.getProperty("uid"));
        if (userProfile != null && userProfile.getSfsUser() == null) {
            userProfile.setSfsUser(user);
        }
        return userProfile;
    }

    public int getCurrZoneUserCount() {
        return cokExtension.getParentZone().getUserCount();
    }
*/

    /**
     * 返回开服时间
     *
     * @return
     */
    public static long getServerOpenTimeStamp() {
        String openTime = new GameConfigManager("servers").getItem(Integer.toString(Constants.SERVER_ID)).get("open_time");
        return DateUtils.parseFullTimeStr(openTime);
    }

    public static long getYangFuTimeStamp(){
        long yangFuTime = 0L;
        long retTime = 0;
        ISFSArray serverInfoArray = SFSMysql.getInstance().query("SELECT yangfu FROM server_info WHERE uid = 'server'");
        if (serverInfoArray != null && serverInfoArray.size() > 0) {
            yangFuTime = serverInfoArray.getSFSObject(0).getLong("yangfu");

        }
        long serverOpenTime = GameEngine.getServerOpenTimeStamp();
        if (0 == yangFuTime) {
            yangFuTime = serverOpenTime;
        } else {
            long now = System.currentTimeMillis();
            if (now >= serverOpenTime && now < yangFuTime) {
                yangFuTime = now;
            }
        }
        return yangFuTime;
    }

    public static List<HashMap<String, String>> getOpenServers(){
        Map<String,String> condition= new HashMap<>();
        condition.put("test", "false");
        List<HashMap<String, String>> list= new GameConfigManager("servers").selectItems(condition);
        return list;
    }

    public void removeUserProfile(String uid) {
        userCache.invalidate(uid);
    }

    public static GameEngine getInstance() {
        return instance;
    }

    public void saveAndClean() {
        //TODO logic
    }

    public void shutdownCOKServer() {

    }

    private void closeSpringContext() {
        if(serverContext != null){
            serverContext.close();
        }
        if(context != null){
            context.close();
        }
        if (dalServerContext != null){
        	dalServerContext.close();
		}
    }

    private void doDisconnectEvent() {
        ExecutorService eventThreadPool = COKExecutors.newFixedThreadPool(
                PropertyFileReader.getIntItem("shutdown_disconnect_thread", "8"),
                new ThreadFactoryBuilder().setNameFormat("DisconnectEventThread-%d").build());
        long curr = System.currentTimeMillis();
        logger.info("shut down stat userProfile count is {} ", userCache.size());
        CountDownLatch latch = new CountDownLatch((int) userCache.size());
        for (UserProfile userProfile : userCache.asMap().values()) {
//            try {
//                userProfile.onDisconnectEvent();
//            } catch (Exception e) {
//                LoggerUtil.getInstance().recordException(e);
//            }
//            eventThreadPool.execute(new UserDisconnectTask(userProfile,latch));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        eventThreadPool.shutdown();
        logger.info("shutdown stat userProfile.onDisconnectEvent() {} s", (System.currentTimeMillis() - curr) / 1000);
    }

    public boolean isIniting() {
        return serverState == CokZoneState.INIT;
    }

    public boolean isRunning() {
        return serverState == CokZoneState.RUNNING;
    }

    public boolean isShutdown() {
        return serverState == CokZoneState.SHUTDOWN;
    }

/*    public WorldService getRemoteWorldService(int serverId) {
        String beanName = Constants.REMOTE_WORLD_SERVICE + serverId;
        if (null != context && context.containsBean(beanName)) {
            return (WorldService) context.getBean(beanName);
        } else {
            synchronized (refreshContextLock) {
                if(!context.containsBean(beanName)) {
                    refreshSpringContext(serverId);
                }
            }
            return (WorldService) context.getBean(beanName);
        }
    }*/

    public void refreshSpringContext(int serverId) {
        try {
            initSpringContext();
            logger.info("refresh {}, rmi successfully", serverId);
        } catch (Exception e) {
            COKLoggerFactory.monitorException(String.format("[refresh rmi] add new server %d", serverId), ExceptionMonitorType.OTHER, COKLoggerFactory.ExceptionOwner.COMMON, e);
        }
    }

    public void clearContext() {
        context.close();
        this.context = null;
    }

    public boolean isUserOnline(String uid) {
        return false;
    }

/*
    public static boolean isUserLogined(User user) {

    }
*/

	/**
	 * 从Global数据库初始化serverTypeMap
	 */
	private void initServerTypeMap() {
		serverTypeMap.clear();
		String sql = "select id, type from server_info";
		ISFSArray retArray = SFSMysql.getInstance().queryGlobal(sql, new Object[]{});
		if (retArray != null && retArray.size() > 0) {
			for (int i = 0; i < retArray.size(); i++) {
				serverTypeMap.put(retArray.getSFSObject(i).getInt("id"), retArray.getSFSObject(i).getInt("type"));
			}
		}

        /**
         * 特殊渠道的服务器列表
         */
        String serverStr = GameConfigManager.getString("pf_server", "pf_servers", "list");
        if (!StringUtils.isBlank(serverStr)) {
            String[] serverStrArr = StringUtils.split(serverStr, ";");
            for (String str : serverStrArr) {
                String[] serverArr = StringUtils.split(str, "-");
                if (serverArr.length == 1) {
                    pfServerMap.put(Integer.parseInt(serverArr[0]), 1);
                }else if (serverArr.length == 2) {
                    int begin = Integer.parseInt(serverArr[0]);
                    int end = Integer.parseInt(serverArr[1]);
                    for (int i = begin; i <= end; i++) {
                        pfServerMap.put(i, 1);
                    }
                }
            }
        }
    }

    public boolean isPfServer() {
        return isPfServer(Constants.SERVER_ID);
    }

    public boolean isPfServer(int serverId) {
        Integer pfValue = pfServerMap.get(serverId);
        if (pfValue == null) {
            return false;
        }

        return pfValue == 1;
    }

    /**
     * 特殊渠道服务器和其他服务器隔离开
     * @param dstServerId
     * @return
     */
    public boolean isSamePfServer(Integer dstServerId) {
        if(dstServerId == null){
            return true;
        }

        boolean isPfServer = isPfServer();
        boolean serverIsPf = isPfServer(dstServerId);
        if (isPfServer ^ serverIsPf) {//异或操作(特殊渠道服务器和其他服务器隔离开)
            return false;
        }
        return true;
    }

    public boolean isTestServer() {
        return isTestServer(ZONE_ID);
    }

	public boolean isTestServer(int serverId) {
		Integer serverType = this.serverTypeMap.get(serverId);
		if (serverType == null) {
			return false;
		}
		return serverType == ServerType.TEST.ordinal()
				|| serverType == ServerType.INNER_TEST.ordinal()
				|| serverType == ServerType.ANCIENT_BATTLE_FIELD_TEST.ordinal()
				|| serverType == ServerType.DRAGON_BATTLE_TEST.ordinal()
				|| serverType == ServerType.DRAGON_PLAYOFF_TEST.ordinal();
	}

	public int getServerType4Judge(int serverId) {
		Integer serverType = this.serverTypeMap.get(serverId);
		if (serverType == null) {
			return 0;
		}
		return serverType;
	}

    /**
     * 是否是跨服战的中立服
     * @return
     */
    public boolean isCrossFightServer() {
        return ZONE_ID > 7000;//TODO 巨龙战役测试
    }

    public boolean isAncientBattlefieldServer(){
        //return ZONE_ID > 7000 && ZONE_ID <= 8000;
        return ZONE_ID > 90000;
    }

    public boolean isDragonBattleServer(){
        return (ZONE_ID > 8000 && ZONE_ID <= 9000);//TODO 巨龙战役测试
    }


    public int getServerType(){
        int retServerType;
        if(isAncientBattlefieldServer()){
            retServerType = ServerType.ANCIENT_BATTLE_FIELD.ordinal();
        }else if(isDragonBattleServer()){
            retServerType = ServerType.DRAGON_BATTLE.ordinal();
        }else if(isTestServer()){
            retServerType = ServerType.TEST.ordinal();
        }else {
            retServerType = ServerType.NORMAL.ordinal();
        }
        return retServerType;
    }
	public boolean isOutTest() {
		return getServerType4Judge(ZONE_ID) == ServerType.TEST.ordinal();
	}

    public void readyStopServer(long delayTime) {
        if (delayTime == 0) {
            serverStopTime = 0;
        } else {
            serverStopTime = System.currentTimeMillis() + delayTime;
        }
        ISFSObject obj = new SFSObject();
        obj.putLong("time", serverStopTime);
//        pushAllPlayersMsg(ConstantsPushDefine.PUSH_SERVER_TOSTOP, obj);
    }


    /**
     * 获取配置信息
     *
     * @param key
     * @return
     */
    public Optional<String> getConfigValue(String key) {
        String configValue;
            configValue = getConfigProperties().getProperty(key);
        return Optional.fromNullable(configValue);
    }

    public String getConfigFileName() {
        String configFileName = String.format("extensions/COK%s/config.properties",Constants.SERVER_ID_STR);
//        if (cokExtension != null) {
//            configFileName = cokExtension.getPropertiesFileName();
//        }
        return configFileName;
    }

    public Properties getConfigProperties() {
        return com.geng.server.GameEngine.getInstance().getConfigProperties();
    }

    /**
     * user cache统计信息
     *
     * @return
     */
    public String toUserCacheStats() {
        return userCache.stats().toString();
    }

    public long userCacheSize(){
        return userCache.size();
    }


    private void setUserCache() {
        userCache = CacheBuilder.newBuilder()
                .maximumSize(PropertyFileReader.getIntItem("cache_max_size", 25000))
                .expireAfterAccess(PropertyFileReader.getIntItem("cache_expire_after_access", 10), TimeUnit.MINUTES)
                .expireAfterWrite(PropertyFileReader.getIntItem("cache_expire_after_write", 10), TimeUnit.MINUTES)
                .removalListener(new RemovalListener<String, UserProfile>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, UserProfile> notification) {
                        UserProfile userProfile = notification.getValue();
                        if (userProfile != null) {
                            userLogger.info("user profile has removed. uid {}, name {}", notification.getKey(), userProfile.getName());
                        }
                    }
                })
                .recordStats()
                .build(new CacheLoader<String, UserProfile>() {
                    @Override
                    public UserProfile load(String uid) {
                        try {
                            UserProfile userProfile = null;//UserProfile.getLoggedUserProfile(uid, false);
//                            ActorRef actorRef = createActor(PlayerActor.class, uid);
//                            userProfile.setActorRef(actorRef);
                            return userProfile;
                        } catch (Exception e) {
                            userLogger.error("load user profile error", e);
                            return null;
                        }
                    }
                });
    }

    private void initDBManager() {
        SFSMysql.getInstance().init(new Zone());
        MyBatisSessionUtil.getInstance().init();
    }

    private void initGameDate() {
        CommonUtils.loadBadWordsList();
//        CommonUtils.loadBadNameWorldsList();

    }



    private void initSpringContext() {
        ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader extensionLoader = getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(extensionLoader);
//        String clientConfigFile = String.format("%s/rpcClient.xml", Constants.GAME_CONFIG_PATH);
//        File clientFile = new File(clientConfigFile);
//        // 首先检测带ServerId的配置文件, 这个是为了配合同一台物理服务器上面,同时共存多个区的情况
//        File serverFile = new File(String.format("%s/rpcServer%s.xml", Constants.GAME_CONFIG_PATH, Constants.SERVER_ID_STR));
//        String serverConfigFile = "/rpcServer.xml";
//        if (!clientFile.exists() && StringUtils.isBlank(getConfigProperties().getProperty("rpc_client_url"))) {
//            // 没有新的rpc模式的配置文件,使用旧的配置文件
//            serverConfigFile = String.format("%s/rmiServer%s.xml", Constants.GAME_CONFIG_PATH, getZoneIdStr());
//            clientConfigFile = String.format("%s/rmiClient.xml", Constants.GAME_CONFIG_PATH);
//        } else {
//            if (serverFile.exists()) {
//                serverConfigFile = serverFile.getPath();
//            }else {
//                // 再检测是否存在不带Server ID的通用配置文件
//                serverFile = new File(String.format("%s/rpcServer.xml", Constants.GAME_CONFIG_PATH));
//                if(serverFile.exists()) {
//                    serverConfigFile = serverFile.getPath();
//                }
//            }
//        }
//        if(serverContext == null){
//            logger.info("init spring server context with config {}", serverConfigFile);
//            Resource serverResource = new ClassPathResource(serverConfigFile, extensionLoader);
//            serverContext = new GenericXmlApplicationContext(serverResource);
//        }
//        initSpringClientContext(extensionLoader, clientConfigFile);
        Thread.currentThread().setContextClassLoader(origLoader);
    }

    private void initSpringClientContext(ClassLoader extensionLoader, String clientConfigFile) {
        // 如果在系统文件里面配置rpc client url的配置,那么尝试使用该配置
        String clientUrl = getConfigProperties().getProperty("rpc_client_url");

        Resource clientResource;
        if(StringUtils.isNotBlank(clientUrl)){
            logger.info("rpc client config by url {}", clientUrl);
            try {
                clientResource = new UrlResource(clientUrl);
            } catch (MalformedURLException e) {
                logger.error("rpc client url error");
                throw new IllegalXMLConfigException("rpc client url config error");
            }
        }else {
            logger.info("init spring client context with config file {}", clientConfigFile);
            clientResource = new ClassPathResource(clientConfigFile, extensionLoader);
        }
        GenericXmlApplicationContext newContext = new GenericXmlApplicationContext(clientResource);
        if(context != null){
            context.close();
        }
        context = newContext;
    }

    private String getZoneIdStr() {
        return ZONE_ID == 0 ? "" : String.valueOf(ZONE_ID);
    }



    public long getServerStopTime() {
        return serverStopTime;
    }

    public boolean isInServerProtectTime() {
        if(isShutdown()){
            return true;
        }
        if(isIniting()){
            return true;
        }
        return false;
    }
	public LoadingCache<String, UserProfile> getUserCache() {
        return userCache;
    }


    private boolean isScoreSwitchOn(){
        String value = new GameConfigManager("item").getItem("function_on11").get("k1").trim();
        return value.equals("1");
    }

    private boolean isAlScoreSwitchOn(){
        String value = new GameConfigManager("item").getItem("function_on_alliance_score_activity").get("k1").trim();
        return value.equals("1");
    }

    /**
     * 读取一些额外的配置
     */
    private void  initConfig(){
        // -Ddev.test=true
        String isDevTest=System.getProperty("dev.test", "false");
        this.isDevTestOpen=Boolean.parseBoolean(isDevTest);
    }

    //确保参数的顺序与展示dialog里面参数的顺序一致
    /*public void pushMsgWithDialog(List<String> paramList, String dialogId, String cmd, PublicPush.PushType pushType, UserProfile userProfile) {
        if(paramList == null || paramList.size() == 0) {
            return;
        }

        ISFSObject pushObj = SFSObject.newInstance();
        ISFSArray paramArr = SFSArray.newInstance();
        for(String paramStr : paramList) {
            ISFSObject param = SFSObject.newInstance();
            String[] params = StringUtils.split(paramStr, "=");

            param.putInt("type", Integer.valueOf(params[0]));
            param.putUtfString("value", params[1]);

            paramArr.addSFSObject(param);
        }

        pushObj.putUtfString("dialog", dialogId);

        pushObj.putSFSArray("params", paramArr);

        if(PublicPush.PushType.ALL_SERVER == pushType) { //全服推送
            if(Versions.Compare(userProfile.getAppVersion(), Versions.VERSION_2_1_6) < 0) {
                pushObj.putUtfString("cmd", cmd);
                R.Global().publish("AllServerPushChannel", pushObj.toJson());
            } else {
                GameEngine.getInstance().sendMsgWithDialog(pushObj, ChatService.PostType.PUBLIC_MESSAGE_ALL_SERVER_PUSH, userProfile);
            }
        } else if(PublicPush.PushType.LOCAL_SERVER ==  pushType) { //本服推送
            pushAllPlayersMsg(cmd, pushObj);
        } else if(PublicPush.PushType.PERSON == pushType) { //推送个人
            pushMsg(cmd, pushObj, userProfile);
        }

    }*/

    //确保参数的顺序与展示dialog里面参数的顺序一致
/*    public void sendMsgWithDialog(ISFSObject msgObj, ChatService.PostType postType, UserProfile userProfile) {
        ISFSObject paramObj = SFSObject.newInstance();
        paramObj.putInt("type", ChatService.ChatType.NOTICE.ordinal());
        paramObj.putInt("post", postType.getValue());
        paramObj.putUtfString("uid", userProfile.getUid());
        paramObj.putUtfString("msg", msgObj.toJson());
        paramObj.putLong("time", System.currentTimeMillis());

        paramObj.putUtfString("senderName", userProfile.getName());
        paramObj.putUtfString("senderPic", userProfile.getPic());
        paramObj.putInt("senderPicVer", userProfile.getPicVer());
        paramObj.putUtfString("userLang", userProfile.getLang());
        paramObj.putUtfString("country", userProfile.getStatReg().getCountry());
        paramObj.putUtfString("pf", userProfile.getPf());
        paramObj.putUtfString("appversion", userProfile.getAppVersion());
        paramObj.putInt("srcserver", userProfile.getCrossFightSrcServerId());
        paramObj.putUtfString("sid", String.valueOf(userProfile.getServerId()));

        R.Global().publish("AllServerTrumpetMessageChannel", paramObj.toJson()); //推送信息到全服

    }*/
}
