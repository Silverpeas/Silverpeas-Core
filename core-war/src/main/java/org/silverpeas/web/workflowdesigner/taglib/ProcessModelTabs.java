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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane;

/**
 * Class implementing the tag &lt;processModelTabs&gt; from workflowEditor.tld
 */
public class ProcessModelTabs extends TagSupport {

  private static final long serialVersionUID = 5607804796452218902L;
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

  /*
   * (non-Javadoc)
   * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
   */
  public int doStartTag() throws JspException {
    GraphicElementFactory gef;
    MultiSilverpeasBundle resource;
    TabbedPane tabbedPane;

    gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
        "SessionGraphicElementFactory");
    resource = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute(
        "resources");
    tabbedPane = gef.getTabbedPane();

    tabbedPane.addTab(resource.getString("GML.head"), "ModifyWorkflow"
        .equals(strCurrentTab) ? "#" : "ModifyWorkflow", "ModifyWorkflow"
        .equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.roles"), "ViewRoles"
        .equals(strCurrentTab) ? "#" : "ViewRoles", "ViewRoles"
        .equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.presentation"),
        "ViewPresentation".equals(strCurrentTab) ? "#" : "ViewPresentation",
        "ViewPresentation".equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.participants"),
        "ViewParticipants".equals(strCurrentTab) ? "#" : "ViewParticipants",
        "ViewParticipants".equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.states"),
        "ViewStates".equals(strCurrentTab) ? "#" : "ViewStates", "ViewStates"
        .equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.actions"),
        "ViewActions".equals(strCurrentTab) ? "#" : "ViewActions",
        "ViewActions".equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.userInfos"),
        "ViewUserInfos".equals(strCurrentTab) ? "#" : "ViewUserInfos",
        "ViewUserInfos".equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.dataFolder"),
        "ViewDataFolder".equals(strCurrentTab) ? "#" : "ViewDataFolder",
        "ViewDataFolder".equals(strCurrentTab));
    tabbedPane.addTab(resource.getString("workflowDesigner.forms"), "ViewForms"
        .equals(strCurrentTab) ? "#" : "ViewForms", "ViewForms"
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
