package org.kevoree.slides.framework;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
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

@Library(name = "KevoreeWeb")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "main", defaultValue = "index.html"),
        @DictionaryAttribute(name = "wsurl", defaultValue = "ws://localhost:8092/keynote", optional = true)
})
public class KevoreeSlidePage extends ParentAbstractPage {

	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (getLastParam(request.getUrl()).equals("keynote")) {
			try {
				String slideURL = request.getUrl().replace("keynote", "");
				response.setRawContent(FileServiceHelper.convertStream(getClass().getClassLoader().getResourceAsStream("display.html")));
				response.setRawContent(new String(response.getRawContent()).replace("http://localhost:8080/", slideURL).getBytes());
                response.setRawContent(new String(response.getRawContent()).replace("ws://localhost:8092/keynote", getDictionary().get("wsurl").toString()).getBytes());
				response.getHeaders().put("Content-Type", "text/html");
                return response;
			} catch (Exception e) {
				logger.error("", e);
			}
		}

        if (getLastParam(request.getUrl()).equals("embed")) {
            try {
                String slideURL = request.getUrl().replace("embed", "");
                response.setRawContent(FileServiceHelper.convertStream(getClass().getClassLoader().getResourceAsStream("embedder.html")));
                response.setRawContent(new String(response.getRawContent()).replace("http://localhost:8080/", slideURL).getBytes());
                response.getHeaders().put("Content-Type", "text/html");
                return response;
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        if (getLastParam(request.getUrl()).contains("ws")) {
            try {
                String roomID = getLastParam(request.getUrl()).replace("ws","");
                String newScript = "<script>" + new String(FileServiceHelper.convertStream(getClass().getClassLoader().getResourceAsStream("scripts/kslideWebSocket.js")), "UTF-8").replace("{roomID}",roomID).replace("{wsurl}",getDictionary().get("wsurl").toString()) + "</script></body>".replace("</body>", "<style>" + new String(FileServiceHelper.convertStream(getClass().getClassLoader().getResourceAsStream("styles/kstyleEmbedder.css")), "UTF-8") + "</style></body>");
                response.setRawContent(FileServiceHelper.convertStream(getClass().getClassLoader().getResourceAsStream(getDictionary().get("main").toString())));
                response.setRawContent(new String(response.getRawContent()).replace("</body>", newScript).getBytes());
                response.getHeaders().put("Content-Type", "text/html");
                return response;
            } catch (Exception e) {
                logger.error("", e);
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
