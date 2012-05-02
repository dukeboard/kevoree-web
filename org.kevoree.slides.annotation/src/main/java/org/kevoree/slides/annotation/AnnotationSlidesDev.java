package org.kevoree.slides.annotation;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.slides.template.KevoreeSlidesShowerTemplateDev;

import java.io.File;

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
@DictionaryType({@DictionaryAttribute(name = "folder")})
public class AnnotationSlidesDev extends KevoreeSlidesShowerTemplateDev {

	private File devDirectory;

	@Start
	public void startPage () {
		super.startPage();
		File f1 = new File((String) super.getDictionary().get("folder"));
		if (f1.isDirectory()) {
			devDirectory = f1;
		}
	}

	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (!load(request, response, devDirectory.getAbsolutePath())) {
			super.process(request, response);
		}
		return response;
	}
}
