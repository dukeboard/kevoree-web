package org.kevoree.web;


import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;

import java.io.File;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/03/12
 * Time: 11:12
 */

@ComponentType
//@DictionaryType()
@DictionaryType({
        @DictionaryAttribute(name = "folder")})

public class KevoreeMainSiteDev extends ParentAbstractPage {

    protected String basePage = "overview.html";


    private File f;
    private PageRenderer krenderer = null;


    @Override
    public void startPage() {
        super.startPage();
        File f1 = new File((String) super.getDictionary().get("folder"));
        if (f1.isDirectory()){
            f=f1;
            krenderer = new PageRenderer(true,f);
        }

    }

    @Override
    public void updatePage() {
        super.updatePage();
        File f1 = new File((String) super.getDictionary().get("folder"));
        if (f1.isDirectory()){
            f=f1;
            krenderer = new PageRenderer(true,f);
        }

    }

    @Override
    public void stopPage() {
        super.stopPage();
    }

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {


        if (FileServiceHelper.checkStaticFileFromDir(basePage, this, request, response,f.getAbsolutePath())) {
            if (request.getUrl().equals("/") || request.getUrl().endsWith(".html") || request.getUrl().endsWith(".css")) {
                String pattern = getDictionary().get("urlpattern").toString();
                if (pattern.endsWith("**")) {
                    pattern = pattern.replace("**", "");
                }
                if (!pattern.endsWith("/")) {
                    pattern = pattern + "/";
                }
                response.setContent(response.getContent().replace("{urlpattern}", pattern));
            }
            return response;
        }
        if (krenderer.checkForTemplateRequest(basePage, this, request, response)) {
            return response;
        }
        response.setContent("Bad request");
        return response;
    }


}
