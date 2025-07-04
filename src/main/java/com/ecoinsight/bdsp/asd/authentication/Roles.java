package com.ecoinsight.bdsp.asd.authentication;


import com.ecoinsight.bdsp.core.entity.Role;

public  abstract class Roles {
     
   
    /**
     * QC 검수자 역할
     */                                
    public static final String USER_QC_ENDUSER = "Researcher";
    public static final int USER_QC_ENDUSER_LEVEL = 60;
    /**
     * 참여 연구원 역할
     */
    public static final String USER_ENDUSER = "Manager";
    public static final int USER_ENDUSER_LEVEL = 50;



    /**
     * 사용자 기본 역할
     */
    public static final String USER_BASIC = "Viewer";    
    public static final int USER_BASIC_LEVEL = 999;

    /**
     * 총괄 관리자 역할
     */
    public static final String APP_ADMIN = "Admin";
    public static final int APP_ADMIN_LEVEL = 1;

    /**
     * 책임 연구원 역할
     */
    public static final String APP_MANAGER = "GeneralManager";
    public static final int APP_MANAGER_LEVEL = 10;

   public static final String APP_SPECIFIEDVIEWER = "SpecifiedViewer";
    public static final int APP_GOVCENTER_LEVEL = 10;


    public static final String SYS_SYSTEMADMIN = "SystemAdmin";
    public static final int SYS_SYSTEMADMIN_LEVEL = 0;

    public static final String SYS_GOD = "God";
    public static final int SYS_GOD_LEVEL = 0;

    private Roles(){}
}
