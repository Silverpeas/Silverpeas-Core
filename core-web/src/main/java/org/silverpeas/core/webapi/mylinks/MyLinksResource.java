/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.webapi.mylinks;

import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.util.logging.SilverLogger.*;

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
    List<LinkDetail> links = getAllUserLinks();
    List<MyLinkEntity> myLinkEntities = new ArrayList<>();
    for (LinkDetail linkDetail : links) {
      if (linkDetail.isVisible()) {
        myLinkEntities.add(MyLinkEntity.fromLinkDetail(linkDetail, getLinkUri(linkDetail)));
      }
    }
    return myLinkEntities;
  }

  /**
   * Gets all the links of the current user.
   * @return
   */
  protected List<LinkDetail> getAllUserLinks() {
    UserDetail curUser = getUserDetail();
    return getMyLinksBm().getAllLinks(curUser.getId());
  }

  /**
   * Gets all the links of the current user.
   * @return the {@link LinkDetail} associated to the given id.
   * @throws javax.ws.rs.WebApplicationException {@link Status#NOT_FOUND} if no link exists with the
   * given identifier, {@link Status#FORBIDDEN} if the owner of the existing link is not the current
   * user.
   */
  protected LinkDetail getUserLink(String linkId) {
    LinkDetail userLink = getMyLinksBm().getLink(linkId);
    if (userLink == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    } else if (!getUserDetail().getId().equals(userLink.getUserId())) {
      throw new WebApplicationException(Status.FORBIDDEN);
    }
    return userLink;
  }

  /**
   * Gets the JSON representation of the user favorite links. Return only link of the current user.
   * @return the response to the HTTP GET request with the JSON representation of the user links.
   */
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity getMyLink(@PathParam("id") String linkId) {
    LinkDetail linkDetail = getUserLink(linkId);
    return MyLinkEntity.fromLinkDetail(linkDetail, getLinkUri(linkDetail));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity addLink(final MyLinkEntity newLink) {
    LinkDetail linkDetail = newLink.toLinkDetail();
    linkDetail.setUserId(getUserDetail().getId());
    getMyLinksBm().createLink(linkDetail);
    return getMyLink(String.valueOf(linkDetail.getLinkId()));
  }

  @PUT
  @Path("{linkId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity updateLink(final MyLinkEntity updatedLink) {
    verifyCurrentUserIsOwner(updatedLink);
    checkMandatoryLinkData(updatedLink);
    LinkDetail linkDetail = updatedLink.toLinkDetail();
    linkDetail.setUserId(getUserDetail().getId());
    getMyLinksBm().updateLink(linkDetail);
    return getMyLink(String.valueOf(linkDetail.getLinkId()));
  }

  @DELETE
  @Path("{linkId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteLink(final @PathParam("linkId") String linkId) {
    verifyCurrentUserIsOwner(linkId);
    getMyLinksBm().deleteLinks(new String[]{linkId});
    return Response.ok().build();
  }

  @POST
  @Path("space/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity addSpaceLink(@PathParam("id") String spaceId) {
    if (StringUtil.isDefined(spaceId) &&
        getOrganisationController().isSpaceAvailable(spaceId, getUserDetail().getId())) {
      SpaceInstLight newFavoriteSpace = getOrganisationController().getSpaceInstLightById(spaceId);
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
      return getMyLink(String.valueOf(linkDetail.getLinkId()));
    }
    throw new WebApplicationException(Status.BAD_REQUEST);
  }

  @POST
  @Path("saveLinesOrder")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveUserLinksOrder(MyLinkPosition linkPosition) {
    if (linkPosition == null) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
    verifyCurrentUserIsOwner(linkPosition.getLinkId());

    int position = -1;
    for (LinkDetail userLink : getAllUserLinks()) {

      // Final position
      final int finalPosition;
      if (userLink.getLinkId() == linkPosition.getLinkId()) {
        finalPosition = linkPosition.getPosition();
      } else {
        position++;
        // If the current position is the one aimed by the change, the next one is taken
        if (position == linkPosition.getPosition()) {
          position++;
        }
        finalPosition = position;
      }

      if (!userLink.hasPosition() || userLink.getPosition() != finalPosition) {
        userLink.setHasPosition(true);
        userLink.setPosition(finalPosition);
        getMyLinksBm().updateLink(userLink);
      }
    }

    return Response.ok(getMyLink(String.valueOf(linkPosition.getLinkId()))).build();
  }

  private StringBuilder buildSpacePathName(String spaceId, String userLanguage) {
    List<SpaceInst> spaces = getOrganisationController().getSpacePath(spaceId);
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
  public MyLinkEntity addAppLink(@PathParam("id") String applicationId) {
    if (StringUtil.isDefined(applicationId) &&
        getOrganisationController().isComponentAvailable(applicationId, getUserDetail().getId())) {
      ComponentInstLight newFavoriteApp =
          getOrganisationController().getComponentInstLight(applicationId);
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
      return getMyLink(String.valueOf(linkDetail.getLinkId()));
    }
    throw new WebApplicationException(Status.BAD_REQUEST);
  }

  private void verifyCurrentUserIsOwner(MyLinkEntity linkEntity) {
    verifyCurrentUserIsOwner(String.valueOf(linkEntity.getLinkId()));
  }

  /**
   * This method verify that the owner of the link represented by the given id is the current user.
   * @param linkId
   */
  private void verifyCurrentUserIsOwner(int linkId) {
    verifyCurrentUserIsOwner(String.valueOf(linkId));
  }

  /**
   * This method verify that the owner of the link represented by the given id is the current user.
   * @param linkId
   */
  private void verifyCurrentUserIsOwner(String linkId) {
    getUserLink(linkId);
  }

  private StringBuilder buildAppPathName(String applicationId, String userLanguage) {
    List<SpaceInst> spaces = getOrganisationController().getSpacePathToComponent(applicationId);
    StringBuilder nameBuilder = new StringBuilder();
    for (SpaceInst spaceInst : spaces) {
      if (nameBuilder.length() > 0) {
        nameBuilder.append(PATH_SEPARATOR);
      }
      nameBuilder.append(spaceInst.getName(userLanguage));
    }
    return nameBuilder;
  }

  public static void checkMandatoryLinkData(final MyLinkEntity myLink) {
    if (!StringUtil.isDefined(myLink.getUrl()) || !StringUtil.isDefined(myLink.getName())) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
  }

  @Override
  public String getComponentId() {
    // Dont need to return a component identifier
    return null;
  }

  private URI getLinkUri(LinkDetail link) {
    URI uri;
    try {
      uri = new URI(getUriInfo().getBaseUri() + "/mylinks/" + link.getLinkId());
    } catch (URISyntaxException e) {
      getLogger(MyLinksResource.class).error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage(), e);
    }
    return uri;
  }

  protected MyLinksService getMyLinksBm() {
    try {
      return ServiceProvider.getService(MyLinksService.class);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

}
