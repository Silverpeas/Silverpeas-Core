/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.viewer.service.ViewerContext;

/**
 * Interface that defines a resource view to be taken in charge by viewer APIs.
 * <p>
 *   This interface allows to decouple the attachment service stuffs from viewer APIs,
 *   and so, to make them usable by any kind of service into Silverpeas context.
 * </p>
 * @author silveryocha
 */
public interface ResourceView extends Securable {

  /**
   * Gets the identifier of the resource view.
   * @return a string representing a unique identifier.
   */
  String getId();

  /**
   * Gets the name of the resource view.
   * @return a string.
   */
  String getName();

  /**
   * Gets the content type of the resource view.
   * @return a string.
   */
  String getContentType();

  /**
   * Gets the {@link ViewerContext} of the resource view.
   * @return a {@link ViewerContext} instance.
   */
  ViewerContext getViewerContext();

  /**
   * Indicates if the media is downloadable by given user.
   * @param user a {@link User} instance representing the current requester.
   * @return true if it is downloadable.
   */
  boolean isDownloadableBy(User user);
}
