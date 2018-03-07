package com.ec.common.spider.generic;

import java.lang.reflect.Field;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.client.AsyncRestTemplate;

import com.ec.common.spider.SpiderAbstract;
import com.ec.common.spider.model.ProxyEntity;
import com.ec.common.spider.model.ProxyType;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AsyncRestTemplateSpider extends SpiderAbstract implements InitializingBean {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(AsyncRestTemplateSpider.class);
	
	@Value("${httpclient.timeout:5000}")
	private int httpclientTimeout;

	@Value("${httpclient.pooling.max-total:16}")
	private int httpclientPoolingMaxTotal;

	@Value("${httpclient.pooling.max-per-route:16}")
	private int httpclientPoolingMaxPerRoute;

	
	public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	protected static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
	}
	
	public HttpHeaders genEmptHeader(){
		HttpHeaders headers = new HttpHeaders();
		return headers;
	}
	
	public HttpHeaders genJsonHeader(){
		HttpHeaders jsonHeaders = new HttpHeaders();
		jsonHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
		
		if(getProxy()!=null){
			jsonHeaders.add("X-Forwarded-For", getProxy().getHost());
			jsonHeaders.add("Proxy-Client-IP", getProxy().getHost());
			jsonHeaders.add("WL-Proxy-Client-IP", getProxy().getHost());
		}
		return jsonHeaders;
	}
	
	public HttpHeaders genFormHeader(){
		HttpHeaders xfomrHeaders = new HttpHeaders();
		xfomrHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		if(getProxy()!=null){
			xfomrHeaders.add("X-Forwarded-For", getProxy().getHost());
			xfomrHeaders.add("Proxy-Client-IP", getProxy().getHost());
			xfomrHeaders.add("WL-Proxy-Client-IP", getProxy().getHost());
		}
		return xfomrHeaders;
	}
	
	public HttpHeaders genDownloadHeaders(){
		HttpHeaders downloadHeaders = new HttpHeaders();
		List<MediaType> list = new ArrayList<MediaType>();
		list.add(MediaType.APPLICATION_OCTET_STREAM);
		downloadHeaders.setAccept(list);
		
		if(getProxy()!=null){
			downloadHeaders.add("X-Forwarded-For", getProxy().getHost());
			downloadHeaders.add("Proxy-Client-IP", getProxy().getHost());
			downloadHeaders.add("WL-Proxy-Client-IP", getProxy().getHost());
		}
		
		return downloadHeaders;
	}

	
	 
	protected PoolingNHttpClientConnectionManager poolingNHttpClientConnectionManager() throws IOReactorException {
		ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
		PoolingNHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
		poolingHttpClientConnectionManager.setMaxTotal(httpclientPoolingMaxTotal);
		poolingHttpClientConnectionManager.setDefaultMaxPerRoute(httpclientPoolingMaxPerRoute);
		return poolingHttpClientConnectionManager;
	}
	
	protected IOReactorConfig iOReactorConfig() throws IOReactorException {
		IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
				.setConnectTimeout(httpclientTimeout)
				.setSoTimeout(httpclientTimeout)
				.setSoKeepAlive(true)
				.setSoReuseAddress(true).build();
		return ioReactorConfig;
	}
	
	
	
	protected HttpComponentsAsyncClientHttpRequestFactory httpComponentsAsyncClientHttpRequestFactory(CloseableHttpAsyncClient httpClient){
		HttpComponentsAsyncClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsAsyncClientHttpRequestFactory(httpClient);
		clientHttpRequestFactory.setConnectTimeout(httpclientTimeout);
		clientHttpRequestFactory.setReadTimeout(httpclientTimeout);
		return clientHttpRequestFactory;
	}
	

	protected AsyncRestTemplate asyncRestTemplate;
	
	@Override
	public void afterPropertiesSet() throws Exception {

		CloseableHttpAsyncClient httpClient = HttpAsyncClientBuilder.create()
				.setConnectionManager(poolingNHttpClientConnectionManager())
				.setMaxConnPerRoute(httpclientPoolingMaxPerRoute)
				.setMaxConnTotal(httpclientPoolingMaxTotal)
				.setDefaultIOReactorConfig(iOReactorConfig())
				.build();
		
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Initializing  AsyncRestTemplate:" + getUrl());
		
		asyncRestTemplate= new AsyncRestTemplate(httpComponentsAsyncClientHttpRequestFactory(httpClient));
	}
	

	public void setProxy(ProxyEntity proxyEntity) {
		
		CloseableHttpAsyncClient httpClient = null;
		setProxyEntity(proxyEntity);
		
		try{
			
			switch (proxyEntity.getProtl()) {
			
				case http:
					HttpHost httpProxy = new HttpHost(proxyEntity.getHost(), proxyEntity.getPort());
					httpClient = HttpAsyncClientBuilder.create()
							.setConnectionManager(poolingNHttpClientConnectionManager())
							.setMaxConnPerRoute(httpclientPoolingMaxPerRoute)
							.setMaxConnTotal(httpclientPoolingMaxTotal)
							.setDefaultIOReactorConfig(iOReactorConfig())
							.setProxy(httpProxy)
							.build();
					break;
					
				case https:
					HttpHost sslProxy = new HttpHost(proxyEntity.getHost(), proxyEntity.getPort());
					TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
						@Override
						public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType)
								throws CertificateException {
							return true;
						}
				    };
				    
				    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
				 
				    httpClient = HttpAsyncClients.custom()
				      .setSSLHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
				      .setSSLContext(sslContext)
				      .setConnectionManager(poolingNHttpClientConnectionManager())
				      .setMaxConnPerRoute(httpclientPoolingMaxPerRoute)
				      .setMaxConnTotal(httpclientPoolingMaxTotal)
				      .setDefaultIOReactorConfig(iOReactorConfig())
				      .setProxy(sslProxy)
				      .build();
	               break;
	               
				default:
					break;
			}
			
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Proxy  AsyncRestTemplate:" + getUrl());
			
			asyncRestTemplate= new AsyncRestTemplate(httpComponentsAsyncClientHttpRequestFactory(httpClient));
			
		}catch(Exception e){
			LOGGER.error(e.getMessage(),e);
		}
		LOGGER.info("set proxy",this.proxyEntity);
		
	}
	
	

	protected void get(String url,HttpHeaders headers) throws Exception {
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
		asyncRestTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class).addCallback(new SCallback(url,null,System.currentTimeMillis()),new FCallback(url,null,System.currentTimeMillis()));;
	}

	protected void postJson(Object params, String url,HttpHeaders headers) throws Exception {

		String postJson = mapper.writeValueAsString(params);
		HttpEntity<String> httpEntity = new HttpEntity<String>(postJson.toString(), headers);
		asyncRestTemplate.exchange(url, HttpMethod.POST, httpEntity, byte[].class)
				.addCallback(new SCallback(url,params,System.currentTimeMillis()),new FCallback(url,params,System.currentTimeMillis()));

	}

	protected void postXForm(Object params, String url,HttpHeaders headers)  {
		LinkedMultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		Map<String, String> map = toMap(params);
		multiValueMap.setAll(map);
		HttpEntity<LinkedMultiValueMap<String, String>> formEntity = new HttpEntity<LinkedMultiValueMap<String, String>>(
				multiValueMap, headers);
		asyncRestTemplate.postForEntity(url, formEntity, byte[].class)
				.addCallback(new SCallback(url,params,System.currentTimeMillis()),new FCallback(url,params,System.currentTimeMillis()));

	}
	
	protected byte[] download(String url,HttpHeaders headers) throws Exception {
		
		ListenableFuture<ResponseEntity<byte[]>> future = asyncRestTemplate.exchange(url, HttpMethod.GET,
				new HttpEntity<byte[]>(headers), byte[].class);

		//future.addCallback(new SCallback(url,url,System.currentTimeMillis()),new FCallback(url,url,System.currentTimeMillis()));
		ResponseEntity<byte[]> response = future.get();
		byte[] result = response.getBody();
		return result;

	}
	
	protected   class SCallback implements SuccessCallback<ResponseEntity<byte[]>> {
		

		protected  final Logger LOGGER = LoggerFactory.getLogger(SCallback.class);

		String url;
		Object params;
		long s;

		public SCallback(String url, Object params, long s) {
			this.url = url;
			this.params = params;
			this.s = s;
		}

		@Override
		public void onSuccess(ResponseEntity<byte[]> result) {
			
			System.err.println(new String(result.getBody()));
			
			StringBuilder sb = new StringBuilder();
			sb.append("onSuccess\r\n");
			sb.append("url:").append(url).append("\r\n");
			sb.append("params:").append(params).append("\r\n");
			if (proxyEntity != null)
				sb.append("proxy:").append(proxyEntity.getHost()).append(",").append(proxyEntity.getPort())
						.append(",").append(proxyEntity.getProtl()).append(",")
						.append(proxyEntity.getUsername()).append(",").append(proxyEntity.getPassword())
						.append("\r\n");
			sb.append("duration:").append(System.currentTimeMillis() - s).append("\r\n");
			
			
			LOGGER.warn(sb.toString());
			LOGGER.warn("\r\n");
			
			try {
				LOGGER.info(new StringBuilder(sb.toString()).append("result:\r\n").append(decode(result)).append("\r\n").toString());
			} catch (Exception e) {LOGGER.error(sb.toString());}
			LOGGER.info("\r\n");

		}
	}

	protected   class FCallback implements FailureCallback {

		protected  final Logger LOGGER = LoggerFactory.getLogger(FCallback.class);

		String url;
		Object params;
		long s;

		public FCallback(String url, Object params, long s) {
			this.url = url;
			this.params = params;
			this.s = s;
		}

		@Override
		public void onFailure(Throwable ex) {
			
			try{
				StringBuilder sb = new StringBuilder();
				sb.append("onFailure\r\n");
				sb.append("url:").append(url).append("\r\n");
				sb.append("params:").append(params).append("\r\n");
				if (proxyEntity != null)
					sb.append("proxy:").append(proxyEntity.getHost()).append(",").append(proxyEntity.getPort())
							.append(",").append(proxyEntity.getProtl()).append(",")
							.append(proxyEntity.getUsername()).append(",").append(proxyEntity.getPassword())
							.append("\r\n");
				sb.append("duration:").append(System.currentTimeMillis() - s).append("\r\n");
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
	}

	public static Map<String, String> toMap(Object object) {
		if (object == null)
			return new HashMap<String, String>();
		if (object instanceof Map) {
			return (Map<String, String>) object;
		} else {
			Class<?> clazz = object.getClass();
			Field[] fields;
			Map<String, String> map = new LinkedHashMap<String, String>();
			for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
				fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					
					field.setAccessible(true);
					try {

						Object val = field.get(object);
						if (val == null)
							continue;
						if (val instanceof Date) {
							map.put(field.getName(), format.format(val));
						} else {
							map.put(field.getName(), val.toString());
						}

					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			return map;
		}

	}
	
	protected String decode(ResponseEntity<byte[]> response) throws Exception {
		
		if (response == null) {
            return null;
        }
		StringBuilder sb=new StringBuilder();
        List<String> headers = response.getHeaders().get("Content-Encoding");// getHeaders("Content-Encoding");
        if(headers!=null&& headers.size()>0){
        	for(String h : headers){
                if(h.indexOf("gzip") > -1){
                	sb.append(new String(decode(response.getBody())));
                }else{
                	sb.append(new String(response.getBody()));
                }
            }
        }else{
        	sb.append(new String(response.getBody()));
        }
        	
        return sb.toString();
        
	}

	
	
	
	/*
	protected void delete(Object params, String url) throws Exception {
		Map<String, String> map = toMap(params);
		asyncRestTemplate.delete(url, map).addCallback(new SCallback(url,params,System.currentTimeMillis()),new FCallback(url,params,System.currentTimeMillis()));
	}
	*/

	
	

	
	


	 
}
