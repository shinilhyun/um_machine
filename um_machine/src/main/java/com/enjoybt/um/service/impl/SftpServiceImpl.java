package com.enjoybt.um.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.enjoybt.um.service.SftpService;
import com.enjoybt.util.SFTPUtil;

@Service
public class SftpServiceImpl implements SftpService {
	
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
		SFTPUtil.disconnection();
	}
	
	public List<String> getList(){
	    return SFTPUtil.getList();
	}
}
