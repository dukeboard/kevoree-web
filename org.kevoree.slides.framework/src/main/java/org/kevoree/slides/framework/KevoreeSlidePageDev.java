package org.kevoree.slides.framework;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
    public void startPage() {
        super.startPage();
        useCache = false;
        File f1 = new File((String) super.getDictionary().get("templateFolder"));
        if (f1.isDirectory()) {
            logger.info(f1.getAbsolutePath());
            devDirectory = f1;
        }
    }

    protected InputStream loadInternal(String name) {
        File fr = new File(devDirectory, name);
        if (fr.exists()) {
            try {
                return new FileInputStream(fr);
            } catch (FileNotFoundException e) {
                logger.error("", e);
            }
        }
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        if (!load(request, response, devDirectory.getAbsolutePath())) {
            super.process(request, response);
        }
        return response;
    }

    public boolean load(KevoreeHttpRequest request, KevoreeHttpResponse response, String baseDir) {
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
