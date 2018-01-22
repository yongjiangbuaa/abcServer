package com.geng.server;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public final class AbcServlet extends HttpServlet {
    public static final Logger logger = LoggerFactory.getLogger(AbcServlet.class);
    public AbcServlet() {
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //每个请求固定
        //登录时发 deviceId包含于data内
        //cmd deviceId/uid data

        String cmd = request.getParameter("cmd");
        String deviceId = request.getParameter("device");
        String uid = request.getParameter("uid");
        String data = request.getParameter("data");
        StringBuilder sb = new StringBuilder();
        PrintWriter writer = response.getWriter();
        if(StringUtils.isEmpty(cmd) ||
                (StringUtils.isEmpty(uid)&&StringUtils.isEmpty(deviceId))){
            writer.println(sb.toString());
//            throw new GameException(GameException.ERROR_CODE,"invalid op!");
        }
        if(StringUtils.isEmpty(uid) && cmd.equals("login")) {
            handleLogin(deviceId,sb);//好像咩有这个逻辑 无状态
        }else{
            dispatchOp(cmd,data,uid,deviceId,sb);
        }
        logger.info("deviceId={}",deviceId);
        response.setContentType("text/plain");

        writer.println(sb.toString());
    }

    private void handleLogin(String deviceId,StringBuilder sb) {
        sb.append("uid=").append("cmd=login").append("device=").append(deviceId).append("data=");
        logger.info(sb.toString());
        //返回 uid。返回数据  。做别的事 记录等等。
        sb.append("gengyongjiang has got your http post!! data is deviceId:".concat(deviceId));

    }

    private void dispatchOp(String cmd, String data, String uid, String deviceId,StringBuilder sb) {
        sb.append("uid=").append(uid).append("cmd=").append(cmd).append("device=").append(deviceId).append("data=").append(data);
        logger.info(sb.toString());
        sb.delete(0,sb.length() - 1);
        //操作派发到相应类
        //组织返回
    }




}
