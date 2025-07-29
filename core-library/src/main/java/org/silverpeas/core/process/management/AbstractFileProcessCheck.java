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
package org.silverpeas.core.process.management;

import org.silverpeas.core.process.check.AbstractProcessCheck;
import org.silverpeas.core.process.check.ProcessCheckType;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.kernel.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract extension of <code>AbstractProcessCheck</code> oriented on file system verifications.
 * @author Yohann Chastagnier
 * @see AbstractProcessCheck
 */
public abstract class AbstractFileProcessCheck extends AbstractProcessCheck {

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.check.Check#getType()
   */
  @Override
  public ProcessCheckType getType() {
    return ProcessCheckType.FILESYSTEM;
  }

  /*
   * (non-Javadoc)
   *
   * org.silverpeas.process.check.Check#check(ProcessExecutionContext
   * )
   */
  @Override
  public final void check(final ProcessExecutionContext processExecutionContext) {
    checkFiles(processExecutionContext, processExecutionContext.getFileHandler());
  }

  /**
   * Contains the treatment of the verification. The file handler (@see {@link FileHandler})
   * associated to the current execution of chained Silverpeas processes is passed.
   * @param processExecutionContext the context of chained list of checks execution.
   * @param fileHandler the instance of the file handler.
   */
  public abstract void checkFiles(ProcessExecutionContext processExecutionContext,
      final FileHandler fileHandler);

  /**
   * Identifying all component instances aimed by the process chained execution
   * @param processExecutionProcess the process execution context
   * @param fileHandler the handler on a file.
   * @return a set of component instance identifiers.
   */
  protected final Set<String> identifyComponentInstances(
      final ProcessExecutionContext processExecutionProcess, final FileHandler fileHandler) {
    final Set<String> componentInstanceIds = new HashSet<>();

    // Component instance id from the context
    if (StringUtil.isDefined(processExecutionProcess.getComponentInstanceId())) {
      componentInstanceIds.add(processExecutionProcess.getComponentInstanceId());
    }

    // Component instance ids from the session
    componentInstanceIds.addAll(fileHandler.getSessionHandledRootPathNames(true));
    return componentInstanceIds;
  }
}
