package com.geng.puredb.model;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.puredb.dao.QueueMapper;
import com.geng.utils.GameService;
import com.geng.utils.MyBatisSessionUtil;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.geng.utils.COKLoggerFactory.userLogger;

public class Queue implements Comparable<Queue>, Serializable {
    private String uuid;
    private String ownerId;
    private int qid;
    private int type;
    private String itemId;
	private long startTime;
    private long endTime;
    private volatile long updateTime;
    private int isHelped;
    private int para;
    private int status;
    private int farmerNum;

    private final static int DELAY_TIME = 5 * 1000;
    private final static long DEFAULT_UPDATE_TIME = Long.MAX_VALUE;

    public boolean isOutDate(long occupyTime) {
        return System.currentTimeMillis() + occupyTime > endTime;
    }

    public boolean isVaild(){
        return endTime == DEFAULT_UPDATE_TIME || endTime > System.currentTimeMillis();
    }

    public enum QueueType {
        BUILDING(0), FOOT_SOLDIER(1), WAR_FORT(2), HOSPITAL(3), AFFAIRS(4), WORLD(5), SCIENCE(6), ALLIANCE_AFFAIRS(7), RIDE_SOLDIER(8), BOW_SOLDIER(9), CAR_SOLDIER(10),
        EQUIP(11),PROP(12), MK_DRAGON(13), MK_DG_EGG(14),MISSILE(28),
        INFANTRY_2_SOLDIER(50), //浮空步兵
        CAVALRY_2_SOLDIER(51), //浮空骑兵
        ARCHER_2_SOLDIER(52), //浮空工兵
        MAGE_2_SOLDIER(53),   //浮空车兵
        SOUL_SOLDIER(54),
        HIDE_SOLDIER(55), //隐藏士兵
        ;

        QueueType(int type) {
            this.type = type;
        }

        private int type;

        public int getType() {
            return type;
        }

        public static QueueType get(int ordinal) {
            for (QueueType q : values())
            {
                if(q.getType()==ordinal)
                {
                    return q;
                }
            }
            return null;
        }
    }

    public enum QueueState {
        FREE, ING, OUT_SYN
    }

    public QueueState getState() {
        long currTime = System.currentTimeMillis() + DELAY_TIME;
        QueueState state;
        if (currTime < updateTime && updateTime != DEFAULT_UPDATE_TIME) {
            state = QueueState.ING;
        } else if (currTime >= updateTime) {//防止updateTime出现很大的情况（并发导致的问题，待检查）
            state = QueueState.OUT_SYN;
        } else {
            state = QueueState.FREE;
        }
        return state;
    }

