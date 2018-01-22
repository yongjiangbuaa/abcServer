package com.geng.server;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public final class AbcServlet extends HttpServlet {
    public AbcServlet() {
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String deviceId = request.getParameter("device");
        System.out.println("deviceId="+deviceId);
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        writer.println("gengyongjiang has got your http post!! data is deviceId:".concat(deviceId));
    }


}
