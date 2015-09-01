/**
**com.lulei.crawl.news.News
**/
/**  
*@Description:   新闻类网站新闻内容 
*/
package org.test.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpException;
import org.test.crawler.utils.DoRegex;

public class News extends CrawlBase {
	/**
	 * <p>
	 * 在线性时间内抽取主题类（新闻、博客等）网页的正文。 采用了<b>基于行块分布函数</b>的方法，为保持通用性没有针对特定网站编写规则。
	 * </p>
	 * 
	 * @author Chen Xin
	 * @version 1.0, 2009-11-11
	 */
	public static class TextExtract {

		private static List<String> lines;
		private final static int blocksWidth;
		private static int threshold;
		private static String html;
		private static int start;
		private static int end;
		private static StringBuilder text;
		private static ArrayList<Integer> indexDistribution;
		private static List<String> old_lines;
		private static String oldhtml;

		static {
			lines = new ArrayList<String>();
			indexDistribution = new ArrayList<Integer>();
			text = new StringBuilder();
			blocksWidth = 3;
			/* 当待抽取的网页正文中遇到成块的新闻标题未剔除时，只要增大此阈值即可。 */
			/* 阈值增大，准确率提升，召回率下降；值变小，噪声会大，但可以保证抽到只有一句话的正文 */
			threshold = 86;
		}

		public static void setthreshold(int value) {
			threshold = value;
		}

		/**
		 * 抽取网页正文，不判断该网页是否是目录型。即已知传入的肯定是可以抽取正文的主题类网页。
		 *
		 * @param htmlStr
		 *            网页HTML字符串
		 *
		 * @return 网页正文string
		 */
		public static String parse(String htmlStr) {
			html = htmlStr;
			preProcess();
			// System.out.println(html);
			return getText();
		}

		private static void preProcess() {
			html = html.replaceAll("(?is)<!DOCTYPE.*?>", "");
			html = html.replaceAll("(?is)<!--.*?-->", ""); // remove html
															// comment
			html = html.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove
																		// javascript
			html = html.replaceAll("(?is)<style.*?>.*?</style>", ""); // remove
																		// css
			html = html.replaceAll("(?is)style=\".*?\"", ""); // remove css
			html = html.replaceAll("&.{2,5};|&#.{2,5};", " "); // remove special
																// char
			oldhtml = html;
			html = html.replaceAll("(?is)<.*?>", "");
			// <!--[if !IE]>|xGv00|9900d21eb16fa4350a3001b3974a9415<![endif]-->
		}

		private static String getText() {
			lines = Arrays.asList(html.split("\n"));
			old_lines = Arrays.asList(oldhtml.split("\n"));
			indexDistribution.clear();
			boolean haveimg_arr[] = new boolean[old_lines.size()];

			for (int i = 0; i < lines.size() - blocksWidth; i++) {
				int wordsNum = 0;
				for (int j = i; j < i + blocksWidth; j++) {
					lines.set(j, lines.get(j).replaceAll("\\s+", ""));
					wordsNum += lines.get(j).length();
				}
				indexDistribution.add(wordsNum);
				if (old_lines.get(i).toLowerCase().contains("<img")) {
					haveimg_arr[i] = true;
				}
			}

			start = -1;
			end = -1;
			boolean boolstart = false, boolend = false;
			text.setLength(0);

			for (int i = 0; i < indexDistribution.size() - 1; i++) {
				if (indexDistribution.get(i) > threshold && !boolstart) {
					if (indexDistribution.get(i + 1).intValue() != 0 || indexDistribution.get(i + 2).intValue() != 0 || indexDistribution.get(i + 3).intValue() != 0) {
						boolstart = true;
						start = i;
						continue;
					}
				}

				if (boolstart) {
					if (haveimg_arr[i]) {
						continue;
					}
					if (indexDistribution.get(i).intValue() == 0 || indexDistribution.get(i + 1).intValue() == 0) {
						end = i;
						boolend = true;
					}
				}
				if (boolend) {
					StringBuilder tmp = new StringBuilder();
					for (int ii = start; ii <= end; ii++) {
						if (haveimg_arr[ii]) {
							String img = getImg(old_lines.get(ii));
							if (img == null)
								continue;
							tmp.append(img + "\n");
							continue;
						}
						if (lines.get(ii).length() < 5)
							continue;

						tmp.append("<p>" + lines.get(ii) + "</p>\n");
					}
					String str = tmp.toString();
					if (str.contains("Copyright") || str.contains("版权所有"))
						continue;

					text.append(str);
					boolstart = boolend = false;
				}
			}

			return text.toString();
		}

		public static String getImg(String s) {
			// String img = TextTool.getBetweenOne(s, "<img", "</img>");
			// if (img == null) {
			// img = TextTool.getBetweenOne(s, "<img", "/>");
			// } else {
			// img = "<img" + img + "</img>";
			// }
			// if (img != null) {
			// img = "<img" + img + "/>";
			// }
			//
			// return img;
			return null;
		}
	}

	private String url;
	private String content;
	private String title;
	private String type;

	private static String contentRegex = "<p.*?>(.*?)</p>";
	private static String titleRegex = "<title>(.*?)</title>";
	private static int maxLength = 300;

	private static HashMap<String, String> params;

	/**
	 * 添加相关头信息，对请求进行伪装
	 */
	static {
		params = new HashMap<String, String>();
		params.put("Referer", "http://www.baidu.com");
		params.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
	}

	/**
	 * @Author:lulei
	 * @Description: 默认p标签内的内容为正文，如果正文长度查过设置的最大长度，则截取前半部分
	 */
	private void setContent() {
		String content = DoRegex.getString(getPageSourceCode(), contentRegex, 1);
		content = content.replaceAll("\n", "").replaceAll("<script.*?/script>", "").replaceAll("<style.*?/style>", "").replaceAll("<.*?>", "");
		this.content = content.length() > maxLength ? content.substring(0, maxLength) : content;
	}

	/**
	 * @Author:lulei
	 * @Description: 默认title标签内的内容为标题
	 */
	private void setTitle() {
		this.title = DoRegex.getString(getPageSourceCode(), titleRegex, 1);
	}

	public News(String url, String charset) throws Exception {
		this.url = url;
		readPage(url, charset, "get", params);
		// setContent();
		setTitle();
		this.content = TextExtract.parse(getPageSourceCode());
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static void setMaxLength(int maxLength) {
		News.maxLength = maxLength;
	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws HttpException
	 * @throws IOException
	 * @Author:lulei
	 * @Description: 测试用例
	 */
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 1; i++) {
			// News news = new
			// News("http://we.sportscn.com/viewnews-1634777.html", "utf-8");
			News news = new News("http://sports.sina.com.cn/o/2015-08-05/doc-ifxfpcxz4787072.shtml", "utf-8");

			// System.out.println(news.getPageSourceCode());
			System.out.println(news.getContent());
			// System.out.println(news.getTitle());
		}
	}
}
