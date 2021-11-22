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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.webapi.selection;

import org.silverpeas.core.BasicIdentifier;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.selection.SelectionBasket;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * It represents the {@link org.silverpeas.core.selection.SelectionBasket} exposed in the web and
 * accessible through a REST-based web API. The selection basket behaves like a FILO list: the
 * first resource put in the basket is at the tail of the basket whereas the last one put is at the
 * head of the basket and hence it will be the first one to get or to pop. The resources put in the
 * basket aren't as such, but they are wrapped within an object that maps them to the context for
 * which they have been put in the basket by the user: a {@link SelectionBasketEntry} instance.
 * @author mmoquillon
 */
@WebService
@Authenticated
@Path(SelectionBasketResource.BASE_URI_PATH)
public class SelectionBasketResource extends RESTWebService {

  static final String BASE_URI_PATH = "selection";

  /**
   * Gets all the content of the selection basket, reverse ordered by the time each item has been
   * put, the last resource put at index 0.
   * @return a reverse ordered list of basket entries. The last one put being the first one in the
   * list. An entry is a mapping between the Silverpeas resource that has been selected (and hence
   * put into the basket) and its selection context.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<SelectionBasketEntry> getAll() {
    SelectionBasket basket = SelectionBasket.get();
    return listBasketEntries(basket);
  }

  /**
   * Puts the specified resource into the basket. It will be placed atop of others items in the
   * basket.
   * @param entry a basket entry. An entry is a mapping between the Silverpeas resource that has
   * been selected and its selection context.
   * @return the new state of the basket, that is to say its updated content.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putInBasket(final SelectionBasketEntry entry) {
    checkEntryIsValid(entry);
    SelectionBasket basket = SelectionBasket.get();
    basket.put(entry.getItem(), entry.getContext());
    return Response.created(identifiedBy(entry.getItem().getIdentifier().asString()))
        .entity(listBasketEntries(basket))
        .build();
  }

  /**
   * Pops the item at the head of the basket. Popping is a way to delete the first item in the
   * basket (the last one put into the basket).
   * @return the new state of the basket, that is to say its new content without the deleted item.
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public List<SelectionBasketEntry> pop() {
    SelectionBasket basket = SelectionBasket.get();
    var resource = basket.pop();
    if (resource.isEmpty()) {
      throw new NotFoundException("No resource to pop in the basket");
    }
    return listBasketEntries(basket);
  }

  /**
   * Deletes in the basket the item with the specified unique identifier. If no
   * such Silverpeas resource is found in the basket, then a {@link NotFoundException} is thrown.
   * @param itemId the unique identifier of a Silverpeas resource placed in the basket.
   * @return the new state of the basket, that is to say its new content without the deleted item.
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/item/{id}")
  public List<SelectionBasketEntry> deleteFromBasket(@PathParam("id") final String itemId) {
    SelectionBasket basket = SelectionBasket.get();
    var resource = basket.getSelectedResources()
        .filter(e -> e.getResource().getIdentifier().asString().equals(itemId))
        .findFirst()
        .orElseThrow(
            () -> new NotFoundException(String.format("No such resource %s in the basket", itemId)))
        .getResource();
    basket.remove(resource);
    return listBasketEntries(basket);
  }

  /**
   * Deletes in the basket the item placed at the specified position. If there is no
   * resource at the given position in the basket, then a {@link NotFoundException} is thrown.
   * @param index the position index of the resource in the basket to remove. The index starts at 0
   * for the head position.
   * @return the new state of the basket, that is to say its new content without the deleted item.
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/index/{index}")
  public List<SelectionBasketEntry> removeFromBasket(@PathParam("index") final int index) {
    SelectionBasket basket = SelectionBasket.get();
    var entry = basket.removeAt(index);
    if (entry.isEmpty()) {
      throw new NotFoundException(
          String.format("No such resource at index %d in the basket", index));
    }
    return listBasketEntries(basket);
  }

  @Override
  public String getComponentId() {
    return null;
  }

  @Override
  protected String getResourceBasePath() {
    return BASE_URI_PATH;
  }

  private URI identifiedBy(final String id) {
    return super.identifiedBy(getUri().getAbsoluteWebResourcePathBuilder(), "/item/", id);
  }

  private List<SelectionBasketEntry> listBasketEntries(final SelectionBasket basket) {
    return basket.getSelectedResources()
        .map(SelectionBasketEntry::from)
        .map(e -> e.withURI(identifiedBy(e.getItem().getIdentifier().asString())))
        .collect(Collectors.toList());
  }

  private void checkEntryIsValid(final SelectionBasketEntry entry) {
    if (entry == null || entry.getItem() == null || entry.getItem().getIdentifier() == null) {
      throw new BadRequestException();
    }
    BasketItem item = entry.getItem();
    if (!(item.getIdentifier() instanceof ContributionIdentifier) &&
        !(item.getIdentifier() instanceof BasicIdentifier)) {
      throw new BadRequestException();
    }
  }
}
