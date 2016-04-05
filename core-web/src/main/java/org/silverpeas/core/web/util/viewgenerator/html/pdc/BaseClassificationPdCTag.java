/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.pdc;

import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.MultiPartElement;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.fieldset;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ResourceLocator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.text.MessageFormat;

import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.web.util.viewgenerator.html.pdc.PdcClassificationTagOperation.*;

/**
 * The base tag for all concrete tags on the PdC classification of a content.
 */
public abstract class BaseClassificationPdCTag extends SimpleTagSupport {

  /**
   * The key with which is associated the language of the user carried in his session.
   */
  private static final String LANGUAGE_KEY = "SilverSessionController";
  /**
   * The identifier of the XHTML tag within which the PdC classification will be displayed.
   */
  public static final String PDC_CLASSIFICATION_WIDGET_TAG_ID = "classification";
  private static final String USE_PDC_COMPONENT_PARAMETER = "usePdc";
  private static final String BROWSING_CONTEXT = "browseContext";
  private String componentId;
  private String contentId;
  private String nodeId;
  private String pdcWidgetId = PDC_CLASSIFICATION_WIDGET_TAG_ID;

  public BaseClassificationPdCTag() {
  }

  /**
   * Sets the identifier of the HTML element in which the classification is rendered.
   *
   * @param id the widget identifier to set.
   */
  public void setId(String id) {
    this.pdcWidgetId = id;
  }

  /**
   * Gets the identifier used to identify the HTML element in which is rendered the classification.
   *
   * @return the id of the widget.
   */
  public String getId() {
    return this.pdcWidgetId;
  }

