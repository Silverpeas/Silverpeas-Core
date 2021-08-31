/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.mylinks;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authenticated;

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
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * A REST Web resource representing user favorite links. It is a web service that provides an access
 * to user links referenced by its URL.
 */
@WebService
@Path(MyLinksResource.PATH)
@Authenticated
public class MyLinksResource extends RESTWebService {

  private static final String PATH_SEPARATOR = " > ";

  static final String PATH = "mylinks";

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

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
        myLinkEntities.add(toWebEntity(linkDetail));
      }
    }
    return myLinkEntities;
  }

  /**
   * Gets all the links of the current user.
   * @return a list of {@link LinkDetail} instances.
   */
  protected List<LinkDetail> getAllUserLinks() {
    return getMyLinksService().getAllLinksByUser(getUser().getId());
  }

  /**
   * Gets all the links of the instance represented by given parameter.
   * @return a list of {@link LinkDetail} instances.
   */
  protected List<LinkDetail> getAllInstanceLinks(String instanceId) {
    return getMyLinksService().getAllLinksByInstance(instanceId);
  }

  /**
   * Gets all the links of the instance represented by given parameter.
   * @return a list of {@link LinkDetail} instances.
   */
  protected List<LinkDetail> getAllInstanceObjectLinks(String instanceId, String objectId) {
    return getMyLinksService().getAllLinksByObject(instanceId, objectId);
  }

  /**
   * Gets the link corresponding to given identifier.
   * @return the {@link LinkDetail} associated to the given id.
   * @throws javax.ws.rs.WebApplicationException {@link Status#NOT_FOUND} if no link exists with the
   * given identifier, {@link Status#FORBIDDEN} if the owner of the existing link is not the current
   * user.
   */
  protected LinkDetail loadLink(String linkId) {
    LinkDetail link = getMyLinksService().getLink(linkId);
    if (link == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else if (!link.canBeModifiedBy(getUser())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return link;
  }

  /**
   * Gets the JSON representation of the user favorite links. Return only link of the current user.
   * @return the response to the HTTP GET request with the JSON representation of the user links.
   */
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity getLink(@PathParam("id") String linkId) {
    LinkDetail linkDetail = loadLink(linkId);
    return toWebEntity(linkDetail);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity addLink(final MyLinkEntity newLink) {
    checkMandatoryLinkData(newLink);
    LinkDetail linkDetail = newLink.toLinkDetail();
    linkDetail.setUserId(getUser().getId());
    return toWebEntity(getMyLinksService().createLink(linkDetail));
  }

  @PUT
  @Path("{linkId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity updateLink(final @PathParam("linkId") String linkId,
      final MyLinkEntity updatedLink) {
    verifyConsistency(linkId, updatedLink);
    verifyLinkCanBeModifiedByCurrentUser(updatedLink);
    checkMandatoryLinkData(updatedLink);
    LinkDetail linkDetail = updatedLink.toLinkDetail();
    linkDetail.setUserId(getUser().getId());
    return toWebEntity(getMyLinksService().updateLink(linkDetail));
  }

  @DELETE
  @Path("{linkId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteLink(final @PathParam("linkId") String linkId) {
    verifyLinkCanBeModifiedByCurrentUser(linkId);
    getMyLinksService().deleteLinks(new String[]{linkId});
    return Response.ok().build();
  }

  @POST
  @Path("space/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity addSpaceLink(@PathParam("id") String spaceId) {
    if (StringUtil.isDefined(spaceId) &&
        getOrganisationController().isSpaceAvailable(spaceId, getUser().getId())) {
      SpaceInstLight newFavoriteSpace = getOrganisationController().getSpaceInstLightById(spaceId);
      String userLanguage = getUserPreferences().getLanguage();
      StringBuilder spaceNameBuilder = buildSpacePathName(spaceId, userLanguage);
      LinkDetail linkDetail = createLinkInstance("/Space/", spaceId, spaceNameBuilder.toString(),
          newFavoriteSpace.getDescription(userLanguage));
      return toWebEntity(getMyLinksService().createLink(linkDetail));
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
    final LinkDetail linkOfPosition = verifyLinkCanBeModifiedByCurrentUser(linkPosition.getLinkId());
    final List<LinkDetail> linksToManage;
    if (isDefined(linkOfPosition.getObjectId())) {
      linksToManage = getAllInstanceObjectLinks(linkOfPosition.getInstanceId(), linkOfPosition.getObjectId());
    } else if (isDefined(linkOfPosition.getInstanceId())) {
      linksToManage = getAllInstanceLinks(linkOfPosition.getInstanceId());
    } else {
      linksToManage = getAllUserLinks();
    }
    int position = -1;
    for (LinkDetail link : linksToManage) {
      // Final position
      final int finalPosition;
      if (link.getLinkId() == linkPosition.getLinkId()) {
        finalPosition = linkPosition.getPosition();
      } else {
        position++;
        // If the current position is the one aimed by the change, the next one is taken
        if (position == linkPosition.getPosition()) {
          position++;
        }
        finalPosition = position;
      }
      if (!link.hasPosition() || link.getPosition() != finalPosition) {
        link.setHasPosition(true);
        link.setPosition(finalPosition);
        getMyLinksService().updateLink(link);
      }
    }
    return Response.ok(getLink(valueOf(linkPosition.getLinkId()))).build();
  }

  private StringBuilder buildSpacePathName(String spaceId, String userLanguage) {
    List<SpaceInstLight> spaces = getOrganisationController().getPathToSpace(spaceId);
    StringBuilder nameBuilder = new StringBuilder();
    for (SpaceInstLight spaceInst : spaces) {
      if (nameBuilder.length() > 0) {
        nameBuilder.append(PATH_SEPARATOR);
      }
      nameBuilder.append(spaceInst.getName(userLanguage));
    }
    return nameBuilder;
  }

  @POST
  @Path("app/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity addAppLink(@PathParam("id") String applicationId) {
    if (StringUtil.isDefined(applicationId) &&
        getOrganisationController().isComponentAvailableToUser(applicationId, getUser().getId())) {
      ComponentInstLight newFavoriteApp =
          getOrganisationController().getComponentInstLight(applicationId);
      String userLanguage = getUserPreferences().getLanguage();
      StringBuilder appNameBuilder = buildAppPathName(applicationId, userLanguage);
      appNameBuilder.append(PATH_SEPARATOR).append(newFavoriteApp.getName(userLanguage));
      LinkDetail linkDetail = createLinkInstance("/Component/", applicationId,
          appNameBuilder.toString(), newFavoriteApp.getDescription(userLanguage));
      return toWebEntity(getMyLinksService().createLink(linkDetail));
    }
    throw new WebApplicationException(Status.BAD_REQUEST);
  }

  private LinkDetail createLinkInstance(final String urlPrefix, final String resourceId,
      final String name, final String description) {
    final LinkDetail linkDetail = new LinkDetail();
    linkDetail.setUrl(urlPrefix + resourceId);
    linkDetail.setName(name);
    linkDetail.setDescription(description);
    linkDetail.setVisible(true);
    linkDetail.setPopup(false);
    linkDetail.setUserId(getUser().getId());
    return linkDetail;
  }

  private LinkDetail verifyLinkCanBeModifiedByCurrentUser(MyLinkEntity linkEntity) {
    return verifyLinkCanBeModifiedByCurrentUser(valueOf(linkEntity.getLinkId()));
  }

  private void verifyConsistency(final String linkId, final MyLinkEntity updatedLink) {
    if (isNotDefined(linkId) || !linkId.equals(valueOf(updatedLink.getLinkId()))) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
  }

  /**
   * This method verify that the owner of the link represented by the given id is the current user.
   * @param linkId an identifier of link.
   */
  private LinkDetail verifyLinkCanBeModifiedByCurrentUser(int linkId) {
    return verifyLinkCanBeModifiedByCurrentUser(valueOf(linkId));
  }

  /**
   * This method verify that the owner of the link represented by the given id is the current user.
   * @param linkId an identifier of link.
   */
  private LinkDetail verifyLinkCanBeModifiedByCurrentUser(String linkId) {
    return loadLink(linkId);
  }

  private StringBuilder buildAppPathName(String applicationId, String userLanguage) {
    List<SpaceInstLight> spaces = getOrganisationController().getPathToComponent(applicationId);
    StringBuilder nameBuilder = new StringBuilder();
    for (SpaceInstLight spaceInst : spaces) {
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
    // no need to return a component identifier
    return null;
  }

  private MyLinkEntity toWebEntity(final LinkDetail linkDetail) {
    return MyLinkEntity.fromLinkDetail(linkDetail, getLinkUri(linkDetail));
  }

  private URI getLinkUri(LinkDetail link) {
    return getUri().getWebResourcePathBuilder().path(valueOf(link.getLinkId())).build();
  }

  protected MyLinksService getMyLinksService() {
    try {
      return ServiceProvider.getService(MyLinksService.class);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

}
