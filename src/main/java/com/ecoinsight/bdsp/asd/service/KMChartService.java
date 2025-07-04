package com.ecoinsight.bdsp.asd.service;

import java.io.File;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ecoinsight.bdsp.asd.entity.MChart;
import com.ecoinsight.bdsp.core.service.ServiceException;

@Service(KMChartService.ID)
public class KMChartService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String ID = "KMChartService";
    public static final String SURVEYH_KIND_CD = "KM-CHAT";
    public static final String REQUEST_URI = "/api/survey";
    @Value("${ecrf.apiplatform.url}") 
    private String targetUrl;
    @Value("${ecrf.apiplatform.key}") 
    private String authKey;
    
    /**
     * 
     * @param chart
     * @param birthday  2020-11-25
     * @param name
     * @throws ServiceException
     */
    public void sendMChart(MChart chart, String birthday, String name) throws ServiceException {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = null;
        try {
            uri = new URI(this.targetUrl+REQUEST_URI);
        } catch(Exception e) {
            throw new ServiceException("Invalid URI - "+e.getMessage(), e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", this.authKey);
        headers.set("Content-Type", "application/json");

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        String registDt=null;
        try {
            date = inputFormat.parse(chart.getRegistDt());
        } catch (ParseException e) {
            throw new ServiceException("Fail to send MChart data to the urban.( 올바르지 않은 날짜 형식입니다 ->" + chart.getRegistDt() );
        }
        registDt = outputFormat.format(date);

        JSONObject json = new JSONObject();
        json.put("researchCd", "asd");
        json.put("surveyKindCd", "KM-CHAT");
        json.put("subjectId", chart.getSubjectId());
        json.put("birthDay", birthday);
        json.put("register", name);
        json.put("registerDt", registDt);
        json.put("numOfTimes", chart.getTrialIndex());
        json.put("surveyResult", chart.getResult());
        String jsonString = json.toString();
        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("-> json="+jsonString);
        }
        HttpEntity<String> request = new HttpEntity<>(jsonString, headers);

        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
        if(result.getStatusCode()!=HttpStatus.CREATED) {
            throw new ServiceException("Fail to send MChart data to the urban.("+result.getStatusCode()+") - "+result.getBody());
        }
        String body = result.getBody();
        if(body==null || body.equals("")) {
            throw new ServiceException("Response is empty.");
        }
        JSONObject res = new JSONObject(result.getBody());
        int statusCode = res.getInt("statusCode");
        String message = res.getString("message");
        if(20000==statusCode) return;
        throw new ServiceException("Fail to send KMChart. - ["+statusCode+"] "+message);
    }
}
