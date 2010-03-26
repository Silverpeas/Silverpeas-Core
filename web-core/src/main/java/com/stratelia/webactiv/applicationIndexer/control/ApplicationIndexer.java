/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.silverpeas.peasCore.*;
import com.stratelia.silverpeas.silvertrace.*;

public class ApplicationIndexer extends Object {

  MainSessionController msc = null;
  OrganizationController oc = null;

  public ApplicationIndexer(MainSessionController msc) {
    this.msc = msc;
  }

  public void indexAll() throws Exception {
    indexAllSpaces();

    indexPersonalComponents();

    indexPdc();
  }

  public void indexAllSpaces() throws Exception {
    index(null, null);
  }

  public void index(String spaceId, String componentId) throws Exception {
    setSilverTraceLevel();
    oc = new OrganizationController();
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.index()",
        "root.MSG_GEN_ENTER_METHOD");
    if (spaceId == null) {
      // index whole application
      String[] spaceIds = oc.getAllSpaceIds();
      SilverTrace.info("applicationIndexer", "ApplicationIndexer.index()",
          "applicationIndexer.MSG_INDEXING_ALL_SPACES");
      for (int i = 0; i < spaceIds.length; i++) {
        indexSpace(spaceIds[i]);
      }
    } else {
      if (componentId == null || "".equals(componentId)
          || "null".equals(componentId)) {
        // index whole space
        indexSpace(spaceId);
      } else {
        // index only one component
        indexComponent(spaceId, componentId);
      }
    }
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.index()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void index(String personalComponent) throws Exception {
    setSilverTraceLevel();
    oc = new OrganizationController();
    if (personalComponent != null) {
      indexPersonalComponent(personalComponent);
    }
  }

