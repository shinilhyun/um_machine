package com.enjoybt.um.service.impl;

import java.io.StringReader;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.enjoybt.um.service.SftpService;
import com.enjoybt.util.SFTPUtil;

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
    
    
    @Override
    public synchronized void run(String xmlData) {
        try{
            //xml 파싱
            logger.info("xmlData \n"+xmlData);
            
            int port = Integer.parseInt(PORT);
            Document doc = new SAXBuilder().build(new StringReader(xmlData));
            // Document doc = new SAXBuilder().build(new File("C:\\WORK\\Simple.xml"));
            
            Element root = doc.getRootElement();
            String filepath = root.getChild("filepath").getValue();
            String filesize = root.getChild("filesize").getValue();
            Element files = root.getChild("filelists");
            List<Element> fileList  = files.getChildren();
            
            init(SFTP_IP, port, ID, PW);
            
            for (Element record : fileList) {
                
                String fileName = record.getValue();
                String remote = filepath;
                String local = LOCAL_FOLDER;
                boolean check = false;
                
                //ftp에서 파일 다운로드
                
                logger.info(fileName+" 다운로드 중...");
                check = downSFtp(remote, fileName, local);
                
                if (check == false) {
                    int i = 0;
                    
                    while ((check == true) || (i<1)) {
                        
                        logger.info("다운로드 실패! 재시도중...");
                        check = downSFtp(remote, fileName, local);
                        i++;
                    }
                }
                if( check == true) {
                    
                    logger.info(fileName+" 다운로드 완료된 파일 삭제중...");
                    if (deleteSFtp(remote, fileName)) {
                        logger.info(fileName + "삭제완료");
                    }
                }
            }
//            sftpService.disconnect();
//            logger.info("sftp 연결 종료");
            
        } catch(Exception e) {
            logger.info("um.do error!");
        } finally {
            disconnect();
            logger.info("sftp 연결 종료");
        }
        
    }
    
    @Override
    public synchronized void downWorking() {
        int port = Integer.parseInt(PORT);
        logger.info("ip : " + SFTP_IP);
        logger.info("port : " + PORT);
        logger.info("id : " + ID);
        logger.info("PW : " + PW);
        
        try{
            disconnect();
            
            init(SFTP_IP, port, ID, PW);
            
            List<String> fileList = getList();
            
            for (String file : fileList) {
                
                
                String fileName = file;
                
                //경로 수정 필요
                String remote = REMOTE_ROOT;
                String local = LOCAL_FOLDER;
                boolean check = false;
                
                //ftp에서 파일 다운로드
                
                logger.info(fileName+" 다운로드 중...");
                check = downSFtp(remote, fileName, local);
                
                if (check == false) {
                    int i = 0;
                    
                    while ((check == true) || (i<1)) {
                        
                        logger.info("다운로드 실패! 재시도중...");
                        check = downSFtp(remote, fileName, local);
                        i++;
                    }
                }
                if( check == true) {
                    
                    logger.info(fileName+" 다운로드 완료된 파일 삭제중...");
                    if (deleteSFtp(remote, fileName)) {
                        logger.info(fileName + "삭제완료");
                    }
                }
            }
            
//          sftpService.disconnect();
//          logger.info("sftp 연결 종료");
        } catch(Exception e) {
            logger.info("sftp error");
        } finally {
            disconnect();
            logger.info("sftp 연결 종료");
        }
    }
    
	/**
	 * SFTP 서버로부터 파일을 수신 하는 기능
	 * @param String ftp_ip 아이피
	 * @param int ftp_port 포트
	 * @param String ftp_id 아이디
	 * @param String ftp_pw 비밀번호
	 * @param String remote 수신할 파일명
	 * @return boolean result 수신성공여부 True/False
	 * @exception Exception
	*/
	@Override
	public boolean downSFtp(String remote,String fileName,String local) throws Exception {
		boolean result = false;
		
		if ((remote != null) 	&& (remote.length() > 0) &&(fileName !=null) &&(fileName.length()>0)) {
			result = SFTPUtil.download(remote,fileName,local);
		}
		return result;
	}
	
	//2. SFTP 파일 삭제
	@Override
	public boolean deleteSFtp(String remote,String removeFileName) throws Exception {
		
		boolean result = false;
		
		if ((remote != null) 	&& (remote.length() > 0) &&(removeFileName.length()>0) && (removeFileName !=null) ) {
			result = SFTPUtil.delete(remote, removeFileName);
		}
		return result;
	}
	
	@Override
	public void init(String ip, int port, String id, String pw) {
		// TODO Auto-generated method stub
		SFTPUtil.init(ip, id, pw, port);
	}
	
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
	    logger.info("sfpt 연결 종료");
		SFTPUtil.disconnection();
	}
	
	public List<String> getList(){
	    return SFTPUtil.getList();
	}
}
