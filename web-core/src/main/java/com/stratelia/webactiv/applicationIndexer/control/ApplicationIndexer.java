/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.applicationIndexer.control;

import com.silverpeas.pdc.PdcIndexer;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.StringUtil;

import javax.inject.Inject;

public class ApplicationIndexer extends AbstractIndexer {

  public static ApplicationIndexer getInstance() {
    return ServiceProvider.getService(ApplicationIndexer.class);
  }

  @Inject
  private OrganizationController organizationController;

  protected ApplicationIndexer() {
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

  public void indexComponent(String spaceId, ComponentInst compoInst) {
    SilverTrace.info(silvertraceModule, "ApplicationIndexer.indexComponent()",
        "applicationIndexer.MSG_START_INDEXING_COMPONENT", "component = " + compoInst.getLabel());

    // index component info
    admin.indexComponent(compoInst.getId());

    // index component content
    ComponentIndexation componentIndexer = getIndexer(compoInst);
    if (componentIndexer != null) {
      try {
        if (!spaceId.equals(compoInst.getDomainFatherId())) {
          compoInst.setDomainFatherId(spaceId);
        }
        componentIndexer.index(compoInst);
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
      PersonalToolIndexation personalToolIndexer = ServiceProvider.getService(compoName);
      personalToolIndexer.index();
    } catch (IllegalStateException ce) {
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
    ComponentInst compoInst = organizationController.getComponentInst(componentId);
    indexComponent(spaceId, compoInst);
  }

  public void indexPdc() throws Exception {
    setSilverTraceLevel();
    PdcIndexer indexer = PdcIndexer.getInstance();
    indexer.index();
  }

  String firstLetterToUpperCase(String str) {
    return StringUtil.capitalize(str);
  }

  String firstLetterToLowerCase(String str) {
    return StringUtil.uncapitalize(str);
  }

  ComponentIndexation getIndexer(ComponentInst compoInst) {
    ComponentIndexation componentIndexer;
    try {
      String qualifier = compoInst.getName() + ComponentIndexation.QUALIFIER_SUFFIX;
      componentIndexer = ServiceProvider.getService(qualifier);
    } catch (IllegalStateException ex) {
      SilverTrace.warn(silvertraceModule, "ApplicationIndexer.getIndexer()",
          "applicationIndexer.EX_INDEXER_COMPONENT_NOT_FOUND",
          "component = " + firstLetterToUpperCase(compoInst.getName()));
      componentIndexer = ServiceProvider.getService(ComponentIndexerAdapter.class);
    }
    return componentIndexer;
  }

  public void indexUsers() {
    admin.indexAllUsers();
  }

  public void indexGroups() {
    admin.indexAllGroups();
  }
}
