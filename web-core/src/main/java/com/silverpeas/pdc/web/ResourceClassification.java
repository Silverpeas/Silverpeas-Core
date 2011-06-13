/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import com.silverpeas.rest.RESTWebService;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.model.PdcException;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * A REST Web service that represents the classification of a Silverpeas's resource on the
 * classification plan (named PdC).
 * 
 * The classification of a resource is defined by a its different positions on the axis of the
 * PdC. A position is then a set of one or more axis values. Each position of a classification can
 * be accessed through this web service by an URI; the classification positions are uniquely
 * identified by an URI.
 */
@Service
@Scope("request")
@Path("pdc/{componentId}/{contentId}")
public class ResourceClassification extends RESTWebService {

  @Inject
  private PdcBm pdcService;
  @Inject
  private ContentManager contentManager;
  @PathParam("componentId")
  private String componentId;
  @PathParam("contentId")
  private String contentId;

  @Override
  protected String getComponentId() {
    return this.componentId;
  }
  
  protected String getContentId() {
    return this.contentId;
  }
  
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public PdcClassificationEntity getPdCClassification() {
    checkUserPriviledges();
    try {
      int silverObjectId = getSilverObjectId(getContentId(), getComponentId());
      getPdcService().getPositions(silverObjectId, getComponentId());
    } catch (ContentManagerException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
    return new PdcClassificationEntity().withURI(getUriInfo().getAbsolutePath());
  }
  
  private int getSilverObjectId(String contentId, String componentId) throws ContentManagerException {
    return getContentManager().getSilverContentId(contentId, componentId);
  }
  
  private PdcBm getPdcService() {
    return this.pdcService;
  }
  
  private ContentManager getContentManager() {
    return this.contentManager;
  }
}
