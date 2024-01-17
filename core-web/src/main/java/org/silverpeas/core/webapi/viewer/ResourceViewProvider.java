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

package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.webapi.media.EmbedMediaViewerResource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * Interface that defines the signature an {@link ResourceView} provider MUST implements to be
 * handled by viewer APIs.
 * <p>
 * The implementations MUST be registered into {@link ResourceViewProviderRegistry} at server
 * starting.
 * </p>
 * <p>
 *   For now, these provider implementations are exclusively used by
 *   {@link DocumentViewResource}, {@link PreviewResource} and {@link EmbedMediaViewerResource}
 *   WEB services.
 * </p>
 * @author silveryocha
 */
public interface ResourceViewProvider {

  /**
   * Gets against the current requester the authorized {@link ResourceView}.
   * @param resourceId the identifier of the resource.
   * @param resourceType the resource type the given identifier is related to.
   * @param language the optional language into which the resource is attempted.
   * @return a {@link ResourceView} instance.
   * @throws WebApplicationException with NOT FOUND code if resource has not been found.
   * @throws WebApplicationException with FORBIDDEN code if resource not authorized for current requester.
   */
  static ResourceView getAuthorizedResourceView(final String resourceId, final String resourceType,
      final String language) {
    if (isNotDefined(resourceId)) {
      throw new WebApplicationException("resourceId is missing", Response.Status.BAD_REQUEST);
    }
    final ResourceView resource = ResourceViewProviderRegistry.get()
        .getByResourceType(resourceType)
        .stream()
        .flatMap(p -> p.getByIdAndLanguage(resourceId, language).stream())
        .findFirst()
        .orElse(null);
    if (resource == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else if (!resource.canBeAccessedBy(User.getCurrentRequester())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return resource;
  }

  /**
   * Gets by a resource identifier the {@link ResourceView} instance if any.
   * @return optional {@link ResourceView} instance.
   */
  Optional<ResourceView> getByIdAndLanguage(final String mediaId, final String language);

  /**
   * Indicates the name of Silverpeas's service the provider is related to.
   * @return name of service.
   */
  String relatedToService();
}
