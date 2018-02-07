/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年11月21日 下午3:27:11
 */
package com.geng.utils;

import com.geng.exceptions.COKException;
import com.geng.exceptions.ExceptionMonitorType;
import com.geng.exceptions.GameExceptionCode;
//import com.geng.gameengine.GeoIpService;
import com.geng.gameengine.ItemManager;
//import com.geng.gameengine.Versions;
//import com.geng.gameengine.pay.HexBin;
//import com.geng.gameengine.reward.RewardManager;
import com.geng.puredb.model.UserProfile;
//import com.geng.puredb.model.UserResource;
//import com.geng.utils.filter.BadWordsFilter;
//import com.geng.utils.myredis.MyRedis、Batch;
//import com.geng.utils.myredis.R;
import com.geng.utils.xml.GameConfigManager;
//import com.google.common.base.Joiner;
import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSArray;
import com.geng.core.data.SFSObject;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.ScanParams;
//import redis.clients.jedis.ScanResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 游戏共通类
 */
public final class CommonUtils {

//    private static BadWordsFilter badWordsFilter;

    public static List<String> BadWordsList = new ArrayList<>();
    public static List<String> regularForbiddenWords = new ArrayList<>();
    public static List<String> strictForbiddenWords = new ArrayList<>();
    public static List<String> patternForbiddenWrods = new ArrayList<>();
    public static List<String> weightForbiddenWords = new ArrayList<String>();
    public static long badWordsFileLastModifyTime=-1;

//    private static BadWordsFilter badNameWordsFilter;
    public static List<String> badNameWordsList = new ArrayList<>();
    public static long badNameWordsFileLastModifyTime = -1;

	private static Map<String,String> langMap = new HashMap<>();

	static {
		langMap.put("cn","zh-Hans");
		langMap.put("zh-chs","zh-Hans");
		langMap.put("zh_cn","zh-Hans");
		langMap.put("zh-CN","zh-Hans");
		langMap.put("zh-cht","zh-Hant");
		langMap.put("tw","zh-Hant");
		langMap.put("zh_tw","zh-Hant");
		langMap.put("id","ms");   //印度和印尼都认为是马来语
		langMap.put("in","ms");
		langMap.put("jp","ja");
		langMap.put("en_gb","en");
		langMap.put("fr_fr","fr");
		langMap.put("it_it","it");
	}

	/**
     * 判断 serverId中是否包含当前运行的服务器.
     * serverId的可以有以下格式:
     * 1.  单个server id : 2
     * 2.  表示所有服     : all
     * 3.  表示一组服     : 1,2,4,6-10
     * @param serverId
     * @return
     */
    public static boolean containCurrentServerId(String serverId) {
        return isValidServerId(serverId,",");
    }

    /**
     * 判断 serverId中是否包含当前运行的服务器.
     * serverId的可以有以下格式:
     * 1.  单个server id : 2
     * 2.  表示所有服     : all
     * 3.  表示一组服     : 1-10;30;32-35;36;37
     * @param serverId
     * @return
     */
	public static boolean isValidServerId(String serverId){
		return isValidServerId(serverId, ";");
	}

