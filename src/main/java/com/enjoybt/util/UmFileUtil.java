package com.enjoybt.util;

public class UmFileUtil {

    /***
     * UM 기상장 저장될 폴더 경로 생성
     * @param root
     * @param nwp          년월일시(UTC) 관측시간
     * @param sub           파일구분   pres(등압면), unis(단일면)
     * @param tmfc          수치모델    g768(UM전지구), g512(UM전지구), r120(UM지역), l015(UM국지)
     * @return  UM 기상장 저장되는 폴더 경로
     */
    public static String getUmFileFolder(String root, String nwp, String sub, String tmfc){
        return root + "/" + nwp + "." + tmfc.substring(0, 8)+".t"+tmfc.substring(8)+"z";
    }

    /***
     *  다운로드할 UM 기상장파일명 가져오기
     * @param nwp          년월일시(UTC) 관측시간
     * @param sub           파일구분   pres(등압면), unis(단일면)
     * @param hhef          003~084
     * @param tmfc          수치모델    g768(UM전지구), g512(UM전지구), r120(UM지역), l015(UM국지)
     * @return  UM 기상장파일명
     */
    public static String getUmDownloadFileName(String nwp, String sub, String hhef, String tmfc){
        return nwp + "_v070_erea_" + sub + "_h" + hhef + "." + tmfc + ".gb2";
    }

    /***
     * targetFolder(저장될 로컬폴더)의 경로 생성
     * @param localFolder
     * @param fileName
     * @return
     */
    public static String getTargetFolderPath(String localFolder, String fileName) {
        return localFolder + "/r120." + fileName.substring(25, 33) + ".t" + fileName.substring(33, 35) + "z";
    }

    /***
     * 원격지의 기상장 파일명 변경 시 [화산 기상장 파일 저장방식] 이름으로 변경
     *
     * ===============[화산 기상장 파일 저장 방식]==================
     *    형식   : r120_v070_erea_[sub]_h[hhef]_[yyyyMMddHH].gb2
     *    ex ) r120_v070_erea_unis_h087.2018121600.gb2
     *
     *    [sub] : unis, pres
     *    [hhef] : 관측시간 ex) 003, 006 ~ 084
     *==========================================================
     * @param originName
     * @return
     */
    public static String changeFileNameVdrsStyle(String originName) {

        String origin = originName;
        String replaceName = origin;

        /**
         *   원격지 파일명을 화산시스템 기상장 파일명으로 변경하는 부분 구현
         *   (현재는 변경이 없어서 그대로 전달)
         */

        return replaceName;
    }


    /***
     * 파일명 변경 시 변경해야하는 changeFileNameVdrsStyle() 메서드의 예제입니다.
     * @param originName
     * @return
     */
    public static String changeFileNameVdrsStyleExample(String originName) {

        /**
         * 원격지에 아래와 같이 기상장 파일이 저장되어 있다고 가정하겠습니다.
         * r120_v070_erea_unis_h087_GDPS_SAMPLE.2018121600.gb2
         */
        String origin = "r120_v070_erea_unis_h087_GDPS_SAMPLE.2018121600.gb2";     // 원격지에 저장되어 있는 원래 파일명
        String replaceName = null;      // 화산 시스템에 저장될 파일명

        replaceName = origin.replace("_GDPS_SAMPLE", "");

        return replaceName;
    }
}
