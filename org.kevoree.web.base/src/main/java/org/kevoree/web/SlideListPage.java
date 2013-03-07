package org.kevoree.web;

import org.kevoree.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 31/05/12
 * Time: 16:42
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class SlideListPage implements ModelListener {

    private Map<String, String[]> slidesList;

    private KevoreeMainSite mainSite;
    private String webSocketUrl;

    private static Map<String, String> variables = new HashMap<String, String>();
    protected ExecutorService threadPool = Executors.newFixedThreadPool(1);

    private Logger logger = LoggerFactory.getLogger(SlideListPage.class.getName());

    public SlideListPage(KevoreeMainSite mainSite, String wsUrl) throws Exception {
        this.mainSite = mainSite;
        webSocketUrl = wsUrl;
        variables = new HashMap<String, String>();
        slidesList = new HashMap<String, String[]>();
        initializeEmbedder();
    }

    private void initializeEmbedder() throws Exception {
        InputStream in = mainSite.getClass().getClassLoader().getResourceAsStream("showcaseEmbedder.html");
        if (in != null) {
            variables.put("embedder", new String(FileServiceHelper.convertStream(in), "UTF-8"));
        }
    }

    @Override
    public boolean preUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
        return true;
    }

    @Override
    public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
        return true;
    }

    @Override
    public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public void modelUpdated() {
        final ContainerRoot model = mainSite.getModelService().getLastModel();
        final ModelListener modelListener = this;
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                slidesList.clear();
                // look for all components that have a super type equals to KevoreeSlidePage
                KevScriptEngine kengine = mainSite.getKevScriptEngineFactory().createKevScriptEngine();
                kengine.addVariable("nodeName", mainSite.getNodeName());
                kengine.addVariable("webSocketUrl", webSocketUrl);
                boolean forwardChannelIsAdded = false;
                for (TypeDefinition typeDefinition : model.getTypeDefinitions()) {
                    boolean isSlideShow = "KevoreeSlidePage".equals(typeDefinition.getName());
                    boolean isSlideShowDev = "KevoreeSlidePageDev".equals(typeDefinition.getName());
                    if (!isSlideShow && !isSlideShowDev) {
                        isSlideShow = false;
                        isSlideShowDev = false;
                        for (TypeDefinition superTypeDefinition : typeDefinition.getSuperTypes()) {
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
                    logger.debug("{} is slideShow: {}", typeDefinition.getName(), isSlideShow);
                    logger.debug("{} is slideShowDev: {}", typeDefinition.getName(), isSlideShowDev);
                    if (isSlideShow && !isSlideShowDev) {
                        kengine.addVariable("instanceName", typeDefinition.getName());
                        kengine.addVariable("typeDefinitionName", typeDefinition.getName());
                        kengine.append("addComponent {instanceName}@{nodeName} : {typeDefinitionName} {urlpattern='/talks/{instanceName}/', wsurl='{webSocketUrl}'}");
                        // find webserver
                        String webServer[] = getWebServerName(model);
                        if (webServer != null) {
                            // find channels
                            String channelRequestName;
                            if (mainSite.isPortBinded("forward")) {
                                channelRequestName = findChannel(model, mainSite.getName(), "forward", mainSite.getNodeName());
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
                            String channelResponseName = findChannel(model, webServer[0], "response", webServer[1]);
                            if (channelRequestName != null && channelResponseName != null) {
                                // add bindings
                                kengine.addVariable("channelRequestName", channelRequestName);
                                kengine.addVariable("channelResponseName", channelResponseName);
                                kengine.append("bind {instanceName}.request@{nodeName} => {channelRequestName}");
                                kengine.append("bind {instanceName}.content@{nodeName} => {channelResponseName}");
                            } else {
                                logger.warn("Unable to find channels to connect slide component");
                            }
                            slidesList.put(typeDefinition.getName(), new String[]{"{urlsite}{urlpattern}talks/" + typeDefinition.getName() + "/", getPaperURL(typeDefinition)});
                        } else {
                            logger.warn("Unable to find webserver to connect slide component");
                        }
                    } else {
                        logger.debug("{} is not a slideshow", typeDefinition.getName());
                    }
                }
                try {
                    mainSite.getModelService().unregisterModelListener(modelListener);
                    kengine.atomicInterpretDeploy();
                    mainSite.getModelService().registerModelListener(modelListener);
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
                    logger.debug("Unable to define talks.", ignored);
                }
            }
        });
    }

    @Override
    public void preRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
    }

    @Override
    public void postRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
    }

    private String getPaperURL(TypeDefinition typeDefinition) {
        if (typeDefinition.getDictionaryType() != null) {
            for (DictionaryValue dictionaryValue : typeDefinition.getDictionaryType().getDefaultValues()) {
                if (dictionaryValue.getAttribute().getName().equals("paperURL")) {
                    return dictionaryValue.getValue();
                }
            }
        }
        return "";
    }

    private String[] getWebServerName(ContainerRoot model) {
        for (ContainerNode node : model.getNodes()) {
            for (ComponentInstance component : node.getComponents()) {
//				for (TypeDefinition typeDefinition : component.getTypeDefinition().getSuperTypesForJ()) {
                logger.debug(component.getTypeDefinition().getName());
                if ("SprayWebServer".equals(component.getTypeDefinition().getName())) {// must be change if the webserver implementation is changed
                    return new String[]{component.getName(), node.getName()};
                }
//				}
            }
        }
        return null;
    }

    private String findChannel(ContainerRoot model, String componentName, String portName, String nodeName) {
        for (MBinding mbinding : model.getMBindings()) {
            if (mbinding.getPort().getPortTypeRef().getName().equals(portName)
                    && ((ComponentInstance) mbinding.getPort().eContainer()).getName().equals(componentName)
                    && ((ContainerNode) mbinding.getPort().eContainer().eContainer()).getName().equals(nodeName)) {
                logger.debug(mbinding.getHub().getName());
                return mbinding.getHub().getName();
            }
        }
        return null;
    }

    private void buildCache() {
        StringBuilder slideListBuilder = new StringBuilder();
        StringBuilder menuBuilder = new StringBuilder();
        boolean isFirst = true;
        for (String componentName : slidesList.keySet()) {
            menuBuilder.append("<li><a onclick=\"document.querySelector('#presentation').innerHTML = '")
                    .append(componentName);
            if (!"".equals(slidesList.get(componentName)[1])) {
                menuBuilder.append(" - <a href=\\'").append(slidesList.get(componentName)[1]).append("\\'>Read the paper</a>");
            }
            menuBuilder.append("'; ks.sendEvent(null, {'type':'RELOAD', 'url':'").append(slidesList.get(componentName)[0]).append("'});\">").append(componentName).append("</a></li>\n");
            if (isFirst) {
                isFirst = false;
                slideListBuilder.append("jQuery(document).ready(function ($) {\n").append("document.querySelector('#presentation').innerHTML = '")
                        .append(componentName).append("';\n\t\t")/*.append("\t\tks.sendEvent(null, {'type':'RELOAD', 'url':'")
                        .append(slidesList.get(componentName)[0]).append("'});")*/.append("\n});");
                if (variables.get("embedder") != null) {
                    variables.put("embedder", variables.get("embedder").replace("{slideurl}", slidesList.get(componentName)[0]));
                }
            }
        }
        logger.debug("menu = {}", menuBuilder.toString());
        logger.debug("slides = {}", slideListBuilder.toString());
        variables.put("menu", menuBuilder.toString());
        variables.put("initSlides", slideListBuilder.toString());
    }

    public static Map<String, String> getVariables() {
        return variables;
    }
}
