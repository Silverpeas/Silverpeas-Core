/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.mylinks.service;

import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.CategoryDetailComparator;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.model.LinkDetailComparator;

import java.util.List;

public interface MyLinksService {

  /**
   * @see #getAllLinksByUser(String)
   * @deprecated used {@link #getAllLinksByUser(String)} instead.
   */
  @Deprecated(forRemoval = true)
  List<LinkDetail> getAllLinks(String userId);

  /**
   * Gets all the categories associated to the user represented by the given id.<br>
   * The result list is sorted by {@link CategoryDetailComparator}.
   * @param userId a user identifier.
   * @return a sorted list of category, empty if no category found.
   */
  List<CategoryDetail> getAllCategoriesByUser(String userId);

  /**
   * Creates a new category from the data given by the {@link CategoryDetail} parameter.
   * @param category the data to register.
   * @return a new instance representing the data saved into database.
   */
  CategoryDetail createCategory(CategoryDetail category);

  /**
   * Gets a {@link CategoryDetail} from its identifier.
   * @param categoryId a category identifier.
   * @return a {@link CategoryDetail} instance, or null if no data.
   */
  CategoryDetail getCategory(String categoryId);

  /**
   * Deletes the categories referenced by the given identifiers.
   * @param categoryIds the category identifiers.
   */
  void deleteCategories(String[] categoryIds);

  /**
   * Updates a category with the given {@link CategoryDetail} data.
   * @param category the data to update.
   * @return the updated data.
   */
  CategoryDetail updateCategory(CategoryDetail category);

  /**
   * Gets all the links associated to the user represented by the given id.<br>
   * The result list is sorted by {@link LinkDetailComparator}.
   * @param userId a user identifier.
   * @return a sorted list of links, empty if no link found.
   */
  List<LinkDetail> getAllLinksByUser(String userId);

  /**
   * Gets all the links associated to the component instance represented by the given id.<br>
   * The result list is sorted by {@link LinkDetailComparator}.
   * @param instanceId a component instance identifier.
   * @return a sorted list of links, empty if no link found.
   */
  List<LinkDetail> getAllLinksByInstance(String instanceId);

  /**
   * Gets all the links associated to the resource represented by the given objectId and hosted into
   * component instance represented by instanceId parameter.<br>
   * The result list is sorted by {@link LinkDetailComparator}.
   * @param instanceId a component instance identifier.
   * @param objectId an identifier of an object.
   * @return a sorted list of links, empty if no link found.
   */
  List<LinkDetail> getAllLinksByObject(String instanceId, String objectId);

  /**
   * Creates a new link from the data given by the {@link LinkDetail} parameter.
   * @param link the data to register.
   * @return a new instance representing the data saved into database.
   */
  LinkDetail createLink(LinkDetail link);

  /**
   * Gets a {@link LinkDetail} from its identifier.
   * @param linkId a link identifier.
   * @return a {@link LinkDetail} instance, or null if no data.
   */
  LinkDetail getLink(String linkId);

  /**
   * Deletes the links referenced by the given identifiers.
   * @param links the link identifiers.
   */
  void deleteLinks(String[] links);

  /**
   * Updates a link with the given {@link LinkDetail} data.
   * @param link the data to update.
   * @return the updated data.
   */
  LinkDetail updateLink(LinkDetail link);

  /**
   * Deletes all the data associated to a user.
   * <p>
   *   Data associated to a user are those with column userid filled with the given identifier
   *   and not linked to links which instanceid or objectid is filled.
   * </p>
   * @param userId a user identifier.
   */
  void deleteUserData(String userId);
}
