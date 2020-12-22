package com.pingsoft.rtime_translate.controller;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final String API_KEY = "e05afb9e-3838-11ea-bc1d-0242ac140007";

    private static final String AUDIOSOURCE_ID = "5e201c800e06c158b526b9dc";

    private static final String SEND_FILE_URL = "https://vaisapis.vais.vn/analytic/v1/digitalization/audio-upsert-execute";

    private static final String GET_MESSAGE_URL = "https://vaisapis.vais.vn/analytic/v1/audios/%s/plugin-data";

    private static HttpClient HTTPCLIENT = HttpClientBuilder.create().build();


    @RequestMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        String saveName = UUID.randomUUID().toString() + ".wav";
        String webRootPath = request.getSession().getServletContext().getRealPath("/");
        String pathname = webRootPath + File.separatorChar + saveName;
        File saveFile = new File(pathname);
        if (saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
        }
        file.transferTo(saveFile);
        String id = sendFile(pathname);
        if (StringUtils.isEmpty(id)) {
            map.put("message", "发送文件返回没有返回id");
            return map;
        }
        JSONObject result = getMessage(id);
        Integer audio_status = result.getInteger("audio_status");
        while (audio_status == 3) {
            result = getMessage(id);
            audio_status = result.getInteger("audio_status");
        }
        map.put("translate_text",result.getJSONObject("asr/normal").getJSONObject("data").getString("full_text"));
        return map;
    }

    private String sendFile(String filePath) {
        HttpPost sendFile = new HttpPost(SEND_FILE_URL);
        sendFile.setHeader("api-key", API_KEY);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();
        sendFile.setConfig(requestConfig);
        FileBody fileBody = new FileBody(new File(filePath));
        StringBody stringBody = new StringBody(AUDIOSOURCE_ID, ContentType.TEXT_PLAIN);
        HttpEntity httpEntity = MultipartEntityBuilder.create().addPart("audiosource_id", stringBody).addPart("audio", fileBody).build();
        sendFile.setEntity(httpEntity);
        try {
            CloseableHttpResponse response = (CloseableHttpResponse) HTTPCLIENT.execute(sendFile);
            HttpEntity responseEntity = response.getEntity();
            String res_str = EntityUtils.toString(responseEntity);
            JSONObject jsonObject = JSONObject.parseObject(res_str);
            return jsonObject.getString("id");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private JSONObject getMessage(String id) {
        String url = String.format(GET_MESSAGE_URL, id);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("api-key", API_KEY);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();
        httpGet.setConfig(requestConfig);
        try {
            CloseableHttpResponse response = (CloseableHttpResponse) HTTPCLIENT.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            String res_str = EntityUtils.toString(responseEntity);
            JSONObject jsonObject = JSONObject.parseObject(res_str);
            return jsonObject;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
