package com.enjoybt.um.controller;

import java.io.StringReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.enjoybt.um.service.SftpService;

@Controller
public class SFTPController {

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
    
	private static final Logger logger = LoggerFactory.getLogger(SFTPController.class);
	
	@Autowired
	private SftpService sftpService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        logger.info("Welcome home! The client locale is {}.", locale);
        
        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        
        String formattedDate = dateFormat.format(date);
        
        model.addAttribute("serverTime", formattedDate );
        
        return "home";
    }
	
	@RequestMapping(value = "/um.do", method = RequestMethod.POST)
	@ResponseBody
	public void getUmData(@RequestParam(value = "filelist") String xmlData) throws Exception {
		
	    int port = Integer.parseInt(PORT);
	    logger.info("ip : " + SFTP_IP);
	    logger.info("port : " + PORT);
	    logger.info("id : " + ID);
	    logger.info("PW : " + PW);
	    
		//접속 ip주소 가져오기
		HttpServletRequest req = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String guestIp = req.getHeader("X-FORWARDED-FOR");
		
        if (guestIp == null) {
			guestIp = req.getRemoteAddr();
		}
		
		//xml 파싱
		logger.info("xmlData="+xmlData);
		Document doc = new SAXBuilder().build(new StringReader(xmlData));
		// Document doc = new SAXBuilder().build(new File("C:\\WORK\\Simple.xml"));
		
		logger.info("guestIp="+guestIp);
		
		Element root = doc.getRootElement();
		String filepath = root.getChild("filepath").getValue();
		String filesize = root.getChild("filesize").getValue();
		Element files = root.getChild("filelists");
		List<Element> fileList  = files.getChildren();
		
		sftpService.init(SFTP_IP, port, ID, PW);
		
        for (Element record : fileList) {
		    
			String fileName = record.getValue();
			String remote = filepath;
			String local = LOCAL_FOLDER;
			boolean check = false;
			
			//ftp에서 파일 다운로드
			
			logger.info(fileName+" 다운로드 중...");
			check = sftpService.downSFtp(remote, fileName, local);
			
            if (check == false) {
                int i = 0;
                
			    while (check == false || i<3) {
			        
			        logger.info("다운로드 실패! 재시도중...");
	                check = sftpService.downSFtp(remote, fileName, local);
	                i++;
	            }
			}
            
        }

        //완료된 파일 삭제
        for (Element record : fileList) {
            
            String fileName = record.getValue();
            String remote = filepath;
            String local = LOCAL_FOLDER + "/"+fileName;
            boolean deleteCheck = false;
            
            logger.info(fileName+" 다운로드 완료된 파일 삭제중...");
            
            sftpService.deleteSFtp(remote, fileName);
                
        } 
        
        sftpService.disconnect();
	}
}
