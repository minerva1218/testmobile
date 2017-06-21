package org.join.wfs.server;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.os.Environment;

public class HttpFileHandler implements HttpRequestHandler {

	private String webRoot;
	private int webshowcounter;

	public HttpFileHandler(final String webRoot) {
		this.webRoot = webRoot;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException {

		String target = URLDecoder.decode(request.getRequestLine().getUri(),
				"UTF-8");
		final File file = new File(this.webRoot, target);

		if (!file.exists()) { // 不存在
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			StringEntity entity = new StringEntity(
					"<html><body><h1>Error 404, file not found.</h1></body></html>",
					"UTF-8");
			response.setHeader("Content-Type", "text/html");
			response.setEntity(entity);
		} else if (file.canRead()) { // 可讀
			response.setStatusCode(HttpStatus.SC_OK);
			HttpEntity entity = null;
			if (file.isDirectory()) { // 文件夾
				entity = createDirListHtml(file, target);
				response.setHeader("Content-Type", "text/html");
			} else { // 文件
				String contentType = URLConnection
						.guessContentTypeFromName(file.getAbsolutePath());
				contentType = null == contentType ? "charset=UTF-8"
						: contentType + "; charset=UTF-8";
				entity = new FileEntity(file, contentType);
				response.setHeader("Content-Type", contentType);
			}
			response.setEntity(entity);
		} else { // 不可讀
			response.setStatusCode(HttpStatus.SC_FORBIDDEN);
			StringEntity entity = new StringEntity(
					"<html><body><h1>Error 403, access denied.</h1></body></html>",
					"UTF-8");
			response.setHeader("Content-Type", "text/html");
			response.setEntity(entity);
		}
	}

	/** 建立文件列表瀏覽網頁 */
	private StringEntity createDirListHtml(File dir, String target)
			throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		sb.append("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n<title>");
		sb.append(null == target ? dir.getAbsolutePath() : target);
		sb.append("</title>\n");
		sb.append("<link rel=\"shortcut icon\" href=\"/mnt/sdcard/.wfs/img/favicon.ico\">\n");
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/mnt/sdcard/.wfs/css/wsf.css\">\n");
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/mnt/sdcard/.wfs/css/examples.css\">\n");
		sb.append("<script type=\"text/javascript\" src=\"/mnt/sdcard/.wfs/js/jquery-1.7.2.min.js\"></script>\n");
		sb.append("<script type=\"text/javascript\" src=\"/mnt/sdcard/.wfs/js/jquery-impromptu.4.0.min.js\"></script>\n");
		sb.append("<script type=\"text/javascript\" src=\"/mnt/sdcard/.wfs/js/wsf.js\"></script>\n");
		sb.append("</head>\n<body>\n<h1 id=\"header\">");
		//sb.append(null == target ? dir.getAbsolutePath() : target);
		sb.append("<body bgcolor=\"black\" text=\"white\">\n");
		sb.append("<p id=\"header\" style=\"text-align:center\"><strong><span style=\"text-shadow:4px 4px 2px silver; font-size:72px\">Wifi Camera Image</span></strong></p>\n");
		sb.append("<p style=\"text-align:center\"><strong><span style=\"color:#a9a9a9; font-size:48px; text-align:center\">點擊縮圖即可瀏覽原圖</span></strong></p>\n");
		sb.append("<p style=\"text-align:center\"><strong><span style=\"color:#a9a9a9; font-size:48px; text-align:center\">(建議使用Google Chrome瀏覽本頁面)</span></strong></p>\n");
		sb.append("<table id=\"table\" align=\"center\">\n");
		
		/* 目錄列表 */
		File[] files = dir.listFiles();
		if (null != files) {
			sort(files); // 排序
			for (File f : files) {
				appendRow(sb, f);
			}
		}
		return new StringEntity(sb.toString(), "UTF-8");
	}

	private boolean isSamePath(String a, String b) {
		String left = a.substring(b.length(), a.length()); // a以b开头
		if (left.length() >= 2) {
			return false;
		}
		if (left.length() == 1 && !left.equals("/")) {
			return false;
		}
		return true;
	}

	/* 排序：文件夾、文件，再各安字符顺序 */
	private void sort(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				if (f1.isDirectory() && !f2.isDirectory()) {
					return -1;
				} else if (!f1.isDirectory() && f2.isDirectory()) {
					return 1;
				} else {
					return f1.toString().compareToIgnoreCase(f2.toString());
				}
			}
		});
	}

	private void appendRow(StringBuffer sb, File f) {
		
		String clazz, link, size;
		if (f.isDirectory()) {
			clazz ="";
			link = f.getName() + "/";
			size = "";
			
			sb.append("<p style=\"text-align:center\">"
					+ "<span style=\"font-size:38px\">"
					+ "<a style=\"color:#00bfff\" href=\"");
			sb.append(link);
			sb.append(WebServer.SUFFIX_ZIP);
			sb.append("\">全部打包(ZIP)下載</a></span></p>");
		} else {
			clazz = "";
			link = f.getName();
			size = formatFileSize(f.length());
			sb.append("<td class=\"operateColumn\">");
			if(webshowcounter==0)
			sb.append("<tr>\n");
			sb.append("<td>"+clazz);
			sb.append("<a href=\"" + link + "\">");
			sb.append("<img src=\"" + link + "\" width=\"315\" height=\"315\"></a><br>\n");
			sb.append("<class=\"operateColumn\"><p style=\"text-align: center\"><span style=\"font-size:30px\">");
			sb.append("<a style=\"color:#00bfff\" href=\"" + link + "\" download=\"" + link +"\">下載照片</a></span></p></td>");
			if(webshowcounter==2){
				sb.append("</tr>\n");
				webshowcounter=0;
			}
			else
				webshowcounter++;
			
		}
		
	}
	public static boolean hasWfsDir(File f) {
		String path = f.isDirectory() ? f.getAbsolutePath() + "/" : f
				.getAbsolutePath();
		return path.indexOf("/.wfs/") != -1;
	}

	/* 獲得文件大小表示 */
	private String formatFileSize(long len) {
		if (len < 1024)
			return len + " B";
		else if (len < 1024 * 1024)
			return len / 1024 + "." + (len % 1024 / 10 % 100) + " KB";
		else if (len < 1024 * 1024 * 1024)
			return len / (1024 * 1024) + "." + len % (1024 * 1024) / 10 % 100
					+ " MB";
		else
			return len / (1024 * 1024 * 1024) + "." + len
					% (1024 * 1024 * 1024) / 10 % 100 + " MB";
	}

}