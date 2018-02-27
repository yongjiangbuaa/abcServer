package com.geng.gameengine.mail.group;

import com.geng.gameengine.mail.MailFunction;
import com.geng.puredb.model.Mail;
import com.google.common.collect.ArrayListMultimap;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * Created by Administrator on 2014/11/28.
 */
public abstract class AbstractSameMailGroup {
    protected int groupType;
    protected List<Integer> containsMailTypeList;
    protected GROUP_RULE groupRule;
    protected String groupString;
    protected MailFunction.MAIL_LABEL label = MailFunction.MAIL_LABEL.per_sys;

    public static enum GROUP_RULE {
        FROM_USER       //根据发送方分组
        ,TYPE           //根据邮件类型分组
        ,UID            //根据邮件uid分组
    }

    public abstract List<Mail> selectLimitMail(SqlSession session, String toUser, String groupIndex, int num, int offset);

    public abstract int deleteMailInGroup(SqlSession session, String toUser, String groupIndex);

    /**
     * 筛选出 符合组别的邮件
     * @param desGroupIndex 目标组索引
     * @param mailItem 需要比较的邮件
     */
    public boolean isInGroup(String desGroupIndex, Mail mailItem, String appVersion) {
        boolean ret = false;
        switch (groupRule) {
            case FROM_USER:
                if (isContainsType(mailItem.getType(), appVersion) && desGroupIndex.equals(mailItem.getFromuser())) {
                    ret = true;
                }
                break;
            case TYPE:
                if (isContainsType(mailItem.getType(), appVersion)) {
                    ret = true;
                }
                break;
            case UID:
                if (isContainsType(mailItem.getType(), appVersion) && desGroupIndex.equals(mailItem.getUid())) {
                    ret = true;
                }
                break;
        }
        return ret;
    }

    public boolean recordGroupIndex(Mail mail, ArrayListMultimap<Integer, String> groupIndexMap) {
        boolean ret = true;
        String groupIndex = "";
        switch (groupRule) {
            case FROM_USER:
                groupIndex = mail.getFromuser();
                break;
            case TYPE:
                groupIndex = Integer.toString(mail.getType());
                break;
            case UID:
                groupIndex = mail.getUid();
                break;
        }
        if (groupIndexMap.get(groupType).contains(groupIndex)) {
            return false;
        } else {
            groupIndexMap.put(groupType, groupIndex);
        }
        return ret;
    }

    public boolean isFindGroup(int groupType, String appVersion) {
        if (getGroupType() == groupType) {
            return  true;
        } else {
            return  false;
        }
    }

    public boolean isContainsType(int type, String appVersion) {
        boolean ret = false;
        if (containsMailTypeList != null) {
            for (int t : containsMailTypeList) {
                if (t == type) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public int getFirstType() {
        int ret = 0;
        if (containsMailTypeList != null) {
            for (int t : containsMailTypeList) {
                ret = t;
                break;
            }
        }
        return ret;
    }

    public String getGroupIndex(Mail mail) {
        String ret = "";
        switch (groupRule) {
            case FROM_USER:
                ret = mail.getFromuser();
                break;
            case TYPE:
                ret = "";
                break;
            case UID:
                ret = mail.getUid();
                break;
        }
        return ret;
    }

    public int getGroupType(Mail mail) {
        return groupType;
    }

    public String getGroupString() {
        return groupString;
    }

    public int getGroupType() {
        return groupType;
    }

    public GROUP_RULE getGroupRule() {
        return groupRule;
    }

    public MailFunction.MAIL_LABEL getLabel() {
        return label;
    }
}
