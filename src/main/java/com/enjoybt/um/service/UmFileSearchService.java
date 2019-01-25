package com.enjoybt.um.service;

/**
 * UM 기상장 파일 조회처리
 */
public interface UmFileSearchService {


    /**
     * 다운로드된 기상장 파일 존재여부 검사
     *
     * @param nwp          년월일시(UTC) 관측시간
     * @param sub           파일구분   pres(등압면), unis(단일면)
     * @param tmfc          수치모델    g768(UM전지구), g512(UM전지구), r120(UM지역), l015(UM국지)
     * @return the boolean
     * @throws Exception the exception
     */
    public boolean hasUmFile(String nwp, String sub, String tmfc) throws Exception;

    /**
     * 검색한 조건의 다운로드 된 기상장 파일 목록 조회
     *
     * @param nwp          년월일시(UTC) 관측시간
     * @param sub           파일구분   pres(등압면), unis(단일면)
     * @param tmfc          수치모델    g768(UM전지구), g512(UM전지구), r120(UM지역), l015(UM국지)
     * @return the um file list
     * @throws Exception the exception
     */
    public String getUmFileList(String nwp, String sub, String tmfc) throws Exception;
}
