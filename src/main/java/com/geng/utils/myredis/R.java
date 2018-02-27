package com.geng.utils.myredis;


/**
 * Created by XuZiHui on 2017/3/20.
 */
public class R {

    private static MyRedis LOCAL=new MyRedis(new LocalRedisPool());
//    private static MyRedis REMOTE=new MyRedis(new RemoteRedisPool());
    private static MyRedis GLOBAL=new MyRedis(new GlobalRedisPool());
    private static MyRedis SecondGLOBAL=new MyRedis(new SecondGlobalRedisPool());

    public static MyRedis Local(){
        return LOCAL;

    }

    public static MyRedis Remote(int serverId){
        MyRedis REMOTE=new MyRedis(new RemoteRedisPool(serverId));
        return REMOTE;

    }

    public static MyRedis Global(){
        return GLOBAL;
    }

    public static MyRedis SecondGlobal(){
        return SecondGLOBAL;
    }

}
