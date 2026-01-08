package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Slf4j
public class BaiduMapUtil {

    private final String ak;
    private final String shopAddress;
    private final Map<String, String> urls;

    /**
     * 通用 GET
     */
    public String get(String apiKey, Map<String, String> params) throws Exception {
        String baseUrl = urls.get(apiKey);
        if (baseUrl == null) {
            throw new IllegalArgumentException("未知的百度地图 API：" + apiKey);
        }

        StringBuilder query = new StringBuilder(baseUrl);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            query.append(entry.getKey())
                    .append("=")
                    .append(UriUtils.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .append("&");
        }
        query.append("ak=").append(ak);

        return doRequest(query.toString());
    }

    /**
     * 商家地址 → 经纬度
     */
    public String geocodeShopAddress() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("address", shopAddress);
        params.put("output", "json");
        return get("geocoding", params);
    }

    /**
     * 用户地址 → 经纬度
     */
    public String geocodeUserAddress(String UserAddress) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("address", UserAddress);
        params.put("output", "json");
        return get("geocoding", params);
    }

    /**
     * 获取用户地址与店铺地址之间的距离
     *
     * @param userLocationJson
     * @param shopLocationJson
     * @return
     */
    public String getDistance(String userLocationJson, String shopLocationJson) throws Exception {
        // 1. 解析用户地址坐标
        JSONObject userObj = JSON.parseObject(userLocationJson);
        JSONObject userLocation = userObj.getJSONObject("result").getJSONObject("location");
        double userLat = userLocation.getDouble("lat");
        double userLng = userLocation.getDouble("lng");
        log.info("用户的lat:{},lng:{}",userLat,userLng);
        String destination = String.valueOf(userLat) + "," + String.valueOf(userLng);

        // 2. 解析商家地址坐标
        JSONObject shopObj = JSON.parseObject(shopLocationJson);
        JSONObject shopLocation = shopObj.getJSONObject("result").getJSONObject("location");
        double shopLat = shopLocation.getDouble("lat");
        double shopLng = shopLocation.getDouble("lng");
        log.info("商家的lat:{},lng:{}",shopLat,shopLng);
        String origin = String.valueOf(shopLat) + "," + String.valueOf(shopLng);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("steps_info","0");
        params.put("riding_type", "1");
        return get("riding", params);
    }

    private String doRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }
}