    public static boolean isValidServerId(String serverId, String separator) {
        if(StringUtils.isBlank(serverId)){
            return false;
        }
        if ("all".equalsIgnoreCase(serverId) || Constants.SERVER_ID_STR.equals(serverId)) {
            return true;
        } else if (serverId.contains(separator)) {
            String[] serverArr = StringUtils.split(serverId, separator);
            for(String serverStr: serverArr){
                String[] server = StringUtils.split(serverStr, "-");
                if (server.length == 1) {
                    if (Constants.SERVER_ID == Integer.parseInt(server[0])) {
                       return true;
                    }
                } else {
                    if (Constants.SERVER_ID >= Integer.parseInt(server[0]) && Constants.SERVER_ID <= Integer.parseInt(server[1])) {
                        return true;
                    }
                }
            }
        } else if ("close".equalsIgnoreCase(serverId)) {
			return false;
		}
		return false;
    }

/**
    public static void loadBadNameWorldsList(){
        File file = new File(Constants.GAME_CONFIG_PATH + "/badwords_name.txt");
        if(!file.exists()){
            COKLoggerFactory.monitorException("loadBadNameWorldsList error", ExceptionMonitorType.CMD, COKLoggerFactory.ExceptionOwner.GWP);
            return;
        }

        if(badNameWordsFileLastModifyTime == file.lastModified()){
            return;
        }
        badNameWordsFileLastModifyTime = file.lastModified();
        try{
            if(badNameWordsFilter == null){
                badNameWordsFilter = new BadWordsFilter(file);
            }else{
                badNameWordsFilter.reload();
            }

            String line;
            List<ZcString> zcList = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            while((line = bufferedReader.readLine())!= null){
                String[] str = StringUtils.split(line, ",");
                for(int i = 0; i < str.length; i++){
                    zcList.add(new ZcString(str[i]));
                }
            }

            Collections.sort(zcList);
            List<String> tmpBadWordsList = new ArrayList<>(zcList.size());
            for(ZcString zc: zcList){
                tmpBadWordsList.add(zc.toString());
            }
            tmpBadWordsList.remove(" ");
            tmpBadWordsList.remove("v");
            badNameWordsList = tmpBadWordsList;
        }catch (Exception e){
            COKLoggerFactory.monitorException("loading bad name words exception", ExceptionMonitorType.CMD, COKLoggerFactory.ExceptionOwner.GWP, e);
        }
    }

    public static void loadBadWordsList(){
        File file = new File(Constants.GAME_CONFIG_PATH + "/badwords.txt");
        if(!file.exists()){
            System.err.println(Constants.GAME_CONFIG_PATH + "/badwords.txt NOT FOUND");
            return;
        }
        //是否更新过文件修改时间
        if(badWordsFileLastModifyTime==file.lastModified()){
            return;
        }
        badWordsFileLastModifyTime=file.lastModified();
        try{
            if (badWordsFilter == null) {
                badWordsFilter = new BadWordsFilter(file);
            }else{
                badWordsFilter.reload();
            }

            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            List<ZcString> zcList = new ArrayList<>();
            while((line = bufferedReader.readLine())!= null){
                String[] str = StringUtils.split(line, ",");
                for(int i = 0; i < str.length; i++){
                    zcList.add(new ZcString(str[i]));
                }
            }
            Collections.sort(zcList);
            List<String> tmpBadWordsList = new ArrayList<>(zcList.size());
            for(ZcString zc: zcList){
                tmpBadWordsList.add(zc.toString());
            }
            tmpBadWordsList.remove(" ");
            tmpBadWordsList.remove("v");

            List<String> tmpRegularForbiddenWords=new ArrayList<>(0);
            List<String> tmpStrictForbiddenWords=new ArrayList<>(0);
            List<String> tmpPatternForbiddenWrods=new ArrayList<>(0);
            List<String> tmpWeightForbiddenWords=new ArrayList<>(0);
//            RedisSession rs = RedisSession.getGlobal(false);
            try{
                tmpRegularForbiddenWords = R.Global().getRangeList("forbidden_words_regular", 0, -1);
                tmpStrictForbiddenWords = R.Global().getRangeList("forbidden_words_strict", 0, -1);
                tmpPatternForbiddenWrods = R.Global().getRangeList("forbidden_words_pattern", 0, -1);
                tmpWeightForbiddenWords = R.Global().getRangeList("forbidden_words_weight", 0, -1);
            }finally {
//                rs.close();
            }

            BadWordsList=tmpBadWordsList;
            regularForbiddenWords=tmpRegularForbiddenWords;
            strictForbiddenWords=tmpStrictForbiddenWords;
            patternForbiddenWrods=tmpPatternForbiddenWrods;
            weightForbiddenWords=tmpWeightForbiddenWords;

        }catch (Exception e){
            COKLoggerFactory.monitorException("loading bad words exception", ExceptionMonitorType.CMD, COKLoggerFactory.ExceptionOwner.ZC, e);
        }
    }



    /**
     * 立即建造时间花费
     *
     * @param remainSec 秒CD花费金币数=max（time（秒）/(k1*(time（秒）/3600)^(k2/100))，1）
     *                  K1，k2为dataconfig中cd_gold的参数
     * @return
     */
    public static int getGoldCost(int remainSec) {
        if (remainSec < 1) {
            LoggerUtil.getInstance().logBySFS("warning_remainSec=" + remainSec);
            return 0;
        }
        Map<String, String> goldMap = new GameConfigManager("item").getItem("cd_gold");
        int cdGoldK1 = Integer.parseInt(goldMap.get("k1"));
        int cdGoldK2 = Integer.parseInt(goldMap.get("k2"));

        int cost = (int) (remainSec / (cdGoldK1 * Math.pow(remainSec / 3600.0f, cdGoldK2 / 100.0)));
        if (cost < 0) {
            cost = Integer.MAX_VALUE;
        } else if (cost == 0) {
            cost = 1;
        }
        return cost;
    }
	public static int getPropGoldCost(int remainSec,String itemId){
		if (remainSec < 1) {
			LoggerUtil.getInstance().logBySFS("warning_remainSec="+remainSec);
			return 0 ;
		}
		Map<String, String> goldMap = new GameConfigManager("goods").getItem(itemId);
		String makeOption=goldMap.get("make");
		if(makeOption==null){
			LoggerUtil.getInstance().logBySFS("make cannot be null");
			return 0 ;
		}
		String[] options=makeOption.split("\\|");
		int goldCost = Integer.parseInt(options[2]);
		float per=remainSec/(Float.parseFloat(options[1]));
		int cost=Math.round(goldCost * per);
        if (cost < 0) {
            cost = Integer.MAX_VALUE;
        }
		return cost;
	}



    public static String fromByteToString(byte[] content) {
        String result = null;
        if (content != null && content.length > 0) {
            try {
                result = Charset.forName("UTF-8").newDecoder()
                        .decode(ByteBuffer.wrap(content)).toString().trim();
            } catch (CharacterCodingException e) {
                LoggerUtil.getInstance().logBySFS("parse blob exception");
            }
        }
        return result;
    }

    public static byte[] fromStringToByte(String content) {
        if (!StringUtils.isBlank(content)) {
            try {
                return Charset.forName("UTF-8").newEncoder()
                        .encode(CharBuffer.wrap(content.trim().toCharArray())).array();
            } catch (CharacterCodingException e) {
                LoggerUtil.getInstance().logBySFS("parse blob exception");
            }
        }
        return null;
    }

