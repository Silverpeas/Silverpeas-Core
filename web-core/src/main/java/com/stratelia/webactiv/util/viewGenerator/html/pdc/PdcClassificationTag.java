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

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.ResourceLocator;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.script;

/**
 * A tag that renders the classification of a content on the PdC configured for the Silverpeas
 * component instance.
 */
public class PdcClassificationTag extends TagSupport {

  private static final long serialVersionUID = 3377113335947703561L;
  /**
   * The key with which is associated the resource locator carried in the request.
   */
  private static final String RESOURCES_KEY = "resources";
  /**
   * The identifier of the XHTML div tag within which the PdC classification will be displayed.
   */
  public static final String PDC_CLASSIFICATION_WIDGET_DIV_ID = "classification";
  private String componentId;
  private String contentId;
  private boolean editable = false;

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
   *
   * @return the component identifier.
   */
  public String getComponentId() {
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
   * Is the classification on the PdC can be edited?
   * @return true if the classification of the content can be edited (to add a new position, to
   * update or to delete an existing position. False otherwise.
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * Sets the edition mode of the PdC classification.
   * @param editable true or false. If true the classification on the PdC can be edited, otherwise
   * it will be read-only rendered.
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  @Override
  public int doStartTag() throws JspException {
    ElementContainer container = initWidget();
    container.output(pageContext.getOut());
    return SKIP_BODY;
  }

  /**
   * Sets up the widget with all required information. It initializes the JQuery comment plugin with
   * and it parameterizes from Silverpeas settings and from the resource for which the comments
   * should be rendered.
   *
   * @return a container of rendering elements.
   * @throws JspException if an error occurs while initializing the JQuery comment plugin.
   */
  private ElementContainer initWidget() throws JspException {
    String context = URLManager.getApplicationURL();
    ElementContainer xhtmlcontainer = new ElementContainer();
    div classification = new div();
    classification.setID(PDC_CLASSIFICATION_WIDGET_DIV_ID);
    script jqueryPlugin = new script().setType("text/javascript").
            setSrc(context + "/util/javaScript/silverpeas-pdc.js");
    script pluginExecution = new script().setType("text/javascript").
            addElement(executePlugin());
    xhtmlcontainer.addElement(jqueryPlugin).
            addElement(classification).
            addElement(pluginExecution);
    return xhtmlcontainer;
  }

  /**
   * This method generates the Javascript instructions to retrieve in AJAX the comments on the given
   * resource and to display them. The generated code is built upon the JQuery toolkit, so that it
   * is required to be included within the the XHTML header section.
   *
   * @return the javascript code to handle a list of comments on a given resource.
   */
  private String executePlugin() throws JspTagException {
    String context = URLManager.getApplicationURL();
    ResourcesWrapper resources = getResources();
    String script = "$('#classification').pdc('open', {resource: {context: '" + context + "', " +
            "component: '" + getComponentId() + "', content: '" + getContentId() + "'}, title: '" +
            resources.getString("GML.PDC") + "', positionLabel: '" +
            resources.getString("pdcPeas.position") + "'";
    if (isEditable()) {
      script += ", mode: 'edition', edition: {ok: '" + resources.getString("GML.validate") + "'," +
              "cancel: '" + resources.getString("GML.cancel") + "', mandatoryLegend: '" +
              resources.getString("GML.requiredField") + "', invariantLegend: '" +
              resources.getString("pdcPeas.notVariants") + "'}, addition: {title: '" +
              resources.getString("GML.PDCNewPosition") + "'}, update: {title: '" +
              resources.getString("GML.modify") + "'}, deletion: {confirmation: '" +
              resources.getString("pdcPeas.confirmDeleteAxis") + "', cannotBeDeleted: \"" + 
              resources.getString("pdcPeas.theContent") + " " +
              resources.getString("pdcPeas.MustContainsMandatoryAxis") + "\", title: '" +
              resources.getString("GML.PDCDeletePosition") + "'}});";
    } else {
      script += ", mode: 'view'});";
    }
    return script;
  }

  private ResourcesWrapper getResources() throws JspTagException {
    ResourcesWrapper resources = (ResourcesWrapper) pageContext.getRequest().getAttribute(
            RESOURCES_KEY);
    String language = resources.getLanguage();
    resources = new ResourcesWrapper(new ResourceLocator(
            "com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle", language), language);
    return resources;

  }
}
