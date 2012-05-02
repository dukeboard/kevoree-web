package org.kevoree.slides.display;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;

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
public class SlideshowDisplay extends ParentAbstractPage {

	/*@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		logger.info("SlideShow is called");
		// TODO return index.html
		// TODO return script.js
		// TODO ask content by sending a request to the slides component ?
		return response;
	}*/
	private final String basePage = "index.html";

		@Override
		public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
			logger.info("Try to get page");
			if (!load(request, response)) {
				response.setStatus(404);
			}
			return response;
		}

		public boolean load (KevoreeHttpRequest request, KevoreeHttpResponse response) {
			if (FileServiceHelper.checkStaticFile(basePage, this, request, response)) {
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
