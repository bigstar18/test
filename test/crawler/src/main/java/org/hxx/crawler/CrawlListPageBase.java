/**
**com.lulei.crawl.CrawlListPageBase
**/
/**  
 *@Description: 获取页面链接地址信息基类  
*/
package org.hxx.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hxx.crawler.utils.DoRegex;

public abstract class CrawlListPageBase extends CrawlBase {
	private String pageurl;

	/**
	 * @param urlStr
	 * @param charsetName
	 * @throws Exception
	 */
	public CrawlListPageBase(String urlStr, String charsetName) throws Exception {
		this(urlStr, charsetName, null);
	}

	/**
	 * @param urlStr
	 * @param charsetName
	 * @param params
	 * @throws Exception
	 */
	public CrawlListPageBase(String urlStr, String charsetName, HashMap<String, String> params) throws Exception {
		this(urlStr, charsetName, "get", params);
	}

	/**
	 * @param urlStr
	 * @param charsetName
	 * @param method
	 * @param params
	 * @throws Exception
	 */
	public CrawlListPageBase(String urlStr, String charsetName, String method, HashMap<String, String> params) throws Exception {
		pageurl = urlStr;
		readPage(urlStr, charsetName, method, params);
	}

	/**
	 * @return List<String>
	 * @Author: lulei
	 * @Description: 返回页面上需求的链接地址
	 */
	public List<String> getPageUrls() {
		List<String> pageUrls = DoRegex.getArrayList(getPageSourceCode(), getUrlRegexString(), getUrlRegexStringNum(), pageurl);
		return pageUrls == null ? new ArrayList<String>() : pageUrls;
	}

	/**
	 * @return String
	 * @Author: lulei
	 * @Description: 返回页面上需求的网址连接的正则表达式
	 */
	public abstract String getUrlRegexString();

	/**
	 * @return int
	 * @Author: lulei
	 * @Description: 正则表达式中要去的字段位置
	 */
	public abstract int getUrlRegexStringNum();
}
