package com.ecoinsight.bdsp.asd.scheduling.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// TODO 이 클래스 패키지는 scheduling 이 아닌 다른 패키지일 듯...

@Component
public class CallRestApi {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public CallRestApi() {
    }

    public void requestPost(String url, JSONObject jsonObject) throws Exception {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> JSON Object - " + jsonObject.toString());
        }

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;
        BufferedReader br = null;

        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("content-type", "application/json");

            HttpEntity httpEntity = new StringEntity(jsonObject.toString(), "UTF-8");
            httpPost.setEntity(httpEntity);

            response = httpClient.execute(httpPost);

            StringBuffer result = new StringBuffer();

            // TODO if 구문이 필요한가?? statusCode와 무관하게 동일한 로직??

            if (response.getStatusLine().getStatusCode() == 200) {
                br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String buffer = null;
                while ((buffer = br.readLine()) != null) {
                    result.append(buffer);
                }
                br.close();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("-> API call completed. The result is status 200 OK. [Url={}]", url);
                }

            } else {
                br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String buffer = null;
                while ((buffer = br.readLine()) != null) {
                    result.append(buffer);
                }
                br.close();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("-> API call completed. The result is not status 200");
                }

            }
        } catch (Exception e) {

            LOGGER.error("-> Failed to call API => {}", url, e);

            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }

            try {
                if (response != null)
                    response.close();
            } catch (Exception e) {
            }

            try {
                if (httpClient != null)
                    httpClient.close();
            } catch (Exception e) {
            }
        }

    }
}
