/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.admin.space.quota.process.check;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceServiceProvider;
import org.silverpeas.core.admin.space.quota.DataStorageSpaceQuotaKey;
import org.silverpeas.core.admin.space.quota.process.check.exception.DataStorageQuotaException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.process.io.IOAccess;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.management.AbstractFileProcessCheck;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.admin.quota.constant.QuotaLoad;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Yohann Chastagnier
 */
public class DataStorageQuotaProcessCheck extends AbstractFileProcessCheck {
  protected static boolean dataStorageInSpaceQuotaActivated;

  static {
    final SettingBundle settings = ResourceLocator.getSettingBundle(
        "org.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings");
    dataStorageInSpaceQuotaActivated =
        settings.getBoolean("quota.space.datastorage.activated", false);
  }

  @Inject
  private OrganizationController organizationController;

  /*
   * (non-Javadoc)
   * @see
   * AbstractFileProcessCheck#checkFiles(org.silverpeas.process
   * .management.ProcessExecutionContext, FileHandler)
   */
  @Override
  public void checkFiles(final ProcessExecutionContext processExecutionContext,
      final FileHandler fileHandler) throws Exception {

    // If not activated, noting is done
    if (!dataStorageInSpaceQuotaActivated) {
      return;
    }

    // Treatment on write only
    if (IOAccess.READ_WRITE.equals(fileHandler.getIoAccess())) {

      // Checking data storage quota on each space detected
      for (final SpaceInst space : indentifyHandledSpaces(processExecutionContext, fileHandler)) {
        try {
          SpaceServiceProvider.getDataStorageSpaceQuotaService()
              .verify(DataStorageSpaceQuotaKey.from(space),
                  SpaceDataStorageQuotaCountingOffset.from(space, fileHandler));
        } catch (final QuotaException quotaException) {

          // Loading the component from which a user action has generated a quota exception
          ComponentInstLight fromComponent = null;
          final String fromComponentInstanceId = processExecutionContext.getComponentInstanceId();
          if (StringUtil.isDefined(fromComponentInstanceId)) {
            fromComponent = organizationController.getComponentInstLight(fromComponentInstanceId);
          }

          // Throwing the data storage exception
          DataStorageQuotaException exception =
              new DataStorageQuotaException(quotaException.getQuota(), space, fromComponent);
          MessageNotifier.addSevere(SilverpeasTransverseErrorUtil
              .performExceptionMessage(exception, MessageManager.getLanguage()));
          throw exception;
        }
      }
    }
  }

  /**
   * Identifying all spaces aimed by the process chained execution
   * @param processExecutionProcess
   * @param fileHandler
   * @return
   */
  private Collection<SpaceInst> indentifyHandledSpaces(
      final ProcessExecutionContext processExecutionProcess, final FileHandler fileHandler) {
    final Set<String> spaceIds = new HashSet<String>();
    final Set<String> componentInstanceIds = new HashSet<String>();

    // Component instance id from the context
    if (StringUtil.isDefined(processExecutionProcess.getComponentInstanceId())) {
      componentInstanceIds.add(processExecutionProcess.getComponentInstanceId());
    }

    // Component instance ids from the session
    componentInstanceIds.addAll(fileHandler.getSessionHandledRootPathNames(true));

    // Space ids
    ComponentInst component;
    for (final String componentInstanceId : componentInstanceIds) {
      component = organizationController.getComponentInst(componentInstanceId);
      if (component != null) {
        spaceIds.add(component.getDomainFatherId());
      }
    }

    // Loading all SpaceInst detected
    final List<SpaceInst> handledSpaces = new ArrayList<SpaceInst>();
    for (final String spaceId : spaceIds) {
      SpaceInst handledSpace = organizationController.getSpaceInstById(spaceId);
      if (!QuotaLoad.UNLIMITED.equals(handledSpace.getDataStorageQuota().getLoad())) {
        handledSpaces.add(handledSpace);
      }
    }
    return handledSpaces;
  }
}
