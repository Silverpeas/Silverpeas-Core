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
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author Yohann Chastagnier
 */
public interface CommonArchive<T extends CommonArchive<T>> {

  /**
   * Denotes whether this archive contains a resource at the specified path
   * @param path
   * @return
   * @throws IllegalArgumentException If the path is not specified
   */
  boolean contains(String path) throws IllegalArgumentException;

  /**
   * Denotes whether this archive contains a class
   * @param aClass
   * @return
   * @throws IllegalArgumentException If the path is not specified
   */
  boolean contains(Class<?> aClass) throws IllegalArgumentException;

  /**
   * Adds the {@link Class}es, and all member (inner) {@link Class}es to the {@link Archive}.
   * @param classes The classes to add to the Archive
   * @return This archive
   * @throws IllegalArgumentException If no classes were specified
   */
  T addClasses(Class<?>... classes) throws IllegalArgumentException;

  /**
   * Adds all classes in the specified {@link Package}s to the {@link Archive}.
   * @param recursive Should the sub packages be added
   * @param packages All the packages to add represented by a String ("my/package")
   * @return This virtual archive
   * @throws IllegalArgumentException If no packages were specified
   * @see ClassContainer#addPackages(boolean, Filter, Package...)
   */
  T addPackages(boolean recursive, String... packages) throws IllegalArgumentException;

  /**
   * Adds the resource as a resource to the container, returning the container itself. <br/>
   * The resource will be placed into the Container Resource path under the same context from which
   * it was retrieved. <br/>
   * <br/>
   * The {@link ClassLoader} used to obtain the resource is up to the implementation.
   * @param resourceName resource to add
   * @return This virtual archive
   * @throws IllegalArgumentException If the resourceName is null
   * @see ClassContainer#addAsResource(Asset, ArchivePath)
   */
  T addAsResource(String resourceName) throws IllegalArgumentException;

  /**
   * Adds the resource as a resource to the container, returning the container itself. <br/>
   * <br/>
   * The {@link ClassLoader} used to obtain the resource is up to the implementation.
   * @param resourceName resource to add
   * @param target The target path within the archive in which to add the resource, relative to the
   * {@link Archive}s
   * resource path.
   * @return This virtual archive
   * @throws IllegalArgumentException if resourceName is null or if target is null
   * @see ClassContainer#addAsResource(Asset, ArchivePath)
   */
  T addAsResource(String resourceName, String target) throws IllegalArgumentException;


  /**
   * Applies a configuration by using directly the {@link WebArchive} instance provided by the
   * ShrinkWrap API.
   * @param onShrinkWrapWar the instance of an anonymous implementation of {@link OnShrinkWrapWar}
   * interface.
   * @return the instance of the war builder.
   */
  T applyManually(OnShrinkWrapWar onShrinkWrapWar);

  /**
   * In order to add configuration on WAR test archive.
   */
  public interface OnShrinkWrapWar {
    void applyManually(WebArchive war);
  }
}
