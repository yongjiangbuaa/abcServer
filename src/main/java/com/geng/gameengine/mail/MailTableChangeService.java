package com.geng.gameengine.mail;

import com.geng.puredb.dao.MailGroupMapper;
import com.geng.puredb.dao.MailMapper;
import com.geng.puredb.dao.OldmailinsertMapper;
import com.geng.puredb.model.Mail;
import com.geng.puredb.model.MailGroup;
import com.geng.puredb.model.OldmailinsertKey;
import com.geng.utils.MyBatisSessionUtil;
import com.geng.utils.SFSMysql;
import com.geng.core.data.ISFSArray;
import org.apache.ibatis.session.SqlSession;

import java.util.LinkedList;
import java.util.List;

import static com.geng.utils.COKLoggerFactory.exceptionLogger;

/**
 * Created by Administrator on 2015/2/12.
 */
public class MailTableChangeService {

	private static boolean isOldMailInserted(String uid) {
		ISFSArray results = SFSMysql.getInstance().query("SELECT * FROM oldMailInsert WHERE uid = ? AND type = 0", new Object[]{uid});
		if (results == null || results.size() < 1) {
			return false;
		} else {
			return true;
		}
	}

	private static void insertOldMail(String uid) {
		SqlSession session = MyBatisSessionUtil.getInstance().getBatchSession();
		try {
			MailGroupMapper groupMapper = session.getMapper(MailGroupMapper.class);
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			List<Mail> oldMailList = mailMapper.selectOldMail(uid);
			List<MailGroup> oldMailGroupList = groupMapper.selectOldMailGroup(uid);
			if (oldMailList != null && !oldMailList.isEmpty()) {
				mailMapper.insertBatch(oldMailList);
			}
			if (oldMailGroupList != null && !oldMailGroupList.isEmpty()) {
				groupMapper.batchReplace(oldMailGroupList);
			}
			List<OldmailinsertKey> insertFlag = new LinkedList<>();
			OldmailinsertKey key = new OldmailinsertKey();
			key.setUid(uid);
			key.setType(0);
			insertFlag.add(key);
			session.getMapper(OldmailinsertMapper.class).insertBatch(insertFlag);
			session.commit();
		} catch (Exception e) {
			exceptionLogger.error("[old mail insert] Exception", e);
			session.rollback();
		} finally {
			session.close();
		}

	}

	public static void checkOldMail(String uid) {
//		if (!isOldMailInserted(uid)) {
//			insertOldMail(uid);
//		}
	}

}
