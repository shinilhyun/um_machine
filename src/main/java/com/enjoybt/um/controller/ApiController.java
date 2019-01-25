package com.enjoybt.um.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sound.midi.MidiDevice.Info;

import com.enjoybt.um.service.UmFileSearchService;
import com.enjoybt.util.UmFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ErrorCoded;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles requests for the application home page.
 */
@Controller
public class ApiController {
	
	private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
	
	@Value("#{config['API_ROOT']}")
	private String ROOT;

	@Value("#{config['SIMULATION.UM.ROOT']}")
	private String SIMUL_UM_PATH;

    @Autowired
    UmFileSearchService umFileSearchService;

	/**
	 * @title				getUmFileList
	 * @author 	 		shin
	 * @date 				2017. 9. 7.
	 * @description    UM 파일 목록 리턴	
	 *
	 * @param nwp          년월일시(UTC) 관측시간
	 * @param sub           파일구분   pres(등압면), unis(단일면)
	 * @param tmfc          수치모델    g768(UM전지구), g512(UM전지구), r120(UM지역), l015(UM국지)
	 * @param session      
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/um/getUmList.do", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String getUmFileList(@RequestParam(value = "nwp") String nwp,
            @RequestParam(value = "sub") String sub, @RequestParam(value = "tmfc") String tmfc,
            HttpSession session, HttpServletResponse response) throws Exception {

	    String result = null;

	    try{
            umFileSearchService.hasUmFile(nwp, sub, tmfc);
            result = umFileSearchService.getUmFileList(nwp, sub, tmfc);
        }catch (Exception e) {
            response.sendError(404);
	        logger.info("getUmList error");
            e.printStackTrace();

            return "404";
        }

        return result;
    }

    /**
     * @title				getUmFile
     * @author 	 		shin
     * @date 				2017. 9. 7.
     * @description    UM 파일 다운로드	
     *
     * @param nwp          년월일시(UTC) 관측시간
     * @param sub           파일구분   pres(등압면), unis(단일면)
     * @param tmfc          수치모델    g768(UM전지구), g512(UM전지구), r120(UM지역), l015(UM국지)
     * @param hh_ef         시간
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/um/getUmFile.do", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
    public void getUmFile(@RequestParam(value = "nwp") String nwp,
            @RequestParam(value = "sub") String sub, 
            @RequestParam(value = "tmfc") String tmfc,
            @RequestParam(value = "hh_ef") String hh_ef, HttpServletResponse response) throws Exception {

        //ex) tmfc = 2017083100

		String folder = UmFileUtil.getUmFileFolder(ROOT, nwp, sub, tmfc);
		String fileName = UmFileUtil.getUmDownloadFileName(nwp, sub, hh_ef, tmfc);
	    File file = new File(folder+"/"+fileName);

	    if(file.exists()) {
            response.setHeader("Content-Disposition","attachment;filename=" + file.getName() + ";");
            response.setContentType("text/plain");
            FileInputStream fileIn = new FileInputStream(file); //파일 읽어오기
            FileCopyUtils.copy(fileIn, response.getOutputStream());
            response.flushBuffer();
        } else {
	        response.sendError(404);
        }
	}

}
