package com.enjoybt.um.service;

import java.util.List;

public interface SftpService {
    
    public void run(String xmlData);
    
    public void downWorking();

	public void init(String ip,int port,String id,String pw);
	
	public void disconnect();
	
	public boolean downSFtp(String remote,String fileName,String local) throws Exception;
	
	public boolean deleteSFtp(String remote,String removeFileName) throws Exception;
	
	public List<String> getList();
}