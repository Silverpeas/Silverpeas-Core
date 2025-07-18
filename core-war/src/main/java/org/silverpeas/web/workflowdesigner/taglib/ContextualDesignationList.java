/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.workflowdesigner.taglib;

import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Class implementing the tag &lt;contextualDesignationList&gt; from workflowEditor.tld
 */
@SuppressWarnings("unused")
public class ContextualDesignationList extends WorkflowTagSupport {

  private static final long serialVersionUID = 2510045275428248323L;


  private String strContext; // the context of the designation
  private String strParentScreen; // the parent screen
  private String strPaneTitleKey; // The resource key to retrieve the pane title
  private String strColumnLabelKey; // The resource key to retrieve the column label

  private ContextualDesignations designations;

  @Override
  public int doStartTag() throws JspException {
    try {
      var gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
          "SessionGraphicElementFactory");
      var resource = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute(
          "resources");
      String strPaneTitle = resource.getString(strPaneTitleKey);
      String strColumnLabel = resource.getString(strColumnLabelKey);
      strContext = URLEncoder.encode(strContext, Charsets.UTF_8);
      strParentScreen = URLEncoder.encode(strParentScreen, Charsets.UTF_8);

      var designationPane = gef.getArrayPane("designationList", strParentScreen,
          pageContext.getRequest(), pageContext.getSession());
      designationPane.setVisibleLineNumber(20);
      designationPane.setTitle(strPaneTitle);
      designationPane.addArrayColumn(resource.getString("GML.language"));
      designationPane.addArrayColumn(resource
          .getString("workflowDesigner.role"));
      designationPane.addArrayColumn(strColumnLabel);
      var column = designationPane.addArrayColumn(resource
          .getString("GML.operations"));
      column.setSortable(false);

      Iterator<ContextualDesignation> iterDesignations = designations.iterateContextualDesignation();
      while (iterDesignations.hasNext()) {
        ContextualDesignation designation = iterDesignations.next();
        String encodedRole = URLEncoder.encode(designation.getRole(), Charsets.UTF_8);
        String encodedLanguage = URLEncoder.encode(designation.getLanguage(), Charsets.UTF_8);

        // Create the parameters
        String modifParameters = "?role=" + encodedRole
            + "&lang=" + encodedLanguage
            + "&parentScreen=" + strParentScreen
            + "&context=" + strContext;
        String removalParameters = "{role: '" + encodedRole
            + "', lang: '" + encodedLanguage
            + "', parentScreen: '" + strParentScreen
            + "', context: '" + strContext
            + "'}";

        String strEditURL = "ModifyContextualDesignation" + modifParameters;

        // Create the remove link
        //
        String strRemoveJS = "javascript:confirmRemove('RemoveContextualDesignation', "
            + removalParameters
            + ", '"
            + resource.getString("workflowDesigner.confirmRemoveJS")
            + " " + WebEncodeHelper.javaStringToJsString(designation.getLanguage())
            + ", "
            + WebEncodeHelper.javaStringToJsString(designation.getRole()) + " ?');";

        var iconPane = addIconPane(gef, resource, strEditURL, strRemoveJS);

        var line = designationPane.addArrayLine();
        line.addArrayCellLink(designation.getLanguage(), strEditURL);
        line.addArrayCellLink(designation.getRole(), strEditURL);
        line.addArrayCellLink(designation.getContent(), strEditURL);
        line.addArrayCellIconPane(iconPane);
      }

      pageContext.getOut().println(designationPane.print());
    } catch (IOException e) {
      throw new JspException("Error when printing the Contextual Designations",
          e);
    }
    return super.doStartTag();
  }

  /**
   * @return the context
   */
  public String getContext() {
    return strContext;
  }

  /**
   * @param context the context to set
   */
  public void setContext(String context) {
    strContext = context;
  }

  /**
   * @return the designations
   */
  public ContextualDesignations getDesignations() {
    return designations;
  }

  /**
   * @param designations the designations to set
   */
  public void setDesignations(ContextualDesignations designations) {
    this.designations = designations;
  }

  /**
   * @return the relative URL of the partent screen
   */
  public String getParentScreen() {
    return strParentScreen;
  }

  /**
   * @param parentScreen the relative URL of the parent screen to set
   */
  public void setParentScreen(String parentScreen) {
    strParentScreen = parentScreen;
  }

  /**
   * @return the paneTitleKey
   */
  public String getPaneTitleKey() {
    return strPaneTitleKey;
  }

  /**
   * @param paneTitleKey the paneTitleKey to set
   */
  public void setPaneTitleKey(String paneTitleKey) {
    strPaneTitleKey = paneTitleKey;
  }

  /**
   * @return the columnLabelKey
   */
  public String getColumnLabelKey() {
    return strColumnLabelKey;
  }

  /**
   * @param columnLabelKey the columnLabelKey to set
   */
  public void setColumnLabelKey(String columnLabelKey) {
    strColumnLabelKey = columnLabelKey;
  }

}