    /*public ISFSObject release(UserProfile userProfile, boolean isLogin) {
        ISFSObject itemObj = null;
        boolean isDefault = true;
        QueueType qt = getQueueType();
        String finishedQueueItemId = null;
        ISFSObject effectObj=null;
        switch(qt) {
            case BUILDING:
                if(StringUtils.isNotBlank(itemId)) {
                    effectObj=userProfile.getUbManager().finishBuilding(itemId, updateTime);
                    if(isLogin){
                        userProfile.addGameLoginEvent(new FarmerLoginEvent(getUuid(),getFarmerNum()));
                    }else {
                        userProfile.getUserFarmerManager().setFarmerFree(getUuid(), null, getFarmerNum());//释放农民
                    }
                }
                break;
//                case DRAGON_BUILDING:
//                    if (StringUtils.isNotBlank(itemId)) {
//                        userProfile.getDragonBuildManager().finishBuilding(itemId, updateTime);
//                    }
//                    break;
            case FOOT_SOLDIER:
            case RIDE_SOLDIER:
            case BOW_SOLDIER:
            case CAR_SOLDIER:
            case WAR_FORT:
            case INFANTRY_2_SOLDIER:
            case CAVALRY_2_SOLDIER:
            case ARCHER_2_SOLDIER:
            case MAGE_2_SOLDIER:
            case MISSILE:
            case SOUL_SOLDIER:
                return itemObj;
            case HOSPITAL:
                if(StringUtils.isNotBlank(itemId)){
                    if (isLogin) {
                        userProfile.getArmyManager().setArmyRelatedInfo(UserArmyManager.ArmyLoginMission.armyRelatedQueueComplete, QueueType.HOSPITAL, itemId, updateTime);
                    } else {
                        userProfile.getArmyManager().armyRelatedQueueComplete(QueueType.HOSPITAL, itemId, updateTime, null);
                    }
//                    UserMakeResourceRecord.removeRecord(uuid);
                }
                break;
            case WORLD:
                if(StringUtils.isNotBlank(itemId)){
                    WorldMarch wm = userProfile.getUserWorld().getMarch(itemId);
                    if(wm != null && wm.getState() != WorldMarch.MarchState.OCCUPY.ordinal() && System.currentTimeMillis() < wm.getArrivalTime()) {
                        isDefault = false;
                        updateTime = wm.getArrivalTime();
                    }
                }
                break;
            case SCIENCE:
                if (isLogin) {
                    userProfile.addGameLoginEvent(new ScienceLoginEvent(getItemId()));
                } else {
                    ScienceService.clearCD(userProfile, getItemId(), null);
                }
                break;
            case EQUIP:
                //"1.0.85"
//                if (userProfile.isOlderThanVersion(Versions.VERSION_1_0_85)) {
//                    itemObj = EquipService.composeFinish(userProfile, getItemId());
//                    finishedQueueItemId = itemObj.getUtfString("itemId");
//                } else {
                    return itemObj;
//                }
//                break;
            case PROP:
                PropService.calNextMaterial(this);
                return itemObj;
            case MK_DG_EGG:
            case MK_DRAGON:
                return itemObj;
            case HIDE_SOLDIER:
                itemObj = SFSObject.newInstance();
                ISFSArray armyArr = userProfile.getArmyManager().backHidedArmy();
                itemObj.putSFSArray("soldiers", armyArr);
                break;
        }
        if(isDefault) {
            reset();
        } else {
            isHelped = 0;
            AllianceService.eraseHelp(uuid);
        }
        setFarmerNum(0);
        update(null);
        ISFSObject queueObj = toSFSObject(itemObj == null ? SFSObject.newInstance() : itemObj);
        if(effectObj!=null)
        {
            queueObj.putSFSObject("effect",effectObj);
        }
        if (finishedQueueItemId != null) {
            LoginFinishedQueue finishedQueue = new LoginFinishedQueue();
            finishedQueue.setQueueUuid(uuid);
            finishedQueue.setItemId(finishedQueueItemId);
            userProfile.addLoginFinishedQueue(finishedQueue);
        }
        return queueObj;
    }

    public ISFSObject cancel(UserProfile userProfile) throws COKException {
        if (getState() != Queue.QueueState.ING) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "no queue is not using");
        }
        QueueType qt = getQueueType();
        ISFSObject retObj = null;
        switch(qt) {
            case BUILDING:
                retObj = userProfile.getUbManager().cancelBuilding(itemId);
                userProfile.getUserFarmerManager().setFarmerFree(uuid, null,getFarmerNum());
                break;
			case SCIENCE:
				retObj = ScienceService.cancelResearch(userProfile, itemId, false,getQid());
				break;
			case FOOT_SOLDIER:
			case RIDE_SOLDIER:
			case BOW_SOLDIER:
			case CAR_SOLDIER:
            case INFANTRY_2_SOLDIER:
            case CAVALRY_2_SOLDIER:
            case ARCHER_2_SOLDIER:
            case MAGE_2_SOLDIER:
            case SOUL_SOLDIER:
				retObj = userProfile.getArmyManager().cancelAdd(itemId, uuid, false);
				break;
			case WAR_FORT:
				retObj =  userProfile.getArmyManager().getWallManager().cancelBuild(itemId, uuid, false);
				break;
			case HOSPITAL:
				retObj = userProfile.getArmyManager().getHospitalManager().cancelHeal(uuid, false);
				break;
            case MISSILE:
                retObj = userProfile.getArmyManager().getMissileManager().cancelBuild(itemId, uuid, false);
                break;
            case HIDE_SOLDIER:
                userProfile.getArmyManager().backHide(uuid, false);
                break;
        }
        reset();
		update(null);
        return retObj;
    }

    public synchronized void reset() {
        AllianceService.eraseHelp(uuid);
        isHelped = 0;
        itemId = null;
        startTime = DEFAULT_UPDATE_TIME;
        updateTime = DEFAULT_UPDATE_TIME;
        para = 0;
        farmerNum=0;
    }

    *//**
     * 空的永久队列：
     * @param ownerId
     * @param qid
     * @param type
     * @return
     */
    public static Queue newDefaultInstance(String ownerId, int qid, int type,int farmernum) {
        Queue q = new Queue();
        q.setUuid(GameService.getGUID());
        q.setOwnerId(ownerId);
        q.setQid(qid);
        q.setType(type);
		q.setStartTime(DEFAULT_UPDATE_TIME);
        q.setEndTime(DEFAULT_UPDATE_TIME);
        q.setUpdateTime(DEFAULT_UPDATE_TIME);
        q.setPara(0);
        q.setFarmerNum(farmernum);

        userLogger.info("farmerqueue |{} | {} | {} ", new Object[]{q.getUuid(),ownerId,farmernum});

        return q;
    }

    public static Queue newInstance(String ownerId, int qid, int type, long time,int farmernum) {
        Queue q = newDefaultInstance(ownerId, qid, type,farmernum);
        q.setEndTime(System.currentTimeMillis() + time);
        q.insert();
        return q;
    }

    public void occupy(String itemId, long updateTime) {
		this.startTime = System.currentTimeMillis();
        this.itemId = itemId;
        this.updateTime = updateTime;
        update(null);
    }

