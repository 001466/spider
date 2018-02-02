package com.ec.spider.proxy.WwwXicidailiCom;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ec.common.spider.generic.AsyncRestTemplateSpider;
import com.ec.spider.proxy.Proxy;

@Component
public class WwwXicidailiCom_ATR extends AsyncRestTemplateSpider implements Proxy {

	protected static final Logger LOGGER = LoggerFactory.getLogger(WwwXicidailiCom_ATR.class);

	@Override
	public String getUrl() {
		return "http://www.xicidaili.com/wt/";
	}

	@Override
	public void crawl() {
		

		HttpHeaders h = genEmptHeader();
		h.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		h.add("Accept-Encoding", "gzip, deflate");
		h.add("Accept-Language", "zh-CN,zh;q=0.8");
		h.add("Cache-Control", "no-cache");
		h.add("Connection", "keep-alive");
		h.add("Cookie","_free_proxy_session=BAh7B0kiD3Nlc3Npb25faWQGOgZFVEkiJTg5ODM4NDgwMTM5NTUyMDk5ZjdhZDNiYjRlNmVkNTdiBjsAVEkiEF9jc3JmX3Rva2VuBjsARkkiMWUvcy9jaEZxY0EzV21EQTg2aEpiNTVHSVRnUlh5OHhKTTlrZzVLTko2VE09BjsARg%3D%3D--e886e29a6d38188981ae4cc8a97da3630848c8c3; Hm_lvt_0cf76c77469e965d2957f0553e6ecf59=1517476624; Hm_lpvt_0cf76c77469e965d2957f0553e6ecf59=1517477823");
		h.add("Host", "www.xicidaili.com");
		h.add("Pragma", "no-cache");
		h.add("Upgrade-Insecure-Requests", "1");
		h.add("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
		try {
			get(getUrl(), h, new SCallback(getUrl(), null, System.currentTimeMillis()));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}

	}

	protected class SCallback extends AsyncRestTemplateSpider.SCallback {

		public SCallback(String url, Object params, long s) {
			super(url, params, s);
		}

		@Override
		public void onSuccess(ResponseEntity<byte[]> result) {
			super.onSuccess(result);
			
			try {
				Document doc = Jsoup.parse(decode(result));
				Parse.parse(doc);
			} catch (Exception e) {}

		}

	}
	
	
	
	
	
	
	
	

}
