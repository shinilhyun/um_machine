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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	    try{

            String folder = ROOT + "/" + nwp + "." + tmfc.substring(0, 8)+".t"+tmfc.substring(8)+"z";
            File f = new File(folder);

            if(!f.exists()){
                response.sendError(404);
            }

        }catch (Exception e) {
            response.sendError(404);
	        logger.info("폴더검색에러");
            e.printStackTrace();

            return "404";
        }

        StringBuffer sb = new StringBuffer("");
        sb.append(subDirList(ROOT, nwp, sub, tmfc));
      
        logger.info(sb.toString());
        
        return sb.toString();
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
//    @RequestMapping(value = "/url/nwp_file_down.php?", method = {RequestMethod.POST, RequestMethod.GET})
    @RequestMapping(value = "/um/getUmFile.do", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
    public void getUmFile(@RequestParam(value = "nwp") String nwp,
            @RequestParam(value = "sub") String sub, 
            @RequestParam(value = "tmfc") String tmfc,
            @RequestParam(value = "hh_ef") String hh_ef, HttpServletResponse response) throws Exception {

        //ex) tmfc = 2017083100

        String folder;

        //시뮬레이션을 위한 UM 요청이면 경로 다르게
        int chekYear = Integer.parseInt(tmfc.substring(0,4));

        if(chekYear < 2018){
            folder = SIMUL_UM_PATH + "/" + nwp + "." + tmfc.substring(0, 8)+".t"+tmfc.substring(8)+"z";
        } else {
            folder = ROOT + "/" + nwp + "." + tmfc.substring(0, 8)+".t"+tmfc.substring(8)+"z";
        }

        String fileName = nwp + "_v070_erea_" + sub + "_h" + hh_ef + "." + tmfc + ".gb2";
	    File file = new File(folder+"/"+fileName);

	    if(file.exists()) {
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + file.getName() + ";");
            response.setContentType("text/plain");

            FileInputStream fileIn = new FileInputStream(file); //파일 읽어오기
            FileCopyUtils.copy(fileIn, response.getOutputStream());

            response.flushBuffer();
        } else {
	        response.sendError(404);
        }
	}
    
    public String subDirList(String source,String nwp, String sub, String tmfc) throws IOException{

		System.out.println("source - " +source);
		File dirFile = new File(source);
        File []fileList = dirFile.listFiles();
        StringBuffer sb = new StringBuffer("");
        
        for (File tempFile : fileList) {
            
            if (tempFile.isFile()) {
                
                if (tempFile.getName().contains(nwp) && tempFile.getName().contains(sub)
                        && tempFile.getName().contains(tmfc)) {
                    
                    sb.append(tempFile.getName() + ", " + tempFile.length() + ",= \n");
                }
            } else if (tempFile.isDirectory()) {
                sb.append(subDirList(tempFile.getCanonicalPath().toString(), nwp, sub, tmfc));
            }
        }
        
        return sb.toString();
    }

}
