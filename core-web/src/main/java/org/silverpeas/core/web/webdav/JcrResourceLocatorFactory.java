/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.webdav;

import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.simple.LocatorFactoryImplEx;

/**
 * @author mmoquillon
 */
public class JcrResourceLocatorFactory extends LocatorFactoryImplEx implements DavLocatorFactory {

  public JcrResourceLocatorFactory(final String repositoryPrefix) {
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
    return SilverpeasJcrWebdavContext.getWebdavContext(path).getJcrDocumentUrlLocation();
  }
}
