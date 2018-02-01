package com.ec.common.spider;

import com.ec.common.spider.model.ProxyEntity;

public interface Spider {
	
	public String getUrl();

	public void crawl();

	public void setProxy(ProxyEntity proxyEntity);
	
	public ProxyEntity getProxy();

}
