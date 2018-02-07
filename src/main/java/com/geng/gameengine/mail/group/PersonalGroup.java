package com.geng.gameengine.mail.group;

import com.geng.gameengine.mail.MailFunction;
import com.geng.gameengine.mail.MailType;
import com.geng.puredb.dao.MailMapper;
import com.geng.puredb.model.Mail;
import org.apache.ibatis.session.SqlSession;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2014/11/28.
 */
public class PersonalGroup extends AbstractSameMailGroup {

    public PersonalGroup() {
        groupType = MailFunction.Group_LABEL.PERSONAL.getLabel();
        groupRule = GROUP_RULE.FROM_USER;
        groupString = "chats";
        containsMailTypeList = new LinkedList<>();
        containsMailTypeList.add(MailType.Send.ordinal());
        containsMailTypeList.add(MailType.Personal.ordinal());
        containsMailTypeList.add(MailType.Alliance_ALL.ordinal());
    }

    @Override
    public List<Mail> selectLimitMail(SqlSession session, String toUser, String groupIndex, int num, int offset) {
        MailMapper mailMapper = session.getMapper(MailMapper.class);
        List<Mail> ret = mailMapper.selectLimitByToUserFrom(toUser, groupIndex, MailType.Alliance_ALL.ordinal() - 1, MailType.Personal.ordinal() + 1, num, offset);
        return  ret;
    }

    @Override
    public int deleteMailInGroup(SqlSession session, String toUser, String groupIndex){
        MailMapper mailMapper = session.getMapper(MailMapper.class);
        return mailMapper.deleteAllFromUser(toUser, groupIndex);
    }
}
