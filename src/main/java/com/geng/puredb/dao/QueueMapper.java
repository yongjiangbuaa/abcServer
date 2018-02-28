package com.geng.puredb.dao;

import com.geng.puredb.model.Queue;

import java.util.List;
import java.util.Map;

public interface QueueMapper {
    List<Queue> select(String ownerId);
    int insert(Queue record);
    int insertBatch(List<Queue> recordList);
    int update(Queue record);
	void delete(String uuid);

    /**
     * 删除用户队列，除了建筑
     * @param uid
     * @return
     */
    int delUserQueues(String uid);
	void deleteByQidType(Map<String, Integer> paramMap);
}
