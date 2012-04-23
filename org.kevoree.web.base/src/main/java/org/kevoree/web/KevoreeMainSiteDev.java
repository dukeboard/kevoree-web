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
@DictionaryType({@DictionaryAttribute(name = "folder")})
public class KevoreeMainSiteDev extends KevoreeMainSite {

    private File f;

    @Override
    public void startPage() {
        useCache = false;
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

}
