package com.ec.common.spider.model;

import com.ec.common.model.BaseEntity;

public class ProxyEntity extends BaseEntity{


	String 		id;
	String 		host;
	Integer 	port;
	ProxyType 	protl;
	String 		username;
	String 		password;

	public ProxyEntity() {
	};

	public ProxyEntity(String ip, Integer port, ProxyType type) {
		this.host = ip;
		this.port = port;
		this.protl = type;
	};

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public ProxyType getProtl() {
		return protl;
	}

	public void setProtl(ProxyType protl) {
		this.protl = protl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}


}
