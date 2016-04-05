/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.workflowdesigner.taglib;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane;
import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane;
import org.silverpeas.core.web.util.viewgenerator.html.icons.Icon;

/**
 * Class implementing the tag &lt;contextualDesignationList&gt; from workflowEditor.tld
 */
public class ContextualDesignationList extends TagSupport {

  private static final long serialVersionUID = 2510045275428248323L;

  private static final String UTF8 = "UTF-8"; // encoding

  private String strContext, // the context of the designation
      strParentScreen, // TODO refactor, use the RR.calculateParentScreen()
      strPaneTitleKey, // The resource key to retrieve the pane title
      strColumnLabelKey; // The resource key to retrieve the column label

  private ContextualDesignations designations;

  /*
   * (non-Javadoc)
   * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
   */
  public int doStartTag() throws JspException {
    ArrayPane designationPane;
    ArrayLine ligne;
    IconPane iconPane;
    Icon updateIcon;
    Icon delIcon;
    ArrayColumn column;
    ContextualDesignation designation;
    StringBuilder sb;
    String strParametersEncoded, strEditURL, strPaneTitle, strColumnLabel;
    GraphicElementFactory gef;
    MultiSilverpeasBundle resource;

    try {
      gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
          "SessionGraphicElementFactory");
      resource = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute(
          "resources");
      strPaneTitle = resource.getString(strPaneTitleKey);
      strColumnLabel = resource.getString(strColumnLabelKey);
      strContext = URLEncoder.encode(strContext, UTF8);
      strParentScreen = URLEncoder.encode(strParentScreen, UTF8);

      designationPane = gef.getArrayPane("designationList", strParentScreen,
          pageContext.getRequest(), pageContext.getSession());
      designationPane.setVisibleLineNumber(20);
      designationPane.setTitle(strPaneTitle);
      designationPane.addArrayColumn(resource.getString("GML.language"));
      designationPane.addArrayColumn(resource
          .getString("workflowDesigner.role"));
      designationPane.addArrayColumn(strColumnLabel);
      column = designationPane.addArrayColumn(resource
          .getString("GML.operations"));
      column.setSortable(false);

      Iterator<ContextualDesignation> iterDesignations = designations.iterateContextualDesignation();
      sb = new StringBuilder();

      while (iterDesignations.hasNext()) {
        designation = iterDesignations.next();

        // Create the parameters
        //
        sb.setLength(0);
        sb.append("?role=");
        sb.append(URLEncoder.encode(designation.getRole(), UTF8));
        sb.append("&lang=");
        sb.append(URLEncoder.encode(designation.getLanguage(), UTF8));
        sb.append("&parentScreen="); // FIXME refactor
        sb.append(strParentScreen);
        sb.append("&context=");
        sb.append(strContext);

        strParametersEncoded = sb.toString();
        strEditURL = "ModifyContextualDesignation" + strParametersEncoded;

        // Create the remove link
        //
        sb.setLength(0);
        sb.append("javascript:confirmRemove('RemoveContextualDesignation");
        sb.append(strParametersEncoded);
        sb.append("', '");
        sb.append(resource.getString("workflowDesigner.confirmRemoveJS"));
        sb.append(" ");
        sb.append(EncodeHelper.javaStringToJsString(designation.getLanguage()));
        sb.append(", ");
        sb.append(EncodeHelper.javaStringToJsString(designation.getRole()));
        sb.append(" ?');");

        iconPane = gef.getIconPane();
        updateIcon = iconPane.addIcon();
        delIcon = iconPane.addIcon();
        updateIcon.setProperties(resource
            .getIcon("workflowDesigner.smallUpdate"), resource
            .getString("GML.modify"), strEditURL);
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
            resource.getString("GML.delete"), sb.toString());
        iconPane.setSpacing("30px");

        ligne = designationPane.addArrayLine();
        ligne.addArrayCellLink(designation.getLanguage(), strEditURL);
        ligne.addArrayCellLink(designation.getRole(), strEditURL);
        ligne.addArrayCellLink(designation.getContent(), strEditURL);
        ligne.addArrayCellIconPane(iconPane);
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
