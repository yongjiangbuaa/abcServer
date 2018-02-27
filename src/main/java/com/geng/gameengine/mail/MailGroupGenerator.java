package com.geng.gameengine.mail;

import com.geng.gameengine.mail.group.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2014/11/29.
 */
public class MailGroupGenerator {

	private static List<AbstractSameMailGroup> allGroupList = new LinkedList<>();

	static
	{
		allGroupList.add(new SingleItemGroup());
		allGroupList.add(new PersonalGroup());
		allGroupList.add(new ResourceGroup());
		allGroupList.add(new ModGroup());
		allGroupList.add(new TradeGroup());
		allGroupList.add(new MonsterGroup());
	}

	public static List<AbstractSameMailGroup> getAllMailGroup () {
		return allGroupList;
	}

	public static AbstractSameMailGroup getGroupByMailGroup(int groupType, String appVersion) {
		AbstractSameMailGroup ret = null;
		List<AbstractSameMailGroup> allMailGroup = getAllMailGroup();
		for (AbstractSameMailGroup group : allMailGroup) {
			if (group.isFindGroup(groupType, appVersion)) {
				ret = group;
				break;
			}
		}
		return ret;
	}

	public static List<AbstractSameMailGroup> getGroup(AbstractSameMailGroup.GROUP_RULE ... rule) {
		List<AbstractSameMailGroup> ret = new LinkedList<>();
		List<AbstractSameMailGroup> allGroup = getAllMailGroup();
		for (AbstractSameMailGroup group : allGroup) {
			for (AbstractSameMailGroup.GROUP_RULE one : rule) {
				if (group.getGroupRule() == one) {
					ret.add(group);
				}
			}
		}
		return ret;
	}

	/**
	 * 注 SingleItemGroup 的 groupType是 0
	 */
	public static AbstractSameMailGroup getGroup(List<AbstractSameMailGroup> allGroup, int comparatorGroupType) {
		AbstractSameMailGroup ret = null;
		for (AbstractSameMailGroup group : allGroup) {
			if (group.getGroupType() == comparatorGroupType) {
				ret = group;
				break;
			}
		}
		return ret;
	}

	public static AbstractSameMailGroup getGroupByMailType(int mailType, String appVersion) {
		AbstractSameMailGroup ret = null;
		List<AbstractSameMailGroup> allGroup = getAllMailGroup();
		for (AbstractSameMailGroup group : allGroup) {
			if (group.isContainsType(mailType, appVersion)) {
				ret = group;
				break;
			}
		}
		return ret;
	}
}
