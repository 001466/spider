package com.ec.spider.proxy;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ec.common.ApplicationContext;
import com.ec.common.spider.Spider;
@Service
//@EnableScheduling
public class ProxySchedule implements InitializingBean{
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(ProxySchedule.class);
	
	//@Scheduled(cron = "${spider.crawl.proxy.cron:0 1 0 * * ?}")
	private void schedule() {
		 Map<String, Proxy> spiderMap=ApplicationContext.getAPPLICATION_CONTEXT().getBeansOfType(Proxy.class, false, true);
		for (Proxy spider : spiderMap.values()) {
			System.err.println(spider);
			((Spider) spider).crawl();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {schedule();}


}
