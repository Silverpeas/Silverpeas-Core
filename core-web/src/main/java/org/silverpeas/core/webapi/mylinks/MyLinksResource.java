/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.webapi.mylinks;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.kernel.util.Mutable;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.valueOf;
import static org.silverpeas.kernel.util.StringUtil.isDefined;
import static org.silverpeas.kernel.util.StringUtil.isNotDefined;
import static org.silverpeas.core.webapi.mylinks.MyLinksResourceURIs.MYLINKS_BASE_URI;

/**
 * A REST Web resource representing user favorite links. It is a web service that provides an access
 * to user links referenced by its URL.
 */
@Tag(name = "My links",
    description = "The favorite links of the authenticated user, and the categories in which " +
    "they are gathered.")
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
  @Operation(summary = "Gets all user categories of favorite links.",
      description = "Return only categories of the current user.")
  @ApiResponse(responseCode = "200", description = "All user categories.",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryEntity.class))))
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
  @Operation(summary = "Gets the user category of favorite links.",
      description = "Return only category of the current user.")
  @ApiResponse(responseCode = "200", description = "The user categories.",
      content = @Content(schema = @Schema(implementation = CategoryEntity.class)))
  @GET
  @Path("categories/{catId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CategoryEntity getCategory(@PathParam("catId") String catId) {
    return toWebEntity(manager.getAuthorizedCategory(catId));
  }

  @Operation(summary = "Adds a category of favorite links to the user.")
  @ApiResponse(responseCode = "200", description = "The added category.",
      content = @Content(schema = @Schema(implementation = CategoryEntity.class)))
  @POST
  @Path("categories")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CategoryEntity addCategory(final CategoryEntity newCategory) {
    return toWebEntity(manager.createCategory(newCategory));
  }

  @Operation(summary = "Updates the given category of favorite links of the user.")
  @ApiResponse(responseCode = "200", description = "The updated category.",
      content = @Content(schema = @Schema(implementation = CategoryEntity.class)))
  @PUT
  @Path("categories/{catId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CategoryEntity updateCategory(final @PathParam("catId") String catId,
      final CategoryEntity updatedCategory) {
    verifyConsistency(catId, updatedCategory);
    return toWebEntity(manager.updateCategory(updatedCategory));
  }

  @Operation(summary = "Deletes the given category of favorite links of the user.")
  @ApiResponse(responseCode = "200", description = "The category has been deleted.")
  @DELETE
  @Path("categories/{catId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteCategory(final @PathParam("catId") String catId) {
    manager.deleteCategories(new String[]{catId});
    return Response.ok().build();
  }

  @Operation(summary = "Saves the order in which the categories are presented to the user.")
  @ApiResponse(responseCode = "200", description = "The category once moved to its new position.",
      content = @Content(schema = @Schema(implementation = CategoryEntity.class)))
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
  @Operation(summary = "Gets the user favorite links.")
  @ApiResponse(responseCode = "200", description = "The user links.",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = MyLinkEntity.class))))
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
  @Operation(summary = "Gets the user favorite links.",
      description = "Return only link of the current user.")
  @ApiResponse(responseCode = "200", description = "The user links.",
      content = @Content(schema = @Schema(implementation = MyLinkEntity.class)))
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity getLink(@PathParam("id") String linkId) {
    return toWebEntity(manager.getAuthorizedLink(linkId));
  }

  @Operation(summary = "Adds a favorite link to the user.")
  @ApiResponse(responseCode = "200", description = "The added link.",
      content = @Content(schema = @Schema(implementation = MyLinkEntity.class)))
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity addLink(final MyLinkEntity newLink) {
    return toWebEntity(manager.createLink(newLink));
  }

  @Operation(summary = "Updates the given favorite link of the user.")
  @ApiResponse(responseCode = "200", description = "The updated link.",
      content = @Content(schema = @Schema(implementation = MyLinkEntity.class)))
  @PUT
  @Path("{linkId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MyLinkEntity updateLink(final @PathParam("linkId") String linkId,
      final MyLinkEntity updatedLink) {
    verifyConsistency(linkId, updatedLink);
    return toWebEntity(manager.updateLink(updatedLink));
  }

  @Operation(summary = "Deletes the given favorite link of the user.")
  @ApiResponse(responseCode = "200", description = "The link has been deleted.")
  @DELETE
  @Path("{linkId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteLink(final @PathParam("linkId") String linkId) {
    manager.deleteLinks(new String[]{linkId});
    return Response.ok().build();
  }

  @Operation(summary = "Saves the order in which the links are presented to the user.")
  @ApiResponse(responseCode = "200", description = "The link once moved to its new position.",
      content = @Content(schema = @Schema(implementation = MyLinkEntity.class)))
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
