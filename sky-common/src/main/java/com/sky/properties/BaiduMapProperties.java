package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "sky.baidu.map")
@Data
public class BaiduMapProperties {

    /**
     * 百度地图 AK
     */
    private String ak;

    /**
     * 商家固定地址（点餐平台）
     */
    private String address;

    /**
     * 百度地图接口地址集合
     */
    private Map<String, String> urls;
}
