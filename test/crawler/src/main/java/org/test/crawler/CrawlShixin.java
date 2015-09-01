
package org.test.crawler;

public class CrawlShixin extends CrawlBase {

	public CrawlShixin() {
		defaultParams.put("Referer", "http://shixin.court.gov.cn/");
		defaultParams.put("DNT", "1");
		defaultParams.put("Accept-Language", "zh-CN,zh;q=0.8");
		defaultParams.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
	}

	public static void main(String[] args) throws Exception {
	}
}
