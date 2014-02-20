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
package org.silverpeas.admin.component.process.check;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.silverpeas.admin.component.exception.ComponentFileFilterException;
import org.silverpeas.admin.component.parameter.ComponentFileFilterParameter;
import org.silverpeas.notification.message.MessageManager;
import org.silverpeas.process.io.IOAccess;
import org.silverpeas.process.io.file.DummyHandledFile;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.management.AbstractFileProcessCheck;
import org.silverpeas.process.management.ProcessExecutionContext;
import org.silverpeas.util.NotifierUtil;
import org.silverpeas.util.error.SilverpeasTransverseErrorUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Yohann Chastagnier
 */
@Named
public class ComponentFileFilterProcessCheck extends AbstractFileProcessCheck {

  @Inject
  private OrganizationController organizationController;

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

      // Loading all ComponentInst detected
      final Set<String> componentInstanceIds =
          indentifyComponentInstances(processExecutionProcess, fileHandler);

      // Checking authorized and forbidden files on each component detected
      for (final String componentInstanceId : componentInstanceIds) {

        final ComponentInst component =
            organizationController.getComponentInst(componentInstanceId);

        // Component file filter that contains authorized and forbidden rules
        final ComponentFileFilterParameter componentFileFilter =
            ComponentFileFilterParameter.from(component);

        // Checking all files included in session
        for (File sessionDirectory : fileHandler.listAllSessionHandledRootPathFiles()) {
          for (File sessionFile : FileUtils
              .listFiles(sessionDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {

            // Verifying current file if it is authorized
            if (sessionFile.getPath().contains(component.getId())) {
              componentFileFilter.verifyFileAuthorized(sessionFile);
            }
          }
        }

        // Checking dummy handled files
        for (DummyHandledFile dummyHandledFile : fileHandler
            .getDummyHandledFiles(componentInstanceId)) {
          if (!dummyHandledFile.isDeleted() &&
              !componentFileFilter.isMimeTypeAuthorized(dummyHandledFile.getMimeType())) {
            ComponentFileFilterException exception =
                new ComponentFileFilterException(componentFileFilter, dummyHandledFile.getName());
            NotifierUtil.addSevere(SilverpeasTransverseErrorUtil
                .performExceptionMessage(exception, MessageManager.getLanguage()));
            throw exception;
          }
        }
      }
    }
  }

  /**
   * Identifying all component instances aimed by the process chained execution
   * @param processExecutionProcess
   * @param fileHandler
   * @return
   */
  private Set<String> indentifyComponentInstances(
      final ProcessExecutionContext processExecutionProcess, final FileHandler fileHandler) {
    final Set<String> componentInstanceIds = new HashSet<String>();

    // Component instance id from the context
    if (StringUtil.isDefined(processExecutionProcess.getComponentInstanceId())) {
      componentInstanceIds.add(processExecutionProcess.getComponentInstanceId());
    }

    // Component instance ids from the session
    componentInstanceIds.addAll(fileHandler.getSessionHandledRootPathNames(true));
    return componentInstanceIds;
  }
}
