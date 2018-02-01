package com.ec.common.spider;

public enum ProxyType {

	/**
	 * represents a direct connection, or the absence of a proxy.
	 */
	direct,
	/**
	 * represents proxy for high level protocols such as http or ftp.
	 */
	http,

	https,
	/**
	 * represents a socks (v4 or v5) proxy.
	 */
	socks


}
