/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.webapi.thumbnail;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailException;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.io.media.image.thumbnail.service.ThumbnailService;
import org.silverpeas.core.webapi.base.RESTWebService;

/**
 * A REST Web resource representing a given thumbnail.
 * It is a web service that provides an access to a thumbnail referenced by its URL.
 */
@Service
@RequestScoped
@Path("thumbnail/{componentId}/{contributionType}/{contributionId}")
@Authorized
public class ThumbnailResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;
  @PathParam("contributionType")
  private String contributionType;
  @PathParam("contributionId")
  private String contributionId;

  @Inject
  private ThumbnailService thumbnailService;

  @Override
  public String getComponentId() {
    return componentId;
  }

  @DELETE
  public void deleteThumbnail() {
    ThumbnailDetail thumbnail = new ThumbnailDetail(componentId,
        Integer.parseInt(getContributionId()), Integer.parseInt(getContributionType()));
    try {
      thumbnailService.deleteThumbnail(thumbnail);
    } catch (ThumbnailException e) {
      throw new WebApplicationException(e, Status.SERVICE_UNAVAILABLE);
    }
  }

  public void setContributionType(String contributionType) {
    this.contributionType = contributionType;
  }

  public String getContributionType() {
    return contributionType;
  }

  public void setContributionId(String contributionId) {
    this.contributionId = contributionId;
  }

  public String getContributionId() {
    return contributionId;
  }

}
