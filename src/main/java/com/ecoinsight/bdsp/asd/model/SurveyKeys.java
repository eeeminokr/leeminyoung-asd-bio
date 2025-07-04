package com.ecoinsight.bdsp.asd.model;

public enum SurveyKeys {
    kdst("K-DST"),
    kmchat("KM-CHAT"),
    kqchat("K-QCHAT"),
    selsi("SELSI"),
    cbcl("CBCL"),
    ados2mt("ADOS-2(Mod T)"),
    ados2m1("ADOS-2(Mod 1)"),
    ados2m2("ADOS-2(Mod 2)"),
    ados2m3("ADOS-2(Mod 3)"),
    srs2("SRS-2"),
    adir("ADI-R"),
    scqlifetime("SCQ(lifetime)"),
    scqcurrent("SCQ(current)"),
    pres("PRES"),
    kcars2("K-CARS-2"),
    bedevelq("BEDEVEL-Q"),
    bedeveli("BEDEVEL-I"),
    bedevelp("BEDEVEL-P"),
    kvineland("K-Vineland"),
    kbayley("K-Bayley-III"),
    kwppsi("K-WPPSI-IV");

    private String name;

    private SurveyKeys(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static SurveyKeys surveyKeys(String name) {
        if (name == null)
            return null;
        if (SurveyKeys.kdst.getName().equals(name))
            return kdst;
        if (SurveyKeys.kmchat.getName().equals(name))
            return kmchat;
        if (SurveyKeys.kqchat.getName().equals(name))
            return kqchat;
        if (SurveyKeys.selsi.getName().equals(name))
            return selsi;
        if (SurveyKeys.cbcl.getName().equals(name))
            return cbcl;
        if (SurveyKeys.ados2mt.getName().equals(name))
            return ados2mt;
        if (SurveyKeys.ados2m1.getName().equals(name))
            return ados2m1;
        if (SurveyKeys.ados2m2.getName().equals(name))
            return ados2m2;
        if (SurveyKeys.ados2m3.getName().equals(name))
            return ados2m3;
        if (SurveyKeys.srs2.getName().equals(name))
            return srs2;
        if (SurveyKeys.adir.getName().equals(name))
            return adir;
        if (SurveyKeys.scqlifetime.getName().equals(name))
            return scqlifetime;
        if (SurveyKeys.pres.getName().equals(name))
            return pres;
        if (SurveyKeys.kcars2.getName().equals(name))
            return kcars2;
        if (SurveyKeys.bedevelq.getName().equals(name))
            return bedevelq;
        if (SurveyKeys.bedeveli.getName().equals(name))
            return bedeveli;
        if (SurveyKeys.bedevelp.getName().equals(name))
            return bedevelp;
        if (SurveyKeys.kvineland.getName().equals(name))
            return kvineland;
        if (SurveyKeys.kbayley.getName().equals(name))
            return kbayley;
        if (SurveyKeys.kwppsi.getName().equals(name))
            return kwppsi;
        if (SurveyKeys.scqcurrent.getName().equals(name))
            return scqcurrent;
        return null;
    }
}
