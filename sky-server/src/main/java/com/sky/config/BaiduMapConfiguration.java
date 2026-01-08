package com.sky.config;

import com.sky.properties.BaiduMapProperties;
import com.sky.utils.BaiduMapUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaiduMapConfiguration {

    @Bean
    public BaiduMapUtil baiduMapUtil(BaiduMapProperties properties) {
        return new BaiduMapUtil(
                properties.getAk(),
                properties.getAddress(),
                properties.getUrls()
        );
    }
}
