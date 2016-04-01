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
package org.silverpeas.core.process.management;

import org.silverpeas.core.process.io.file.FileHandler;

import org.silverpeas.core.admin.user.model.UserDetail;

/**
 * This is an Silverpeas process execution context container.
 * At least, current user informations and component instance id from which
 * <code>ProcessManagement</code> execution call is done.
 * This class should be extended when additional contextual data have to be accessible into
 * processes.
 * @author Yohann Chastagnier
 */
public class ProcessExecutionContext {

  private FileHandler fileHandler;
  private final UserDetail user;
  private final String componentInstanceId;
  private final boolean requiresNewFileTransaction;

  /**
   * Default constructor
   * @param componentInstanceId
   */
  public ProcessExecutionContext(final String componentInstanceId) {
    this(null, componentInstanceId, false);
  }

  /**
   * Default constructor
   * @param user
   * @param componentInstanceId
   */
  public ProcessExecutionContext(final UserDetail user, final String componentInstanceId) {
    this(user, componentInstanceId, false);
  }

  /**
   * Default constructor
   * @param user
   * @param componentInstanceId
   * @param requiresNewFileTransaction
   */
  public ProcessExecutionContext(final UserDetail user, final String componentInstanceId,
      final boolean requiresNewFileTransaction) {
    super();
    this.user = user;
    this.componentInstanceId = componentInstanceId;
    this.requiresNewFileTransaction = requiresNewFileTransaction;
  }

  /**
   * @return the user
   */
  public UserDetail getUser() {
    return user;
  }

  /**
   * @return the componentInstanceId
   */
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * @return the requiredNewFileTransaction
   */
  public boolean requiresNewFileTransaction() {
    return requiresNewFileTransaction;
  }

  /**
   * @return the fileHandler
   */
  FileHandler getFileHandler() {
    return fileHandler;
  }

  /**
   * @param fileHandler the fileHandler to set
   */
  void setFileHandler(final FileHandler fileHandler) {
    this.fileHandler = fileHandler;
  }
}
