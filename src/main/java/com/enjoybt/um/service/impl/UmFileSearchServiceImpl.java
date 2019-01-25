package com.enjoybt.um.service.impl;

import com.enjoybt.um.service.UmFileSearchService;
import com.enjoybt.util.UmFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class UmFileSearchServiceImpl implements UmFileSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UmFileSearchServiceImpl.class);

    @Value("#{config['API_ROOT']}")
    private String ROOT;

    @Value("#{config['SIMULATION.UM.ROOT']}")
    private String SIMUL_UM_PATH;

    @Override
    public boolean hasUmFile(String nwp, String sub, String tmfc) throws Exception {
        String folder = UmFileUtil.getUmFileFolder(ROOT, nwp, sub, tmfc);
        File f = new File(folder);
        if(!f.exists()){
            throw new Exception("UmFile not Exist ERROR");
        }
        return true;
    }

    @Override
    public String getUmFileList(String nwp, String sub, String tmfc) throws Exception {
        StringBuffer sb = new StringBuffer("");
        sb.append(subDirList(ROOT, nwp, sub, tmfc));
        LOGGER.info(sb.toString());
        return sb.toString();
    }

    /**
     * 해당 조건의 기상장파일 검색 (재귀검색)
     * @param source
     * @param nwp
     * @param sub
     * @param tmfc
     * @return
     * @throws IOException
     */
    public String subDirList(String source,String nwp, String sub, String tmfc) throws IOException {

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
