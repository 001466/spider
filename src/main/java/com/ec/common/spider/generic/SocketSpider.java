package com.ec.common.spider.generic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.ec.common.spider.SpiderAbstract;
import com.ec.common.spider.model.ProxyEntity;
import com.ec.common.spider.model.ProxyType;

public abstract class SocketSpider extends SpiderAbstract implements InitializingBean{
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(SocketSpider.class);

	protected Socket socket;
	protected URI uri;
	
	private static final byte CR = '\r';  
    private static final byte LF = '\n';  
    //private static final byte[] CRLF = {CR, LF};  
    
    

    
    protected static byte[] readBody(InputStream in, int contentLength) throws IOException {  
        ByteArrayOutputStream buff = new ByteArrayOutputStream(contentLength);  
        int b;  
        int count = 0;  
        while(count++ < contentLength) {  
            b = in.read(); 
            buff.write(b);  
        }  
        return buff.toByteArray();  
    }
    
    protected static String readLine(InputStream in) throws IOException {  
	        int b;  
	        ByteArrayOutputStream buff = new ByteArrayOutputStream();  
	        while((b = in.read()) != CR) {  
	            buff.write(b);  
	        }  
	        in.read();       
	        String line = buff.toString();  
	        return line;  
	}  
	 
	
	protected static Map<String, String> readHeaders(InputStream in) throws IOException {  
        Map<String, String> headers = new HashMap<String, String>();  
        String line;  
        while(!("".equals(line = readLine(in)))) {  
            String[] nv = line.split(": ");     
            if(nv.length>1)
            	headers.put(nv[0], nv[1]);  
        }  
        return headers;  
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		uri = new URI(getUrl());
		this.socket = new Socket(uri.getHost(), uri.getPort() == -1 ? 80 : uri.getPort());
	}

	@Override
	public void setProxy(ProxyEntity proxyEntity) {
		
		/* Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("username", "password"
                        .toCharArray());
            }
        });*/
		
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyEntity.getHost(),proxyEntity.getPort()));
        this.socket = new Socket(proxy);
        try {
			socket.connect(new InetSocketAddress(uri.getHost(), uri.getPort() == -1 ? 80 : uri.getPort()));
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
		}
        setProxyEntity(proxyEntity);
        
        LOGGER.info("set proxy",this.proxyEntity);
	        
		/*
		 
		Properties prop = System.getProperties();  
	    // 设置http访问要使用的代理服务器的地址  
	    prop.setProperty("http.proxyHost", "192.168.0.254");  
	    // 设置http访问要使用的代理服务器的端口  
	    prop.setProperty("http.proxyPort", "8080");  
	    // 设置不需要通过代理服务器访问的主机，可以使用*通配符，多个地址用|分隔  
	    prop.setProperty("http.nonProxyHosts", "localhost|192.168.0.*");  
	    // 设置安全访问使用的代理服务器地址与端口  
	    // 它没有https.nonProxyHosts属性，它按照http.nonProxyHosts 中设置的规则访问  
	    prop.setProperty("https.proxyHost", "192.168.0.254");  
	    prop.setProperty("https.proxyPort", "443");  
	    // 使用ftp代理服务器的主机、端口以及不需要使用ftp代理服务器的主机  
	    prop.setProperty("ftp.proxyHost", "192.168.0.254");  
	    prop.setProperty("ftp.proxyPort", "2121");  
	    prop.setProperty("ftp.nonProxyHosts", "localhost|192.168.0.*");  
	    // socks代理服务器的地址与端口  
	    prop.setProperty("socksProxyHost", "192.168.0.254");  
	    prop.setProperty("socksProxyPort", "8000");  
	    // 设置登陆到代理服务器的用户名和密码  
	    Authenticator.setDefault(new MyAuthenticator("userName", "Password"));  
		 
		 */
	}
	
	
	
	public void onFailure(Throwable ex, List<NameValuePair> params) {
		
		try{
			StringBuilder sb = new StringBuilder();
			sb.append("onFailure\r\n");
			sb.append("url:").append(getUrl()).append("\r\n");
			sb.append("params:").append(params).append("\r\n");
			if (proxyEntity != null)
				sb.append("proxy:").append(proxyEntity.getHost()).append(",").append(proxyEntity.getPort())
						.append(",").append(proxyEntity.getProtl()).append(",")
						.append(proxyEntity.getUsername()).append(",").append(proxyEntity.getPassword())
						.append("\r\n");
			sb.append("error:").append(ex.getMessage()).append("\r\n");
			sb.append("\r\n");
			LOGGER.error(sb.toString());
		}catch(Exception e){
			LOGGER.error(e.getMessage(),e);
		}finally {
			if (proxyEntity != null){
				proxyFeign.del(proxyEntity.getId());
				LOGGER.info("del proxy",proxyEntity);
			}
			setProxy(proxyFeign.get(ProxyType.http.name()).getData());
		}

	}

	
	
	/*
	 
	  static class MyAuthenticator extends Authenticator {  
		    private String user = "";  
		    private String password = "";  
		    public MyAuthenticator(String user, String password) {  
		        this.user = user;  
		        this.password = password;  
		    }  
		    protected PasswordAuthentication getPasswordAuthentication() {  
		        return new PasswordAuthentication(user, password.toCharArray());  
		    }  
		}  
	 
	 */
}
