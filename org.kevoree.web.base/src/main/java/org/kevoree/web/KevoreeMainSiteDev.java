package org.kevoree.web;


import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/03/12
 * Time: 11:12
 */

@ComponentType
@DictionaryType({@DictionaryAttribute(name = "folder")})
public class KevoreeMainSiteDev extends KevoreeMainSite {

    private File f;

    @Override
    public void startPage() {
        useCache = true;
        super.startPage();
        File f1 = new File((String) super.getDictionary().get("folder"));
        if (f1.isDirectory()) {
            f = f1;
            krenderer = new PageRenderer(true, f);
        }
    }

    @Override
    public void updatePage() {
        super.updatePage();
        File f1 = new File((String) super.getDictionary().get("folder"));
        if (f1.isDirectory()) {
            f = f1;
            krenderer = new PageRenderer(true, f);
        }
    }

    @Override
    public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
        if (getLastParam(request.getUrl()).startsWith("talks/") || getLastParam(request.getUrl()).startsWith("/talks/")) {
            logger.debug("forward request to slide pages for url: {} with completeURL = {}", getLastParam(request.getUrl()), request.getCompleteUrl());
            return forward(request, response);
        }
        if (FileServiceHelper.checkStaticFileFromDir(basePage, this, request, response,f.getAbsolutePath())) {
            if (request.getUrl().equals("/") || request.getUrl().endsWith(".html") || request.getUrl().endsWith(".css") || request.getUrl().endsWith(".jnlp")) {
                // FIXME according to KevoreeSlidesShowerTemplate
                replaceGlobalVariables(request, response);
            }
            if (useCache) {
                cacheResponse(request, response);
            }
            return response;
        }
        return super.process(request,response);
    }






}
