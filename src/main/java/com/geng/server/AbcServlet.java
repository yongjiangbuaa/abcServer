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

        GameEngine.getInstance().protocal(cmd,deviceId,uid,data,sb);

        logger.info("deviceId={}",deviceId);
        response.setContentType("text/plain");

        writer.println(sb.toString());
    }





}
