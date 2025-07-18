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
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane;
import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane;
import org.silverpeas.core.workflow.api.model.RelatedUser;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Class implementing the tag &lt;relatedUserList&gt; from workflowEditor.tld
 */
@SuppressWarnings("unused")
public class RelatedUserList extends WorkflowTagSupport {
  private static final long serialVersionUID = 5328962749360952253L;
  private String strContext;
  private String strCurrentScreen;
  private Iterator<RelatedUser> iterRelatedUser;

  @Override
  public int doStartTag() throws JspException {
    try {
      GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
          "SessionGraphicElementFactory");
      MultiSilverpeasBundle resource = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute(
          "resources");
      String strPaneTitle = resource.getString("workflowDesigner.list.relatedUser");
      String strContextEncoded = URLEncoder.encode(strContext + "/relatedUser", Charsets.UTF_8);

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

      while (iterRelatedUser.hasNext()) {
        RelatedUser relatedUser = iterRelatedUser.next();
        String modifParams = "?context=" + strContextEncoded;
        String removalParams = "{context: '" + strContextEncoded + "'";

        String strParticipant;
        if (relatedUser.getParticipant() != null) {
          strParticipant = relatedUser.getParticipant().getName();
          modifParams += "&participant=" + URLEncoder.encode(strParticipant, Charsets.UTF_8);
          removalParams += ", participants: '" + URLEncoder.encode(strParticipant, Charsets.UTF_8) + "'";
        } else {
          strParticipant = "";
        }

        String strFolderItem;
        if (relatedUser.getFolderItem() != null) {
          strFolderItem = relatedUser.getFolderItem().getName();
          modifParams += "&folderItem=" + URLEncoder.encode(strFolderItem, Charsets.UTF_8);
          removalParams += ", folderItems: '" + URLEncoder.encode(strFolderItem, Charsets.UTF_8) + "'";
        } else {
          strFolderItem = "";
        }

        String strRelation;
        if (relatedUser.getRelation() != null) {
          strRelation = relatedUser.getRelation();
          modifParams += "&relation=" + URLEncoder.encode(strRelation, Charsets.UTF_8);
          removalParams += ", relations: '" + URLEncoder.encode(strRelation, Charsets.UTF_8) + "'";
        } else {
          strRelation = "";
        }

        String strRole;
        if (relatedUser.getRole() != null) {
          strRole = relatedUser.getRole();
          modifParams += "&role=" + URLEncoder.encode(strRole, Charsets.UTF_8);
          removalParams += ", roles: '" + URLEncoder.encode(strRole, Charsets.UTF_8) + "'";
        } else {
          strRole = "";
        }

        String strEditURL = "ModifyRelatedUser" + modifParams;
        String strRemoveJS = "javascript:confirmRemove('RemoveRelatedUser', "
            + removalParams + ", '" + resource.getString("workflowDesigner.confirmRemoveJS")
            + " "
            + WebEncodeHelper.javaStringToJsString(resource.getString("workflowDesigner.relatedUser"))
            + " ?');";

        IconPane iconPane = addIconPane(gef, resource, strEditURL, strRemoveJS);

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
