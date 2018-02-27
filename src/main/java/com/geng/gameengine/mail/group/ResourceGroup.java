package com.geng.gameengine.mail.group;

import com.geng.core.GameEngine;
import com.geng.gameengine.mail.MailFunction;
import com.geng.gameengine.mail.MailType;
import com.geng.puredb.dao.MailMapper;
import com.geng.puredb.model.Mail;
import com.geng.puredb.model.UserProfile;
import org.apache.ibatis.session.SqlSession;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2014/11/28.
 */
public class ResourceGroup extends AbstractSameMailGroup {

    public ResourceGroup() {
        groupType = MailFunction.Group_LABEL.RESOURCE.getLabel();
        groupRule = GROUP_RULE.TYPE;
        groupString = "collect";
        containsMailTypeList = new LinkedList<>();
        containsMailTypeList.add(MailType.Resource.ordinal());
    }

    @Override
    public List<Mail> selectLimitMail(SqlSession session, String toUser, String groupIndex, int num, int offset) {
        MailMapper mailMapper = session.getMapper(MailMapper.class);
        List<Mail> ret = mailMapper.selectLimitTypeMails(toUser, MailType.Resource.ordinal(), num, offset);
        return  ret;
    }

    @Override
    public int deleteMailInGroup(SqlSession session, String toUser, String groupIndex){
        Object lock = null;
        UserProfile userProfile = GameEngine.getInstance().getPresentUserProfile(toUser);
        if (userProfile != null) {
            lock = userProfile.getMailLock();
        }
        MailMapper mailMapper = session.getMapper(MailMapper.class);
        int ret;
        if (lock != null) {
            synchronized (lock) {
                ret = mailMapper.deleteByType(toUser, MailType.Resource.ordinal());
            }
        } else {
            ret = mailMapper.deleteByType(toUser, MailType.Resource.ordinal());
        }
        return ret;
    }
}
