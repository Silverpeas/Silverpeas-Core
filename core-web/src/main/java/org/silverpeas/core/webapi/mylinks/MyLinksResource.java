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

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.inject.Inject;
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
import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.valueOf;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.isNotDefined;
import static org.silverpeas.core.webapi.mylinks.MyLinksResourceURIs.MYLINKS_BASE_URI;

/**
 * A REST Web resource representing user favorite links. It is a web service that provides an access
 * to user links referenced by its URL.
 */
@WebService
@Path(MyLinksResource.PATH)
@Authenticated
public class MyLinksResource extends RESTWebService {

  static final String PATH = MYLINKS_BASE_URI;

  @Inject
  private MyLinksResourceURIs uri;

  @Inject
  private MyLinksWebManager manager;

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  /**
   * Gets the JSON representation of all user categories of favorite links. Return only categories of the current user.
   * @return the response to the HTTP GET request with the JSON representation of all user categories.
   */
  @GET
  @Path("categories")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CategoryEntity> getAllCategoriesOfUser() {
    return manager.getAllCategoriesOfCurrentUser()
        .stream()
        .map(this::toWebEntity)
        .collect(Collectors.toList());
  }

  /**
   * Gets the JSON representation of the user category of favorite links. Return only category of the current user.
   * @return the response to the HTTP GET request with the JSON representation of the user categories.
   */
  @GET
  @Path("categories/{catId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CategoryEntity getCategory(@PathParam("catId") String catId) {
    return toWebEntity(manager.getAuthorizedCategory(catId));
  }

  @POST
  @Path("categories")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CategoryEntity addCategory(final CategoryEntity newCategory) {
    return toWebEntity(manager.createCategory(newCategory));
  }

  @PUT
  @Path("categories/{catId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CategoryEntity updateCategory(final @PathParam("catId") String catId,
      final CategoryEntity updatedCategory) {
    verifyConsistency(catId, updatedCategory);
    return toWebEntity(manager.updateCategory(updatedCategory));
  }

  @DELETE
  @Path("categories/{catId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteCategory(final @PathParam("catId") String catId) {
    manager.deleteCategories(new String[]{catId});
    return Response.ok().build();
  }

  @POST
  @Path("categories/saveLinesOrder")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveUserCategoriesOrder(CategoryPosition categoryPosition) {
    if (categoryPosition == null) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
    manager.getAuthorizedCategory(valueOf(categoryPosition.getCatId()));
    final List<CategoryDetail> categoryToManage = manager.getAllCategoriesOfCurrentUser();
    performPositionUpdate(categoryPosition.getCatId(), categoryPosition.getPosition(),
        categoryToManage.stream(),
        CategoryDetail::getId, CategoryDetail::hasPosition, CategoryDetail::getPosition, (c, p) -> {
          c.setHasPosition(true);
          c.setPosition(p);
          manager.updateCategory(toWebEntity(c));
        });
    return Response.ok(getCategory(valueOf(categoryPosition.getCatId()))).build();
  }

  /**
   * Gets the JSON representation of the user favorite links.
   * @return the response to the HTTP GET request with the JSON representation of the user links.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<MyLinkEntity> getMyLinks() {
    return manager.getAllLinksOfCurrentUser()
        .stream()
        .filter(LinkDetail::isVisible)
        .map(this::toWebEntity)
        .collect(Collectors.toList());
  }

  /**
   * Gets the JSON representation of the user favorite links. Return only link of the current user.
   * @return the response to the HTTP GET request with the JSON representation of the user links.
   */
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity getLink(@PathParam("id") String linkId) {
    return toWebEntity(manager.getAuthorizedLink(linkId));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity addLink(final MyLinkEntity newLink) {
    return toWebEntity(manager.createLink(newLink));
  }

  @PUT
  @Path("{linkId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity updateLink(final @PathParam("linkId") String linkId,
      final MyLinkEntity updatedLink) {
    verifyConsistency(linkId, updatedLink);
    return toWebEntity(manager.updateLink(updatedLink));
  }

  @DELETE
  @Path("{linkId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteLink(final @PathParam("linkId") String linkId) {
    manager.deleteLinks(new String[]{linkId});
    return Response.ok().build();
  }

  @POST
  @Path("saveLinesOrder")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveUserLinksOrder(MyLinkPosition linkPosition) {
    if (linkPosition == null) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
    final LinkDetail linkOfPosition = manager.getAuthorizedLink(valueOf(linkPosition.getLinkId()));
    final List<LinkDetail> linksToManage;
    if (isDefined(linkOfPosition.getObjectId())) {
      linksToManage = manager.getAllLinksOfResourceOnInstance(linkOfPosition.getObjectId(),
          linkOfPosition.getInstanceId());
    } else if (isDefined(linkOfPosition.getInstanceId())) {
      linksToManage = manager.getAllLinksOfInstance(linkOfPosition.getInstanceId());
    } else {
      linksToManage = manager.getAllLinksOfCurrentUser();
    }
    performPositionUpdate(linkPosition.getLinkId(), linkPosition.getPosition(),
        linksToManage.stream()
            .filter(l -> sameCategoryFilter(linkOfPosition.getCategory(), l.getCategory())),
        LinkDetail::getLinkId, LinkDetail::hasPosition, LinkDetail::getPosition, (l, p) -> {
          l.setHasPosition(true);
          l.setPosition(p);
          manager.updateLink(toWebEntity(l));
        });
    return Response.ok(getLink(valueOf(linkPosition.getLinkId()))).build();
  }

  private <T> void performPositionUpdate(final int targetIdentifier, final int targetPosition,
      final Stream<T> details, final ToIntFunction<T> identifier, final Predicate<T> hasPosition,
      final ToIntFunction<T> getPosition, final ObjIntConsumer<T> updater) {
    final Mutable<Integer> position = Mutable.of(-1);
    details.forEach(d -> {
      // Final position
      final int finalPosition;
      if (identifier.applyAsInt(d) == targetIdentifier) {
        finalPosition = targetPosition;
      } else {
        position.set(position.get() + 1);
        // If the current position is the one aimed by the change, the next one is taken
        if (position.get() == targetPosition) {
          position.set(position.get() + 1);
        }
        finalPosition = position.get();
      }
      if (!hasPosition.test(d) || getPosition.applyAsInt(d) != finalPosition) {
        updater.accept(d, finalPosition);
      }
    });
  }

  private boolean sameCategoryFilter(final CategoryDetail ref, final CategoryDetail category) {
    if (ref == null) {
      return category == null;
    } else {
      return ref.equals(category);
    }
  }

  private void verifyConsistency(final String linkId, final MyLinkEntity updatedLink) {
    if (isNotDefined(linkId) || !linkId.equals(valueOf(updatedLink.getLinkId()))) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
  }

  private void verifyConsistency(final String catId, final CategoryEntity updatedCategory) {
    if (isNotDefined(catId) || !catId.equals(valueOf(updatedCategory.getCategoryId()))) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
  }

  @Override
  public String getComponentId() {
    // no need to return a component identifier
    return null;
  }

  private MyLinkEntity toWebEntity(final LinkDetail linkDetail) {
    return MyLinkEntity.fromLinkDetail(linkDetail, uri.ofLink(linkDetail));
  }

  private CategoryEntity toWebEntity(final CategoryDetail categoryDetail) {
    return CategoryEntity.fromCategoryDetail(categoryDetail, uri.ofCategory(categoryDetail));
  }
}
