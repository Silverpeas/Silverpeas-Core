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

import org.silverpeas.core.util.EncodeHelper;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.workflow.api.model.RelatedUser;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane;
import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane;
import org.silverpeas.core.web.util.viewgenerator.html.icons.Icon;

/**
 * Class implementing the tag &lt;relatedUserList&gt; from workflowEditor.tld
 */
public class RelatedUserList extends TagSupport {
  private static final long serialVersionUID = 5328962749360952253L;
  private String strContext, strCurrentScreen;
  private Iterator<RelatedUser> iterRelatedUser;

  /*
   * (non-Javadoc)
   * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
   */
  public int doStartTag() throws JspException {
    String strEditURL, strPaneTitle, strParticipant = "", strFolderItem = "", strRelation = "", strRole =
        "", strContextEncoded, strParametersEncoded;

    try {
      GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
          "SessionGraphicElementFactory");
      MultiSilverpeasBundle resource = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute(
          "resources");
      strPaneTitle = resource.getString("workflowDesigner.list.relatedUser");
      strContextEncoded = URLEncoder.encode(strContext + "/relatedUser", "UTF-8");

      ArrayPane relatedUserPane = gef.getArrayPane("relatedUserList", strCurrentScreen,
          pageContext.getRequest(), pageContext.getSession());
      relatedUserPane.setVisibleLineNumber(20);
      relatedUserPane.setTitle(strPaneTitle);
      relatedUserPane.addArrayColumn(resource.getString("workflowDesigner.participant"));
      relatedUserPane.addArrayColumn(resource.getString("workflowDesigner.folderItem"));
      relatedUserPane.addArrayColumn(resource.getString("workflowDesigner.relation"));
      relatedUserPane.addArrayColumn(resource.getString("workflowDesigner.role"));
      ArrayColumn column = relatedUserPane.addArrayColumn(resource.getString("GML.operations"));
      column.setSortable(false);

      StringBuilder sb = new StringBuilder(2000);
      while (iterRelatedUser.hasNext()) {
        RelatedUser relatedUser = iterRelatedUser.next();
        sb.setLength(0);
        sb.append("?context=");
        sb.append(strContextEncoded);
        if (relatedUser.getParticipant() != null) {
          strParticipant = relatedUser.getParticipant().getName();
          sb.append("&participant=");
          sb.append(URLEncoder.encode(strParticipant, "UTF-8"));
        } else {
          strParticipant = "";
        }

        if (relatedUser.getFolderItem() != null) {
          strFolderItem = relatedUser.getFolderItem().getName();
          sb.append("&folderItem=");
          sb.append(URLEncoder.encode(strFolderItem, "UTF-8"));
        } else {
          strFolderItem = "";
        }

        if (relatedUser.getRelation() != null) {
          strRelation = relatedUser.getRelation();
          sb.append("&relation=");
          sb.append(URLEncoder.encode(strRelation, "UTF-8"));
        } else {
          strRelation = "";
        }

        if (relatedUser.getRole() != null) {
          strRole = relatedUser.getRole();
          sb.append("&role=");
          sb.append(URLEncoder.encode(strRole, "UTF-8"));
        } else {
          strRole = "";
        }

        strParametersEncoded = sb.toString();
        strEditURL = "ModifyRelatedUser" + strParametersEncoded;

        // Create the remove link
        //
        sb.setLength(0);
        sb.append("javascript:confirmRemove('RemoveRelatedUser");
        sb.append(strParametersEncoded);
        sb.append("', '");
        sb.append(resource.getString("workflowDesigner.confirmRemoveJS"));
        sb.append(" ");
        sb.append(EncodeHelper.javaStringToJsString(resource
            .getString("workflowDesigner.relatedUser")));
        sb.append(" ?');");

        IconPane iconPane = gef.getIconPane();
        Icon updateIcon = iconPane.addIcon();
        Icon delIcon = iconPane.addIcon();
        updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"), resource
            .getString("GML.modify"), strEditURL);
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
            resource.getString("GML.delete"), sb.toString());
        iconPane.setSpacing("30px");
        ArrayLine row = relatedUserPane.addArrayLine();
        row.addArrayCellLink(strParticipant, strEditURL);
        row.addArrayCellLink(strFolderItem, strEditURL);
        row.addArrayCellLink(strRelation, strEditURL);
        row.addArrayCellLink(strRole, strEditURL);
        row.addArrayCellIconPane(iconPane);
      }
      pageContext.getOut().println(relatedUserPane.print());
    } catch (IOException e) {
      throw new JspException("Error when printing the Related Users", e);
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
   * @return the currentScreen
   */
  public String getCurrentScreen() {
    return strCurrentScreen;
  }

  /**
   * @param currentScreen the currentScreen to set
   */
  public void setCurrentScreen(String currentScreen) {
    this.strCurrentScreen = currentScreen;
  }

  /**
   * @return the related user iterator
   */
  public Iterator<RelatedUser> getIterRelatedUser() {
    return iterRelatedUser;
  }

  /**
   * @param iterRelatedUser the Related User iterator to set
   */
  public void setIterRelatedUser(Iterator<RelatedUser> iterRelatedUser) {
    this.iterRelatedUser = iterRelatedUser;
  }
}
