package com.pingsoft.rtime_translate.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TranslateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateController.class);

    private static HttpClient HTTPCLIENT = HttpClientBuilder.create().build();

    @Value("${translate_url}")
    private String translate_url;

    @PostMapping("/translate")
    public Map<String, String> translate(@RequestParam String text, @RequestParam String srcLang, @RequestParam String tgtLang) throws Exception {
        HttpPost post = new HttpPost(translate_url);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("method", "translate"));
        urlParameters.add(new BasicNameValuePair("srcLang", srcLang));
        urlParameters.add(new BasicNameValuePair("tgtLang", tgtLang));
        urlParameters.add(new BasicNameValuePair("useSocket", "True"));
        urlParameters.add(new BasicNameValuePair("text", srcLang.equals("vi") ? "startnmtpy " + text : text));

        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(3000).setConnectTimeout(3000).build();
        post.setConfig(requestConfig);
        post.setEntity(new UrlEncodedFormEntity(urlParameters, "utf-8"));
        HttpResponse response = HTTPCLIENT.execute(post);
        StringBuffer result = new StringBuffer();
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), "utf-8"));
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        JSONObject result_obj = JSONObject.parseObject(result.toString());
        EntityUtils.consume(response.getEntity());
        if (result_obj.getString("success").equals("true")) {
            String data = result_obj.getString("data").replaceAll("\\$number", "");
            Map<String, String> map = new HashMap<>();
            map.put("result", data);
            return map;
        } else {
            throw new Exception("翻译接口返回的success为false");
        }

    }

}
