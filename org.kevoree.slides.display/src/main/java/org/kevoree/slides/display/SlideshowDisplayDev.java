package org.kevoree.slides.display;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;

import java.io.File;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/04/12
 * Time: 10:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "slideshow")
@ComponentType
@DictionaryType({@DictionaryAttribute(name = "folder")})
public class SlideshowDisplayDev extends ParentAbstractPage {

	private final String basePage = "index.html";
	private File devDirectory;

	@Start
	public void startPage () {
		super.startPage();
		File f1 = new File((String) super.getDictionary().get("folder"));
		if (f1.isDirectory()) {
			devDirectory = f1;
		}
	}
	/*@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		logger.info("SlideShow is called");
		// TODO return index.html
		// TODO return script.js
		// TODO ask content by sending a request to the slides component ?
		return response;
	}*/

	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		logger.info("Try to get page");
		if (!load(request, response, devDirectory.getAbsolutePath())) {
			response.setStatus(404);
		}
		return response;
	}

	public boolean load (KevoreeHttpRequest request, KevoreeHttpResponse response, String baseDir) {
		if (FileServiceHelper.checkStaticFileFromDir(basePage, this, request, response, baseDir)) {
			String pattern = getDictionary().get("urlpattern").toString();
			if (pattern.endsWith("**")) {
				pattern = pattern.replace("**", "");
			}
			if (!pattern.endsWith("/")) {
				pattern = pattern + "/";
			}
			if (pattern.equals(request.getUrl() + "/") || request.getUrl().endsWith(".html") || request.getUrl().endsWith(".css")) {
				if (response.getRawContent() != null) {
					response.setRawContent(new String(response.getRawContent()).replace("{urlpattern}", pattern).getBytes());
				} else {
					response.setContent(response.getContent().replace("{urlpattern}", pattern));
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
