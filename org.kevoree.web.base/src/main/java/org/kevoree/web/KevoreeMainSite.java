package org.kevoree.web;


import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/03/12
 * Time: 11:12
 */

@ComponentType
//@Provides({@ProvidedPort(name = "gitnews",type = PortType.MESSAGE)})
@DictionaryType({@DictionaryAttribute(name = "webSocketLocation", defaultValue = "http://localhost:8092")})
public class KevoreeMainSite extends ParentAbstractPage {

	protected String basePage = "overview.html";

    protected HashMap<String, byte[]> contentRawCache = new HashMap<String, byte[]>();
	protected HashMap<String, String> contentTypeCache = new HashMap<String, String>();
	protected PageRenderer krenderer = null;
	protected Boolean useCache = true;
	protected DownloadHelper downloadHelper;
	private SlideListPage slideList;

	@Override
	public void startPage () {
		downloadHelper = new DownloadHelper(getBootStrapperService(), this);
		downloadHelper.start();

		krenderer = new PageRenderer(false, null);
		super.startPage();
		contentRawCache.clear();
		contentTypeCache.clear();

		String wsUrl = getDictionary().get("webSocketLocation").toString();
		try {
			slideList = new SlideListPage(this, wsUrl);
            getModelService().registerModelListener(slideList);
		} catch (Exception e) {
			logger.warn("Unable to initialize Talks page", e);
		}

	}

	@Override
	public void updatePage () {
		super.updatePage();
		contentRawCache.clear();
		contentTypeCache.clear();
	}

	@Override
	public void stopPage () {
		super.stopPage();
		contentRawCache.clear();
		contentTypeCache.clear();
		downloadHelper.stop();
		getModelService().unregisterModelListener(slideList);
	}

	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
        logger.debug("receive request: {}", request.getUrl());
		if (getLastParam(request.getUrl()).startsWith("talks/") || getLastParam(request.getUrl()).startsWith("/talks/")) {
			logger.debug("forward request to slide pages for url: {} with completeURL = {}", getLastParam(request.getUrl()), request.getCompleteUrl());
			return forward(request, response);
		}

		if (useCache) {
			if (contentTypeCache.containsKey(request.getUrl())) {
				response.setRawContent(contentRawCache.get(request.getUrl()));
				response.getHeaders().put("Content-Type", contentTypeCache.get(request.getUrl()));
				return response;
			}
		}

		if (FileServiceHelper.checkStaticFile(basePage, this, request, response)) {
			if (request.getUrl().equals("/") || request.getUrl().endsWith(".html") || request.getUrl().endsWith(".css") || request.getUrl().endsWith(".jnlp")) {
				// FIXME according to KevoreeSlidesShowerTemplate
				replaceGlobalVariables(request, response);
			}
			if (useCache) {
				cacheResponse(request, response);
			}
			return response;
		}
		if (krenderer.checkForTemplateRequest(basePage, this, request, response)) {
			replaceGlobalVariables(request, response);
			if (useCache && !request.getUrl().equals("/download")) {
				cacheResponse(request, response);
			}
			return response;
		}
		if (downloadHelper.checkForDownload(basePage, this, request, response)) {
			return response;
		}
		response.setContent("Bad request from " + getName() + "@" + getNodeName());
		return response;
	}

	public void cacheResponse (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		logger.debug("put cache: {}", request.getUrl());
		if (response.getRawContent() != null) {
			contentRawCache.put(request.getUrl(), response.getRawContent());
		} else {
			contentRawCache.put(request.getUrl(), response.getContent().getBytes());
		}
		contentTypeCache.put(request.getUrl(), response.getHeaders().get("Content-Type"));
	}

	public void invalidateCacheResponse (String url) {
		if (contentRawCache.remove(url) == null && contentTypeCache.remove(url) == null) {
			logger.debug("nothing to invalidate for {}", url);
		}
	}

	protected KevoreeHttpResponse replaceGlobalVariables (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		String pattern = getDictionary().get("urlpattern").toString();
		if (pattern.endsWith("**")) {
			pattern = pattern.replace("**", "");
		}
		if (!pattern.endsWith("/")) {
			pattern = pattern + "/";
		}
        String urlSite = request.getCompleteUrl().replace(request.getUrl(), "");
        response.setContent(response.getContent().replace("{urlsite}", urlSite));
		response.setContent(response.getContent().replace("{urlpattern}", pattern));
		return response;
	}
}
