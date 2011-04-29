/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.applicationIndexer.control;

import com.silverpeas.pdc.PdcIndexer;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ComponentInst;

public class ApplicationIndexer {

  private MainSessionController mainSessionController = null;
  final OrganizationController organizationController = new OrganizationController();

  public ApplicationIndexer(MainSessionController msc) {
    this.mainSessionController = msc;
  }

  public void indexAll() throws Exception {
    indexAllSpaces();
    indexPersonalComponents();
    indexPdc();
    indexUsers();
  }

  public void indexAllSpaces() throws Exception {
    index(null, null);
  }

  public void index(String currentSpaceId, String componentId) throws Exception {
    setSilverTraceLevel();
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.index()", "root.MSG_GEN_ENTER_METHOD");
    if (currentSpaceId == null) {
      // index whole application
      String[] spaceIds = organizationController.getAllSpaceIds();
      SilverTrace.info("applicationIndexer", "ApplicationIndexer.index()",
          "applicationIndexer.MSG_INDEXING_ALL_SPACES");
      for (String spaceId : spaceIds) {
        indexSpace(spaceId);
      }
    } else {
      if (!StringUtil.isDefined(componentId)) {
        // index whole space
        indexSpace(currentSpaceId);
      } else {
        // index only one component
        indexComponent(currentSpaceId, componentId);
      }
    }
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.index()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void index(String personalComponent) throws Exception {
    setSilverTraceLevel();
    if (personalComponent != null) {
      indexPersonalComponent(personalComponent);
    }
  }

  private void indexSpace(String spaceId) throws Exception {
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.indexSpace()",
        "applicationIndexer.MSG_START_INDEXING_SPACE", "spaceId = " + spaceId);
    String[] componentIds = organizationController.getAllComponentIdsRecur(spaceId);
    for (String componentId : componentIds) {
      indexComponent(spaceId, componentId);
    }
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.indexSpace()",
        "applicationIndexer.MSG_END_INDEXING_SPACE", "spaceId = " + spaceId);
  }

  private void indexComponent(String spaceId, ComponentInst compoInst) {
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.indexComponent()",
        "applicationIndexer.MSG_START_INDEXING_COMPONENT", "component = " + compoInst.getLabel());
    ComponentIndexerInterface componentIndexer = getIndexer(compoInst);
    if (componentIndexer != null) {
      try {
        ComponentContext componentContext = mainSessionController.createComponentContext(spaceId,
            compoInst.getId());
        componentIndexer.index(mainSessionController, componentContext);
      } catch (Exception e) {
        SilverTrace.error("applicationIndexer", "ApplicationIndexer.indexComponent()",
            "applicationIndexer.EX_INDEXING_COMPONENT_FAILED", "component = "
            + compoInst.getLabel(), e);
      }
      SilverTrace.info("applicationIndexer", "ApplicationIndexer.indexComponent()",
          "applicationIndexer.MSG_END_INDEXING_COMPONENT", "component = "
          + compoInst.getLabel());
    } else {
      SilverTrace.info("applicationIndexer", "ApplicationIndexer.indexComponent()",
          "applicationIndexer.MSG_COMPONENT_INDEXER_NOT_FOUND", "component = "
          + compoInst.getLabel());
    }
  }

  private void indexPersonalComponent(String personalComponent) {
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.indexPersonalComponent()",
        "applicationIndexer.MSG_START_INDEXING_PERSONAL_COMPONENT",
        "personalComponent = " + personalComponent);
    String compoName = firstLetterToLowerCase(personalComponent);
    try {
      ComponentContext componentContext = mainSessionController.createComponentContext(null, null);
      componentContext.setCurrentComponentId(personalComponent);
      ComponentIndexerInterface componentIndexer = (ComponentIndexerInterface) Class.forName(
          "com.stratelia.webactiv." + compoName + "." + personalComponent + "Indexer").newInstance();
      componentIndexer.index(mainSessionController, componentContext);
    } catch (ClassNotFoundException ce) {
      SilverTrace.error("applicationIndexer", "ApplicationIndexer.indexPersonalComponent()",
          "applicationIndexer.EX_INDEXER_PERSONAL_COMPONENT_NOT_FOUND",
          "personalComponent = " + personalComponent);
    } catch (Exception e) {
      SilverTrace.error("applicationIndexer", "ApplicationIndexer.indexPersonalComponent()",
          "applicationIndexer.EX_INDEXING_PERSONAL_COMPONENT_FAILED",
          "personalComponent = " + personalComponent, e);
    }
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.indexPersonalComponent()",
        "applicationIndexer.MSG_END_INDEXING_PERSONAL_COMPONENT",
        "personalComponent = " + personalComponent);
  }

  private void indexComponent(String spaceId, String componentId) throws Exception {
    ComponentInst compoInst = organizationController.getComponentInst(componentId);
    indexComponent(spaceId, compoInst);
  }

  public void indexPersonalComponents() throws Exception {
    index("Agenda");
    index("Todo");
  }

  public void indexPdc() throws Exception {
    setSilverTraceLevel();
    PdcIndexer indexer = new PdcIndexer();
    indexer.index();
  }

  String firstLetterToUpperCase(String str) {
    String c = str.substring(0, 1);
    c = c.toUpperCase();
    return c + str.substring(1);
  }

  String firstLetterToLowerCase(String str) {
    String c = str.substring(0, 1);
    c = c.toLowerCase();
    return c + str.substring(1);
  }

  private ComponentIndexerInterface getIndexer(ComponentInst compoInst) {
    String compoName = firstLetterToUpperCase(compoInst.getName());
    ComponentIndexerInterface componentIndexer = null;
    boolean classNotFound = false;

    String packageName = compoInst.getName();
    String className = firstLetterToUpperCase(compoInst.getName());
    if ("toolbox".equalsIgnoreCase(packageName)) {
      packageName = "kmelia";
      className = "Kmelia";
    } else if ("bookmark".equalsIgnoreCase(packageName)) {
      packageName = "webSites";
      className = "WebSites";
    } else if ("pollingStation".equalsIgnoreCase(packageName)) {
      packageName = "survey";
      className = "Survey";
    } else if ("webPages".equalsIgnoreCase(packageName)) {
      packageName = "webpages";
      className = "WebPages";
    }
    try {
      componentIndexer = (ComponentIndexerInterface) Class.forName(
          "com.stratelia.webactiv." + packageName + "." + className + "Indexer").newInstance();
    } catch (ClassNotFoundException ce) {
      classNotFound = true;
    } catch (Exception e) {
      SilverTrace.debug("applicationIndexer", "ApplicationIndexer.getIndexer()",
          "applicationIndexer.EX_INDEXING_PERSONAL_COMPONENT_FAILED", "component = " + compoName, e);
    }
    if (classNotFound) {
      try {
        componentIndexer = (ComponentIndexerInterface) Class.forName(
            "com.silverpeas." + packageName + "." + className + "Indexer").newInstance();
      } catch (ClassNotFoundException ce) {
        SilverTrace.debug("applicationIndexer", "ApplicationIndexer.getIndexer()",
            "applicationIndexer.EX_INDEXER_COMPONENT_NOT_FOUND", "component = " + compoName);
      } catch (Exception e) {
        SilverTrace.debug("applicationIndexer", "ApplicationIndexer.getIndexer()",
            "applicationIndexer.EX_INDEXING_COMPONENT_FAILED", "component = " + compoName, e);
      }
    }
    return componentIndexer;
  }

  private void setSilverTraceLevel() {
    SilverTrace.setTraceLevel("applicationIndexer", SilverTrace.TRACE_LEVEL_INFO);
  }

  public void indexUsers() {
    AdminController admin = new AdminController(null);
    admin.indexAllUsers();
    admin = null;
  }
}