    /**
     * 判断两个时间戳(Timestamp)是否在同一天
     *
     * @param time1
     * @param time2
     * @return
     */
    public static boolean isTheSameDate(long time1, long time2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(time1);
        int y1 = c1.get(Calendar.YEAR);
        int m1 = c1.get(Calendar.MONTH);
        int d1 = c1.get(Calendar.DATE);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(time2);
        int y2 = c2.get(Calendar.YEAR);
        int m2 = c2.get(Calendar.MONTH);
        int d2 = c2.get(Calendar.DATE);
        if (y1 == y2 && m1 == m2 && d1 == d2) {
            return true;
        }
        return false;
    }

    public static int[] fromSFSArrayToArray(ISFSArray sfsArr) {
        int[] retArr = new int[sfsArr.size()];
        int index = 0;
        for (int i = 0, size = sfsArr.size(); i < size; i++) {
            retArr[index++] = sfsArr.getInt(i);
        }
        return retArr;
    }

    public static int random(List<Integer> probability) {
        return random(probability.toArray(new Integer[0]));
    }

    public static Integer[] strArray2Int(String[] srcArray) {
        Integer[] ret = new Integer[srcArray.length];
        for (int index = 0; index < srcArray.length; index++) {
            ret[index] = Integer.parseInt(srcArray[index]);
        }
        return ret;
    }

    public static int random(String[] probability) {
        return random(strArray2Int(probability));
    }

    public static int random(Integer[] probability) {
        return random(probability, new Random());
    }

    public static int random(Integer[] probability, Random random) {
        int result = Integer.MAX_VALUE;
        int total = 0;
        for (int i = 0; i < probability.length; i++) {
            total += probability[i] == null ? 0 : probability[i];
        }
        if (total <= 0)
            throw new IllegalArgumentException(String.format(
                    "random, probability{%s} is invalid",
                    StringUtils.join(probability, ",")));
        int p = 0;
        int rndNum = random.nextInt(total) + 1;
        for (int i = 0, size = probability.length; i < size; i++) {
            p += probability[i];
            if (rndNum <= p) {
                result = i;
                break;
            }
        }
        return result;
    }


    public static int random(Float[] probability) {
        int result = Integer.MAX_VALUE;
        float total = 0;
        for (int i = 0; i < probability.length; i++) {
            total += probability[i] == null ? 0 : probability[i];
        }
        if (total <= 0)
            throw new IllegalArgumentException(String.format(
                    "random, probability{%s} is invalid",
                    StringUtils.join(probability, ",")));
        float p = 0;
        float rndNum = new Random().nextFloat() * total;
        for (int i = 0, size = probability.length; i < size; i++) {
            p += probability[i];
            if (rndNum <= p) {
                result = i;
                break;
            }
        }
        return result;
    }

    public static boolean isSameStr(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return false;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        if (str1.equals(str2)) {
            return true;
        } else {
            return false;
        }
    }

    public static int[] getXYByPointId(int pointId){
        int x = pointId % 1201 - 1;
        int y = pointId / 1201;
        int[] point = new int[2];
        point[0]=x;
        point[1]=y;
        return point;
    }

    /**
     * 将指定格式的日期时间描述解析为 毫秒级 时间戳
     *
     * @param //pattern 格式 (yyyy-年 MM-月 dd-日 HH-小时 mm-分钟 ss-秒)
     * @param //timeStr 时间描述
     * @return ret      异常返回-1
     */
    /*public static long parseTimeStamp(String pattern, String timeStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        long ret = -1;
        try {
            ret = dateFormat.parse(timeStr.trim()).getTime();
        } catch (ParseException pException) {
            LoggerUtil.getInstance().logBySFS("parse time exception");
            COKLoggerFactory.monitorException("parseTimeStamp failed", ExceptionMonitorType.WORLD, COKLoggerFactory.ExceptionOwner.GWP, pException);
        }
        return ret;
    }*/

   /* public static String makeMD5(String sourceStr) {
        try {
            byte[] bytes = sourceStr.getBytes("utf-8");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(bytes);
            byte[] md5Byte = md5.digest();
            if (md5Byte != null) {
                return HexBin.encode(md5Byte).toLowerCase();
            }
        } catch (NoSuchAlgorithmException e) {
            LoggerUtil.getInstance().logBySFS(e);
        } catch (UnsupportedEncodingException e) {
            LoggerUtil.getInstance().logBySFS(e);
        }
        return null;
    }*/

    private static int[] getIntArray(String[] strArray) {
        int ret[] = {};
        if (strArray == null) {
            return ret;
        }
        ret = new int[strArray.length];
        for (int index = 0; index < strArray.length; index++) {
            ret[index] = Integer.parseInt(strArray[index]);
        }
        return ret;
    }

