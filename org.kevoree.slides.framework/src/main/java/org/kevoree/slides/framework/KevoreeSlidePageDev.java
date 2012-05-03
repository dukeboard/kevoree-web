package org.kevoree.slides.framework;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import java.io.File;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/04/12
 * Time: 10:53
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@Library(name = "KevoreeWeb")
@ComponentType
@DictionaryType({@DictionaryAttribute(name = "templateFolder")})
public class KevoreeSlidePageDev extends KevoreeSlidePage {

	private File devDirectory;

	@Start
	public void startPage () {
		super.startPage();
		File f1 = new File((String) super.getDictionary().get("templateFolder"));
		if (f1.isDirectory()) {
			logger.info(f1.getAbsolutePath());
			devDirectory = f1;
		}
	}


	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (!load(request, response, devDirectory.getAbsolutePath())) {
              super.process(request,response);
		}
		return response;
	}

	public boolean load (KevoreeHttpRequest request, KevoreeHttpResponse response, String baseDir) {
		if (FileServiceHelper.checkStaticFileFromDir(getDictionary().get("main").toString(), this, request, response, baseDir)) {
			String pattern = getDictionary().get("urlpattern").toString();
			if (pattern.endsWith("**")) {
				pattern = pattern.replace("**", "");
			}
			if (!pattern.endsWith("/")) {
				pattern = pattern + "/";
			}
			logger.info(pattern);
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
