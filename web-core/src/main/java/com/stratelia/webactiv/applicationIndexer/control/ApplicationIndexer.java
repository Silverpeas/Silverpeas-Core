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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.applicationIndexer.control;

import org.silverpeas.core.admin.OrganisationControllerFactory;
import com.silverpeas.pdc.PdcIndexer;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;

public class ApplicationIndexer extends AbstractIndexer {

  private MainSessionController mainSessionController = null;

  public ApplicationIndexer(MainSessionController msc) {
    this.mainSessionController = msc;
  }

  public void indexAll() throws Exception {
    indexAllSpaces();
    indexPersonalComponents();
    indexPdc();
    indexGroups();
    indexUsers();
  }

  public void index(String personalComponent) throws Exception {
    setSilverTraceLevel();
    if (personalComponent != null) {
      indexPersonalComponent(personalComponent);
    }
  }

  private void indexComponent(String spaceId, ComponentInstLight compoInst) {
    SilverTrace.info(silvertraceModule, "ApplicationIndexer.indexComponent()",
        "applicationIndexer.MSG_START_INDEXING_COMPONENT", "component = " + compoInst.getLabel());

    // index component info
    admin.indexComponent(compoInst.getId());

    // index component content
    ComponentIndexerInterface componentIndexer = getIndexer(compoInst);
    if (componentIndexer != null) {
      try {
        ComponentContext componentContext = mainSessionController.createComponentContext(spaceId,
            compoInst.getId());
        componentIndexer.index(mainSessionController, componentContext);
      } catch (Exception e) {
        SilverTrace.error(silvertraceModule, "ApplicationIndexer.indexComponent()",
            "applicationIndexer.EX_INDEXING_COMPONENT_FAILED", "component = "
            + compoInst.getLabel(), e);
      }
      SilverTrace.info(silvertraceModule, "ApplicationIndexer.indexComponent()",
          "applicationIndexer.MSG_END_INDEXING_COMPONENT", "component = "
          + compoInst.getLabel());
    } else {
      SilverTrace.info(silvertraceModule, "ApplicationIndexer.indexComponent()",
          "applicationIndexer.MSG_COMPONENT_INDEXER_NOT_FOUND", "component = "
          + compoInst.getLabel());
    }
  }

  @Override
  public void indexPersonalComponent(String personalComponent) {
    SilverTrace.info(silvertraceModule, "ApplicationIndexer.indexPersonalComponent()",
        "applicationIndexer.MSG_START_INDEXING_PERSONAL_COMPONENT",
        "personalComponent = " + personalComponent);
    String compoName = firstLetterToLowerCase(personalComponent);
    try {
      ComponentContext componentContext = mainSessionController.createComponentContext(null, null);
      componentContext.setCurrentComponentId(personalComponent);
      ComponentIndexerInterface componentIndexer = (ComponentIndexerInterface) Class.forName(
          "com.stratelia.webactiv." + compoName + "." + personalComponent + "Indexer")
          .newInstance();
      componentIndexer.index(mainSessionController, componentContext);
    } catch (ClassNotFoundException ce) {
      SilverTrace.warn(silvertraceModule, "ApplicationIndexer.indexPersonalComponent()",
          "applicationIndexer.EX_INDEXER_PERSONAL_COMPONENT_NOT_FOUND",
          "personalComponent = " + personalComponent);
    } catch (Exception e) {
      SilverTrace.error(silvertraceModule, "ApplicationIndexer.indexPersonalComponent()",
          "applicationIndexer.EX_INDEXING_PERSONAL_COMPONENT_FAILED",
          "personalComponent = " + personalComponent, e);
    }
    SilverTrace.info(silvertraceModule, "ApplicationIndexer.indexPersonalComponent()",
        "applicationIndexer.MSG_END_INDEXING_PERSONAL_COMPONENT",
        "personalComponent = " + personalComponent);
  }

  @Override
  public void indexComponent(String spaceId, String componentId) throws Exception {
    try {
      ComponentInstLight compoInst = OrganisationControllerFactory.getOrganisationController()
          .getComponentInstLight(componentId);
      indexComponent(spaceId, compoInst);
    } catch (Exception e) {
      SilverTrace.error(silvertraceModule, "ApplicationIndexer.indexComponent()",
          "applicationIndexer.EX_INDEXING_COMPONENT_FAILED", "component = " + componentId, e);
    }
  }