	public static boolean isVersionGreaterEqual(String version1, String version2) {
		int v = compareVersion(version1, version2);
		return v == 0 || v == 1;
	}
    /**
     * 比较版本号
     *
     * @return 0-version1大  1-相等  2-version2大
     */
    public static int compareVersion(String version1, String version2) {
        int versionNum1[] = getIntArray(StringUtils.split(version1, '.'));
        int versionNum2[] = getIntArray(StringUtils.split(version2, '.'));
        int comPare;
        if (versionNum1[0] > versionNum2[0]) {
            comPare = 0;
        } else if (versionNum1[0] < versionNum2[0]) {
            comPare = 2;
        } else if (versionNum1[1] > versionNum2[1]) {
            comPare = 0;
        } else if (versionNum1[1] < versionNum2[1]) {
            comPare = 2;
        } else if (versionNum1[2] > versionNum2[2]) {
            comPare = 0;
        } else if (versionNum1[2] < versionNum2[2]) {
            comPare = 2;
        } else {
            comPare = 1;
        }
        return comPare;
    }
    /**
     * 随机指定范围内N个不重复的数
     * 在初始化的无重复待选数组中随机产生一个数放入结果中，
     * 将待选数组被随机到的数，用待选数组(len-1)下标对应的数替换
     * 然后从len-2里随机产生下一个随机数，如此类推
     * @param max  指定范围最大值
     * @param min  指定范围最小值
     * @param n  随机数个数
     * @return int[] 随机数结果集
     */
    public static int[] randomArray(int min,int max,int n){
        int len = max-min+1;

        if(max < min || n > len){
            return null;
        }

        //初始化给定范围的待选数组
        int[] source = new int[len];
        for (int i = min; i < min+len; i++){
            source[i-min] = i;
        }

        int[] result = new int[n];
        Random rd = new Random();
        int index = 0;
        for (int i = 0; i < result.length; i++) {
            //待选数组0到(len-2)随机一个下标
            index = Math.abs(rd.nextInt() % len--);
            //将随机到的数放入结果集
            result[i] = source[index];
            //将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换
            source[index] = source[len];
        }
        return result;
    }

    /**
     * 去掉字符串中的中文
     */
    public static String removeZH(String str) {
        str = str.replaceAll("[\\u4e00-\\u9fa5]+", "**");
        if (StringUtils.isBlank(str)) {
            str = "*zh*";
        }
        return str;
    }

    public static boolean containsEmoji(String source) {
        return emoji.matcher(source).find();
    }

    public static boolean checkName(String str) {
        if (containsEmoji(str)) {
            return false;
        }
        char[] searchChar = {'{', '}'};
        if (!StringUtils.containsAny(str, searchChar)) {
            return true;
        }
        Stack stack = new Stack();
        char[] charArray = str.toCharArray();
        boolean ret = true;
        for (int index = 0; index < charArray.length; index++) {
            if (charArray[index] == '{') {
                stack.push('{');
            } else if (charArray[index] == '}') {
                if (!stack.isEmpty()) {
                    stack.pop();
                } else {
                    ret = false;
                    break;
                }
            }
        }
        if (ret && !stack.isEmpty()) {
            ret = false;
        }
        return ret;
    }

