package com.qingclass.squirrel.utils;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;

public class PropertiesLoader {

    public static String getProperty(String key) {
        String value= null;
        java.util.Properties props;
        try {
            // server.properties文件在classpath目录下,若是在其他目录下, 则必须写成正确的路径
            props = PropertiesLoaderUtils.loadAllProperties("application.properties");
            value= props.getProperty(key);//根据key得到对应的value
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;

    }
}
