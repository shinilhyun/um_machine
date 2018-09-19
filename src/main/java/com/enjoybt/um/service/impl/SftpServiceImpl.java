package com.enjoybt.um.service.impl;

import com.enjoybt.common.dao.CommonDAO;
import com.enjoybt.um.service.SftpService;
import com.enjoybt.util.SFTPUtil;
import com.jcraft.jsch.SftpException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SftpServiceImpl implements SftpService {

    private static final Logger logger = LoggerFactory.getLogger(SftpServiceImpl.class);

    @Autowired
    CommonDAO dao;

    @Value("#{config['SFTP_IP']}")
    private String SFTP_IP;

    @Value("#{config['SFTP_PORT']}")
    private String PORT;

    @Value("#{config['SFTP_ID']}")
    private String ID;

    @Value("#{config['SFTP_PW']}")
    private String PW;

    @Value("#{config['SFTP_TARGET']}")
    private String LOCAL_FOLDER;

    @Value("#{config['REMOTE_ROOT']}")
    private String REMOTE_ROOT;

    @Value("#{config['VDRS_URL']}")
    private String VDRS_URL;

    @Value("#{config['SFTP_TEMPFOLDER']}")
    private String TEMP_FOLDER;

    @Value("#{config['CMD.MOVE']}")
    private String moveCommand;

    private SFTPUtil sftpUtil = SFTPUtil.getInstance();

    @Override
    public synchronized void run(String xmlData) {
        int file_no = 0;
        int log_sn = insertDownStartLog(xmlData);

        if (sftpUtil.channelSftp != null) {
            System.out.println("channelSftp  already exist");
            return;
        }

        try {
            //xml 파싱
            logger.info("xmlData \n" + xmlData);

            int port = Integer.parseInt(PORT);
            Document doc = new SAXBuilder().build(new StringReader(xmlData));
            // Document doc = new SAXBuilder().build(new File("C:\\WORK\\Simple.xml"));

            Element root = doc.getRootElement();
            String filepath = root.getChild("filepath").getValue();
            String filesize = root.getChild("filesize").getValue();
            Element files = root.getChild("filelists");
            List<Element> fileList = files.getChildren();
            disconnect();
            init(SFTP_IP, port, ID, PW);

            for (Element record : fileList) {

                String fileName = record.getValue();
                String remote = filepath;
                String local = TEMP_FOLDER;
                boolean check = false;

                //ftp에서 파일 다운로드

                logger.info(fileName + " downloading...");
                file_no = insertFileStartLog(log_sn, fileName);

                try {
                    check = downSFtp(remote, fileName, local);
                } catch (Exception e) {
                    logger.info("download fail(maybe already download file..)");
                    check = false;
                }

                if (check == true) {

                    logger.info(fileName + " download complete file deleting...");
                    if (deleteSFtp(remote, fileName)) {
                        logger.info(fileName + "delete success");
                    }

                    //개별 파일 temp폴더에 저장완료 시간
                    updateFileTempLog(file_no);

                } else {

                }

            }

            //temp폴더에 저장완료 시간 update
            updateTempTime(log_sn);

            //tempFolder 파일 결과폴더로 이동
            umFileMove();

            if (checkSuccess(fileList)) {
                logger.info("file check finish : result Y, log_sn : " + log_sn);
                updateEndLog(log_sn, "Y");
            } else {
                logger.info("file check finish : result N, log_sn : " + log_sn);
                updateEndLog(log_sn, "N");
            }

        } catch (Exception e) {
            logger.info("um.do error!");
        } finally {
            disconnect();
        }

    }

    @Override
    public void downWorking() {

        if (sftpUtil.channelSftp != null) {
            logger.info("channelSftp  already exist");
            logger.info("do not excute dwonWroking");
            return;
        }

        logger.info("start downWorking...........");

        int port = Integer.parseInt(PORT);

        try {
            init(SFTP_IP, port, ID, PW);

            List<String> fileList = getList();

            for (String file : fileList) {

                String fileName = file;


                //경로 수정 필요
                String remote = REMOTE_ROOT;
                String local = TEMP_FOLDER;

                boolean check = false;

                try {
                    check = downSFtp(remote, fileName, local);
                } catch (Exception e) {
                    logger.info("download fail(maybe already downloaded)");
                    check = false;
                }

                if (check == true) {

                    logger.info(fileName + " complete file deleting...");
                    if (deleteSFtp(remote, fileName)) {
                        logger.info(fileName + "delete finish");
                    }
                }
            }

            umFileMove();

//          sftpService.disconnect();
//          logger.info("sftp 연결 종료");
        } catch (SftpException se) {
            logger.info("already sftp disconnected");
            return;
        } catch (Exception e) {
            logger.info("downWorking error");
        } finally {
            disconnect();
        }
    }

    @Override
    public boolean downSFtp(String remote, String fileName, String local) throws Exception {
        boolean result = false;

        if ((remote != null) && (remote.length() > 0) && (fileName != null) && (fileName.length()
                > 0)) {
            result = sftpUtil.download(remote, fileName, local);
        }
        return result;
    }

    //2. SFTP 파일 삭제
    @Override
    public boolean deleteSFtp(String remote, String removeFileName) throws Exception {

        boolean result = false;

        if ((remote != null) && (remote.length() > 0) && (removeFileName.length() > 0) && (
                removeFileName != null)) {
            result = sftpUtil.delete(remote, removeFileName);
        }
        return result;
    }

    @Override
    public void init(String ip, int port, String id, String pw) {
        // TODO Auto-generated method stub
        logger.info("SFTP init!!!");
        sftpUtil.init(ip, id, pw, port);
    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub
        logger.info("remote storage disconnected");
        sftpUtil.disconnection();
    }

    public List<String> getList() {
        return sftpUtil.getList();
    }

    @Override
    public boolean umFileMove() {
        boolean result = true;
        File ff = new File(TEMP_FOLDER);
        File[] fileList = ff.listFiles();

        String targetFolder = null;
        for (File record : fileList) {

            //r120_v070_erea_pres_h066.2017111000

            String fileName = record.getName();
            String arr[] = fileName.split("_");
            String fileName2 = null;

            try {

                if (arr[3].equals("pres")) {
                    fileName2 = arr[0] + "_" + arr[1] + "_" + arr[2] + "_unis_" + arr[4];
                    File f = new File(TEMP_FOLDER + "/" + fileName2);

                    if (f.exists()) {
                        logger.info(arr[4] + "file check finished, file move start!");
                        // 파일 1, 2 모두 경로 이동
                        targetFolder =
                                LOCAL_FOLDER + "/r120." + fileName.substring(25, 33) + ".t"
                                        + fileName
                                        .substring(33, 35) + "z";

                        File tf = new File(targetFolder);

                        if (!tf.exists()) {
                            tf.mkdirs();
                        }


                        fileCopy(TEMP_FOLDER + "/" + fileName, targetFolder + "/" + fileName);
                        updateFileEndLog(fileName, "Y");
                        fileCopy(TEMP_FOLDER + "/" + fileName2, targetFolder + "/" + fileName2);
                        updateFileEndLog(fileName2, "Y");
                    } else {
                        logger.info(fileName2 + "file not exist.. so don't move file");
                        result = false;
                    }
                }
            } catch (InterruptedException ie) {
                logger.info(fileName2 + "file move fail!!", ie);
            } catch (Exception e) {
                logger.info(fileName2 + "file not exist.. so don't move file");
                result = false;
            }

        }
        return result;
    }

    public void fileCopy(String inFileName, String outFileName) throws InterruptedException {

        System.out.println(inFileName + " move start");
        Process pc = null;
        Runtime rt = Runtime.getRuntime();
        BufferedReader ebr = null;
        String pcErrorLog;

        String command = moveCommand + " " + inFileName + " " + outFileName;

        try {

            logger.info("command = " + command);
            pc = rt.exec(command);

            ebr = new BufferedReader(new InputStreamReader(pc.getErrorStream()));

            while(true) {
                pcErrorLog = ebr.readLine();
                if (pcErrorLog == null) break;
                logger.info(pcErrorLog);
            }
            pc.waitFor();

        } catch (Exception e) {
            logger.info("ERROR", e);
        } finally {


            if (pc != null) {
                pc.destroy();
            }
        }
    }

    public boolean checkSuccess(List<Element> fileList){
        boolean result = false;
        String targetFolder;
        for (Element e : fileList) {
            String fileName = e.getValue();
            targetFolder = LOCAL_FOLDER + "/r120." + fileName.substring(25, 33) + ".t" + fileName
                            .substring(33, 35) + "z";

            result = checkTargetFolder(targetFolder,fileName);

            if(result == false){
                return false;
            }

        }

        return result;
    }

    public boolean checkTargetFolder(String path, String fileName) {

        File dirFile = new File(path);
        File[] fileList = dirFile.listFiles();
        boolean result= false;

        for (File tempFile : fileList) {

            if (tempFile.getName().equals(fileName)) {
                return true;
            }
        }
        return result;
    }

    //다운로드 시작 로그 기록
    public int insertDownStartLog(String xmlData){
        int log_sn = 0;
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            params.put("xmlData", xmlData);
            dao.insert("um.insertStartLog",params);
            log_sn = Integer.parseInt(params.get("log_sn").toString());
        } catch (Exception e) {
            logger.info("ERROR", e);
        }

        return log_sn;
    }

    public void updateTempTime(int log_sn) {

        try {
            dao.update("um.updateTempTime",log_sn);
        } catch (Exception e) {
            logger.info("ERROR", e);
        }
    }

    public void updateEndLog(int log_sn, String flag) {

        Map<String, Object> params = new HashMap<String, Object>();

        try {
            params.put("log_sn", log_sn);
            params.put("flag", flag);

            dao.update("um.updateEndLog",params);
        } catch (Exception e) {
            logger.info("ERROR", e);
        }
    }

    public int insertFileStartLog(int log_sn, String fileName){

        Map<String, Object> params = new HashMap<String, Object>();
        int file_no = 0;

        try {
            params.put("log_sn", log_sn);
            params.put("file_name", fileName);

            dao.insert("um.insertFileStartLog",params);
            file_no = Integer.parseInt(params.get("file_no").toString());
        } catch (Exception e) {
            logger.info("ERROR", e);
        }
        return file_no;
    }

    public void updateFileTempLog(int file_no){
        try {
            dao.update("um.updateFileTempLog",file_no);
        } catch (Exception e) {
            logger.info("ERROR", e);
        }
    }

    public void updateFileEndLog(String fileName, String flag) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("file_name", fileName);
        params.put("flag", flag);
        try {
            dao.update("um.updateFileEndLog",params);
        } catch (Exception e) {
            logger.info("ERROR", e);
        }
    }
}
