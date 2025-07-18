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

import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane;

import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * Class implementing the tag &lt;processModelTabs&gt; from workflowEditor.tld
 */
public class ProcessModelTabs extends WorkflowTagSupport {

  private static final long serialVersionUID = 5607804796452218902L;
  private static final String MODIFY_WORKFLOW = "ModifyWorkflow";
  private static final String VIEW_PRESENTATION = "ViewPresentation";
  private static final String VIEW_PARTICIPANTS = "ViewParticipants";
  private static final String VIEW_STATES = "ViewStates";
  private static final String VIEW_ACTIONS = "ViewActions";
  private static final String VIEW_USER_INFOS = "ViewUserInfos";
  private static final String VIEW_DATA_FOLDER = "ViewDataFolder";
  private static final String VIEW_FORMS = "ViewForms";
  private static final String VIEW_ROLES = "ViewRoles";

  private String strCurrentTab;

  /**
   * @return the current tab name
   */
  public String getCurrentTab() {
    return strCurrentTab;
  }

  /**
   * @param currentTab the current Tab name to set
   */
  public void setCurrentTab(String currentTab) {
    strCurrentTab = currentTab;
  }

  @Override
  public int doStartTag() throws JspException {
    GraphicElementFactory gef;
    MultiSilverpeasBundle resource;
    TabbedPane tabbedPane;

    gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
        "SessionGraphicElementFactory");
    resource = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute(
        "resources");
    tabbedPane = gef.getTabbedPane();

    tabbedPane.addTab(resource.getString("GML.head"), MODIFY_WORKFLOW
        .equals(strCurrentTab) ? "#" : MODIFY_WORKFLOW, MODIFY_WORKFLOW
        .equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.roles"), VIEW_ROLES
        .equals(strCurrentTab) ? "#" : VIEW_ROLES, VIEW_ROLES
        .equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.presentation"),
        VIEW_PRESENTATION.equals(strCurrentTab) ? "#" : VIEW_PRESENTATION,
        VIEW_PRESENTATION.equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.participants"),
        VIEW_PARTICIPANTS.equals(strCurrentTab) ? "#" : VIEW_PARTICIPANTS,
        VIEW_PARTICIPANTS.equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.states"),
        VIEW_STATES.equals(strCurrentTab) ? "#" : VIEW_STATES, VIEW_STATES
        .equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.actions"),
        VIEW_ACTIONS.equals(strCurrentTab) ? "#" : VIEW_ACTIONS,
        VIEW_ACTIONS.equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.userInfos"),
        VIEW_USER_INFOS.equals(strCurrentTab) ? "#" : VIEW_USER_INFOS,
        VIEW_USER_INFOS.equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.dataFolder"),
        VIEW_DATA_FOLDER.equals(strCurrentTab) ? "#" : VIEW_DATA_FOLDER,
        VIEW_DATA_FOLDER.equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.forms"), VIEW_FORMS
        .equals(strCurrentTab) ? "#" : VIEW_FORMS, VIEW_FORMS
        .equals(strCurrentTab));

    try {
      pageContext.getOut().println(tabbedPane.print());
    } catch (IOException e) {
      throw new JspException("Error when printing the Workflow Designer tabs",
          e);
    }
    return super.doStartTag();
  }

}
