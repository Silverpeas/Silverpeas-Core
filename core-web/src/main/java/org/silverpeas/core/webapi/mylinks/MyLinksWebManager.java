/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static org.silverpeas.kernel.bundle.ResourceLocator.getLocalizationBundle;
import static org.silverpeas.kernel.util.StringUtil.isNotDefined;


/**
 * Permits to centralize WEB service processing between {@link MyLinksResource} and the WAR
 * controller.
 * @author silveryocha
 */
@Service
public class MyLinksWebManager {

  @Inject
  private MyLinksService service;

  protected MyLinksWebManager() {
  }

  public static MyLinksWebManager get() {
    return ServiceProvider.getService(MyLinksWebManager.class);
  }

  /**
   * Gets all the categories of the current user.
   * @return List of category.
   */
  public List<CategoryDetail> getAllCategoriesOfCurrentUser() {
    final User currentRequester = User.getCurrentRequester();
    if (currentRequester != null) {
      return service.getAllCategoriesByUser(currentRequester.getId());
    }
    return emptyList();
  }

  /**
   * Gets category details from its id by verifying user modification rights.
   * @param categoryId the identifier of a category.
   * @return a {@link CategoryDetail} instance or null if id is unknown.
   */
  public CategoryDetail getAuthorizedCategory(String categoryId) {
    if (isNotDefined(categoryId)) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final CategoryDetail category = service.getCategory(categoryId);
    if (category == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else if (!category.canBeModifiedBy(User.getCurrentRequester())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return category;
  }

  /**
   * Creates a category into persistence.
   * @param newCategory data of a new category.
   * @return the persisted data.
   */
  public CategoryDetail createCategory(CategoryEntity newCategory) {
    checkMandatoryCategoryData(newCategory);
    final User currentRequester = User.getCurrentRequester();
    CategoryDetail category = newCategory.toCategoryDetail();
    category.setUserId(currentRequester.getId());
    if (!category.canBeModifiedBy(currentRequester)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return service.createCategory(category);
  }

  /**
   * Updates a category into persistence.
   * @param updatedCategory data of a updated category.
   * @return the saved data.
   */
  public CategoryDetail updateCategory(CategoryEntity updatedCategory) {
    getAuthorizedCategory(valueOf(updatedCategory.getCategoryId()));
    checkMandatoryCategoryData(updatedCategory);
    CategoryDetail category = updatedCategory.toCategoryDetail();
    category.setUserId(User.getCurrentRequester().getId());
    return service.updateCategory(category);
  }

  /**
   * Deletes categories into persistence.
   * @param categoryIds category identifiers.
   */
  public void deleteCategories(String[] categoryIds) {
    if (categoryIds.length > 0) {
      for (String categoryId : categoryIds) {
        getAuthorizedCategory(categoryId);
      }
      service.deleteCategories(categoryIds);
      getMessager().addSuccess(getBundle().getString("myLinks.deleteCategories.messageConfirm"),
          categoryIds.length);
    }
  }

  /**
   * Gets all the links associated to given component instance identifier.
   * @return List of link.
   */
  public List<LinkDetail> getAllLinksOfInstance(final String instanceId) {
    return service.getAllLinksByInstance(instanceId);
  }

  /**
   * Gets all the links associated to given resource identifier hosted into to given component
   * instance identifier.
   * @return List of link.
   */
  public List<LinkDetail> getAllLinksOfResourceOnInstance(final String resourceId,
      final String instanceId) {
    return service.getAllLinksByObject(instanceId, resourceId);
  }

  /**
   * Gets all the links of the current user.
   * @return List of link.
   */
  public List<LinkDetail> getAllLinksOfCurrentUser() {
    final User currentRequester = User.getCurrentRequester();
    if (currentRequester != null) {
      return service.getAllLinksByUser(currentRequester.getId());
    }
    return emptyList();
  }

  /**
   * Gets link details from its id by verifying user modification rights.
   * @param linkId the identifier of a link.
   * @return a {@link LinkDetail} instance or null if id is unknown.
   */
  public LinkDetail getAuthorizedLink(String linkId) {
    if (isNotDefined(linkId)) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final LinkDetail link = service.getLink(linkId);
    if (link == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else if (!link.canBeModifiedBy(User.getCurrentRequester())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return link;
  }

  /**
   * Creates a link into persistence.
   * @param newLink data of a link.
   * @return the persisted data.
   */
  public LinkDetail createLink(MyLinkEntity newLink) {
    checkMandatoryLinkData(newLink);
    LinkDetail linkDetail = newLink.toLinkDetail();
    final User currentRequester = User.getCurrentRequester();
    linkDetail.setUserId(currentRequester.getId());
    if (!linkDetail.canBeModifiedBy(currentRequester)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    linkDetail = service.createLink(linkDetail);
    return linkDetail;
  }

  /**
   * Updates a link into persistence
   * @param updatedLink the updated date of a link.
   * @return the saved data.
   */
  public LinkDetail updateLink(MyLinkEntity updatedLink) {
    getAuthorizedLink(valueOf(updatedLink.getLinkId()));
    checkMandatoryLinkData(updatedLink);
    LinkDetail linkDetail = updatedLink.toLinkDetail();
    linkDetail.setUserId(User.getCurrentRequester().getId());
    linkDetail = service.updateLink(linkDetail);
    return linkDetail;
  }

  /**
   * Deletes links into persistence.
   * @param links list of id about links to delete.
   */
  public void deleteLinks(String[] links) {
    if (links.length > 0) {
      for (String linkId : links) {
        getAuthorizedLink(linkId);
      }
      service.deleteLinks(links);
      getMessager().addSuccess(getBundle().getString("myLinks.deleteLinks.messageConfirm"),
          links.length);
    }
  }

  private void checkMandatoryCategoryData(final CategoryEntity category) {
    if (!StringUtil.isDefined(category.getName())) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  private void checkMandatoryLinkData(final MyLinkEntity myLink) {
    if (!StringUtil.isDefined(myLink.getUrl()) || !StringUtil.isDefined(myLink.getName())) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  /**
   * Gets the common calendar bundle according to the given locale.
   * @return a localized bundle.
   */
  private LocalizationBundle getBundle() {
    User owner = User.getCurrentRequester();
    String userLanguage = owner.getUserPreferences().getLanguage();
    return getLocalizationBundle("org.silverpeas.mylinks.multilang.myLinksBundle", userLanguage);
  }

  private WebMessager getMessager() {
    return WebMessager.getInstance();
  }
}
