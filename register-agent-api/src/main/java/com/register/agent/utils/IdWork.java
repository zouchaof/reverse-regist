package com.register.agent.utils;

public class IdWork {

    private static Snowflake snowflake = new Snowflake();


    public static String getId(){
        return String.valueOf(snowflake.nextId());
    }

}