    /**
     * 取得某一路经下某类或某接口的所有子类或实现类
     *
     * @param:absolutePath,绝对路径，用'/'来分隔,起始位com/elex/cok/
     */
    public static List<Class> getSubClasses(Class clz, String absolutePath) {
        List<Class> subClasses = new ArrayList<>();
        try {
            List<String> classNames = new ArrayList<>();
            String packageName = absolutePath.replace('/', '.');
            ClassLoader classLoader = clz.getClassLoader();
            URL url = classLoader.getResource(absolutePath);
            String filePath = url.toString();
            //如果是个jar包：
            String first = filePath.substring(0, 3);
            if (filePath.substring(0, 3).equals("jar")) {
                //截取jar:file:....jar\之间的部分
                filePath = filePath.substring(9, filePath.lastIndexOf('!'));
                JarFile jarFile = new JarFile(filePath);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    String className = entries.nextElement().getName();
                    //如果jar包中的文件跟传入路径属于同一包中，则将这个路径中的文件全部存入列表
                    if (className.contains(absolutePath)) {
                        className = className.substring(className.lastIndexOf('/') + 1);
                        if (!StringUtils.isBlank(className))
                            classNames.add(className);
                    }
                }
            } else {
                //不是jar包
                File f = new File(url.getFile());
                for (File file : f.listFiles()) {
                    String filename = file.getName();
                    if (file.isDirectory()) continue;
                    classNames.add(filename);
                }
            }

            //将指定路径中的所有文件拿出来比对，如果是指定类或接口的子类或实现类，则存入。
            for (String className : classNames) {
                try {
                    Class subClz = Class.forName(packageName + "." + (className.substring(0, className.lastIndexOf('.'))));
                    if (clz.isAssignableFrom(subClz) && !subClz.equals(clz)) {
                        subClasses.add(subClz);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        } catch (Exception e) {
            LoggerUtil.getInstance().recordException(e);
        }
        return subClasses;
    }

    /**
     * 取得某类所在包下所有类（含递归）
     *
     * @param:recursive, 是否递归
     */
    public static List<Class> getClasses(Class clz, Boolean recursive) {
        List<Class> classList = new ArrayList<>();
        List<String> fileNameList = new ArrayList<>();
        try {
            String packageName = clz.getPackage().getName();
            String absolutePath = packageName.replace('.', '/');
            URL url = clz.getClassLoader().getResource(absolutePath);
            File file = new File(url.getFile());
            fileNameList.addAll(getFileNameList(file, recursive)) ;

            for (String fileName : fileNameList) {
                fileName = fileName.replace('\\', '.');
                String clzName = fileName.substring(fileName.indexOf(packageName), fileName.lastIndexOf('.')) ;
                try {
                    classList.add(Class.forName(clzName));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        } catch (Exception e) {
            LoggerUtil.getInstance().recordException(e);
        }
        return classList;
    }

    private static List<String> getFileNameList(File file, Boolean recursive) {
        List<String> fileNameList = new ArrayList<>();
        for (File f : file.listFiles()) {
            String filename = f.getPath();
            if (f.isDirectory()) {
                if (recursive) {
                    fileNameList.addAll(getFileNameList(f, recursive)) ;
                } else {
                    continue;
                }
            } else {
                fileNameList.add(filename);
            }
        }
        return fileNameList ;
    }

    public static ISFSObject map2SFSObj(Map<String, Object> map) {
        ISFSObject obj = new SFSObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) continue;
            if (value instanceof String) {
                obj.putUtfString(key, (String) value);
            } else if (value instanceof Integer) {
                obj.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                obj.putLong(key, (Long) value);
            } else if (value instanceof Boolean) {
                obj.putBool(key, (Boolean) value);
            } else if (value instanceof Byte) {
                obj.putByte(key, (Byte) value);
            } else if (value instanceof Short) {
                obj.putShort(key, (Short) value);
            } else if (value instanceof BigDecimal) {
                BigDecimal bigValue = (BigDecimal) value;
                obj.putLong(key, bigValue.longValue());
            }
        }
        return obj;
    }

    /**
     * 是否包含违禁词汇，不论中间是否有其他字符 eg： (abcdefg) 包含 (adf)
     * 检查逻辑：遍历检查adf的每个字符在abcdefg中的index，如果d的index > a的index，继续遍历，如果每个字符的index都大于上一个，则为true
     * 如果遍历中途发现index为-1（不包含这个字符）或小于上一个（顺序不对）,则返回false
     * */
    public static boolean containsForbiddenWords(String msg){
        String checkingStr = StringUtils.lowerCase(msg).trim().replaceAll(" +", " ");
        for(String word: patternForbiddenWrods){
            Pattern pattern = Pattern.compile(word);
            if(pattern.matcher(checkingStr).find()){
                COKLoggerFactory.monitorException("[pattern forbidden msg] msg:" + msg + " - forbidden word:"+word, ExceptionMonitorType.CMD, COKLoggerFactory.ExceptionOwner.ZC);
                return true;
            }
        }
        for(String word: strictForbiddenWords){
            String wordLowerCase = StringUtils.lowerCase(word);
            StringBuilder patternBuilder = new StringBuilder();
            for(char c: wordLowerCase.toCharArray()){
                patternBuilder.append(c + ".*");
            }
            Pattern pattern = Pattern.compile(patternBuilder.toString());
            if(pattern.matcher(checkingStr).find()){
                COKLoggerFactory.monitorException("[regular forbidden msg] msg:" + msg + " - forbidden word:"+word, ExceptionMonitorType.CMD, COKLoggerFactory.ExceptionOwner.ZC);
                return true;
            }
        }
        for(String word: regularForbiddenWords){
            String wordLowerCase = StringUtils.lowerCase(word);
            StringBuilder patternBuilder = new StringBuilder();
            for(char c: wordLowerCase.toCharArray()){
                patternBuilder.append(c + ".{0,3}");

            }
            Pattern pattern = Pattern.compile(patternBuilder.toString());
            if(pattern.matcher(checkingStr).find()){
                COKLoggerFactory.monitorException("[strict forbidden msg] msg:" + msg + " - forbidden word:"+word, ExceptionMonitorType.CMD, COKLoggerFactory.ExceptionOwner.ZC);
                return true;
            }
        }
        return false;
    }

    /*public static String filterBadWords(String message){
        if (badWordsFilter != null){
            return badWordsFilter.replaceBadWords(message);
        }
        return message;
    }

    public static boolean containsBadWords(String nickName) {
        if(badWordsFilter != null){
            return badWordsFilter.containsBadWords(nickName);
        }
        return containsBadWordsReg(nickName, false);
    }*/

    /**
     *
     * @param nickName
     * @param isNickName 是否是用户名字
     * @return
     */
    private static boolean containsBadWordsReg(String nickName, boolean isNickName) {
        Iterator<String> it = null;
        if(isNickName){
            it = badNameWordsList.iterator();
        }else{
            it = BadWordsList.iterator();
        }

        Pattern pattern = Pattern.compile("\\W");
        try {
            while (it.hasNext()) {
                String badword = it.next();
                if (pattern.matcher(badword).find()) { //如果脏话库里面不是英文单词构成，则不用分词，整句替换
                    int length = badword.length();
                    StringBuilder regexBuilder = new StringBuilder();
                    for(int i = 0 ; i < length; i++){
                        char c = badword.charAt(i);
                        if(c == '+' || c == '.' || c == '{' || c == '}' || c == '*' || c == '[' || c == ']' || c == '(' || c == ')' || c == '\\' || c == '?'){
                            regexBuilder.append("\\");
                        }
                        regexBuilder.append(c);
                        regexBuilder.append("\\s*");
                    }
                    String regex = regexBuilder.toString();
                    Pattern pattern1 = Pattern.compile(regex);
                    if (pattern1.matcher(nickName).find()) {
                        return true;
                    }
                } else { //如果是英文单词类似的语法，则屏蔽整个单词，而不是屏蔽单词里匹配的部分
                    Pattern pattern2 = Pattern.compile("\\b(?i)" + badword + "\\b");
                    if (pattern2.matcher(nickName).find()) {
                        return true;
                    }
                }

            }
        } catch (Exception e) {
        }
        return false;
    }

    /*public static boolean nicknamecontainsBadWords(String nickName) {
        if(badNameWordsFilter != null){
            return badNameWordsFilter.containsBadWords(nickName);
        }
        return containsBadWordsReg(nickName, true);
    }*/



    public static String formatLang(String lang) {
		String toLang = langMap.get(lang.toLowerCase());
		if (toLang == null){
			return lang;
		}
		return toLang;
    }

    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    public static String Bit32(String SourceString) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        digest.update(SourceString.getBytes());
        byte messageDigest[] = digest.digest();
        return toHexString(messageDigest);
    }

    public static String Bit16(String SourceString) {
        return Bit32(SourceString).substring(8, 24);
    }

    private static Pattern serverIdPattern = Pattern.compile("s[0-9]+");
    /**
     * 未找到时 返回 -1
     */
    public static int getServerIdByUrl(String url) {
        String serverId = null;
        if (StringUtils.isNotBlank(url)) {
            Matcher m = serverIdPattern.matcher(url);
            while (m.find()) {
                serverId = StringUtils.substring(m.group(), 1);
                break;
            }
        }
        return serverId == null ? -1 : Integer.parseInt(serverId);
    }

    public static int getNameLength(String name) {
        int ret = 0;
        if (StringUtils.isNotBlank(name)) {
            //泰文 0E00-0E7F
            //126-1535各种拉丁文ascii
            try {
                int length = name.length();
                StringBuilder compressedName = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    char c = name.charAt(i);
                    if (126 <= (int) c && 1535 >= (int) c) {
                        ret += 1; //拉丁系字母占两个长度
                    } else {
                        compressedName.append(c);
                    }
                }
                ret += compressedName.toString().getBytes("gbk").length;
//				ret = name.getBytes("gbk").length;
            } catch (UnsupportedEncodingException e) {
                COKLoggerFactory.monitorException("encoding error " + name, ExceptionMonitorType.NAME_LENGTH, COKLoggerFactory.ExceptionOwner.LYJ, e);
            }
        }
        return ret;
    }

