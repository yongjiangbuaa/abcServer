package com.geng.puredb.model;

import com.geng.gameengine.login.LoginInfo;
import com.geng.utils.CommonUtils;
import com.geng.utils.DateUtils;
import com.geng.utils.SFSMysql;
//import com.geng.utils.bcde.BCDEUtil;
import com.geng.utils.SFSMysql;
import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StatLogin extends StatLoginKey {
    private long disconnect;

    private String ip;

    private int level;
    private int castlelevel;

	public static long getSplitTime() {
		return DateUtils.parseFullTimeStr("2015-04-01 00:00:00");
	}

	public static String getTableName(long loginTime) {
		long splitTime = getSplitTime();
		if (loginTime < splitTime) {
			return "stat_login";
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(loginTime);
			cal.get(Calendar.YEAR);
			cal.get(Calendar.MONTH);
			return new StringBuilder("stat_login").append('_').append(cal.get(Calendar.YEAR)).append('_').append(cal.get(Calendar.MONTH)).toString();
		}
	}

	public static Long writelog(UserProfile userProfile, String ip){
		/*if(ip == null && userProfile.getSfsUser() != null && userProfile.getSfsUser().getIpAddress() != null){
			ip = userProfile.getSfsUser().getIpAddress();
		}
		long now = System.currentTimeMillis();
		String statLoginTable = StatLogin.getTableName(now);
		StatReg statReg = userProfile.getStatReg();
		if (null == statReg) {
			statReg = StatReg.getWithUid(userProfile);
			userProfile.setStatReg(statReg);
		}
		UserReg reg = UserReg.getWithUid(userProfile.getUid());
		String deviceId = "";
		if (reg != null) {
			deviceId = reg.getDeviceid();
		} else {
			deviceId = userProfile.getDeviceId();
		}
		if(BCDEUtil.isBCDEEnabled("stat_login")){
			Map<String,String> updateValues = new HashMap<>();
			updateValues.put("uid",userProfile.getUid());
			updateValues.put("time",String.valueOf(now));
			updateValues.put("disconnect","0");
			updateValues.put("ip",ip);
			updateValues.put("level",String.valueOf(userProfile.getLevel()));
			updateValues.put("castlelevel",String.valueOf(userProfile.getUbManager().getMainBuildingLevel()));
			updateValues.put("payTotal",String.valueOf(userProfile.getPayTotal()));
			updateValues.put("deviceId",deviceId);
			updateValues.put("regTime",String.valueOf(userProfile.getRegTime()));
			updateValues.put("pf",String.valueOf(statReg.getPf()));
			updateValues.put("country",statReg.getCountry());
			BCDEUtil.put("stat_login",updateValues);
		}else {
			// TODO: 这些可以修改成异步写入数据库
			//运维需要，新加这些字段，trulyIp，linkName暂时是什么含义不清楚
			String trulyIp = null;
			String linkName = null;
			String countryCode = CommonUtils.ip2Country(ip);
			String curCountry = StringUtils.isBlank(countryCode) ? null : countryCode;
			String curGaid = null;
			String curDeviceId = null;
			String appVersion = null;
			String curIp = null;
			String curPf=null;
			LoginInfo loginInfo = userProfile.getLoginInfo();
			if(loginInfo != null){
				curGaid = loginInfo.getGaid();
				curDeviceId = loginInfo.getDeviceId();
				appVersion = loginInfo.getClientVersion();
				curIp = ip;
				curPf = loginInfo.getPf();
				trulyIp=ip;
			}
			SFSMysql.getInstance().execute("insert into " + statLoginTable + " (time, uid, disconnect, ip, level, castlelevel, `payTotal`, `deviceId`, `regTime`, " +
							"`pf`, `country`, `curcountry`, `curgaid`, `curdeviceid`, `appversion`, `curip`, `curpf`,`trulyip`) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?,?)",
					new Object[]{now, userProfile.getUid(), 0, ip, userProfile.getLevel(), userProfile.getUbManager().getMainBuildingLevel(), userProfile.getPayTotal(),
							deviceId, userProfile.getRegTime(), statReg.getPf(), statReg.getCountry(), curCountry, curGaid, curDeviceId, appVersion, curIp,curPf,trulyIp});
		}
		UserLoginIp.recordLoginIp(userProfile.getUid(), ip);*/
		return System.currentTimeMillis();
	}

	public static void writeDisconnectLog(UserProfile userProfile){
		String statLoginTable = StatLogin.getTableName(userProfile.getLoginTime());
		String updateSql = "update " + statLoginTable + " set disconnect = ? where uid = ? and time = ?";
		SFSMysql.getInstance().execute(updateSql, new Object[] {System.currentTimeMillis(),userProfile.getUid(),userProfile.getLoginTime()});
	}

    public long getDisconnect() {
        return disconnect;
    }

    public void setDisconnect(long disconnect) {
        this.disconnect = disconnect;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCastlelevel() {
        return castlelevel;
    }

    public void setCastlelevel(int castlelevel) {
        this.castlelevel = castlelevel;
    }
}
