package com.ec.common.spider.dao;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ec.common.model.Response;
import com.ec.common.spider.model.ProxyEntity;

@FeignClient(name = "proxy")
public interface ProxyFeign {
	@RequestMapping("/get/{protl}")
	public Response<ProxyEntity> get(@RequestParam("protl") String protl);
	
	@RequestMapping("/add")
	public Response<String> add(@RequestBody ProxyEntity proxyEntity);
	
	@RequestMapping("/batchadd")
	public Response<String> batchadd(@RequestBody List<ProxyEntity> list);
	
	
	@RequestMapping("/del{id}")
	public Response<String> del(@RequestParam("id") String id);
}