    private static String[] newFlag = new String[]{"Angel", "Cattle", "FireWolf", "Grail", "Knight"};

    public static void allianceIconCompatible(String version, ISFSObject pointObj) {
        if (!pointObj.containsKey("aic")) {
            return;
        }
        String allianceFlag = pointObj.getUtfString("aic");
        if (Arrays.asList(newFlag).contains(allianceFlag)) {
            pointObj.putUtfString("aic", "");
        }
        return;
    }

    public static int nextRangeInt(int min, int max) {
        if (min > max) {
            int tmp = min;
            min = max;
            max = tmp;
        }
        int rnd = new Random().nextInt(max - min + 1);
        return min + rnd;
    }

    public static Object toString(Object[] params) {
		if (params == null){
			return "";
		}
        StringBuilder sb = new StringBuilder("[");
        for (Object obj : params) {
            sb.append(obj).append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    public static Collection<String> getUtfStringArray(ISFSArray paramArr) {
        Collection<String> list = new ArrayList<>();
        if (paramArr == null) {
            return list;
        }
        for (int i = 0; i < paramArr.size(); i++) {
            String s = paramArr.getUtfString(i);
            if(!StringUtils.isBlank(s)) {
                list.add(s);
            }
        }
        return list;
    }

	/*public static ISFSArray getChatRecordsBySeqId(String redisKey, long start, long end){
		ISFSArray ret = new SFSArray();
		try{
//            new RedisSession()
			List<String> records = R.Local().getRangeList(redisKey, 0, -1);
			if(records == null || records.size() == 0){
				return ret;
			}
			for(String msg: records){
				ISFSObject msgObj = SFSObject.newFromJsonData(msg);
				if(!msgObj.containsKey("seqId")){
					continue;
				}
				long seqId = msgObj.getLong("seqId");
				if(seqId < start){
					continue;
				}else if(seqId > end){
					break;
				}
				ret.addSFSObject(msgObj);
			}
		}catch (Exception e){
			COKLoggerFactory.userLogger.error("get chat records exception", e);
		}
		return ret;
	}*/

    public static String[] randomArray(String[] sourceArray, int rndNum) {
        if (sourceArray == null) {
            throw new IllegalArgumentException("source array is null");
        } else if (rndNum > sourceArray.length) {
            throw new IllegalArgumentException("source array length too short");
        } else if (rndNum == sourceArray.length) {
            return Arrays.copyOf(sourceArray, rndNum);
        } else {
            String retArray[] = new String[rndNum];
            String[] sourceArrayCopy = sourceArray;
            Random random = new Random();
            for (int index = 0; index < rndNum; index++) {
                int rndIndex = random.nextInt(sourceArrayCopy.length);
                retArray[index] = sourceArrayCopy[rndIndex];
                sourceArrayCopy = (String[]) ArrayUtils.remove(sourceArrayCopy, rndIndex);
            }
            return retArray;
        }
    }

    public static List getRangeClosed(int min,int max)
    {
        List<Integer> list = getRangeOpen( min, max);
        list.add(min);list.add(max);
        return list;
    }

    public static List getRangeOpen(int min,int max)
    {
        List<Integer> list=new ArrayList<>();
        for(int i=min+1;i<max;i++)
        {
            list.add(i);
        }
        return list;
    }

//    public static String getRangeToStr(List list,String regex)
//    {
//        return Joiner.on(regex).skipNulls().join(list);
//    }

    public static List getRangeClosed(int min1,int max1,int min2,int max2)
    {
        List list=getRangeClosed(min1,max1);
        list.addAll(getRangeClosed(min2,max2));
        return list;
    }

    public static int[] randomIndexArray(Integer[] sourceArray, int rndNum){
        int[] retArr = new int[rndNum];
        if (sourceArray == null) {
            throw new IllegalArgumentException("source array is null");
        } else if (rndNum > sourceArray.length) {
            throw new IllegalArgumentException("source array length too short");
        } else if (rndNum == sourceArray.length){
            for(int index = 0; index < rndNum; index++){
                retArr[index] = index;
            }
        } else {
            Random random = new Random();
            for(int index = 0; index < rndNum; index++){
                int rnd = random(sourceArray, random);
                retArr[index] = rnd;
                sourceArray[rnd] = 0;
            }
        }

        return retArr;
    }

    public static int[] randomIndexArray(int sourceSize,int rndNum){
        if(sourceSize <= 0){
            return null;
        }

        Integer[] sourceArray = new Integer[sourceSize];
        for(int i =0 ;i<sourceSize;i++){
                sourceArray[i]=1;
        }

        int[] retArr = new int[rndNum];
        if (sourceArray == null) {
            throw new IllegalArgumentException("source array is null");
        } else if (rndNum > sourceArray.length) {
            retArr = new int[sourceArray.length];
            for(int index = 0; index < sourceArray.length; index++){
                retArr[index] = index;
            }
        } else if (rndNum == sourceArray.length){
            for(int index = 0; index < rndNum; index++){
                retArr[index] = index;
            }
        } else {
            Random random = new Random();
            for(int index = 0; index < rndNum; index++){
                int rnd = random(sourceArray, random);
                retArr[index] = rnd;
                sourceArray[rnd] = 0;
            }
        }

        return retArr;
    }

    public static boolean isEquip(int itemId) {
        if (itemId >= 1000000 && itemId <= 1999999) {
            return true;
        }
        return false;
    }

    public static boolean isChinaPlatForm(String pf){
        boolean flag = false;
        if (StringUtils.isNotBlank(pf)) {
            if (pf.equals("tencent")) {
                flag = true;
            } else {
                if (pf.length() > 3) {
                    String sub = StringUtils.substring(pf, 0, 3);
                    if ("cn_".equals(sub)) {
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }

//    public static String ip2Country(String ip) {
//        return GeoIpService.getCountryCode(ip);
//    }

    public static int checkAndMultiply(int left, int right) throws COKException {
        int result = left * right;
        long realResult = (long) left * right;
        if (realResult != result) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "Overflow");
        }
        return result;
    }

    /**
     * 是否是同一天
     * @param time1
     * @param time2
     * @return
     */
    public static boolean isSameDay(long time1, long time2){
        Date day1 = new Date();
        day1.setTime(time1);
        Date day2 = new Date();
        day2.setTime(time2);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if(formatter.format(day1).equals(formatter.format(day2))){
            return true;
        }

        return false;
    }

    private static Pattern emoji = Pattern.compile(
                    "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

    public static String filterEmoji(String source) {
        if(StringUtils.isEmpty(source))
            return source;
        return emoji.matcher(source).replaceAll("");
    }

    /**
     * k1版本k2外网服k72k107测试服
     * userProfile  用户数据
     * xmlList  pay_servercontrol.xml
     *
     * @return
     */
    public static boolean isEventOpened(UserProfile userProfile, Map<String, String> xmlList) {


        return true;
    }


    /**
     * 服务器id是否在策划配置服务器列表里
     *
     * @param giveServers close表示所有都关 all表示所有 K=0-10;15;20-100；表示0到10服，15服，20到100服全开
     * @param serverId
     * @return
     */
    public static boolean isServerInCheckServers(String giveServers, int serverId) {
        if ("all".equals(giveServers)) {
            return true;
        }
        if ("close".equals(giveServers)) {
            return false;
        }
        String[] servers = StringUtils.split(giveServers, ';');
        boolean flag;
        for (String item : servers) {
            String[] rangeArr = StringUtils.split(item, "-");
            if (rangeArr.length == 2) {
                flag = serverId >= Integer.parseInt(rangeArr[0]) && serverId <= Integer.parseInt(rangeArr[1]);
            } else {
                flag = serverId == Integer.parseInt(item);
            }
            if (flag) {
                return true;
            }
        }
        return false;
    }

	public static <K> Map<K, List<Integer>> convertContentToMapList(String content, Class<K> kClass) {
		Map<K, List<Integer>> ret = new HashMap<>();
		String[] entryArray = StringUtils.splitByWholeSeparator(content, "|");
		if (entryArray.length != 0) {
			for (String entry : entryArray) {
				String[] keyValueArray = StringUtils.splitByWholeSeparator(entry, ";");
				if (keyValueArray.length < 2) {
					continue;
				}

				List<Integer> listInfo = new ArrayList<>();
				String[] funcArray = StringUtils.split(keyValueArray[1], "_");
				for (int i = 0; i < funcArray.length; i++) {
					String funcMember = funcArray[i];
					if (StringUtils.isBlank(funcMember)) {
						continue;
					}

					listInfo.add(Integer.parseInt(funcMember));
				}

				ret.put(convert(kClass, keyValueArray[0]), listInfo);
			}
		}
		return ret;
	}

	/**
	 * 1;1|2:2类型的串转成map
	 *
	 * @param
	 * @return
	 */
	public static <K, V> Map<K, V> convertContentToMap(String content, Class<K> kClass, Class<V> vClass) {
		Map<K, V> ret = new HashMap<>();
		String[] entryArray = StringUtils.splitByWholeSeparator(content, "|");
		if (entryArray.length != 0) {
			for (String entry : entryArray) {
				String[] keyValueArray = StringUtils.splitByWholeSeparator(entry, ";");
				if (keyValueArray.length == 2) {
					ret.put(convert(kClass, keyValueArray[0]), convert(vClass, keyValueArray[1]));
				}
			}
		}
		return ret;
	}

	/**
	 * 1,2,3转成list
	 * 
	 * @param content
	 * @param kClass
	 * @param <K>
	 * @return
	 */
	public static <K> List<K> convertContentToList(String content, Class<K> kClass) {
		List<K> ret = new LinkedList<>();
		String[] entryArray = StringUtils.splitByWholeSeparator(content, ",");
		for (String entry : entryArray) {
			ret.add(convert(kClass, entry));
		}
		return ret;
	}

	/**
	 * list转化为1,2,3类型的串
	 * 
	 * @param list
	 * @param <K>
	 * @return
	 */
	public static <K> String convertListToContent(List<K> list) {
		StringBuilder sb = new StringBuilder("");
		String spe = "";
		for (K entry : list) {
			sb.append(spe).append(entry.toString());
			spe = ",";
		}
		return sb.toString();
	}

	/**
	 * map转成1;1|2:2类型的串
	 * 
	 * @param map
	 * @param <K>
	 * @param <V>
	 * @return
	 */
	public static <K, V> String convertMapToContent(Map<K, V> map) {
		String seperator = "";
		StringBuilder strBuilder = new StringBuilder();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			strBuilder.append(seperator).append(entry.getKey()).append(";").append(entry.getValue());
			seperator = "|";
		}
		return strBuilder.toString();

	}

	public static <T> T convert(Class<T> clazz, String content) {
		if (clazz.isAssignableFrom(Integer.class)) {
			return clazz.cast(Integer.parseInt(content));
		} else if (clazz.isAssignableFrom(Long.class)) {
			return clazz.cast(Long.parseLong(content));
		} else if (clazz.isAssignableFrom(Short.class)) {
			return clazz.cast(Short.parseShort(content));
		} else if (clazz.isAssignableFrom(Byte.class)) {
			return clazz.cast(Byte.parseByte(content));
		} else if (clazz.isAssignableFrom(Boolean.class)) {
			return clazz.cast(Boolean.parseBoolean(content));
		} else if (clazz.isAssignableFrom(Double.class)) {
			return clazz.cast(Double.parseDouble(content));
		} else if (clazz.isAssignableFrom(String.class)) {
			return clazz.cast(content);
		} else {
			throw new RuntimeException("不支持的类型");
		}
	}

    /**
     * 合并奖励(数目相加)
     * @param totalReward
     * @param utilReward
     */
    /*public static void mergeRewardValue(ISFSArray totalReward, ISFSArray utilReward) {
        if (utilReward == null)
            return ;
        Map<String, ISFSObject> uuidArrayMap = new HashMap<>();
        for (int index = 0; index < utilReward.size(); index++) {
            ISFSObject outObj = utilReward.getSFSObject(index);
            int type=outObj.getInt("type");
            if(type== RewardManager.RewardType.GOODS.ordinal())
            {
                ISFSObject obj=outObj.getSFSObject("value");
                String uuid = obj.getUtfString("uuid");
                if (uuidArrayMap.containsKey(uuid)) {
                    int tmpAdd = uuidArrayMap.get(uuid).getSFSObject("value").getInt("rewardAdd");
                    int add = obj.getInt("rewardAdd") + tmpAdd;
                    obj.putInt("rewardAdd", add);

                    int tmpCount = uuidArrayMap.get(uuid).getSFSObject("value").getInt("count");
                    int count = Math.max(obj.getInt("count"), tmpCount);
                    obj.putInt("count", count);

                    uuidArrayMap.put(uuid, outObj);
                } else {
                    uuidArrayMap.put(uuid, outObj);
                }
            }else if(type==RewardManager.RewardType.EXP.ordinal()
                    || type==RewardManager.RewardType.HONOR.ordinal()
                    || type==RewardManager.RewardType.ALLIANCE_POINT.ordinal()
                    || type==RewardManager.RewardType.CHIP.ordinal()
                    || type==RewardManager.RewardType.DIAMOND.ordinal()
                    || type==RewardManager.RewardType.ROSE_CROWN.ordinal())
            {
                if(uuidArrayMap.containsKey(""+type))
                {
                    int total= Math.max(outObj.getInt("total") ,uuidArrayMap.get(""+type).getInt("total"));
                    int value=outObj.getInt("value")+uuidArrayMap.get(""+type).getInt("value");
                    outObj.putInt("total",total);
                    outObj.putInt("value",value);
                    uuidArrayMap.put(""+type, outObj);
                }else
                {
                    uuidArrayMap.put(""+type, outObj);
                }
            }else
            {
                if(uuidArrayMap.containsKey(""+type))
                {
                    long total= Math.max(outObj.getLong("total") ,uuidArrayMap.get(""+type).getLong("total"));
                    int value=outObj.getInt("value")+uuidArrayMap.get(""+type).getInt("value");
                    outObj.putLong("total",total);
                    outObj.putInt("value",value);
                    uuidArrayMap.put(""+type, outObj);
                }else
                {
                    uuidArrayMap.put(""+type, outObj);
                }
            }

        }
        for (ISFSObject obj : uuidArrayMap.values()) {
            totalReward.addSFSObject(obj);
        }
    }*/
}
