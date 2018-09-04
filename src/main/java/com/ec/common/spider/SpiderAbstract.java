package com.ec.common.spider;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.NameValuePair;

import com.ec.common.spider.model.ProxyEntity;

public abstract class SpiderAbstract implements Spider {

	/*@Autowired
	protected ProxyFeign proxyFeign;*/

	protected ProxyEntity proxyEntity;

	@Override
	public ProxyEntity getProxy() {
		return proxyEntity;
	}

	public void setProxyEntity(ProxyEntity proxyEntity) {
		this.proxyEntity = proxyEntity;
	}

	protected static byte[] decode(byte[] b) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(b);
		GZIPInputStream gunzip = new GZIPInputStream(in);
		byte[] buffer = new byte[256];
		int n;
		while ((n = gunzip.read(buffer)) >= 0) {
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}

	protected static String decode(java.io.InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String data;
		StringBuilder sb = new StringBuilder();
		while ((data = br.readLine()) != null) {
			sb.append(data).append("\r\n");
		}
		return sb.toString();
	}

	public String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	

}
