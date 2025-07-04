package com.ecoinsight.bdsp.asd;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class Constants extends com.ecoinsight.bdsp.core.Constants {
    /**
     * QC 상태 - 데이터 미등록
     */
    public static final String IMAGING_QC_NO_DATA = "NO_DATA";
    /**
     * QC 상태 - QC 미검증
     */
    public static final String IMAGING_QC_NO_QC= "NO_QC";
    /**
     * QC 상태 - QC 완료
     */
    public static final String IMAGING_QC_DONE_QC= "DONE_QC";
    /**
     * QC 상태 - 대상아님
     */
    public static final String IMAGING_QC_NO_SUBJECT= "NO_SUBJECT";


    public static final String IMAGING_BOARD_NEW_UPLOAD_STATE ="NEW_UPLOAD_STAGE";

    public static final String IMAGING_BOARD_NEW_NO_STATE ="NEW_NO_STATE";


    private static Map<String, String> dic = new HashMap<String, String>(){{
        put(GENDER_MALE, GENDER_MALE_KR);
        put(GENDER_FEMALE, GENDER_FEMALE_KR);
    }};

    /**
     * Constants에 정의된 코드들을 한글로 매핑하여 리턴한다.
     * @param constant
     * @return
     */
    public static String translate(String constant) {
        return dic.containsKey(constant) ? dic.get(constant) : constant;
    }

    public static int monthSince(String birthDay) {
        LocalDate birthDate = LocalDate.parse(birthDay);
        LocalDate now = LocalDate.now();
    
        int months = (int) ChronoUnit.MONTHS.between(birthDate, now);
    
        if (now.getDayOfMonth() < birthDate.getDayOfMonth()) {
            months--;
        }
    
        return months;
    }
    
    
    // public static String monthSince(String birthDay) {
    //     LocalDate birthDate = LocalDate.parse(birthDay);
    //     LocalDate now = LocalDate.now();
    
    //     int months = (int) ChronoUnit.MONTHS.between(birthDate, now);
    
    //     if (now.getDayOfMonth() < birthDate.getDayOfMonth()) {
    //         months--;
    //     }
    
    //     return String.valueOf(months);
    // }


}
