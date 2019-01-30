package com.enjoybt.um.service.impl;

import com.enjoybt.common.dao.CommonDAO;
import com.enjoybt.um.DownNotice;
import com.enjoybt.um.service.LogService;
import com.enjoybt.um.service.SftpService;
import com.enjoybt.util.SFTPUtil;
import com.enjoybt.util.UmFileUtil;
import com.jcraft.jsch.SftpException;

import java.io.*;
import java.util.List;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SftpServiceImpl implements SftpService {

    private static final Logger logger = LoggerFactory.getLogger(SftpServiceImpl.class);

    @Value("#{config['SFTP_IP']}")
    private String sftpIp;

    @Value("#{config['SFTP_PORT']}")
    private String PORT;

    @Value("#{config['SFTP_ID']}")
    private String ID;

    @Value("#{config['SFTP_PW']}")
    private String PW;

    @Value("#{config['SFTP_TARGET']}")
    private String localFolder;

    @Value("#{config['REMOTE_ROOT']}")
    private String remoteRoot;

    @Value("#{config['VDRS_URL']}")
    private String vdrsUrl;

    @Value("#{config['SFTP_TEMPFOLDER']}")
    private String tempFolder;

    @Value("#{config['CMD.MOVE']}")
    private String moveCommand;

    @Autowired
    CommonDAO dao;

    @Autowired
    LogService logService;

    private SFTPUtil sftpUtil = SFTPUtil.getInstance();

    @Override
    public synchronized void run(String xmlData) {
        int file_no = 0;
        int log_sn = logService.insertDownStartLog(xmlData);

        if (sftpUtil.channelSftp != null) {
            System.out.println("channelSftp  already exist");
            return;
        }

        try {
            logger.info("xmlData \n" + xmlData);

            //sftp remote 연결
            int port = Integer.parseInt(PORT);
            disconnect();
            init(sftpIp, port, ID, PW);

            //XML 파일 파싱하여 다운로드 할 파일 정보(list) 가져옴
            DownNotice downInfo = new DownNotice(xmlData);

            for (Element record : downInfo.getFileList()) {
                String fileName = record.getValue();
                String remote = downInfo.getFilePath();
                boolean isDownSuccess = false;

                //ftp에서 파일 다운로드
                logger.info(fileName + " downloading...");
                file_no = logService.insertFileStartLog(log_sn, fileName);

                try {
                    isDownSuccess = downSFtp(remote, fileName, tempFolder);
                } catch (Exception e) {
                    logger.info("download fail(maybe already download file..)");
                    logService.updateFileComment(file_no,"sftp download fail.. (maybe already downloaded file)");
                    isDownSuccess = false;
                }

                if (isDownSuccess) {
                    logger.info(fileName + " download complete file deleting...");
                    if (deleteSFtp(remote, fileName)) {
                        logger.info(fileName + "delete success");
                    }

                    //개별 파일 temp폴더에 저장완료 시간
                    logService.updateFileTempLog(file_no);
                }
            }

            //temp폴더에 저장완료 시간 update
            logService.updateTempTime(log_sn);

            //tempFolder 파일 결과폴더로 이동
            umFileMove();

            if (checkSuccess(downInfo.getFileList())) {
                logger.info("file check finish : result Y, log_sn : " + log_sn);
                logService.updateEndLog(log_sn, "Y");
            } else {
                logger.info("file check finish : result N, log_sn : " + log_sn);
                logService.updateEndLog(log_sn, "N");
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

        int file_no = 0;
        int log_sn = logService.insertDownStartLog("admin downWorking!");

        logger.info("start downWorking...........");

        int port = Integer.parseInt(PORT);

        try {
            init(sftpIp, port, ID, PW);
            List<String> fileList = getList(remoteRoot);

            for (String file : fileList) {

                String fileName = file;
                boolean check = false;

                file_no = logService.insertFileStartLog(log_sn, fileName);
                logger.info(fileName + " downloading...");

                try {
                    check = downSFtp(remoteRoot, fileName, tempFolder);
                } catch (Exception e) {
                    logger.info("download fail(maybe already downloaded)");
                    logService.updateFileComment(file_no,"sftp download fail.. (maybe already downloaded file)");
                    check = false;
                }

                if (check == true) {
                    logger.info(fileName + " complete file deleting...");
                    if (deleteSFtp(remoteRoot, fileName)) {
                        logger.info(fileName + "delete finish");
                    }
                    //개별 파일 temp폴더에 저장완료 시간
                    logService.updateFileTempLog(file_no);
                }
            }

            //temp폴더에 저장완료 시간 update
            logService.updateTempTime(log_sn);

            //tempFolder 파일 결과폴더로 이동
            umFileMove();

            if (downWorkingCheckSuccess(fileList)) {
                logger.info("file check finish : result Y, log_sn : " + log_sn);
                logService.updateEndLog(log_sn, "Y");
            } else {
                logger.info("file check finish : result N, log_sn : " + log_sn);
                logService.updateEndLog(log_sn, "N");
            }

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

        if ((remote != null) && (remote.length() > 0) && (fileName != null) && (fileName.length()> 0)) {
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

    public List<String> getList(String remoteRoot) {
        return sftpUtil.getList(remoteRoot);
    }

    @Override
    public boolean umFileMove() {
        boolean result = true;
        File ff = new File(tempFolder);
        File[] fileList = ff.listFiles();
        int fileNo;
        int fileNo2;

        String targetFolder = null;
        for (File record : fileList) {

            //r120_v070_erea_pres_h066.2017111000

            String fileName = record.getName();

            String arr[] = fileName.split("_");
            String fileName2 = arr[0] + "_" + arr[1] + "_" + arr[2] + "_unis_" + arr[4];

            fileNo = logService.getLastFileNoFromFilename(fileName);
            fileNo2 = logService.getLastFileNoFromFilename(fileName2);

            if (fileNo == -1 || fileNo2 == -1) {
                result = false;
                continue;
            }

            try {
                if (arr[3].equals("pres")) {
                    File f = new File(tempFolder + "/" + fileName2);
                    if (f.exists()) {
                        logger.info(arr[4] + "file check finished, file move start!");
                        // 파일 1, 2 모두 경로 이동
                        targetFolder = UmFileUtil.getTargetFolderPath(localFolder, fileName);
                        File tf = new File(targetFolder);

                        if (!tf.exists()) {
                            tf.mkdirs();
                        }

                        fileCopy(tempFolder + "/" + fileName, targetFolder + "/" + fileName);
                        logService.updateFileEndLog(fileNo, "Y");
                        fileCopy(tempFolder + "/" + fileName2, targetFolder + "/" + fileName2);
                        logService.updateFileEndLog(fileNo2, "Y");
                    } else {
                        logger.info(fileName2 + "file not exist.. so don't move file");
                        logService.updateFileComment(fileNo,"File pairs do not match");
                        logService.updateFileComment(fileNo2,"File pairs do not match");
                        result = false;
                    }
                }
            } catch (InterruptedException ie) {
                logger.info(fileName2 + "file move fail!!", ie);
                logService.updateFileComment(fileNo2,"file Move fail : " + ie);
            } catch (Exception e) {
                logger.info(fileName2 + "file not exist.. so don't move file");
                logService.updateFileComment(fileNo2,"file Move fail : " + e);
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
        } catch (Exception e) {
            logger.info("ERROR", e);
        } finally {
            pc.waitFor();
            if (pc != null) pc.destroy();
        }
    }

    public boolean checkSuccess(List<Element> fileList){
        boolean result = false;
        String targetFolder;
        for (Element e : fileList) {
            String fileName = e.getValue();
            targetFolder = UmFileUtil.getTargetFolderPath(localFolder, fileName);

            result = checkTargetFolder(targetFolder,fileName);

            if(result == false){
                return false;
            }
        }
        return result;
    }

    public boolean downWorkingCheckSuccess(List<String> fileList){
        boolean result = false;
        String targetFolder;

        for (String fileName : fileList) {
            targetFolder = UmFileUtil.getTargetFolderPath(localFolder, fileName);
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

}
