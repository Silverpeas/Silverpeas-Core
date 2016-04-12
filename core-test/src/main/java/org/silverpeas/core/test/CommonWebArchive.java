/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.test;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * @author Yohann Chastagnier
 */
public interface CommonWebArchive<T> extends CommonArchive<CommonWebArchive<T>> {

  /**
   * Adds the resource as a WEB-INF resource to the container, returning the container itself.
   * <br/>
   * The {@link ClassLoader} used to obtain the resource is up to the implementation.
   * @param resourceName resource to add
   * @param target The target path within the archive in which to add the resource, relative to the
   * {@link Archive}s
   * WEB-INF path.
   * @return This {@link Archive}
   * @throws IllegalArgumentException if resourceName or target is not specified
   */
  CommonWebArchive<T> addAsWebInfResource(String resourceName, String target) throws IllegalArgumentException;

  /**
   * Adds the {@link Asset} as a WEB-INF resource to the container, returning the container itself.
   * @param resource {@link Asset} resource to add
   * @param target The target path within the archive in which to add the resource, relative to the
   * {@link Archive}s
   * WEB-INF path.
   * @return This {@link Archive}
   * @throws IllegalArgumentException If the resource or target is not specified
   */
  CommonWebArchive<T> addAsWebInfResource(Asset resource, String target) throws IllegalArgumentException;
}
