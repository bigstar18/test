
package org.test.crawler;

import org.test.crawler.utils.InstallCert;

public class CrawlSSL extends CrawlBase {
	public static void main(String[] args) throws Exception {
		// installCert();
		testSelfSignedInStore();
	}

	private static void testSinged() throws Exception {
		CrawlSSL crawl = new CrawlSSL();
		crawl.readPageBySSL("https://www.baidu.com", "utf-8", "get", CrawlSSL.defaultParams, null);
		System.out.println(crawl.getPageSourceCode());
	}

	private static void testSelfSignedInStore() throws Exception {
		CrawlSSL crawl = new CrawlSSL();
		crawl.readPageBySSL("https://kyfw.12306.cn/otn/leftTicket/init", "utf-8", "get", null, CrawlBase.SSL_TRUST_KEYSTORE);
		System.out.println(crawl.getPageSourceCode());
	}

	private static void testSelfSignedAll() throws Exception {
		CrawlSSL crawl = new CrawlSSL();
		crawl.readPageBySSL("https://kyfw.12306.cn/otn/leftTicket/init", "utf-8", "get", null, CrawlBase.SSL_TRUST_ALL);
		System.out.println(crawl.getPageSourceCode());
	}

	private static void installCert() throws Exception {
		InstallCert.installCert("kyfw.12306.cn");
	}
}
