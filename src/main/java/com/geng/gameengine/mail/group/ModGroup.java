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
public class ModGroup extends AbstractSameMailGroup {

    public ModGroup() {
        groupType = MailFunction.Group_LABEL.MOD.getLabel();
        groupRule = GROUP_RULE.FROM_USER;
        groupString = "chats";
        label = MailFunction.MAIL_LABEL.mod;
        containsMailTypeList = new LinkedList<>();
        containsMailTypeList.add(MailType.ModPersonal.ordinal());
        containsMailTypeList.add(MailType.ModSend.ordinal());
    }

    @Override
    public List<Mail> selectLimitMail(SqlSession session, String toUser, String groupIndex, int num, int offset) {
        MailMapper mailMapper = session.getMapper(MailMapper.class);
        List<Mail> ret = mailMapper.selectLimitByToUserFrom(toUser, groupIndex, MailType.ModSend.ordinal() - 1, MailType.ModPersonal.ordinal() + 1, num, offset);
        return  ret;
    }

    @Override
    public int deleteMailInGroup(SqlSession session, String toUser, String groupIndex){
        MailMapper mailMapper = session.getMapper(MailMapper.class);
        return mailMapper.deleteAllFromUserMod(toUser, groupIndex);
    }
}