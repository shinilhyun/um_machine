package com.enjoybt.um.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.enjoybt.um.service.SftpService;
import com.enjoybt.util.SFTPUtil;
import com.jcraft.jsch.SftpException;

@Service
public class SftpServiceImpl implements SftpService {

    private static final Logger logger = LoggerFactory.getLogger(SftpServiceImpl.class);

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

    @Value("#{config['remote_root']}")
    private String REMOTE_ROOT;
    
    @Value("#{config['VDRS_URL']}")
    private String VDRS_URL;

    @Value("#{config['SFTP_TEMPFOLDER']}")
    private String TEMP_FOLDER;

    private SFTPUtil sftpUtil = SFTPUtil.getInstance();

    @Override
    public synchronized void run(String xmlData, String log_sn) {

        if (sftpUtil.channelSftp != null) {
            System.out.println("channelSftp  이미 존재");
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

                logger.info(fileName + " 다운로드 중...");
                //check = downSFtp(remote, fileName, local);

                if (check == true) {

                    logger.info(fileName + " 다운로드 완료된 파일 삭제중...");
//                    if (deleteSFtp(remote, fileName)) {
//                        logger.info(fileName + "삭제완료");
//                    }
                }
                //TODO 다운로드 받은 path/temp 폴더의 파일 체크 후 화산 시스템으로 결과 날려주는 부분 필요
            }
            
            if (checkAndMove(fileList)) {
                logger.info("기상장파일 체크완료 : 결과 Y, log_sn : " + log_sn);
                sendResult(log_sn, "Y");
            } else {
                logger.info("기상장파일 체크완료 : 결과 F, log_sn : " + log_sn);
                sendResult(log_sn, "F");
            }

        }
//        catch (SftpException se) {
//            logger.info("이미 SFTP 접속 끊김");
//            return;
//        }
        catch (Exception e) {
            logger.info("um.do error!");
        } finally {
            disconnect();
        }

    }

    @Override
    public void downWorking() {

        if (sftpUtil.channelSftp != null) {
            System.out.println("channelSftp  이미 존재");
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
                String local = LOCAL_FOLDER;
                boolean check = false;

                //ftp에서 파일 다운로드

                logger.info(fileName + " 다운로드 중...");
                check = downSFtp(remote, fileName, local);

                if (check == true) {

                    logger.info(fileName + " 다운로드 완료된 파일 삭제중...");
                    if (deleteSFtp(remote, fileName)) {
                        logger.info(fileName + "삭제완료");
                    }
                }
            }

//          sftpService.disconnect();
//          logger.info("sftp 연결 종료");
        } catch (SftpException se) {
            logger.info("이미 sftp 연결이 끊김");
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

        if ((remote != null) && (remote.length() > 0) && (fileName != null) && (fileName.length() > 0)) {
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
        logger.info("원격스토리지 접속 종료");
        sftpUtil.disconnection();
    }

    public List<String> getList() {
        return sftpUtil.getList();
    }

    public boolean checkAndMove(List<Element> fileList){
        boolean result = true;
        
        String targetFolder = null;
        for (Element record : fileList) {
            
            //r120_v070_erea_pres_h066.2017111000
            
            String fileName = record.getValue();
            String arr[] = fileName.split("_");
            String fileName2 = null;
            
            if(arr[3].equals("pres")){
                fileName2 = arr[0]+"_"+arr[1]+"_"+arr[2]+"_unis_"+arr[4];
                File f = new File(TEMP_FOLDER + "/" + fileName2);
                
                if(f.exists()){
                    logger.info(arr[4] + "파일 검증 완료 파일이동 시작");
                    // 파일 1, 2 모두 경로 이동
                    targetFolder = LOCAL_FOLDER + "/r120." + fileName.substring(25, 33) + ".t" + fileName.substring(33, 35) + "z";
                    
                    File tf = new File(targetFolder);
                    
                    if(!tf.exists()) {
                        tf.mkdirs();
                    }

                    fileCopy(TEMP_FOLDER + "/" + fileName, targetFolder + "/" + fileName);
                    fileCopy(TEMP_FOLDER + "/" + fileName2, targetFolder + "/" + fileName2);
                } else {
                    logger.info(fileName2 + "파일이 존재하지 않으므로 파일 이동을 하지 않습니다.");
                    result = false;
                }
            }

        }
        return result;
    }
    
    public void fileCopy(String inFileName, String outFileName) {
        try {
            System.out.println(inFileName + "복사시작");

            FileInputStream inputStream = new FileInputStream(inFileName);
            FileOutputStream outputStream = new FileOutputStream(outFileName);

            FileChannel fcin = inputStream.getChannel();
            FileChannel fcout = outputStream.getChannel();

            long size = fcin.size();
            fcin.transferTo(0, size, fcout);

            fcout.close();
            fcin.close();

            outputStream.close();
            inputStream.close();

            System.out.println(inFileName + "복사 완료");
            
            //복사후 파일 삭제
            File f = new File(inFileName);
            f.delete();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void sendResult(String log_sn, String flag) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("log_sn", log_sn);
        map.add("flag",flag);

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.postForObject(VDRS_URL, map, String.class);
    }

}
