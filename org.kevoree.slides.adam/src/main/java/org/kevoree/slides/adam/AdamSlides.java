package org.kevoree.slides.adam;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.slides.template.KevoreeSlidesShowerTemplate;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/04/12
 * Time: 10:57
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "slideshow")
@ComponentType
public class AdamSlides extends KevoreeSlidesShowerTemplate {
	private final String basePage = "index.html";

	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		logger.info("Try to get page");
		if (!super.loadTemplate(request, response)) {
			if (!loadSlides(request, response)) {
				response.setStatus(404);
			}
		}
		return response;
	}

	public boolean loadSlides (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (FileServiceHelper.checkStaticFile(basePage, this, request, response)) {
			if (request.getUrl().equals("/") || request.getUrl().endsWith(".html")) {
				String pattern = getDictionary().get("urlpattern").toString();
				if (pattern.endsWith("**")) {
					pattern = pattern.replace("**", "");
				}
				if (!pattern.endsWith("/")) {
					pattern = pattern + "/";
				}

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
