/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.viewGenerator.html.pdc;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.ResourceLocator;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.MultiPartElement;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.fieldset;
import org.apache.ecs.xhtml.script;
import static com.silverpeas.util.StringUtil.*;
import static com.stratelia.webactiv.util.viewGenerator.html.pdc.PdcTagOperation.*;

/**
 * The base tag for all concrete tags on the PdC classification of a content.
 */
public abstract class BaseClassificationPdCTag extends TagSupport {

  private static final long serialVersionUID = -486056418553072731L;
  /**
   * The key with which is associated the resource locator carried in the request.
   */
  private static final String RESOURCES_KEY = "resources";
  /**
   * The identifier of the XHTML tag within which the PdC classification will be displayed.
   */
  public static final String PDC_CLASSIFICATION_WIDGET_TAG_ID = "classification";
  private static final String USE_PDC_COMPONENT_PARAMETER = "usePdc";
  private static final String BROWSING_CONTEXT = "browseContext";
  private String componentId;
  private String contentId;
  private String nodeId;
  private PdcTagOperation operation;

  /**
   * Sets the unique identifier of the Silverpeas component instance to which the resource
   * belongs.
   *
   * @param componentId the unique identifier of the instance of a Silverpeas component.
   */
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  /**
   * Sets the unique identifier of the resource content.
   *
   * @param contentId the unique identifier of the resource content.
   */
  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  /**
   * Gets the identifier of the Silverpeas component instance to which the resource belongs.
   * If no component instance identifier is set, then it is fecthed from the request.
   *
   * @return the component identifier.
   */
  public String getComponentId() {
    if (!isDefined(componentId)) {
      String[] context = (String[]) pageContext.getRequest().getAttribute(BROWSING_CONTEXT);
      componentId = context[3];
    }
    return componentId;
  }

  /**
   * Gets the unique identifier of the resource content in Silverpeas.
   *
   * @return the resource content identifier.
   */
  public String getContentId() {
    return contentId;
  }
  
  /**
   * Gets the identifier of the node to which the content belongs.
   * A node is an hierarchic way to organize the contents. A node can represents a category, a topic,
   * or a folder and its semantic depends on the Silverpeas component that handle the content.
   * @return the identifier of the node or an empty string.
   */
  public String getNodeId() {
    return nodeId;
  }

  /**
   * Sets the identifier of the node to which the content belongs (if any).
   * A node is an hierarchic way to organize the contents. A node can represents a category, a topic,
   * or a folder and its semantic depends on the Silverpeas component that handle the content.
   * @param nodeId the identifier of the node.
   */
  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  /**
   * Gets the invoked operation.
   * @return the operation that is executed or null if no operation is invoked.
   */
  protected PdcTagOperation getInvokedOperation() {
    return operation;
  }

  /**
   * Invokes the underlying JQuery PdC plugin with the appropriate function according to the
   * specified operation.
   * The operation is actually invoked only if the PdC is used by the underlying Silverpeas
   * component instance.
   * @param operation the operation to invoke.
   * @return the HTML elements container with the code to render.
   * @throws JspException 
   */
  public ElementContainer invoke(final PdcTagOperation operation) throws JspException {
    ElementContainer container;
    if (isPdcUsed()) {
      this.operation = operation;
      container = initWidget();
    } else {
      container = new ElementContainer();
    }
    return container;
  }

  /**
   * Sets up the widget with all required information. It initializes the PdC classification JQuery
   * plugin and it calls it to render the classification of the refered content onto the PdC.
   * @return a container of rendering elements.
   * @throws JspException if an error occurs while initializing the JQuery comment plugin.
   */
  private ElementContainer initWidget() throws JspException {
    ElementContainer xhtmlcontainer = new ElementContainer();
    MultiPartElement classification;
    if (getInvokedOperation() == READ_CLASSIFICATION) {
      classification = new div();
    } else {
      classification = new fieldset();
    }
    classification.setID(PDC_CLASSIFICATION_WIDGET_TAG_ID);
    if (getInvokedOperation() == READ_CLASSIFICATION) {
      classification.setClass("preview bgDegradeGris");
    } else {
      classification.setClass("skinFieldset");
    }
    script pluginExecution = new script().setType("text/javascript").
            addElement(executePlugin());
    xhtmlcontainer.addElement(classification).
            addElement(pluginExecution);
    return xhtmlcontainer;
  }

