package com.ecoinsight.bdsp.asd.scheduling.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.ecoinsight.bdsp.asd.entity.SmsScheduler;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.OmniSubject;
import com.ecoinsight.bdsp.asd.model.Subject;
import com.ecoinsight.bdsp.asd.repository.ISmsSchudlerHistoryRepository;

@Component
public class TextMessageSender {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public TextMessageSender() {
    }

    @Value("${sms.send}")
    private String smsSend;

    @Value("${sms.android.appStoreUrl}")
    private String androidAppStoreUrl;

    @Value("${sms.apple.appStoreUrl}")
    private String appleAppStoreUrl;

    @Autowired
    private ISmsSchudlerHistoryRepository _smsSchedulerHistory;

    public void sendExecuteTaskPeriod(List<Map<String, String>> recipients) throws Exception {
        try {
            final String encodingType = "utf-8";
            final String boundary = "____boundary____";

            // Authentication information
            String sms_url = "https://apis.aligo.in/send/";
            Map<String, String> sms = new HashMap<>();

            sms.put("user_id", "eco0517");
            sms.put("key", "7w3q9y8gwtgspgnnsy3m3lcye9wiwmoo");

            // Iterate over the recipients and send SMS
            for (Map<String, String> recipientMap : recipients) {
                try {
                    // Set current recipient and message
                    String phoneNumber = recipientMap.get("receiver");
                    sms.put("receiver", phoneNumber);
                    sms.put("msg", recipientMap.get("msg"));

                    // Other parameters
                    sms.put("sender", ""); // 발신번호
                    sms.put("rdate", ""); // 예약일자 - 20161004 : 2016-10-04일기준
                    sms.put("rtime", ""); // 예약시간 - 1930 : 오후 7시30분
                    sms.put("testmode_yn", smsSend); // Y 인경우 실제문자 전송X , 자동취소(환불) 처리
                    sms.put("title", "[아이AI연구]");

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setBoundary(boundary);
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.setCharset(Charset.forName(encodingType));

                    for (Iterator<String> i = sms.keySet().iterator(); i.hasNext();) {
                        String key = i.next();
                        builder.addTextBody(key, sms.get(key), ContentType.create("Multipart/related", encodingType));
                    }

                    HttpEntity entity = builder.build();
                    HttpClient client = HttpClients.createDefault();
                    HttpPost post = new HttpPost(sms_url);
                    post.setEntity(entity);

                    HttpResponse res = client.execute(post);
                    StatusLine status = res.getStatusLine();

                    SmsScheduler smsScheduler = new SmsScheduler();

                    if (status.getStatusCode() != 200) {
                        LOGGER.info("Failed to send Text Message to phone(" + phoneNumber + "). HTTP status code: "
                                + status.getStatusCode());
                    } else {

                        String result = "";
                        if (res != null) {
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(res.getEntity().getContent(), encodingType));
                            String buffer = null;
                            while ((buffer = in.readLine()) != null) {
                                result += buffer;
                            }
                            in.close();
                        }

                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info(
                                    "Sent a text to phone(" + phoneNumber + ")[" + status.toString() + "] : " + result);

                        }
                        smsScheduler.setSubjectId(recipientMap.get("subjectId"));
                        smsScheduler.setProjectSeq(Long.valueOf(recipientMap.get("projectSeq")));
                        smsScheduler.setTrialIndex(Integer.valueOf(recipientMap.get("trialIndex")));
                        smsScheduler.setSystemId(recipientMap.get("systemId"));
                        smsScheduler.setPhoneNumber(phoneNumber);
                        smsScheduler.setResult(recipientMap.get("result"));
                        smsScheduler.setTaskName(recipientMap.get("taskName"));
                        smsScheduler.setDateTimeExecuted(LocalDateTime.now());
                        _smsSchedulerHistory.addSchedulerHistory(smsScheduler);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to send a text to phone number(" + recipientMap.get("receiver") + ").", e);
                    // Optionally, you can throw a RuntimeException here if you want to handle it at
                    // a higher level
                    throw new RuntimeException(
                            "Fail to send a text to phone number(" + recipientMap.get("receiver") + "). - "
                                    + e.getMessage(),
                            e);
                }
            }
        } catch (Exception e) {
            // Handle the outer exception if needed
        }
    }

    /**
     * 개별 SMS 메시지 전송 메소드
     * 
     * @param subject
     * @throws Exception
     */
    public void send(Subject subject) throws Exception {
        try {
            final String encodingType = "utf-8";
            final String boundary = "____boundary____";

            /**************** 문자전송하기 예제 ******************/
            /* "result_code":결과코드,"message":결과문구, */
            /* "msg_id":메세지ID,"error_cnt":에러갯수,"success_cnt":성공갯수 */
            /*
             * 동일내용 > 전송용 입니다.
             * /******************** 인증정보
             ********************/
            String sms_url = "https://apis.aligo.in/send/"; // 전송요청 URL

            Map<String, String> sms = new HashMap<String, String>();

            sms.put("user_id", "eco0517"); // SMS 아이디
            sms.put("key", "7w3q9y8gwtgspgnnsy3m3lcye9wiwmoo"); // 인증키

            /******************** 인증정보 ********************/

            /******************** 전송정보 ********************/
            sms.put("msg", "아이 AI 연구 대상자 등록이 완료되었습니다. \n\n아래 링크를 사용하여 앱을 다운 받으시고, 아동과 함께 검사를 진행해 주시기 바랍니다. \n\n"
                    + "ID : " + subject.getSubjectId() + "\n"
                    + "PW : " + subject.getBirthDay().replaceAll("-", "").substring(2, 8) + "\n\n"
                    + "앱 사용 및 데이터 수집 방법과 관련하여 문의사항이 있으실 경우 1:1 게시판을 통해서 남겨주시면 답변 드리겠습니다. \n\n"
                    + "그 외 문의사항이 있으신 경우 등록하신 병원의 담당 연구원에게 연락을 부탁드리겠습니다. \n\n"
                    + "구글플레이스토어 : [" + androidAppStoreUrl + "] \n\n" // 메세지 내용
                    + "애플앱스토어 : [" + appleAppStoreUrl + "]");
            sms.put("receiver", subject.getPhoneNumber()); // 수신번호
            // sms.put("destination", "01111111111|담당자,01111111112|홍길동"); // 수신인 %고객명% 치환
            sms.put("sender", ""); // 발신번호
            sms.put("rdate", ""); // 예약일자 - 20161004 : 2016-10-04일기준
            sms.put("rtime", ""); // 예약시간 - 1930 : 오후 7시30분
            sms.put("testmode_yn", smsSend); // Y 인경우 실제문자 전송X , 자동취소(환불) 처리
            sms.put("title", "제목입력"); // LMS, MMS 제목 (미입력시 본문중 44Byte 또는 엔터 구분자 첫라인)

            String image = "";
            // image = "/tmp/pic_57f358af08cf7_sms_.jpg"; // MMS 이미지 파일 위치

            /******************** 전송정보 ********************/

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.setBoundary(boundary);
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setCharset(Charset.forName(encodingType));

            for (Iterator<String> i = sms.keySet().iterator(); i.hasNext();) {
                String key = i.next();
                builder.addTextBody(key, sms.get(key), ContentType.create("Multipart/related", encodingType));
            }

            File imageFile = new File(image);
            if (image != null && image.length() > 0 && imageFile.exists()) {

                // builder.addPart(image, null)("image",
                // new FileBody(imageFile, ContentType.create("application/octet-stream"),
                // URLEncoder.encode(imageFile.getName(), encodingType)));
            }

            HttpEntity entity = builder.build();

            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(sms_url);
            post.setEntity(entity);

            HttpResponse res = client.execute(post);

            String result = "";
            if (res != null) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(res.getEntity().getContent(), encodingType));
                String buffer = null;
                while ((buffer = in.readLine()) != null) {
                    result += buffer;
                }
                in.close();
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("-> Sended Text Message for Subjects : " + result);
            }

        } catch (Exception e) {

        }
    }

    private static final String SMS_URL = "https://apis.aligo.in/send/"; // SMS API URL

    public void sendRetryMessage(AsdProjectSubject subject) throws Exception {
        final String encodingType = "utf-8";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> smsParams = new LinkedMultiValueMap<>();
        smsParams.add("user_id", "eco0517"); // SMS 아이디
        smsParams.add("key", "7w3q9y8gwtgspgnnsy3m3lcye9wiwmoo"); // 인증키
        smsParams.add("msg", "아이AI 토들러 앱을 통해 부모-아동 상호작용 과제를 재수행해 주시기 바랍니다.");
        smsParams.add("receiver", subject.getPhoneNumber());
        smsParams.add("sender", "");
        smsParams.add("rdate", "");
        smsParams.add("rtime", "");
        smsParams.add("testmode_yn", smsSend);
        smsParams.add("title", "제목입력");

        org.springframework.http.HttpEntity<MultiValueMap<String, String>> requestEntity = new org.springframework.http.HttpEntity<>(
                smsParams, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                SMS_URL,
                HttpMethod.POST,
                requestEntity,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String result = response.getBody();
            System.out.println("Sended Text Message for Subjects: " + result);
        } else {
            System.out.println("Failed to send SMS. Status code: " + response.getStatusCodeValue());
        }
    }

    /**
     * dump SMS 전송 메소드
     * 
     * @param subjects
     * @throws Exception
     */
    public void send_mass(List<OmniSubject> subjects) throws Exception {
        try {

            final String encodingType = "utf-8";
            final String boundary = "____boundary____";

            /**************** 문자전송하기 예제 ******************/
            /* "result_code":결과코드,"message":결과문구, */
            /* "msg_id":메세지ID,"error_cnt":에러갯수,"success_cnt":성공갯수 */
            /*
             * 각각 다른 개별 내용 > 동시 전송용 입니다.
             * /******************** 인증정보
             ********************/
            String sms_url = "https://apis.aligo.in/send_mass/"; // 전송요청 URL

            Map<String, String> sms = new HashMap<String, String>();

            sms.put("user_id", "eco0517"); // SMS 아이디
            sms.put("key", "7w3q9y8gwtgspgnnsy3m3lcye9wiwmoo"); // 인증키

            /******************** 인증정보 ********************/

            /******************** 전송정보 ********************/
            sms.put("sender", ""); // 발신번호 서울대 전달해야할 것
            // sms.put("rdate",
            // LocalDate.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"))); // 예약일자 -
            // 20161004 : 2016-10-04일기준
            // sms.put("rtime", "1000"); // 예약시간 - 1930 : 오후 7시30분
            sms.put("testmode_yn", "Y"); // Y 인경우 실제문자 전송X , 자동취소(환불) 처리
            sms.put("msg_type", "SMS"); // SMS(단문) , LMS(장문), MMS(그림문자) = 필수항목

            String msg = "과제 참여자 서울대 자폐플랫폼 \n테스트 메시지\n무료거부:080xxxxxxxxx"; // 메세지 내용
            // int cnt = 500;

            int cnt = subjects.size();
            for (int i = 0; i < cnt; i++) {
                // sms.put("rec_" + i, "01100000" + (i<100 ? (i<10 ? "00"+i : "0"+i) : i) ); //
                // 수신번호_$i 번째 = 필수항목
                sms.put("rec_" + (i + 1), subjects.get(i).getPhone()); // 수신번호_$i 번째 = 필수항목
                sms.put("msg_" + (i + 1), msg.replaceFirst("과제 참여자", "과제 참여자" + subjects.get(i).getId() + "님")); // 내용_$i번째
                                                                                                                 // =
                                                                                                                 // 필수항목
            }

            sms.put("cnt", String.valueOf(cnt));
            // sms.put("title", "제목입력"); // LMS, MMS 제목 (미입력시 본문중 44Byte 또는 엔터 구분자 첫라인)

            String image = "";
            // image = "/tmp/pic_57f358af08cf7_sms_.jpg"; // MMS 이미지 파일 위치

            /******************** 전송정보 ********************/

            /*****/
            /***
             * ※ 중요 - 기존 send 와 다른 부분 ***
             * msg_type 추가 : SMS 와 LMS 구분자 = 필수항목
             * receiver(수신번호) 와 msg(내용) 가 rec_1 ~ rec_500 과 msg_1 ~ msg_500 으로 설정가능 =
             * 필수입력(최소 1개이상)
             * cnt 추가 : 위 rec_갯수 와 msg_갯수에 지정된 갯수정보 지정 = 필수항목 (최대 500개)
             * /
             ******/

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.setBoundary(boundary);
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setCharset(Charset.forName(encodingType));

            for (Iterator<String> i = sms.keySet().iterator(); i.hasNext();) {
                String key = i.next();
                builder.addTextBody(key, sms.get(key), ContentType.create("Multipart/related", encodingType));
            }

            File imageFile = new File(image);
            if (image != null && image.length() > 0 && imageFile.exists()) {

                // builder.addPart(image, null)("image",
                // new FileBody(imageFile, ContentType.create("application/octet-stream"),
                // URLEncoder.encode(imageFile.getName(), encodingType)));
            }

            HttpEntity entity = builder.build();

            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(sms_url);
            post.setEntity(entity);

            HttpResponse res = client.execute(post);

            String result = "";
            if (res != null) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(res.getEntity().getContent(), encodingType));
                String buffer = null;
                while ((buffer = in.readLine()) != null) {
                    result += buffer;
                }
                in.close();
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("-> Sended Text Message for Subjects");
            }

        } catch (Exception e) {

        }
    }

    public void sendText(String phoneNumber, String text) {
        final String encodingType = "utf-8";
        final String boundary = "____boundary____";

        /**************** 문자전송하기 예제 ******************/
        /* "result_code":결과코드,"message":결과문구, */
        /* "msg_id":메세지ID,"error_cnt":에러갯수,"success_cnt":성공갯수 */
        /*
         * 동일내용 > 전송용 입니다.
         * /******************** 인증정보
         ********************/
        String sms_url = "https://apis.aligo.in/send/"; // 전송요청 URL

        Map<String, String> sms = new HashMap<String, String>();

        sms.put("user_id", "eco0517"); // SMS 아이디
        sms.put("key", "7w3q9y8gwtgspgnnsy3m3lcye9wiwmoo"); // 인증키

        /******************** 인증정보 ********************/

        /******************** 전송정보 ********************/
        sms.put("msg", text);
        sms.put("receiver", phoneNumber); // 수신번호
        // sms.put("destination", "01111111111|담당자,01111111112|홍길동"); // 수신인 %고객명% 치환
        sms.put("sender", ""); // 발신번호
        sms.put("rdate", ""); // 예약일자 - 20161004 : 2016-10-04일기준
        sms.put("rtime", ""); // 예약시간 - 1930 : 오후 7시30분
        sms.put("testmode_yn", smsSend); // Y 인경우 실제문자 전송X , 자동취소(환불) 처리
        sms.put("title", "제목입력"); // LMS, MMS 제목 (미입력시 본문중 44Byte 또는 엔터 구분자 첫라인)

        /******************** 전송정보 ********************/
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        builder.setBoundary(boundary);
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.setCharset(Charset.forName(encodingType));

        for (Iterator<String> i = sms.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            builder.addTextBody(key, sms.get(key), ContentType.create("Multipart/related", encodingType));
        }

        try {
            HttpEntity entity = builder.build();

            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(sms_url);
            post.setEntity(entity);

            HttpResponse res = client.execute(post);
            StatusLine status = res.getStatusLine();
            if (status.getStatusCode() != 200) {
                LOGGER.info("-> Sended Text Message to phone(" + phoneNumber + ")");
            } else {
                String result = "";
                if (res != null) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(res.getEntity().getContent(), encodingType));
                    String buffer = null;
                    while ((buffer = in.readLine()) != null) {
                        result += buffer;
                    }
                    in.close();
                }

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.error(
                            "-> Sended a text to phone(" + phoneNumber + ")[" + status.toString() + "] : " + result);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Fail to send a text to phone number(" + phoneNumber + "). - " + e.getMessage(),
                    e);
        }
    }
}
