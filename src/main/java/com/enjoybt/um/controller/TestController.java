package com.enjoybt.um.controller;

import com.enjoybt.um.service.SftpService;
import com.enjoybt.um.service.TestService;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

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
    
	private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private TestService testService;
	
	@RequestMapping(value = "/test.do", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public String test() throws IOException {
	    logger.info("test.do");

        String result = testService.test();

		return result;
	}
}
