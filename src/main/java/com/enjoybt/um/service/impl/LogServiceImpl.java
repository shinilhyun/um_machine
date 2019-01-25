package com.enjoybt.um.service.impl;

import com.enjoybt.common.dao.CommonDAO;
import com.enjoybt.um.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LogServiceImpl implements LogService {

    private static Logger LOGGER = LoggerFactory.getLogger(LogServiceImpl.class);

    @Autowired
    CommonDAO dao;

    @Override
    public int insertDownStartLog(String xmlData){
        int log_sn = 0;
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            params.put("xmlData", xmlData);
            dao.insert("um.insertStartLog",params);
            log_sn = Integer.parseInt(params.get("log_sn").toString());
        } catch (Exception e) {
            LOGGER.info("ERROR", e);
        }

        return log_sn;
    }

    @Override
    public void updateTempTime(int log_sn) {

        try {
            dao.update("um.updateTempTime",log_sn);
        } catch (Exception e) {
            LOGGER.info("ERROR", e);
        }
    }

    @Override
    public void updateEndLog(int log_sn, String flag) {

        Map<String, Object> params = new HashMap<String, Object>();

        try {
            params.put("log_sn", log_sn);
            params.put("flag", flag);

            dao.update("um.updateEndLog",params);
        } catch (Exception e) {
            LOGGER.info("ERROR", e);
        }
    }

    @Override
    public int insertFileStartLog(int log_sn, String fileName){

        Map<String, Object> params = new HashMap<String, Object>();
        int file_no = 0;

        try {
            params.put("log_sn", log_sn);
            params.put("file_name", fileName);

            dao.insert("um.insertFileStartLog",params);
            file_no = Integer.parseInt(params.get("file_no").toString());
        } catch (Exception e) {
            LOGGER.info("ERROR", e);
        }
        return file_no;
    }

    @Override
    public void updateFileTempLog(int file_no){
        try {
            dao.update("um.updateFileTempLog",file_no);
        } catch (Exception e) {
            LOGGER.info("ERROR", e);
        }
    }

    @Override
    public void updateFileEndLog(int fileNo, String flag) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("file_no", fileNo);
        params.put("flag", flag);
        try {
            dao.update("um.updateFileEndLog",params);
        } catch (Exception e) {
            LOGGER.info("ERROR", e);
        }
    }

    @Override
    public void updateFileComment(int fileNo, String comment) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("comment", comment);
        params.put("file_no", fileNo);

        try {
            dao.update("um.updateFileComment", params);
        } catch (Exception e) {
            LOGGER.info("ERROR", e);
        }
    }

    @Override
    public int getLastFileNoFromFilename(String fileName) {
        int fileNo = -1;
        try {
            fileNo = (Integer)dao.selectObject("um.getFileNoFromFilename", fileName);
        } catch (Exception e) {
            LOGGER.info("file_no : " + fileNo);
            LOGGER.info("fileName : " + fileName);
            LOGGER.info("getLastFileNo ERROR", e);
        }
        return fileNo;
    }
}
