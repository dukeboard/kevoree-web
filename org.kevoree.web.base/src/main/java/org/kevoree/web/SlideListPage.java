package org.kevoree.web;

import org.kevoree.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.Constants;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 31/05/12
 * Time: 16:42
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class SlideListPage implements ModelListener {

	private Map<String, String> slidesList;

	private KevoreeMainSite mainSite;
	private String webSocketUrl;

	private static Map<String, String> variables = new HashMap<String, String>();

	private Logger logger = LoggerFactory.getLogger(SlideListPage.class.getName());

	public SlideListPage (KevoreeMainSite mainSite, String wsUrl) throws Exception {
		this.mainSite = mainSite;
		webSocketUrl = wsUrl;
		variables = new HashMap<String, String>();
		slidesList = new HashMap<String, String>();
		initializeEmbedder();
	}

	private void initializeEmbedder () throws Exception {
		InputStream in = mainSite.getClass().getClassLoader().getResourceAsStream("embedder.html");
		if (in != null) {
			variables.put("embedder", new String(FileServiceHelper.convertStream(in), "UTF-8"));
		}
	}

	@Override
	public boolean preUpdate (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		return true;
	}

	@Override
	public boolean initUpdate (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		return true;
	}

	@Override
	public void modelUpdated () {
		slidesList.clear();
		// look for all component that have a super type equals to KevoreeSlidePage
		KevScriptEngine kengine = mainSite.getKevScriptEngineFactory().createKevScriptEngine();
		kengine.addVariable("nodeName", mainSite.getNodeName());
		kengine.addVariable("webSocketUrl", webSocketUrl);
		boolean forwardChannelIsAdded = false;
		for (TypeDefinition typeDefinition : mainSite.getModelService().getLastModel().getTypeDefinitionsForJ()) {
			boolean isSlideShow = "KevoreeSlidePage".equals(typeDefinition.getName());
			boolean isSlideShowDev = "KevoreeSlidePageDev".equals(typeDefinition.getName());
			logger.debug("{} is slideShow: {}", typeDefinition.getName(), isSlideShow);
			logger.debug("{} is slideShowDev: {}", typeDefinition.getName(), isSlideShowDev);
			if (!isSlideShow && !isSlideShowDev) {
				isSlideShow = false;
				isSlideShowDev = false;
				for (TypeDefinition superTypeDefinition : typeDefinition.getSuperTypesForJ()) {
					if ("KevoreeSlidePage".equals(superTypeDefinition.getName())) {
						isSlideShow = true;
					} else if ("KevoreeSlidePageDev".equals(superTypeDefinition.getName())) {
						isSlideShowDev = true;
					}
				}
			} else {
				isSlideShow = false;
				isSlideShowDev = false;
			}
			if (isSlideShow && !isSlideShowDev) {
				kengine.addVariable("instanceName", typeDefinition.getName());
				kengine.addVariable("typeDefinitionName", typeDefinition.getName());
				kengine.append("addComponent {instanceName}@{nodeName} : {typeDefinitionName} {urlpattern='/talks/{instanceName}/', wsurl='{webSocketUrl}'}");
				// find webserver
				String webServer[] = getWebServerName();
				if (webServer != null) {
					// find channels
					String channelRequestName;
					if (mainSite.isPortBinded("forward")) {
						channelRequestName = findChannel(mainSite.getName(), "forward", mainSite.getNodeName());
					} else {
						channelRequestName = "forwardChannel";
						if (!forwardChannelIsAdded) {
							forwardChannelIsAdded = true;
							kengine.addVariable("mainSiteName", mainSite.getName());
							kengine.addVariable("mainSiteNodeName", mainSite.getNodeName());
							kengine.append("addChannel forwardChannel : defMSG");
							kengine.append("bind {mainSiteName}.forward@{mainSiteNodeName} => forwardChannel");
						}
					}
					String channelResponseName = findChannel(webServer[0], "response", webServer[1]);
					if (channelRequestName != null && channelResponseName != null) {
						// add bindings
						kengine.addVariable("channelRequestName", channelRequestName);
						kengine.addVariable("channelResponseName", channelResponseName);
						kengine.append("bind {instanceName}.request@{nodeName} => {channelRequestName}");
						kengine.append("bind {instanceName}.content@{nodeName} => {channelResponseName}");
					} else {
						logger.warn("Unable to find channels to connect slide component");
					}
					Option<String> ipOption = KevoreePropertyHelper
							.getStringNetworkProperty(mainSite.getModelService().getLastModel(), mainSite.getNodeName(), Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
					String ip = "localhost";
					if (ipOption.isDefined()) {
						ip = ipOption.get();
					}
					Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForComponent(mainSite.getModelService().getLastModel(), webServer[0], "port");
					int port = 8000;
					if (portOption.isDefined()) {
						port = portOption.get();
					}
					slidesList.put(typeDefinition.getName(), "{urlsite}{urlpattern}talks/" + typeDefinition.getName() + "/");
				} else {
					logger.warn("Unable to find webserver to connect slide component");
				}
			} else {
				logger.debug("{} is not a slideshow", typeDefinition.getName());
			}
		}
		try {
			mainSite.getModelService().unregisterModelListener(this);
			kengine.atomicInterpretDeploy();
			mainSite.getModelService().registerModelListener(this);
			buildCache();
			String pattern = mainSite.getDictionary().get("urlpattern").toString();
			if (pattern.endsWith("**")) {
				pattern = pattern.replace("**", "");
			}
			if (!pattern.endsWith("/")) {
				pattern = pattern + "/";
			}
			mainSite.invalidateCacheResponse(pattern + "talks");
		} catch (Exception ignored) {

		}
	}

	public boolean checkSlide (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		logger.debug(Thread.currentThread() + "TITI" + request.getUrl());
		for (String componentName : slidesList.keySet()) {
			if (request.getUrl().startsWith(componentName) || request.getUrl().startsWith("/" + componentName)) {
				response.setStatus(418);
				return true;
			}
		}
		return false;
	}

	private String[] getWebServerName () {
		for (ContainerNode node : mainSite.getModelService().getLastModel().getNodesForJ()) {
			for (ComponentInstance component : node.getComponentsForJ()) {
//				for (TypeDefinition typeDefinition : component.getTypeDefinition().getSuperTypesForJ()) {
				logger.debug(component.getTypeDefinition().getName());
				if ("KTinyWebServer".equals(component.getTypeDefinition().getName())) {
					return new String[]{component.getName(), node.getName()};
				}
//				}
			}
		}
		return null;
	}

	private String findChannel (String componentName, String portName, String nodeName) {
		for (MBinding mbinding : mainSite.getModelService().getLastModel().getMBindingsForJ()) {
			if (mbinding.getPort().getPortTypeRef().getName().equals(portName)
					&& ((ComponentInstance) mbinding.getPort().eContainer()).getName().equals(componentName)
					&& ((ContainerNode) mbinding.getPort().eContainer().eContainer()).getName().equals(nodeName)) {
				logger.debug(mbinding.getHub().getName());
				return mbinding.getHub().getName();
			}
		}
		return null;
	}

	private void buildCache () {
		StringBuilder slideListBuilder = new StringBuilder();
		StringBuilder menuBuilder = new StringBuilder();
		boolean isFirst = true;
		for (String componentName : slidesList.keySet()) {

			menuBuilder.append("<li><a onclick=\"document.querySelector('#presentation').innerHTML = '")
					.append(componentName).append("';slideURL = '")
					.append(slidesList.get(componentName)).append("'; loadIFrame();\">").append(componentName).append("</a></li>\n");
			if (isFirst) {
				isFirst = false;
				slideListBuilder.append("document.querySelector('#presentation').innerHTML = '")
						.append(componentName).append("';\n\t\t").append("var slideURL = '")
						.append(slidesList.get(componentName)).append("';\n\t\t")
						.append("window.onload = init;");
			}
		}
		logger.debug("menu = {}", menuBuilder.toString());
		logger.debug("slides = {}", slideListBuilder.toString());
		variables.put("menu", menuBuilder.toString());
		variables.put("initSlides", slideListBuilder.toString());
	}

	public static Map<String, String> getVariables () {
		return variables;
	}
}