  /**
   * This method calls the JQuery PdC plugin with the appropriate function according to the
   * operation to invoke.
   * @return the javascript code to handle the classification onto the PdC.
   * @throws if an error occurs during the processing of the plugin.
   */
  private String executePlugin() throws JspTagException {
    String context = URLManager.getApplicationURL();
    ResourcesWrapper resources = getResources();
    String function = getPdcPluginFunction();
    String script = "$('#classification').pdc('" + function + "', {resource: {context: '" + context
            + "', " + "component: '" + getComponentId() + "', content: '" + getContentId()
            + "', " + "node: '" + getNodeId() + "'}, title: '" + resources.getString("pdcPeas.classifyPublication")
            + "', positionLabel: '" + resources.getString("pdcPeas.position")
            + "', positionsLabel: '" + resources.getString("pdcPeas.positions") + "'";
    if (getInvokedOperation() != READ_CLASSIFICATION) {
      script += ", messages: {mandatoryMessage: \""
              + resources.getString("pdcPeas.MustBeClassified") + "\", contentMustHaveAPosition: \""
              + resources.getString("pdcPeas.theContent") + " " + resources.getString(
              "pdcPeas.MustContainsMandatoryAxis") + "\", positionAlreayInClassification: \""
              + resources.getString("pdcPeas.positionAlreadyExist") + "\", positionMustBeValued: \""
              + resources.getString("GML.selectAValue") + "\" }"
              + ", mode: 'edition', edition: {ok: '" + resources.getString("GML.validate") + "',"
              + "cancel: '" + resources.getString("GML.cancel") + "', mandatoryLegend: '"
              + resources.getString("GML.requiredField") + "', invariantLegend: '" + resources.
              getString("pdcPeas.notVariants") + "', mandatoryAxisDefaultValue: \"" + resources.
              getString("GML.selectAValue") + "\"}, addition: {title: '" + resources.getString(
              "GML.PDCNewPosition") + "'}, update: {title: '" + resources.getString("GML.modify")
              + "'}, deletion: {confirmation: '" + resources.getString("pdcPeas.confirmDeleteAxis")
              + "', title: '" + resources.getString("GML.PDCDeletePosition") + "'}});";
    } else {
      script += ", mode: 'view'});";
    }
    return script;
  }

  /**
   * Gets the resources from which translated text and PdC settings can be get.
   * @return a resources wrapper instance.
   * @throws JspTagException  if an error occurs while fetching the resources.
   */
  protected ResourcesWrapper getResources() throws JspTagException {
    ResourcesWrapper resources = (ResourcesWrapper) pageContext.getRequest().getAttribute(
            RESOURCES_KEY);
    String language = resources.getLanguage();
    resources = new ResourcesWrapper(new ResourceLocator(
            "com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle", language), language);
    return resources;

  }

  private String getPdcPluginFunction() throws JspTagException {
    String function = null;
    switch (getInvokedOperation()) {
      case READ_CLASSIFICATION:
      case OPEN_CLASSIFICATION:
        function = "open";
        break;
      case CREATE_CLASSIFICATION:
        function = "create";
        break;
      default:
        throw new JspTagException("The operation to invoke is null!");
    }
    return function;
  }

  /**
   * Is the PdC is used currently by the underlying SIlverpeas component instance.
   * @return true if the component instance uses the PdC, false otherwise.
   */
  protected boolean isPdcUsed() {
    MainSessionController sessionController = (MainSessionController) pageContext.getSession().
            getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    String parameterValue = sessionController.getComponentParameterValue(getComponentId(),
            USE_PDC_COMPONENT_PARAMETER);
    return getBooleanValue(parameterValue);
  } 
}
