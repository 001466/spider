package com.ec.spider.proxyspider;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ec.common.ApplicationContext;
import com.ec.spider.Crawl;
@Service
public class ProxyCrawlTask extends Crawl<ProxyCrawl>{

	
	protected static final Logger LOGGER = LoggerFactory.getLogger(ProxyCrawlTask.class);

	
	public Map<String, ProxyCrawl> getSpiders() {
		return ApplicationContext.getAPPLICATION_CONTEXT().getBeansOfType(ProxyCrawl.class, false, true);
	}


}
