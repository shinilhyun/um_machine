package com.enjoybt.um.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	/**
	 * um 기상장 다운로드 (외부에서 요청들어오면 실행)
	 * 다운로드 통보문 수신 시 실행 됨
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/um.do", method = RequestMethod.POST)
	@ResponseBody
	public String getUmData(@RequestBody MultiValueMap<String,String> map) {

	    logger.info("ip : " + SFTP_IP);
	    logger.info("port : " + PORT);
	    logger.info("id : " + ID);
	    logger.info("PW : " + PW);

		String xmlData = map.getFirst("xmlData");
	    sftpService.run(xmlData);
       
	    return "success";
	}

	/***
	 * 근무자 수동 다운로드 (원격지의 미다운로드 파일 전부 다운로드 시작)
	 */
	@RequestMapping(value = "/downWorking.do", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public void downWorking() {
	    logger.info("downWorking.do");
        sftpService.downWorking();
	}

	/**
	 * SFTP 연결 수동 disconnect
	 * @throws IOException
	 */
	@RequestMapping(value = "/disconnect.do", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public void disconnect() throws IOException {
	    logger.info("disconnect.do");
	    sftpService.disconnect();
	}

	/**
	 * 임시 파일경로의 잔여 기상장 파일 체크하여 짝 맞으면 이동시키도록 명령
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/move.do", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public String move() throws IOException {
	    logger.info("move.do");

	    if(sftpService.umFileMove()){
	    	return "true";
		} else {
	    	return "false";
		}
	}
}
