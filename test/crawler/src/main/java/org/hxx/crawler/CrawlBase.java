/**
**com.lulei.crawl.CrawlBase
**/
/**  
*@Description: 获取网页信息基类
*/
package org.hxx.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;
import org.hxx.crawler.utils.DoRegex;
import org.hxx.crawler.utils.InstallCert;

public abstract class CrawlBase {
	private static HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
		// 默认最大访问次数
		private int maxConnectTimes = 3;

		public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
			if (executionCount >= maxConnectTimes) {
				// Do not retry if over max retry count
				return false;
			}
			if (exception instanceof InterruptedIOException) {
				// Timeout
				return false;
			}
			if (exception instanceof UnknownHostException) {
				// Unknown host
				return false;
			}
			if (exception instanceof ConnectTimeoutException) {
				// Connection refused
				return false;
			}
			if (exception instanceof SSLException) {
				// SSL handshake exception
				return false;
			}
			HttpClientContext clientContext = HttpClientContext.adapt(context);
			HttpRequest request = clientContext.getRequest();
			boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
			if (idempotent) {
				// Retry if the request is considered idempotent
				return true;
			}
			return false;
		}
	};

	public static HashMap<String, String> defaultParams = new HashMap<String, String>();

	static {
		defaultParams.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
	}

	// 链接源代码
	private String pageSourceCode = "";
	// 返回头信息
	private Header[] responseHeaders = null;
	private CloseableHttpClient sslHttpClient = createSSLClient();

	// 连接超时时间
	private static int connectTimeout = 2000;
	// 连接读取时间
	private static int readTimeout = 5000;
	public static String SSL_TRUST_KEYSTORE = "SSL_TRUST_KEYSTORE";
	public static String SSL_TRUST_ALL = "SSL_TRUST_ALL";

	private static RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeout).setConnectTimeout(connectTimeout).build();

	private static CloseableHttpClient httpClient = createCustomBuilder().build();

	/**
	 * @param urlStr
	 * @param charsetName
	 * @param method
	 * @param params
	 * @return
	 * @throws Exception
	 * @Author: lulei
	 * @Description: method方式访问页面
	 */
	public void readPage(String urlStr, String charsetName, String method, HashMap<String, String> params) throws Exception {
		if ("post".equalsIgnoreCase(method)) {
			readPageByPost(urlStr, charsetName, params);
		} else {
			readPageByGet(urlStr, charsetName, params);
		}
	}

	public void readPageBySSL(String urlStr, String charsetName, String method, HashMap<String, String> params, String trust) throws Exception {
		if (SSL_TRUST_KEYSTORE.equals(trust))
			sslHttpClient = createSSLSelfSignedFromStore();
		else if (SSL_TRUST_ALL.equals(trust))
			sslHttpClient = createSSLSelfSignedAll();

		if ("post".equalsIgnoreCase(method)) {
			readPageByPost(urlStr, charsetName, params);
		} else {
			readPageByGet(urlStr, charsetName, params);
		}
	}

	/**
	 * @param urlStr
	 * @param charsetName
	 * @param xmlString
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws HttpException
	 * @throws IOException
	 * @Author:lulei
	 * @Description: 提交xml流参数
	 */
	public void readPageByPostXml(String urlStr, String charsetName, String xmlString) throws Exception {
		HttpPost postMethod = createPostMethodXml(urlStr, xmlString);
		readPage(urlStr, postMethod, charsetName);
	}

	/**
	 * @param urlStr
	 * @param charsetName
	 * @param jsonString
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws HttpException
	 * @throws IOException
	 * @Author:lulei
	 * @Description: 提交json流参数
	 */
	public void readPageByPostJson(String urlStr, String charsetName, String jsonString) throws Exception {
		HttpPost postMethod = createPostMethodJson(urlStr, jsonString);
		readPage(urlStr, postMethod, charsetName);
	}

	/**
	 * @param urlStr
	 * @param charsetName
	 * @param params
	 * @return 访问是否成功
	 * @throws Exception
	 * @Author: lulei
	 * @Description: Get方式访问页面
	 */
	private void readPageByGet(String urlStr, String charsetName, HashMap<String, String> params) throws Exception {
		HttpGet getMethod = createGetMethod(urlStr, params, charsetName);
		readPage(urlStr, getMethod, charsetName);
	}

	/**
	 * @param urlStr
	 * @param charsetName
	 * @param params
	 * @return 访问是否成功
	 * @throws Exception
	 * @throws HttpException
	 * @throws IOException
	 * @Author: lulei
	 * @Description: Post方式访问页面
	 */
	private void readPageByPost(String urlStr, String charsetName, HashMap<String, String> params) throws Exception {
		HttpPost postMethod = createPostMethod(urlStr, params, charsetName);
		readPage(urlStr, postMethod, charsetName);
	}

	/**
	 * @param request
	 * @param charset
	 * @param urlStr
	 * @return 访问是否成功
	 * @throws Exception
	 * @throws HttpException
	 * @throws IOException
	 * @Author: lulei
	 * @Description: 读取页面信息和头信息
	 */
	private void readPage(String url, HttpUriRequest request, String charset) throws Exception {
		CloseableHttpResponse response = null;
		try {
			if (url.startsWith("https://"))
				response = sslHttpClient.execute(request);
			else
				response = httpClient.execute(request);
			// 获取头信息
			responseHeaders = response.getAllHeaders();
			pageSourceCode = EntityUtils.toString(response.getEntity(), charset);

			setCookieStore(response);
			// System.out.println(pageSourceCode.length());
		} finally {
			if (response != null)
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private static void setCookieStore(HttpResponse httpResponse) {
		// CookieStore cookieStore = new BasicCookieStore();
		String cookieStr = httpResponse.getFirstHeader("Set-Cookie").getValue();
		// Set-Cookie: IPLOC=CN3201; expires=Fri, 14-Aug-15 07:00:44 GMT; domain=.sogou.com; path=/

		if (cookieStr != null && !cookieStr.trim().isEmpty()) {
			String[] strs = cookieStr.split(";");
			for (int i = 0; i < strs.length; i++) {
				System.out.println(strs[i]);
			}
		}

		// 新建一个Cookie
		// BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", JSESSIONID);
		// cookie.setVersion(0);
		// cookie.setDomain("127.0.0.1");
		// cookie.setPath("/CwlProClient");
		// cookieStore.addCookie(cookie);
	}

	/**
	 * @param urlStr
	 * @param params
	 * @return GetMethod
	 * @Author: lulei
	 * @Description: 设置get请求参数
	 */
	private HttpGet createGetMethod(String urlStr, HashMap<String, String> params, String charset) {
		urlStr = encodeUrlCh(urlStr);
		HttpGet getMethod = new HttpGet(urlStr);
		getMethod.setConfig(requestConfig);
		if (params == null) {
			return getMethod;
		}
		Iterator<Entry<String, String>> iter = params.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			getMethod.addHeader(key, val);
		}
		return getMethod;
	}

	/**
	 * @param urlStr
	 * @param params
	 * @return PostMethod
	 * @throws Exception
	 * @Author: lulei
	 * @Description: 设置post请求参数
	 */
	private HttpPost createPostMethod(String urlStr, HashMap<String, String> params, String charset) throws Exception {
		urlStr = encodeUrlCh(urlStr);
		HttpPost postMethod = new HttpPost(urlStr);
		postMethod.setConfig(requestConfig);
		if (params == null) {
			return postMethod;
		}

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		Iterator<Entry<String, String>> iter = params.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			formparams.add(new BasicNameValuePair(key, val));
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, CharsetUtils.get(charset));
		postMethod.setEntity(entity);

		return postMethod;
	}

	/**
	 * @param urlStr
	 * @param xmlString
	 * @return
	 * @throws UnsupportedEncodingException
	 * @Author:lulei
	 * @Description: 设置xml格式流参数
	 */
	private HttpPost createPostMethodXml(String urlStr, String xmlString) throws UnsupportedEncodingException {
		urlStr = encodeUrlCh(urlStr);
		HttpPost postMethod = new HttpPost(urlStr);
		postMethod.setConfig(requestConfig);
		StringEntity entity = new StringEntity(xmlString, ContentType.TEXT_XML.withCharset(Consts.UTF_8));
		postMethod.setEntity(entity);
		return postMethod;
	}

	/**
	 * @param urlStr
	 * @param jsonString
	 * @return
	 * @throws UnsupportedEncodingException
	 * @Author:lulei
	 * @Description: 设置json格式流参数
	 */
	private HttpPost createPostMethodJson(String urlStr, String jsonString) throws UnsupportedEncodingException {
		urlStr = encodeUrlCh(urlStr);
		HttpPost postMethod = new HttpPost(urlStr);
		postMethod.setConfig(requestConfig);
		StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON.withCharset(Consts.UTF_8));
		postMethod.setEntity(entity);
		return postMethod;
	}

	/**
	 * @param url
	 * @return
	 * @Author:lulei
	 * @Description: 对URL中的中文做预处理
	 */
	private String encodeUrlCh(String url) {
		try {
			return DoRegex.encodeUrlCh(url);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return url;
		}
	}

	private static CloseableHttpClient createSSLClient() {
		try {
			return createCustomBuilder().setSSLSocketFactory(SSLConnectionSocketFactory.getSocketFactory()).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return HttpClients.createDefault();
	}

	private static CloseableHttpClient createSSLSelfSignedFromStore() {
		try {
			File file = InstallCert.getKeyStroe();
			InputStream in = new FileInputStream(file);
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(in, "changeit".toCharArray());
			in.close();
			// Trust own CA and all self-signed certs
			SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(ks, new TrustSelfSignedStrategy()).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			return createCustomBuilder().setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return HttpClients.createDefault();
	}

	private static CloseableHttpClient createSSLSelfSignedAll() {
		try {
			RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory> create();
			// ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
			// registryBuilder.register("http", plainSF);
			// 指定信任密钥存储对象和连接套接字工厂
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			SSLContext sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(trustStore, new AnyTrustStrategy()).build();
			LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			registryBuilder.register("https", sslSF).build();

			return createCustomBuilder().setSSLSocketFactory(sslSF).build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return HttpClients.createDefault();
	}

	public static class AnyTrustStrategy implements TrustStrategy {
		@Override
		public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			return true;
		}
	}

	private static HttpClientBuilder createCustomBuilder() {
		// HttpHost proxy = new HttpHost("someproxy", 8080);
		// DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

		return HttpClients.custom().setRetryHandler(myRetryHandler).setRedirectStrategy(new LaxRedirectStrategy());
	}

	/**
	 * @return String
	 * @Author: lulei
	 * @Description: 获取网页源代码
	 */
	public String getPageSourceCode() {
		return pageSourceCode;
	}

	/**
	 * @return Header[]
	 * @Author: lulei
	 * @Description: 获取网页返回头信息
	 */
	public Header[] getHeader() {
		return responseHeaders;
	}
}
