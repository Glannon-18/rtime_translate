package com.pingsoft.rtime_translate;

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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

@SpringBootTest
class RtimeTranslateApplicationTests {

    private static final String API_KEY = "e05afb9e-3838-11ea-bc1d-0242ac140007";

    private static final String AUDIOSOURCE_ID = "5e201c800e06c158b526b9dc";

    private static final String SEND_FILE_URL = "https://vaisapis.vais.vn/analytic/v1/digitalization/audio-upsert-execute";

    private static final String GET_MESSAGE_URL = "https://vaisapis.vais.vn/analytic/v1/audios/%s/plugin-data";

    private static HttpClient HTTPCLIENT = HttpClientBuilder.create().build();

    @Test
    void contextLoads() {
        String[] list = new File("D:\\20201229").list();
        String translate_dir_path = "D:\\translate_txt";
        for (String path : list) {
            try {
                File t = new File(translate_dir_path + File.separatorChar + path.substring(path.lastIndexOf("\\") + 1, path.indexOf(".")) + ".txt");
                if (t.exists()) {
                    System.out.println(String.format("文件 %s 已经存在", translate_dir_path + File.separatorChar + path.substring(path.lastIndexOf("\\") + 1, path.indexOf(".")) + ".txt"));
                    continue;
                }

                String name = path.substring(path.lastIndexOf("\\") + 1, path.indexOf(".")) + ".txt";
                String filePath = "D:\\20201229" + File.separatorChar + path;
                String id = sendFile(filePath);
                if (id != null) {
                    System.out.println(String.format("正在处理文件 %s", filePath));
                    JSONObject result = getMessage(id);
                    if (result != null) {
                        Integer audio_status = result.getInteger("audio_status");
                        int count = 0;
                        while (audio_status != 4) {
                            if (count == 16) {
                                break;
                            }
                            Thread.sleep(1000l);
                            result = getMessage(id);
                            audio_status = result.getInteger("audio_status");

                            count++;

                            System.out.println(String.format("尝试第 %d 次获取文件信息，文件 %s 的返回 audio_status 的值为 %d", count, filePath, audio_status));
                        }
                        if (audio_status == 4) {
                            String translate = result.getJSONObject("asr/normal").getJSONObject("data").getString("full_text");
                            File translate_file = new File(translate_dir_path + File.separatorChar + name);
                            OutputStreamWriter outputStreamWriter = null;
                            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(translate_file), "utf-8");
                            outputStreamWriter.write(translate);
                            outputStreamWriter.flush();
                            outputStreamWriter.close();
                        }

                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

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
