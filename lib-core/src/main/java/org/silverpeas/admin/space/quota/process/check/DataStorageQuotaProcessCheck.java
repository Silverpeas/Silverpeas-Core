/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.admin.space.quota.process.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.silverpeas.admin.space.SpaceServiceFactory;
import org.silverpeas.admin.space.quota.DataStorageSpaceQuotaKey;
import org.silverpeas.admin.space.quota.process.check.exception.DataStorageQuotaException;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.process.io.IOAccess;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.management.AbstractFileProcessCheck;
import org.silverpeas.process.management.ProcessExecutionContext;
import org.silverpeas.quota.exception.QuotaException;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInst;

/**
 * @author Yohann Chastagnier
 */
@Named
public class DataStorageQuotaProcessCheck extends AbstractFileProcessCheck {

  @Inject
  private OrganisationController organizationController;

  /*
   * (non-Javadoc)
   * @see
   * org.silverpeas.process.management.AbstractFileProcessCheck#checkFiles(org.silverpeas.process
   * .management.ProcessExecutionContext, org.silverpeas.process.io.file.FileHandler)
   */
  @Override
  public void checkFiles(final ProcessExecutionContext processExecutionProcess,
      final FileHandler fileHandler) throws Exception {

    // Treatment on write only
    if (IOAccess.READ_WRITE.equals(fileHandler.getIoAccess())) {

      // Checking data storage quota on each space detected
      for (final SpaceInst space : indentifyHandledSpaces(processExecutionProcess, fileHandler)) {
        try {
          SpaceServiceFactory.getDataStorageSpaceQuotaService().verify(
              DataStorageSpaceQuotaKey.from(space),
              SpaceDataStorageQuotaCountingOffset.from(space, fileHandler));
        } catch (final QuotaException quotaException) {

          // Loading the component from which a user action has generated a quota exception
          ComponentInstLight fromComponent = null;
          final String fromComponentInstanceId = processExecutionProcess.getComponentInstanceId();
          if (StringUtil.isDefined(fromComponentInstanceId)) {
            fromComponent = organizationController.getComponentInstLight(fromComponentInstanceId);
          }

          // Throwing the data storage exception
          throw new DataStorageQuotaException(quotaException.getQuota(), space, fromComponent);
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
    final List<SpaceInst> spaces = new ArrayList<SpaceInst>();
    for (final String spaceId : spaceIds) {
      spaces.add(organizationController.getSpaceInstById(spaceId));
    }
    return spaces;
  }
}
