package com.enjoybt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


public class SFTPUtil{

    private static SFTPUtil sftpUtil;
    
    private SFTPUtil(){}
    
    public static synchronized SFTPUtil getInstance() {
        
        if (sftpUtil == null) {
           sftpUtil = new SFTPUtil();
        }
        
        return sftpUtil;
    }
    
	private static Session session = null;
    private static Channel channel = null;
    public static ChannelSftp channelSftp = null;
    private static String REMOTE_ROOT = "/home/volcano/kma2volcano";

    /**

     * 서버와 연결에 필요한 값들을 가져와 초기화 시킴

     *

     * @param host

     *            서버 주소

     * @param userName

     *            접속에 사용될 아이디

     * @param password

     *            비밀번호

     * @param port

     *            포트번호

     */

    public static void init(String host, String userName, String password, int port) {

        JSch jsch = new JSch();

        try {
            session = jsch.getSession(userName, host, port);
            session.setPassword(password);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();

        } catch (JSchException e) {
            e.printStackTrace();
        }
        channelSftp = (ChannelSftp) channel;
    }

    /**

     * 하나의 파일을 업로드 한다.

     *

     * @param dir

     *            저장시킬 주소(서버)

     * @param file

     *            저장할 파일

     */

    public static void upload(String dir, File file) {
        FileInputStream in = null;

        try {
            in = new FileInputStream(file);
            channelSftp.cd(dir);
            channelSftp.put(in, file.getName());
            
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {

            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**

     * 하나의 파일을 다운로드 한다.

     *

     * @param dir
     *            remote 경로(서버)
     * @param downloadFileName
     *            다운로드할 파일
     * @param path
     *            저장될 공간(local)
     * @throws SftpException

     */

    public static boolean download(String dir, String downloadFileName, String path) throws SftpException {

        InputStream in = null;
        FileOutputStream out = null;
        ChecksumUtill checksum = new ChecksumUtill();
        
        channelSftp.cd(dir);
        in = channelSftp.get(downloadFileName);

        try {
            // 폴더경로 맞추기
            String[] arr = downloadFileName.split("\\.");
            System.out.println(arr[1]);
            
            System.out.println(path);
            File targetPath = new File(path);
            
            if (!targetPath.exists()) {     // 폴더 없으면 생성
                targetPath.mkdirs();
            }
            
            System.out.println("targetPath+fileName : " + path + "/" + downloadFileName);
            File f = new File(path + "/"+ downloadFileName);
            out = new FileOutputStream(f);
            int i;
            while ((i = in.read()) != -1) {
                out.write(i);
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return false;
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                return false;
            }
        }
        
        System.out.println(downloadFileName+"다운로드 완료");
        return true;
    }
    
    public static List<String> getList() {
        List<String> fileList = new ArrayList<String>();
        try{
            
            //경로 확인 필요
            channelSftp.cd(REMOTE_ROOT);
            
            Vector<LsEntry> files = channelSftp.ls("*.gb2");
            
            for (ChannelSftp.LsEntry file : files) {
                fileList.add(file.getFilename());
            }
        } catch(Exception e) {
            System.out.println("sftp list exception");
        }
        return fileList;
    }
    
    /**
     * 
     */
    public static boolean delete(String dir, String deleteFileName) {
        try {
            channelSftp.cd(dir);
            channelSftp.rm(deleteFileName);
            
        } catch (SftpException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**

     * 서버와의 연결을 끊는다.

     */
    public static void disconnection() {
        try{
                session.disconnect();
                channelSftp.quit();
                channel.disconnect();
                
                channelSftp = null;
                session = null;
                channel = null;
            
        } catch (NullPointerException ne) {
            //System.out.println("이미 disconnect 되어있음");
        }
    }
}








