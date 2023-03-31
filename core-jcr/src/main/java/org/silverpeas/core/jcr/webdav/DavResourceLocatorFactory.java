/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.core.jcr.webdav;

import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.simple.LocatorFactoryImplEx;

/**
 * A factory of locators of resources in the JCR accessed through the WebDAV protocol. The resource
 * locator has for goal two things:
 * <ul>
 *   <li>convert the URL of the accessed resource to the path of this
 *  resource in the JCR tree after extracting from the URL the repository and the workspace
 *  name</li>
 *   <li>convert the path of the resource in the JCR tree to an URL for a WebDAV access of this
 *   resource.
 *   </li>
 * </ul>
 * @author mmoquillon
 */
public class DavResourceLocatorFactory extends LocatorFactoryImplEx {

  public DavResourceLocatorFactory(final String repositoryPrefix) {
    super(repositoryPrefix);
  }

  @Override
  public DavResourceLocator createResourceLocator(final String prefix, final String href) {
    return super.createResourceLocator(prefix, filterPath(href));
  }

  @Override
  public DavResourceLocator createResourceLocator(String prefix, String workspacePath, String path,
      boolean isResourcePath) {
    return super.createResourceLocator(prefix, workspacePath, filterPath(path), isResourcePath);
  }

  @Override
  public DavResourceLocator createResourceLocator(final String prefix, final String workspacePath,
      final String resourcePath) {
    return super.createResourceLocator(prefix, workspacePath, filterPath(resourcePath));
  }

  private String filterPath(String path) {
    return WebDavContext.getWebDavContext(path).getDocumentURL();
  }
}