  /**
   * Sets the unique identifier of the Silverpeas component instance to which the resource belongs.
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
   * Gets the identifier of the Silverpeas component instance to which the resource belongs. If no
   * component instance identifier is set, then it is fecthed from the request.
   *
   * @return the component identifier.
   */
  public String getComponentId() {
    if (!isDefined(componentId)) {
      String[] context = getRequestAttribute(BROWSING_CONTEXT);
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
   * Gets the identifier of the node to which the content belongs. A node is an hierarchic way to
   * organize the contents. A node can represents a category, a topic, or a folder and its semantic
   * depends on the Silverpeas component that handle the content.
   *
   * @return the identifier of the node or an empty string.
   */
  public String getNodeId() {
    return nodeId;
  }

  /**
   * Sets the identifier of the node to which the content belongs (if any). A node is an hierarchic
   * way to organize the contents. A node can represents a category, a topic, or a folder and its
   * semantic depends on the Silverpeas component that handle the content.
   *
   * @param nodeId the identifier of the node.
   */
  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  /**
   * Invokes the underlying JQuery PdC plugin with the appropriate function according to the
   * specified operation. The operation is actually invoked only if the PdC is used by the
   * underlying Silverpeas component instance.
   *
   * @param operation the operation to invoke.
   * @return the HTML elements container with the code to render.
   * @throws JspException
   */
  public ElementContainer invoke(final PdcClassificationTagOperation operation) throws JspException {
    ElementContainer container;
    if (isPdcUsed()) {
      container = initWidget(operation);
    } else {
      container = new ElementContainer();
    }
    return container;
  }

  /**
   * Sets up the widget with all required information. It initializes the PdC classification JQuery
   * plugin and it calls it to render the classification of the refered content on the PdC.
   *
   * @param operation the operation with which the widget is initialized.
   * @return a container of rendering elements.
   * @throws JspException if an error occurs while initializing the JQuery comment plugin.
   */
  private ElementContainer initWidget(PdcClassificationTagOperation operation) throws JspException {
    ElementContainer xhtmlcontainer = new ElementContainer();
    MultiPartElement classification;
    if (operation == PREVIEW_CLASSIFICATION || operation == PREDEFINE_CLASSIFICATION) {
      classification = new div();
    } else {
      classification = new fieldset();
    }
    classification.setID(pdcWidgetId);
    if (operation == PREVIEW_CLASSIFICATION) {
      classification.setClass(PDC_CLASSIFICATION_WIDGET_TAG_ID + " preview bgDegradeGris");
    } else {
      classification.setClass(PDC_CLASSIFICATION_WIDGET_TAG_ID + " skinFieldset");
    }
    script pluginDependency1 = new script().setType("text/javascript").
        setSrc(URLUtil.getApplicationURL() + "/util/javaScript/silverpeas-pdc-widgets.js");
    script pluginDependency2 = new script().setType("text/javascript").
        setSrc(URLUtil.getApplicationURL() + "/util/javaScript/silverpeas-pdc-classification.js");
    script pluginExecution = new script().setType("text/javascript").
        addElement(executePlugin(operation));
    xhtmlcontainer.addElement(pluginDependency1).
        addElement(pluginDependency2).
        addElement(classification).
        addElement(pluginExecution);
    return xhtmlcontainer;
  }

  /**
   * This method calls the JQuery PdC plugin with the appropriate function according to the
   * operation to invoke.
   *
   * @param operation the operation to execute.
   * @return the javascript code to handle the classification onto the PdC.
   * @throws JspTagException if an error occurs during the processing of the plugin.
   */
  private String executePlugin(PdcClassificationTagOperation operation) throws JspTagException {
    String context = URLUtil.getApplicationURL();
    MultiSilverpeasBundle resources = getResources();
    String function = operation.getPluginFunction();
    String positionAddingLabel = (operation.equals(PREDEFINE_CLASSIFICATION)
        || operation.equals(CREATE_CLASSIFICATION) ? resources.getString(
        "pdcPeas.saveThePosition") : resources.getString("GML.PDCNewPosition"));
    String script = "$('#" + pdcWidgetId + "').pdcClassification('" + function
        + "', {resource: {context: '"
        + context
        + "', " + "component: '" + getComponentId() + "', content: '" + getContentId()
        + "', " + "node: '" + getNodeId() + "'}, title: '" + resources.getString(
        "pdcPeas.classifyPublication")
        + "', modificationTitle: '" + resources.getString("GML.Validation")
        + "', positionLabel: '" + resources.getString("pdcPeas.position")
        + "', positionsLabel: '" + resources.getString("pdcPeas.positions")
        + "', inheritedPositionsLabel: '" + resources.getString("pdcPeas.inheritedPositions")
        + "', canBeModified: '" + resources.getString("pdcPeas.predefined.update.allowed.true")
        + "', cannotBeModified: '" + resources.getString("pdcPeas.predefined.update.allowed.false")
        + "'";
    if (operation != PREVIEW_CLASSIFICATION) {
      if (operation != READ_CLASSIFICATION) {
        String nodeName = "";
        if (operation == PREDEFINE_CLASSIFICATION) {
          if (!isDefined(nodeId)) {
            nodeId = "0";
          }
          NodeDetail node = NodeService.get().getDetail(new NodePK(nodeId, componentId));
          MainSessionController controller = getSessionAttribute(LANGUAGE_KEY);
          String inLanguage = controller.getFavoriteLanguage();
          nodeName = node.getName(inLanguage);
        }
        String inheritanceMessage = MessageFormat.format(resources.getString(
            "pdcPeas.CanDoAPredefineClassification"), nodeName);
        script += ", messages: {mandatoryMessage: \""
            + resources.getString("pdcPeas.MustBeClassified") + ", \", inheritanceMessage: \""
            + inheritanceMessage
            + "\", contentMustHaveAPosition: \""
            + resources.getString("pdcPeas.theContent") + " " + resources.getString(
            "pdcPeas.MustContainsMandatoryAxis") + "\", positionAlreayInClassification: \""
            + resources.getString("pdcPeas.positionAlreadyExist")
            + "\", positionMustBeValued: \""
            + resources.getString("GML.selectAValue") + "\" }"
            + ", mode: 'edition', edition: {ok: '" + resources.getString("GML.validate") + "',"
            + "cancel: '" + resources.getString("GML.cancel") + "', mandatoryLegend: '"
            + resources.getString("GML.requiredField") + "', invariantLegend: '" + resources.
            getString("pdcPeas.notVariants") + "', mandatoryAxisDefaultValue: \"" + resources.
            getString("GML.selectAValue") + "\"}, addition: {title: '" + positionAddingLabel
            + "'}, update: {title: '" + resources.getString("GML.modify")
            + "'}, deletion: {confirmation: '"
            + resources.getString("pdcPeas.confirmDeletePosition")
            + "', title: '" + resources.getString("GML.PDCDeletePosition") + "'}});";
      } else {
        script += ", mode: 'view'});";
      }
    } else {
      script += "});";
    }
    return script;
  }

  /**
   * Gets the resources from which translated text and PdC settings can be get.
   *
   * @return a resources wrapper instance.
   * @throws JspTagException if an error occurs while fetching the resources.
   */
  protected MultiSilverpeasBundle getResources() throws JspTagException {
    MainSessionController controller = getSessionAttribute(LANGUAGE_KEY);
    String language = controller.getFavoriteLanguage();
    MultiSilverpeasBundle resources =
        new MultiSilverpeasBundle(ResourceLocator.getLocalizationBundle(
        "org.silverpeas.pdcPeas.multilang.pdcBundle", language), language);
    return resources;

  }

  /**
   * Is the PdC is used currently by the underlying Silverpeas component instance. For some
   * components, the use of the PdC can be set per instance. For others, the PdC is used by default.
   *
   * @return true if the component instance uses the PdC, false otherwise.
   */
  protected boolean isPdcUsed() {
    boolean pdcUsed = true; // by default the PdC is considered as used for a silverpeas component.
    MainSessionController sessionController = getSessionAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    String parameterValue = sessionController.getComponentParameterValue(getComponentId(),
        USE_PDC_COMPONENT_PARAMETER);
    if (isDefined(parameterValue)) {
      pdcUsed = getBooleanValue(parameterValue);
    }
    return pdcUsed;
  }

  protected <T> T getRequestAttribute(String name) {
    return (T) getJspContext().getAttribute(name, PageContext.REQUEST_SCOPE);
  }

  protected <T> T getSessionAttribute(String name) {
    return (T) getJspContext().getAttribute(name, PageContext.SESSION_SCOPE);
  }

  protected JspWriter getOut() {
    return getJspContext().getOut();
  }
}
