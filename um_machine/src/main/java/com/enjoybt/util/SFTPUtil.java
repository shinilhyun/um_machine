package com.enjoybt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPUtil{

	private static Session session = null;
    private static Channel channel = null;
    private static ChannelSftp channelSftp = null;

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

     */

    public static boolean download(String dir, String downloadFileName, String path) {

        InputStream in = null;
        FileOutputStream out = null;
        ChecksumUtill checksum = new ChecksumUtill();
        
        try {
            channelSftp.cd(dir);
            in = channelSftp.get(downloadFileName);
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        try {
            // 폴더경로 맞추기
            String[] arr = downloadFileName.split("\\.");
            System.out.println(arr[1]);
            
            path += "/" + downloadFileName.substring(0, 4) + "." + arr[1].substring(0, 8)
                    + ".t" + arr[1].substring(8,10) + "z";
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
            
//            //checksum 구현 불가능 (sftp에서는 로컬 파일만 checksum 체크 가능)
//            if(checksum.getCRC32Value(path + "/" + downloadFileName) != checksum.getRemoteCRC32Value(in,f.length())){
//                System.out.println("체크섬 불일치");
//                return false;
//            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        System.out.println(downloadFileName+"다운로드 완료");
        return true;
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
        channelSftp.quit();
    }
}








