package com.geng.puredb.model;

import com.geng.core.data.*;
import com.geng.db.MyBatisSessionUtil;
import com.geng.puredb.dao.UserItemMapper;
import com.geng.utils.GameService;
import com.geng.utils.LoggerUtil;
import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserItem implements Serializable {
    private String uuid;
    private String ownerId;
    private String itemId;
    private int count;
    private int value;
    private long vanishTime;
    //城建模式自动使用道具,临时值排序用,不需要持久化
    private int increaseValue;

    public static UserItem newInstance(String uid, String itemId, int count, int value) {
        UserItem item = newInstanceWithoutSave(uid, itemId, count, value);
        item.insert();
        return item;
    }

    public static UserItem newInstanceWithoutSave(String uid, String itemId, int count, int value) {
        UserItem item = new UserItem();
        item.setUuid(GameService.getGUID());
        item.setOwnerId(uid);
        item.setItemId(itemId);
        item.setCount(count);
        item.setValue(value);
        item.setVanishTime(item.configVanishTime());
        return item;
    }

    public static UserItem copyItem(String uid, String itemId, int count, int value, long vanishTime) {
        UserItem item = new UserItem();
        item.setUuid(GameService.getGUID());
        item.setOwnerId(uid);
        item.setItemId(itemId);
        item.setCount(count);
        item.setValue(value);
        item.setVanishTime(vanishTime);
        return item;
    }

    public Map<String, String> getItemMap() {
        return new GameConfigManager("goods").getItem(itemId.trim());
    }

    public void incrCount(int delta) {
        count += delta;
    }

    public void decrCount(int delta) {
        count -= delta;
        count = Math.max(0, count);
    }

    public boolean checkOverLap() {
        try{
            Map<String, String> itemConfig = new GameConfigManager("goods").getItem(itemId.trim());
            if(itemConfig.containsKey("overlap")){
                return !itemConfig.get("overlap").equals("1");
            }
        }catch (Exception e){
//            LoggerUtil.getInstance().recordException(e);
        }
        return true;
    }

    public boolean checkValid(){
        if(vanishTime > 0 && vanishTime < System.currentTimeMillis()){
            delete();
            return false;
        }
        return true;
    }

    public ISFSObject toSFSObject() {
        ISFSObject itemObj = new SFSObject();
        itemObj.putUtfString("uuid", uuid);
        itemObj.putUtfString("itemId", itemId);
        itemObj.putInt("count", count);
        itemObj.putLong("vanishTime", vanishTime);
        List<String> paraList = getParas();
        for (int i = 0; i < paraList.size(); i++) {
            if(!StringUtils.isBlank(paraList.get(i)))
                itemObj.putUtfString("para" + (i + 1), paraList.get(i));
        }
        return itemObj;
    }

    private List<String> getParas() {
        Map<String, String> itemConfig = new GameConfigManager("goods").getItem(itemId.trim());
        List<String> paraList = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            String para = "para" + i;
            if (itemConfig.containsKey(para)) {
                paraList.add(itemConfig.get(para));
            }
        }
        return paraList;
    }

    private long configVanishTime() {
        try{
            Map<String, String> itemConfig = new GameConfigManager("goods").getItem(itemId.trim());
            if(itemConfig.containsKey("vanishTime")){
                Long vanishTime = Integer.parseInt(itemConfig.get("vanishTime")) * 3600 * 1000L;
                return System.currentTimeMillis() +  vanishTime;
            }
        }catch (Exception e){
//            LoggerUtil.getInstance().recordException(e);
        }
        return 0;
    }

    public static List<UserItem> getItems(String uid) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
            return userItemMapper.select(uid);
        } finally {
            session.close();
        }
    }

    public static UserItem selectItem(String uid, String itemId, int value) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
            List<UserItem> itemList = userItemMapper.selectItemById(uid, itemId, value);
            if(itemList.size() > 0){
                return itemList.get(0);
            }
            return null;
        } finally {
            session.close();
        }
    }

    public static UserItem selectItem(String itemUUID) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
            return userItemMapper.selectItemByUUID(itemUUID);
        } finally {
            session.close();
        }
    }

    public static List<UserItem> getMutiItemByItemIds(String uid, List<String> itemIdList){
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
            return userItemMapper.selectItemListByItemIds(uid, itemIdList);
        } finally {
            session.close();
        }
    }

	public static List<UserItem> selectItemList(String[] itemUUIDs) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
            return userItemMapper.selectItemListByUUIDs(itemUUIDs);
        } finally {
            session.close();
        }
    }
	
    public static void insertBatch(List<UserItem> itemList, SqlSession session) {
        if (itemList == null || itemList.isEmpty()) {
            return;
        }
        session.getMapper(UserItemMapper.class).insertBatch(itemList);
    }

    public int insert() {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
            int ret = userItemMapper.insert(this);
            session.commit();
            return ret;
        } finally {
            session.close();
        }
    }

    public int update() {
        if(count <= 0) {
            return delete();
        }
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		int ret = 0;
        try {
            UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
            ret = userItemMapper.update(this);
			session.commit();
        } finally {
            session.close();
        }
		return ret;
    }

    public int update(SqlSession session) {
        if(count <= 0) {
            return delete(session);
        }
        UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
        return userItemMapper.update(this);
    }

    public int delete() {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
            int ret = userItemMapper.delete(uuid);
            session.commit();
            return ret;
        } finally {
            session.close();
        }
    }

    public int delete(SqlSession session) {
        UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
        int ret = userItemMapper.delete(uuid);
        return ret;
    }

    public static int deleteByOwnerIdAndItemId(SqlSession session, String ownerId, List<String> itemIdList){
        UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
        int ret = userItemMapper.deleteByOwnerIdAndItemId(ownerId,itemIdList);
        session.commit();
        return ret;
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
        this.ownerId = ownerId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setVanishTime(long vanishTime) {
        this.vanishTime = vanishTime;
    }

    public long getVanishTime() {
        return vanishTime;
    }

    public int getIncreaseValue() {
        return increaseValue;
    }

    public void setIncreaseValue(int increaseValue) {
        this.increaseValue = increaseValue;
    }
}