/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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
package org.silverpeas.mylinks.web;

import static com.stratelia.webactiv.util.JNDINames.MYLINKSBM_EJBHOME;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.silverpeas.rating.web.RatingEntity;
import org.silverpeas.util.NotifierUtil;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.myLinks.ejb.MyLinksBm;
import com.silverpeas.myLinks.model.LinkDetail;
import com.silverpeas.util.StringUtil;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;

/**
 * A REST Web resource representing user favorite links. 
 * It is a web service that provides an access to user links referenced by its URL.
 */
@Service
@RequestScoped
@Path("mylinks")
@Authorized
public class MyLinkResource extends RESTWebService {
  
  /**
   * Gets the JSON representation of the user favorite links.
   * @return the response to the HTTP GET request with the JSON representation of the user links.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<MyLinkEntity> getMyLinks() {
    UserDetail curUser = getUserDetail();
    
    Collection<LinkDetail> links = getMyLinksBm().getAllLinks(curUser.getId());
    String baseUri = getUriInfo().getAbsolutePath().toString();
    
    List<MyLinkEntity> entities = new ArrayList<MyLinkEntity>();
    for (LinkDetail linkDetail : links) {
      URI uri = getURI(linkDetail, baseUri);
      if (linkDetail.isVisible()) {
        entities.add(MyLinkEntity.fromLinkDetail(linkDetail, uri));
      }
    }
    return entities;
  }
  
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addLink(final MyLinkEntity newLink) {
    LinkDetail linkDetail = newLink.toLinkDetail();
    linkDetail.setUserId(getUserDetail().getId());
    getMyLinksBm().createLink(linkDetail);
    return Response.ok(newLink).build();
  }

  @Override
  public String getComponentId() {
    // Dont need to return a component identifier
    return null;
  }
  
  private URI getURI(LinkDetail link, String baseUri) {
    URI uri;
    try {
      uri = new URI(baseUri + "/mylinks/" + link.getLinkId());
    } catch (URISyntaxException e) {
      Logger.getLogger(MyLinkResource.class.getName()).log(Level.SEVERE, null, e);
      throw new RuntimeException(e.getMessage(), e);
    }
    return uri;
  }
  
  private MyLinksBm getMyLinksBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(MYLINKSBM_EJBHOME, MyLinksBm.class);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }
  
}
