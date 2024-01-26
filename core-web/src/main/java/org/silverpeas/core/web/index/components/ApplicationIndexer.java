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
package org.silverpeas.core.web.index.components;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.pdc.pdc.service.PdcIndexer;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.index.AbstractIndexer;
import org.silverpeas.core.web.index.tools.PersonalToolIndexation;

public class ApplicationIndexer extends AbstractIndexer {

  protected ApplicationIndexer() {
  }

  public static ApplicationIndexer getInstance() {
    return ServiceProvider.getService(ApplicationIndexer.class);
  }

  @Override
  protected void indexAllData() {
    indexAllSpaces();
    indexPersonalComponents();
    indexPdc();
    indexGroups();
    indexUsers();
  }

  public void index(String personalComponent) {
    if (personalComponent != null) {
      indexPersonalComponent(personalComponent);
    }
  }

  private void indexComponent(SilverpeasComponentInstance compoInst) {

    SilverLogger.getLogger(this)
        .info("starting indexation of component ''{0}'' with id ''{1}''", compoInst.getLabel(),
            compoInst.getId());

    // index component info
    admin.indexComponent(compoInst.getId());

    // index component content
    ComponentIndexation componentIndexer = getIndexer(compoInst);
    if (componentIndexer != null) {
      try {
        componentIndexer.index(compoInst, !isIndexAllProcess());
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("failure while indexing component ''{0}'' with id ''{1}''",
                new String[]{compoInst.getLabel(), compoInst.getId()}, e);
      }
    }

    SilverLogger.getLogger(this)
        .info("ending indexation of component ''{0}'' with id ''{1}''", compoInst.getLabel(),
            compoInst.getId());
  }

  @Override
  public void indexPersonalComponent(String personalComponent) {
    SilverLogger.getLogger(this)
        .info("starting indexation of personal component of type  ''{0}''", personalComponent);
    String compoName = firstLetterToLowerCase(personalComponent);
    try {
      PersonalToolIndexation personalToolIndexer =
          ServiceProvider.getService(compoName + PersonalToolIndexation.QUALIFIER_SUFFIX);
      personalToolIndexer.index();
    } catch (SilverpeasRuntimeException ce) {
      SilverLogger.getLogger(this)
          .silent(ce)
          .warn("cannot get personal component of type ''{0}'' ({1})", personalComponent,
              ce.getMessage());
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("failure while indexing personal component of type ''{0}''",
              new String[]{personalComponent}, e);
    }
    SilverLogger.getLogger(this)
        .info("ending indexation of personal component of type  ''{0}''", personalComponent);
  }

  @Override
  public void indexComponent(final String spaceId, final String componentId) {
    try {
      OrganizationController.get()
          .getComponentInstance(componentId)
          .ifPresent(this::indexComponent);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("failure while indexing component with id ''{0}''", new String[]{componentId}, e);
    }
  }

  public void indexPdc() {
    try {
      PdcIndexer indexer = PdcIndexer.getInstance();
      indexer.index();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("failure while indexing PDC", e);
    }
  }

  private String firstLetterToLowerCase(String str) {
    return StringUtil.uncapitalize(str);
  }

  private ComponentIndexation getIndexer(SilverpeasComponentInstance compoInst) {
    ComponentIndexation componentIndexer;
    try {
      String qualifier = compoInst.getName() + ComponentIndexation.QUALIFIER_SUFFIX;
      componentIndexer = ServiceProvider.getService(qualifier);
    } catch (SilverpeasRuntimeException ex) {
      SilverLogger.getLogger(this)
          .silent(ex)
          .warn("no indexer for component ''{0}'' with id ''{1}'' ({2})", compoInst.getLabel(),
              compoInst.getId(), ex.getMessage());
      componentIndexer = ServiceProvider.getService(ComponentIndexerAdapter.class);
    }
    return componentIndexer;
  }

  public void indexUsers() {
    SilverLogger.getLogger(this).debug("starting indexation of users");
    admin.indexAllUsers();
    SilverLogger.getLogger(this).debug("ending indexation of users");
  }

  public void indexGroups() {
    SilverLogger.getLogger(this).debug("starting indexation of groups");
    admin.indexAllGroups();
    SilverLogger.getLogger(this).debug("ending indexation of groups");
  }
}
