package com.ec.common.spider.generic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.ec.common.spider.SpiderAbstract;
import com.ec.common.spider.model.ProxyEntity;
import com.ec.common.spider.model.ProxyType;

public abstract class URLSpider extends SpiderAbstract implements InitializingBean{

	protected static final Logger LOGGER = LoggerFactory.getLogger(URLSpider.class);
	
	protected URL URL=null;
	
	protected HttpURLConnection connection=null;
	
	protected Proxy 	connetProxy;
	
	@Override
	public void setProxy(ProxyEntity proxyEntity) {
		this.proxyEntity=proxyEntity;
		
		try {
			
			connetProxy=new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyEntity.getHost(), proxyEntity.getPort()));

			connection= (HttpURLConnection) URL.openConnection(connetProxy);
			
			connection.setRequestProperty("X-Forwarded-For", getProxy().getHost());
			connection.setRequestProperty("Proxy-Client-IP", getProxy().getHost());
			connection.setRequestProperty("WL-Proxy-Client-IP", getProxy().getHost());
		
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
		}
		
		/*
		System.getProperties().put( "proxySet", "true" );
		
		System.getProperties().put( "socksProxyHost", "127.0.0.1" );
		System.getProperties().put( "socksProxyPort", "1234" );
		
		System.setProperty("http.proxyHost", proxyEntity.getIp());  
		System.setProperty("http.proxyPort", proxyEntity.getPort().toString());  
		
		System.setProperty("http.proxyUserName", proxyEntity.getUsername());  
		System.setProperty("http.proxyPassword", proxyEntity.getPassword());  
	
		*/
	}

	

	@Override
	public void afterPropertiesSet() throws Exception {
		URL=new URL(getUrl());
		connection= (HttpURLConnection) URL.openConnection();
	}
	
	
	
	public void onFailure(HttpURLConnection connection, List<NameValuePair> params,Exception ex) throws IOException{
		 
        StringBuilder sb = new StringBuilder();
		sb.append("\r\n");
		sb.append("url:").append(getUrl()).append("\r\n");
		sb.append("params:").append(params).append("\r\n");
		sb.append("status:").append(connection.getResponseCode()).append("\r\n");
		sb.append("msg:").append(connection.getResponseMessage()).append("\r\n");
		
		if (proxyEntity != null)
			sb.append("proxy:").append(proxyEntity.getHost()).append(",").append(proxyEntity.getPort())
					.append(",").append(proxyEntity.getProtl()).append(",")
					.append(proxyEntity.getUsername()).append(",").append(proxyEntity.getPassword())
					.append("\r\n");
		sb.append("error:").append(ex.getMessage()).append("\r\n");
		sb.append("\r\n");
		LOGGER.error(sb.toString());
		setProxy(proxyFeign.get(ProxyType.http.name()).getData());
	}
	
	public void onSuccess(HttpURLConnection connection, List<NameValuePair> params) throws IOException{
		
		StringBuilder sb = new StringBuilder();
		sb.append("\r\n");
		sb.append("url:").append(getUrl()).append("\r\n");
		sb.append("params:").append(params).append("\r\n");
		sb.append("status:").append(connection.getResponseCode()).append("\r\n");
		sb.append("msg:").append(connection.getResponseMessage()).append("\r\n");

		if (proxyEntity != null)
			sb.append("proxy:").append(proxyEntity.getHost()).append(",").append(proxyEntity.getPort())
					.append(",").append(proxyEntity.getProtl()).append(",")
					.append(proxyEntity.getUsername()).append(",").append(proxyEntity.getPassword())
					.append("\r\n");
		
		
		LOGGER.warn(sb.toString());
		LOGGER.warn("\r\n");
		
		String content_encode = connection.getContentEncoding();
		String result;
        if (null != content_encode && !"".equals(content_encode) && content_encode.contains("gzip")) {
        	 result=decode(new GZIPInputStream(connection.getInputStream()));
        }else{
        	result=decode(connection.getInputStream());
        }
        
		try {
			LOGGER.info(new StringBuilder(sb.toString()).append("result:\r\n").append(result).append("\r\n").toString());
		} catch (Exception e) {LOGGER.error(sb.toString());}
		LOGGER.info("\r\n");
		
		
	}
	
	

}
