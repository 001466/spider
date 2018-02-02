package com.ec.spider.proxy.WwwXicidailiCom;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ec.common.spider.model.ProxyEntity;
import com.ec.common.spider.model.ProxyType;

class Parse {
	public static  void parse(Document doc){
		
		
		int i=0;
		Elements trs = doc.select("#ip_list tr");
		
		
		for (Element tr : trs) {
			if(i==0){i=1;continue;}
			
			Element hostN=tr.child(1);
			Element portN=tr.child(2);
			Element anonymousN=tr.child(4);
			Element protlN=tr.child(5);
			
			String host=hostN.text();
			String port=portN.text();
			String protl=protlN.text();
			String anonymous=anonymousN.text();
			
			anonymous=anonymous.equals("透明")?"0":"1";
			ProxyEntity pe=new ProxyEntity(host, Integer.parseInt(port), ProxyType.valueOf(protl.toLowerCase()), Short.parseShort(anonymous));
			System.err.println(pe);

		}

	}
}
