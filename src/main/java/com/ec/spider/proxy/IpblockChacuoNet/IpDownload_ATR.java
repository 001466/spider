package com.ec.spider.proxy.IpblockChacuoNet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import com.ec.common.spider.generic.AsyncRestTemplateSpider;
import com.ec.spider.proxy.Proxy;
@Component
public class IpDownload_ATR extends AsyncRestTemplateSpider implements Proxy {

	
	
	static String fromFilePath="country.txt";
	static String toFilePath="ips.txt";
	static FileWriter toFile=null;
	HttpHeaders h = genEmptHeader();
	
	@Override
	public String getUrl() {
		return "http://ipblock.chacuo.net/down/t_txt=c_";
	}

	@Override
	public void crawl() {
		
		

				
				h.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
				h.add("Accept-Encoding", "gzip, deflate");
				h.add("Accept-Language", "zh-CN,zh;q=0.9");
				h.add("Cache-Control", "no-cache");
				h.add("Connection", "keep-alive");
				h.add("Cookie", "__cfduid=d4c90ea157c4948842d5a322adffe6c1f1534307482; yjs_id=3da87e91ce21b9f9ecc3559f0717a5b0; bdshare_firstime=1534306707680; Hm_lvt_ef483ae9c0f4f800aefdf407e35a21b3=1534306447,1534306708,1536042496; ctrl_time=1; Hm_lpvt_ef483ae9c0f4f800aefdf407e35a21b3=1536044035");
				h.add("Host", "ipblock.chacuo.net");
				h.add("Pragma", "no-cache");
				h.add("Upgrade-Insecure-Requests", "1");
				h.add("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Safari/537.36*/");
				
				
				
				try {
					
					
					if(!new File(toFilePath).exists()){
						new File(toFilePath).createNewFile();
					}
					toFile=new FileWriter(toFilePath,true);
					
					File fromFile=new  File(fromFilePath);
					BufferedReader reader  = new BufferedReader(new FileReader(fromFile));
					String tempString = null;

					while ((tempString = reader.readLine()) != null) {

						if(tempString==null || "".equals(tempString)){
							continue;
						}
						
						
						crawl(tempString,h);

					}
					reader.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				};
				
				
		
	}

	
	public void crawl(String country,HttpHeaders h) {
		
		try {
			get(getUrl()+country.toUpperCase(), h);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
	
	
	protected class SCallback  implements SuccessCallback<ResponseEntity<byte[]>> {


		public SCallback(String url, Object params, long s) {
			
		}

		@Override
		public void onSuccess(ResponseEntity<byte[]> result) {
			
			
			try {
				Document doc = Jsoup.parse(decode(result));
				doc.setBaseUri(getUrl());
				Elements e = doc.select("pre");
				
				BufferedReader reader = new BufferedReader(new StringReader(e.text()));
				String tempString = null;
				while ((tempString = reader.readLine()) != null) {

					// 显示行号
					analyIP(tempString);

				}

				reader.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
	
	protected   class FCallback implements FailureCallback {


		String url;
		Object params;
		HttpHeaders h;
		long s;

		public FCallback(String url, Object params, HttpHeaders h,long s) {
			this.url = url;
			this.params = params;
			this.s = s;
		}

		@Override
		public void onFailure(Throwable ex) {
			System.err.println("Failur!url="+url+",ex="+ex.getMessage());
			try {
				get(url,h);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	protected SuccessCallback<ResponseEntity<byte[]>> onSuccess() {

		return new SCallback(getUrl(),null,System.currentTimeMillis());
	
	}
	
	
	protected FailureCallback onFailure(String url,Object params,HttpHeaders headers){
		return new FCallback(url,params,headers,System.currentTimeMillis());
	}
	
	static Pattern pattern = Pattern .compile("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)");

	public static void analyIP(String line) throws IOException {
		String ipS=null;
		Matcher m = pattern.matcher(line);
		if(m.find()){
			ipS=m.group();
		}
		if(ipS==null)return;
		
		String[] ipSplit=ipS.split("\\.");
		writeIP(ipSplit[0]+"."+ipSplit[1]+"."+ipSplit[2]+".*");
	}
	
	public static void writeIP(String content) {
        try {
        	toFile.write(content+"\r\n");
        } catch (IOException e) {
            System.out.println("文件写入失败！" + e);
            e.printStackTrace();
        }
    }
	
}