    public void occupy(String itemId, long updateTime, SqlSession session) {
        this.startTime = System.currentTimeMillis();
        this.itemId = itemId;
        this.updateTime = updateTime;
        update(session);
    }


/*
    public ISFSObject toSFSObject(UserProfile userProfile) {
        ISFSObject qObj = toSimpleSFSObj();
        ISFSObject itemObj = new SFSObject();
        if(!StringUtils.isBlank(itemId)) {
            QueueType qt = getQueueType();
            switch(qt) {
                case BUILDING:
                    UserBuilding building = userProfile.getUbManager().getUserBulidingByUuid(itemId);
                    if(building == null){
                        userLogger.info("building is empty in queue ,do reset |{} | {} | {} ", new Object[]{userProfile.getUid(),itemId, qObj.toJson()});
                        this.reset();
                        update(null);
                        qObj = toSimpleSFSObj();
                        break;
                    }
                    itemObj = userProfile.getUbManager().getQueueInfo(itemId);
                    break;
//                case DRAGON_BUILDING:
//                    itemObj = userProfile.getDragonBuildManager().getQueueInfo(itemId);
//                    break;
                case EQUIP:
                    UserEquip composeEquip = UserEquip.getWithUuid(itemId);
                    itemObj = composeEquip.toQueueObj();
                    break;
                case PROP:
                    String retId = "";
                    if (StringUtils.isNotBlank(itemId)) {
                        String idArr[] = StringUtils.split(itemId, ';');
                        if (para < idArr.length) {
                            retId = idArr[para];
                        } else {
                            retId = idArr[0];
                        }
                    }
                    itemObj.putUtfString("itemId", retId);
                    itemObj.putInt("para", para);
                    itemObj.putUtfString("allId", itemId);
                    break;
                default:
					itemObj = new SFSObject();
					itemObj.putUtfString("itemId", itemId);
					break;
			}
        }
        qObj.putSFSObject("itemObj", itemObj);
        qObj.putInt("isHelped", isHelped);
        return qObj;
    }*/

    public ISFSObject toSFSObject(ISFSObject itemObj) {
        ISFSObject qObj = toSimpleSFSObj();
        qObj.putSFSObject("itemObj", itemObj);
        return qObj;
    }

    public ISFSObject toSimpleSFSObj() {
        ISFSObject qObj = new SFSObject();
        qObj.putUtfString("uuid", uuid);
        qObj.putInt("qid", qid);
        qObj.putInt("type", type);
        qObj.putSFSObject("itemObj", new SFSObject());
        qObj.putLong("startTime", startTime == DEFAULT_UPDATE_TIME ? 0 : startTime);
        qObj.putLong("endTime", endTime == DEFAULT_UPDATE_TIME ? 0 : endTime);
        qObj.putLong("updateTime", updateTime == DEFAULT_UPDATE_TIME ? 0 : updateTime);
        return qObj;
    }

    public static List<Queue> getWithUid(String uid) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            QueueMapper queueDao = session.getMapper(QueueMapper.class);
            return queueDao.select(uid);
        } finally {
            session.close();
        }
    }

    public int insert() {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        int ret = 0;
        try {
            QueueMapper queueDao = session.getMapper(QueueMapper.class);
            ret = queueDao.insert(this);
            session.commit();
        } finally {
            session.close();
        }
        return ret;
    }

    public void update(SqlSession session) {
		boolean isClose = false;
        if (session == null) {
            session = MyBatisSessionUtil.getInstance().getSession();
			isClose = true;
        }
        try{
            session.getMapper(QueueMapper.class).update(this);
			if(isClose) {
				session.commit();
			}
        }finally{
			if (isClose) {
				session.close();
			}
        }
    }

	public void delete() {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			session.getMapper(QueueMapper.class).delete(uuid);
			session.commit();
		} finally {
			session.close();
		}
	}

	public static void deleteByQidType(int qid, int type) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			Map<String, Integer> paramMap = new HashMap<>();
			paramMap.put("qid", qid);
			paramMap.put("type", type);
			session.getMapper(QueueMapper.class).deleteByQidType(paramMap);
			session.commit();
		} finally {
			session.close();
		}
	}

    @Override
    public int compareTo(Queue o) {
        return qid > o.getQid() ? 1 : (qid == o.getQid() ? 0 : -1);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId == null ? null : ownerId.intern();
    }

    public int getQid() {
        return qid;
    }

    public void setQid(int qid) {
        this.qid = qid;
    }

    public int getType() {
        return type;
    }

    public QueueType getQueueType() {
        return QueueType.get(type);
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getFarmerNum() {
        return farmerNum;
    }

    public void setFarmerNum(int farmerNum) {
        this.farmerNum = farmerNum;
    }

    public synchronized void decrUpdateTime(int delta) {
        if(updateTime != DEFAULT_UPDATE_TIME)
            updateTime -= delta;
    }

    public long incrEndTime(long para2) {
        long currTime = System.currentTimeMillis();
        if(currTime < endTime) {
            endTime += para2;
        } else {
            endTime = currTime + para2;
        }
        return endTime;
    }

    public int getIsHelped() {
        return isHelped;
    }

    public void setIsHelped(int isHelped) {
        this.isHelped = isHelped;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPara() {
        return para;
    }

    public void setPara(int para) {
        this.para = para;
    }


}
