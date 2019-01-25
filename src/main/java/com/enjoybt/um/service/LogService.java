package com.enjoybt.um.service;

import java.util.HashMap;
import java.util.Map;

/**
 * UmFile download log manage service
 */
public interface LogService {

    /**
     * insert download start log
     *
     * @param xmlData the xml data
     * @return the log sn
     */
    public int insertDownStartLog(String xmlData);

    /**
     *  update all file Temp folder download complete time
     *
     * @param log_sn the log sn
     */
    public void updateTempTime(int log_sn);

    /**
     * Update all download process end log.
     *
     * @param log_sn the log sn
     * @param flag   the flag
     */
    public void updateEndLog(int log_sn, String flag);

    /**
     * Insert file start log int.
     *
     * @param log_sn   the log sn
     * @param fileName the file name
     * @return the int
     */
    public int insertFileStartLog(int log_sn, String fileName);

    /**
     * Update each file temp folder download complete time.
     *
     * @param file_no the file no
     */
    public void updateFileTempLog(int file_no);

    /**
     * Update each file download end log.
     *
     * @param fileNo the file no
     * @param flag   the flag (Y/F/N)
     */
    public void updateFileEndLog(int fileNo, String flag);

    /**
     * Update file comment.
     *
     * @param fileNo  the file no
     * @param comment the comment
     */
    public void updateFileComment(int fileNo, String comment);

    /**
     * Gets last file no from filename.
     *
     * @param fileName the file name
     * @return the last file no
     */
    public int getLastFileNoFromFilename(String fileName);
}
