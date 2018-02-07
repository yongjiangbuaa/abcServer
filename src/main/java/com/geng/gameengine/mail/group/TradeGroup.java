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
public class TradeGroup extends AbstractSameMailGroup {

    public TradeGroup() {
        groupType = MailFunction.Group_LABEL.TRADE.getLabel();
        groupString = "collect";
        groupRule = GROUP_RULE.TYPE;
        containsMailTypeList = new LinkedList<>();
        containsMailTypeList.add(MailType.TradeResource.ordinal());
    }

    @Override
    public List<Mail> selectLimitMail(SqlSession session, String toUser, String groupIndex, int num, int offset) {
        MailMapper mailMapper = session.getMapper(MailMapper.class);
        List<Mail> ret = mailMapper.selectLimitTypeMails(toUser, MailType.TradeResource.ordinal(), num, offset);
        return  ret;
    }

    @Override
    public int deleteMailInGroup(SqlSession session, String toUser, String groupIndex){
        MailMapper mailMapper = session.getMapper(MailMapper.class);
        return mailMapper.deleteByType(toUser, MailType.TradeResource.ordinal());
    }
}