  public void indexPdc() throws Exception {
    setSilverTraceLevel();
    PdcIndexer indexer = new PdcIndexer();
    indexer.index();
  }

  String firstLetterToUpperCase(String str) {
    return StringUtil.capitalize(str);
  }

  String firstLetterToLowerCase(String str) {
    return StringUtil.uncapitalize(str);
  }

  ComponentIndexerInterface getIndexer(ComponentInstLight compoInst) {
    ComponentIndexerInterface componentIndexer;
    String compoName = firstLetterToUpperCase(compoInst.getName());
    String className = getClassName(compoInst);
    String packageName = getPackage(compoInst);
    try {
      componentIndexer =
          loadIndexer("com.stratelia.webactiv." + packageName + '.' + className + "Indexer");
      if (componentIndexer == null) {
        componentIndexer =
            loadIndexer("com.silverpeas." + packageName + '.' + className + "Indexer");
      }
      if (componentIndexer == null) {
        componentIndexer =
            loadIndexer("com.silverpeas.components." + packageName + '.' + className + "Indexer");
      }
      if (componentIndexer == null) {
        componentIndexer =
            loadIndexer("org.silverpeas." + packageName + '.' + className + "Indexer");
      }
      if (componentIndexer == null) {
        componentIndexer =
            loadIndexer("org.silverpeas.components." + packageName + '.' + className + "Indexer");
      }
    } catch (InstantiationException e) {
      SilverTrace.warn(silvertraceModule, "ApplicationIndexer.getIndexer()",
          "applicationIndexer.EX_INDEXING_PERSONAL_COMPONENT_FAILED", "component = " + compoName, e);
      componentIndexer = new ComponentIndexerAdapter();
    } catch (IllegalAccessException e) {
      SilverTrace.warn(silvertraceModule, "ApplicationIndexer.getIndexer()",
          "applicationIndexer.EX_INDEXING_PERSONAL_COMPONENT_FAILED", "component = " + compoName, e);
      componentIndexer = new ComponentIndexerAdapter();
    }
    if (componentIndexer == null) {
      SilverTrace.warn(silvertraceModule, "ApplicationIndexer.getIndexer()",
          "applicationIndexer.EX_INDEXER_COMPONENT_NOT_FOUND",
          "component = " + compoName + " with classes com.stratelia.webactiv." + packageName + "."
          + className + "Indexer and com.silverpeas." + packageName + "." + className + "Indexer");
      return new ComponentIndexerAdapter();
    }
    return componentIndexer;
  }

  private ComponentIndexerInterface loadIndexer(String className)
      throws InstantiationException, IllegalAccessException {
    try {
      return (ComponentIndexerInterface) Class.forName(className).newInstance();
    } catch (ClassNotFoundException ex) {
      return null;
    }
  }

  String getClassName(ComponentInstLight compoInst) {
    String name = compoInst.getName();
    String className = firstLetterToUpperCase(name);
    if ("toolbox".equalsIgnoreCase(name)) {
      return "Kmelia";
    }
    if ("bookmark".equalsIgnoreCase(name)) {
      return "WebSites";
    }
    if ("pollingStation".equalsIgnoreCase(name)) {
      return "Survey";
    }
    if ("webPages".equalsIgnoreCase(name)) {
      return "WebPages";
    }
    if ("mydb".equalsIgnoreCase(name)) {
      return "MyDB";
    }
    if ("organizationchart".equalsIgnoreCase(name) || "orgchartGroup".equalsIgnoreCase(name)) {
      return "OrganizationChart";
    }
    return className;
  }

  String getPackage(ComponentInstLight compoInst) {
    String packageName = firstLetterToLowerCase(compoInst.getName());
    if ("toolbox".equalsIgnoreCase(packageName)) {
      return "kmelia";
    }
    if ("bookmark".equalsIgnoreCase(packageName)) {
      return "webSites";
    }
    if ("pollingStation".equalsIgnoreCase(packageName)) {
      return "survey";
    }
    if ("webPages".equalsIgnoreCase(packageName) || "resourcesManager".equalsIgnoreCase(packageName)
        || "mydb".equalsIgnoreCase(packageName) || "formsOnline".equalsIgnoreCase(packageName)
        || "suggestionBox".equalsIgnoreCase(packageName)) {
      return packageName.toLowerCase();
    }
    return packageName;
  }

  public void indexUsers() {
    admin.indexAllUsers();
  }

  public void indexGroups() {
    admin.indexAllGroups();
  }
}
