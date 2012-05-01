package org.kevoree.slides.template;

import org.kevoree.annotation.ComponentFragment;
import org.kevoree.annotation.Library;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/04/12
 * Time: 10:53
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@Library(name = "slideshow")
@ComponentFragment
public abstract class KevoreeSlidesShowerTemplate extends ParentAbstractPage {
	private final String basePage = "index.html";

	public boolean loadTemplate (KevoreeHttpRequest request, KevoreeHttpResponse response) {
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
