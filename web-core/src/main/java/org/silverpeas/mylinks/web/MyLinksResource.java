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

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.silverpeas.core.admin.OrganisationController;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.myLinks.ejb.MyLinksBm;
import com.silverpeas.myLinks.model.LinkDetail;
import com.silverpeas.util.StringUtil;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;

/**
 * A REST Web resource representing user favorite links. It is a web service that provides an access
 * to user links referenced by its URL.
 */
@Service
@RequestScoped
@Path("mylinks")
@Authorized
public class MyLinksResource extends RESTWebService {

  private static final String PATH_SEPARATOR = " > ";

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

    List<MyLinkEntity> myLinkEntities = new ArrayList<MyLinkEntity>();
    for (LinkDetail linkDetail : links) {
      URI uri = getURI(linkDetail, baseUri);
      if (linkDetail.isVisible()) {
        myLinkEntities.add(MyLinkEntity.fromLinkDetail(linkDetail, uri));
      }
    }
    return myLinkEntities;
  }

  /**
   * Gets the JSON representation of the user favorite links. Return only link of the current user.
   * @return the response to the HTTP GET request with the JSON representation of the user links.
   */
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity getMyLink(@PathParam("id") String linkId) {
    Collection<LinkDetail> links = getMyLinksBm().getAllLinks(getUserDetail().getId());
    for (LinkDetail linkDetail : links) {
      if (linkDetail.getLinkId() == Integer.parseInt(linkId)) {
        MyLinkEntity myLinkEntity =
            MyLinkEntity.fromLinkDetail(linkDetail, getUriInfo().getAbsolutePath());
        return myLinkEntity;
      }
    }
    throw new WebApplicationException(Status.FORBIDDEN);
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

  @PUT
  @Path("{linkId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateLink(@PathParam("commentId") String linkId, final MyLinkEntity updatedLink) {
    checkIsValid(updatedLink);
    LinkDetail linkDetail = updatedLink.toLinkDetail();
    linkDetail.setUserId(getUserDetail().getId());
    getMyLinksBm().updateLink(linkDetail);
    return Response.ok(updatedLink).build();
  }

  @POST
  @Path("space/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addSpaceLink(@PathParam("id") String spaceId) {
    if (StringUtil.isDefined(spaceId) &&
        organisationController.isSpaceAvailable(spaceId, getUserDetail().getId())) {
      SpaceInstLight newFavoriteSpace = organisationController.getSpaceInstLightById(spaceId);
      String userLanguage = getUserPreferences().getLanguage();
      LinkDetail linkDetail = new LinkDetail();
      linkDetail.setUrl("/Space/" + spaceId);
      StringBuilder spaceNameBuilder = buildSpacePathName(spaceId, userLanguage);
      linkDetail.setName(spaceNameBuilder.toString());
      linkDetail.setDescription(newFavoriteSpace.getDescription(userLanguage));
      linkDetail.setVisible(true);
      linkDetail.setPopup(false);
      linkDetail.setUserId(getUserDetail().getId());
      getMyLinksBm().createLink(linkDetail);
      MyLinkEntity createdEntity =
          MyLinkEntity.fromLinkDetail(linkDetail,
              getURI(linkDetail, getUriInfo().getAbsolutePath().toString()));
      return Response.ok(createdEntity).build();
    } else {
      return Response.status(Status.BAD_REQUEST).build();
    }
  }
  
  @POST
  @Path("saveLinesOrder")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveLinesOrder(@QueryParam("ids") List<String> ids) {
    if (ids != null && ids.size() != 0) {

      return Response.ok().build();
    } else {
      return Response.status(Status.BAD_REQUEST).build();
    }
  }

  private StringBuilder buildSpacePathName(String spaceId, String userLanguage) {
    List<SpaceInst> spaces = organisationController.getSpacePath(spaceId);
    StringBuilder nameBuilder = new StringBuilder();
    for (SpaceInst spaceInst : spaces) {
      if (nameBuilder.length() > 0) {
        nameBuilder.append(PATH_SEPARATOR);
      }
      nameBuilder.append(spaceInst.getName(userLanguage));
    }
    return nameBuilder;
  }

  @POST
  @Path("app/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addAppLink(@PathParam("id") String applicationId) {
    if (StringUtil.isDefined(applicationId) &&
        organisationController.isComponentAvailable(applicationId, getUserDetail().getId())) {
      ComponentInstLight newFavoriteApp =
          organisationController.getComponentInstLight(applicationId);
      String userLanguage = getUserPreferences().getLanguage();
      LinkDetail linkDetail = new LinkDetail();
      linkDetail.setUrl("/Component/" + applicationId);
      StringBuilder appNameBuilder = buildAppPathName(applicationId, userLanguage);
      appNameBuilder.append(PATH_SEPARATOR).append(newFavoriteApp.getName(userLanguage));
      linkDetail.setName(appNameBuilder.toString());
      linkDetail.setDescription(newFavoriteApp.getDescription(userLanguage));
      linkDetail.setVisible(true);
      linkDetail.setPopup(false);
      linkDetail.setUserId(getUserDetail().getId());
      getMyLinksBm().createLink(linkDetail);
      MyLinkEntity createdEntity =
          MyLinkEntity.fromLinkDetail(linkDetail,
              getURI(linkDetail, getUriInfo().getAbsolutePath().toString()));
      return Response.ok(createdEntity).build();
    } else {
      return Response.status(Status.BAD_REQUEST).build();
    }
  }

  private StringBuilder buildAppPathName(String applicationId, String userLanguage) {
    List<SpaceInst> spaces = organisationController.getSpacePathToComponent(applicationId);
    StringBuilder nameBuilder = new StringBuilder();
    for (SpaceInst spaceInst : spaces) {
      if (nameBuilder.length() > 0) {
        nameBuilder.append(PATH_SEPARATOR);
      }
      nameBuilder.append(spaceInst.getName(userLanguage));
    }
    return nameBuilder;
  }

  protected void checkIsValid(final MyLinkEntity myLink) {
    if (!StringUtil.isDefined(myLink.getUrl()) || !StringUtil.isDefined(myLink.getName())) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
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
      Logger.getLogger(MyLinksResource.class.getName()).log(Level.SEVERE, null, e);
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

  @Inject
  private OrganisationController organisationController;

}
