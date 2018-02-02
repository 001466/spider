package com.ec.spider.proxy.WwwXicidailiCom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.ec.common.spider.model.ProxyEntity;
import com.ec.common.spider.model.ProxyType;
@Component
class Parse {
	public static  List<ProxyEntity> parse(Document doc){
		
		int i=0;
		Elements trs = doc.select("#ip_list tr");
		List<ProxyEntity> list=new ArrayList<>();
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
			pe.setCreateby(doc.baseUri());
			pe.setCreatetime(new Date());
			list.add(pe);

		}
		return list;

	}
}
