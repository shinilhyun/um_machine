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
}
