package org.kevoree.slides.display;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/04/12
 * Time: 10:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "slideshow")
@ComponentType
public class SlideshowDisplay extends ParentAbstractPage {

	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		logger.info("SlideShow is called");
		// TODO return display.html
		// TODO return display_script.js
		// TODO ask content by sending a request to the slides component ?
		return response;
	}
}
