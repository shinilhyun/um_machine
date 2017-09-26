package com.enjoybt.um.service;

public interface SftpService {

	public void init(String ip,int port,String id,String pw);
	
	public void disconnect();
	
	public boolean downSFtp(String remote,String fileName,String local) throws Exception;
	
	public boolean deleteSFtp(String remote,String removeFileName) throws Exception;
}