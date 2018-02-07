package com.geng.gameengine.mail.group;

import com.geng.gameengine.mail.MailType;
import com.geng.puredb.model.Mail;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * Created by Administrator on 2014/11/28.
 */
public class SingleItemGroup extends AbstractSameMailGroup{

    public SingleItemGroup() {
        groupType = 0;
        groupString = "detail";
        groupRule = GROUP_RULE.UID;
        containsMailTypeList = null;
    }

    @Override
    public boolean isFindGroup(int groupType, String appVersion) {
        if (isContainsType(groupType, appVersion)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isContainsType(int mailType, String appVersion) {
        boolean ret = false;
        if (mailType == MailType.System.ordinal()
                || mailType == MailType.Cure_Soldier.ordinal() || mailType == MailType.WORLD_NEW_EXPLORE.ordinal()
                || mailType == MailType.AllianceInvite.ordinal() || mailType == MailType.AllianceApply.ordinal()
                || mailType == MailType.InviteMovePoint.ordinal() || mailType == MailType.KickAllianceUser.ordinal()
                || mailType == MailType.GIFT.ordinal() || mailType == MailType.GIFT_EXCHANGE.ordinal() || mailType == MailType.GIFT_ALLIANCE.ordinal()
                || mailType == MailType.RefuseAllianceApply.ordinal()){
            ret = true;
        }
        return ret;
    }

    @Override
    public List<Mail> selectLimitMail(SqlSession session, String toUser, String groupIndex, int num, int offset) {
        return null;
    }

    @Override
    public int getGroupType(Mail mail) {
        return mail.getType();
    }

    @Override
    public int deleteMailInGroup(SqlSession session, String toUser, String fromUser){
        return 0;
    }
}
