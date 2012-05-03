package org.kevoree.slides.framework;

import org.kevoree.annotation.*;
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

@Library(name = "KevoreeWeb")
@ComponentType
@DictionaryType({@DictionaryAttribute(name = "main", defaultValue = "index.html")})
public class KevoreeSlidePage extends ParentAbstractPage {

	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
        if(getLastParam(request.getUrl()).equals("pres")){
            try {
                String slideURL = request.getUrl().replace("pres","");
                response.setRawContent(FileServiceHelper.convertStream(getClass().getClassLoader().getResourceAsStream("display.html")));
                response.setRawContent(new String(response.getRawContent()).replace("http://localhost:8080/", slideURL).getBytes());
                response.getHeaders().put("Content-Type","text/html");
            } catch (Exception e) {
                logger.error("",e);
            }
        }
		if (!load(request, response)) {
			response.setStatus(404);
		}
		return response;
	}

	public boolean load (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (FileServiceHelper.checkStaticFile(getDictionary().get("main").toString(), this, request, response)) {
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