  private void indexSpace(String spaceId) throws Exception {
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.indexSpace()",
        "applicationIndexer.MSG_START_INDEXING_SPACE", "spaceId = " + spaceId);
    String[] compos = oc.getAllComponentIdsRecur(spaceId);
    for (int nI = 0; nI < compos.length; nI++)
      indexComponent(spaceId, compos[nI]);
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.indexSpace()",
        "applicationIndexer.MSG_END_INDEXING_SPACE", "spaceId = " + spaceId);
  }

  private void indexComponent(String spaceId, ComponentInst compoInst)
      throws Exception {
    SilverTrace.info("applicationIndexer",
        "ApplicationIndexer.indexComponent()",
        "applicationIndexer.MSG_START_INDEXING_COMPONENT", "component = "
        + compoInst.getLabel());
    ComponentIndexerInterface cii = getIndexer(compoInst);
    if (cii != null) {
      try {
        ComponentContext componentContext = msc.createComponentContext(spaceId,
            compoInst.getId());
        cii.index(msc, componentContext);
      } catch (Exception e) {
        SilverTrace.error("applicationIndexer",
            "ApplicationIndexer.indexComponent()",
            "applicationIndexer.EX_INDEXING_COMPONENT_FAILED", "component = "
            + compoInst.getLabel(), e);
      }
      SilverTrace.info("applicationIndexer",
          "ApplicationIndexer.indexComponent()",
          "applicationIndexer.MSG_END_INDEXING_COMPONENT", "component = "
          + compoInst.getLabel());
    } else {
      SilverTrace.info("applicationIndexer",
          "ApplicationIndexer.indexComponent()",
          "applicationIndexer.MSG_COMPONENT_INDEXER_NOT_FOUND", "component = "
          + compoInst.getLabel());
    }
  }

  private void indexPersonalComponent(String personalComponent)
      throws Exception {
    SilverTrace.info("applicationIndexer",
        "ApplicationIndexer.indexPersonalComponent()",
        "applicationIndexer.MSG_START_INDEXING_PERSONAL_COMPONENT",
        "personalComponent = " + personalComponent);
    String compoName = firstLetterToLowerCase(personalComponent);
    ComponentIndexerInterface cii = null;
    try {
      ComponentContext componentContext = msc
          .createComponentContext(null, null);
      componentContext.setCurrentComponentId(personalComponent);
      cii = (ComponentIndexerInterface) Class.forName(
          "com.stratelia.webactiv." + compoName + "." + personalComponent
          + "Indexer").newInstance();
      cii.index(msc, componentContext);
    } catch (ClassNotFoundException ce) {
      SilverTrace.error("applicationIndexer",
          "ApplicationIndexer.indexPersonalComponent()",
          "applicationIndexer.EX_INDEXER_PERSONAL_COMPONENT_NOT_FOUND",
          "personalComponent = " + personalComponent, ce);
    } catch (Exception e) {
      SilverTrace.error("applicationIndexer",
          "ApplicationIndexer.indexPersonalComponent()",
          "applicationIndexer.EX_INDEXING_PERSONAL_COMPONENT_FAILED",
          "personalComponent = " + personalComponent, e);
    }
    SilverTrace.info("applicationIndexer",
        "ApplicationIndexer.indexPersonalComponent()",
        "applicationIndexer.MSG_END_INDEXING_PERSONAL_COMPONENT",
        "personalComponent = " + personalComponent);
  }

  private void indexComponent(String spaceId, String componentId)
      throws Exception {
    ComponentInst compoInst = oc.getComponentInst(componentId);
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

  private String firstLetterToUpperCase(String str) {
    String c = str.substring(0, 1);
    c = c.toUpperCase();
    return c + str.substring(1);
  }

  private String firstLetterToLowerCase(String str) {
    String c = str.substring(0, 1);
    c = c.toLowerCase();
    return c + str.substring(1);
  }

  private ComponentIndexerInterface getIndexer(ComponentInst compoInst) {
    String compoName = firstLetterToUpperCase(compoInst.getName());
    ComponentIndexerInterface cii = null;
    boolean classNotFound = false;

    String packageName = compoInst.getName();
    String className = firstLetterToUpperCase(compoInst.getName());
    if (packageName.equalsIgnoreCase("toolbox")) {
      packageName = "kmelia";
      className = firstLetterToUpperCase("kmelia");
    } else if (packageName.equalsIgnoreCase("bookmark")) {
      packageName = "webSites";
      className = firstLetterToUpperCase("webSites");
    } else if (packageName.equalsIgnoreCase("pollingStation")) {
      packageName = "survey";
      className = firstLetterToUpperCase("survey");
    } else if (packageName.equalsIgnoreCase("webPages")) {
      packageName = "webpages";
      className = firstLetterToUpperCase("webPages");
    }
    try {
      cii = (ComponentIndexerInterface) Class.forName(
          "com.stratelia.webactiv." + packageName + "." + className
          + "Indexer").newInstance();
    } catch (ClassNotFoundException ce) {
      classNotFound = true;
    } catch (Exception e) {
      SilverTrace.debug("applicationIndexer",
          "ApplicationIndexer.getIndexer()",
          "applicationIndexer.EX_INDEXING_PERSONAL_COMPONENT_FAILED",
          "component = " + compoName, e);
    }
    if (classNotFound) {
      try {
        cii = (ComponentIndexerInterface) Class.forName(
            "com.silverpeas." + packageName + "." + className + "Indexer")
            .newInstance();
      } catch (ClassNotFoundException ce) {
        SilverTrace.debug("applicationIndexer",
            "ApplicationIndexer.getIndexer()",
            "applicationIndexer.EX_INDEXER_COMPONENT_NOT_FOUND", "component = "
            + compoName, ce);
      } catch (Exception e) {
        SilverTrace.debug("applicationIndexer",
            "ApplicationIndexer.getIndexer()",
            "applicationIndexer.EX_INDEXING_COMPONENT_FAILED", "component = "
            + compoName, e);
      }
    }
    return cii;
  }

  private void setSilverTraceLevel() {
    SilverTrace.setTraceLevel("applicationIndexer",
        SilverTrace.TRACE_LEVEL_INFO);
  }
}