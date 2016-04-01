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
package org.silverpeas.core.process.io.file;

/**
 * This class permits to wrap a representation of a file that it will be used only for its
 * information. It will not be physically manipulated.
 * <p/>
 * It is useful for process check operation.
 * <p/>
 * User: Yohann Chastagnier
 * Date: 17/10/13
 */
public interface DummyHandledFile {

  /**
   * Gets the component instance id at which the virtual file is associated.
   * @return the identifier of the component instance.
   */
  String getComponentInstanceId();

  /**
   * Gets the path of the virtual file.
   * In most of cases, this information is the same as the name one.
   * @return the virtual path of the file.
   */
  String getPath();

  /**
   * Gets the name of the virtual file.
   * @return the virtual name of the file.
   */
  String getName();

  /**
   * Gets the size of the virtual file (bytes).
   * @return the virtual size of the file.
   */
  long getSize();

  /**
   * Gets the mime type of the virtual file.
   * @return the virtual mime type of the file.
   */
  String getMimeType();

  /**
   * Indicates if the virtual file is a deleted one.
   * @return true if the virtual file is being to be deleted, false otherwise.
   */
  boolean isDeleted();
}
