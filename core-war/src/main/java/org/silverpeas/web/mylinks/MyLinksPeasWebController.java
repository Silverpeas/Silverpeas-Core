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
package org.silverpeas.web.mylinks;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternal;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;
import org.silverpeas.core.webapi.mylinks.CategoryEntity;
import org.silverpeas.core.webapi.mylinks.MyLinksWebManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.util.URLUtil.getURL;
import static org.silverpeas.core.webapi.mylinks.CategoryEntity.fromCategoryDetail;
import static org.silverpeas.core.webapi.mylinks.MyLinkEntity.fromLinkDetail;
import static org.silverpeas.web.mylinks.MyLinksPeasWebController.MYLINKS_COMPONENT_NAME;

@WebComponentController(MYLINKS_COMPONENT_NAME)
public class MyLinksPeasWebController extends
    org.silverpeas.core.web.mvc.webcomponent.WebComponentController<MyLinksPeasWebRequestContext> {

  public static final String MYLINKS_COMPONENT_NAME = "myLinksPeas";

  private static final int SCOPE_USER = 0;
  private static final int SCOPE_COMPONENT = 1;
  private static final int SCOPE_OBJECT = 2;
  private static final String VIEW_LINKS_VIEW = "ViewLinks";
  private static final String VIEW_CATEGORIES_VIEW = "ViewCategories";
  private static final String LINK_ID_PARAM = "linkId";
  private static final String SCOPE_PARAM = "scope";
  private static final String CATEGORY_ID_PARAM = "catId";
  private static final String INSTANCE_ID_PARAM = "InstanceId";
  private static final String OBJECT_ID_PARAM = "ObjectId";
  private static final String URL_RETURN_PARAM = "UrlReturn";

  private int scope = SCOPE_USER;
  private String url = null;
  private String instanceId = null;
  private String objectId = null;

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public MyLinksPeasWebController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.mylinks.multilang.myLinksBundle",
        "org.silverpeas.mylinks.settings.myLinksIcons");
  }

  @Override
  protected void onInstantiation(final MyLinksPeasWebRequestContext context) {
    setComponentRootName(getComponentName());
  }

  @Override
  public String getComponentName() {
    return MYLINKS_COMPONENT_NAME;
  }

  @Override
  protected void beforeRequestProcessing(final MyLinksPeasWebRequestContext context) {
    super.beforeRequestProcessing(context);
    applyScopeFromRequestIfAny(context);
  }

  /**
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternal(VIEW_LINKS_VIEW)
  public void home(MyLinksPeasWebRequestContext context) {
    setScope(SCOPE_USER, context);
    context.getMessager().addInfo(getString("myLinks.draganddrop.info"));
  }

  /**
   * Views the categories.
   * @param context the context of the incoming request.
   */
  @GET
  @Path(VIEW_CATEGORIES_VIEW)
  @RedirectToInternalJsp("viewCategories.jsp")
  public void viewCategories(MyLinksPeasWebRequestContext context) {
    final Collection<CategoryDetail> categories =
        verifyCategoryScope(() -> getManager().getAllCategoriesOfCurrentUser());
    context.getRequest().setAttribute("Categories", categories);
    context.getMessager().addInfo(getString("myLinks.category.draganddrop.info"));
  }

  /**
   * Gets the form HTML block of a category for a creation.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("categories/new/form")
  @RedirectToInternalJsp("categoryFormBlock.jsp")
  public void newCategory(MyLinksPeasWebRequestContext context) {
    final CategoryEntity category = verifyCategoryScope(
        () -> CategoryEntity.fromCategoryDetail(new CategoryDetail(), null));
    context.getRequest().setAttribute("creationMode", true);
    context.getRequest().setAttribute("Category", category);
  }

  /**
   * Gets the form HTML block of a category for a modification.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("categories/{" + CATEGORY_ID_PARAM + "}/form")
  @RedirectToInternalJsp("categoryFormBlock.jsp")
  public void editCategory(MyLinksPeasWebRequestContext context) {
    final String catId = context.getPathVariables().get(CATEGORY_ID_PARAM);
    final CategoryEntity category = verifyCategoryScope(() -> {
      final CategoryDetail details = getManager().getAuthorizedCategory(catId);
      return CategoryEntity.fromCategoryDetail(details, context.uri().ofCategory(details));
    });
    context.getRequest().setAttribute("Category", category);
  }

  /**
   * Removes categories from the persistence.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("RemoveCategories")
  @RedirectToInternal(VIEW_CATEGORIES_VIEW)
  public void deleteCategories(MyLinksPeasWebRequestContext context) {
    verifyCategoryScope(() -> {
      final String[] catIds = context.getRequest().getParameterValues("categoryIds");
      if (catIds != null) {
        getManager().deleteCategories(catIds);
      }
      return null;
    });
  }

  /**
   * Views the links.
   * @param context the context of the incoming request.
   */
  @GET
  @Path(VIEW_LINKS_VIEW)
  @RedirectToInternalJsp("viewLinks.jsp")
  public void viewLinks(MyLinksPeasWebRequestContext context) {
    verifyAccessAndExecute(() -> {final Collection<LinkDetail> links;
      final List<CategoryDetail> categories;
      switch (scope) {
        case SCOPE_COMPONENT:
          links = getManager().getAllLinksOfInstance(instanceId);
          categories = new ArrayList<>();
          break;
        case SCOPE_OBJECT:
          links = getManager().getAllLinksOfResourceOnInstance(objectId, instanceId);
          categories = new ArrayList<>();
          break;
        default:
          links = getManager().getAllLinksOfCurrentUser();
          categories = getManager().getAllCategoriesOfCurrentUser();
      }
      final CategoryDetail withoutCategory = new CategoryDetail();
      withoutCategory.setId(-1);
      withoutCategory.setName(getString("myLinks.withoutCategory"));
      categories.add(0, withoutCategory);
      final Map<CategoryDetail, List<LinkDetail>> realLinksByCategory = links.stream()
          .collect(groupingBy(linkDetail -> linkDetail.getCategory() != null ?
              linkDetail.getCategory() :
              withoutCategory, mapping(l -> l, toList())));
      final Map<CategoryDetail, List<LinkDetail>> linksByCategory = categories.stream()
          .collect(toMap(c -> c, c -> realLinksByCategory.getOrDefault(c, emptyList()), (l, o) -> l, LinkedHashMap::new));
      context.getRequest().setAttribute("LinksByCategory", linksByCategory);
      context.getRequest().setAttribute(URL_RETURN_PARAM, url);
      context.getRequest().setAttribute(INSTANCE_ID_PARAM, instanceId);
      context.getRequest().setAttribute(OBJECT_ID_PARAM, objectId);
    });
  }

  /**
   * Gets the form HTML block of a link for a creation.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("links/new/form")
  @RedirectToInternalJsp("linkFormBlock.jsp")
  public void newLink(MyLinksPeasWebRequestContext context) {
    final LinkDetail link = new LinkDetail();
    link.setInstanceId(instanceId);
    context.getRequest().setAttribute("creationMode", true);
    context.getRequest().setAttribute("Link", fromLinkDetail(link, null));
    setAllUserCategoriesAttribute(context);
  }

  /**
   * Gets the form HTML block of a link for an update of category of several links.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("links/updateCategoryOnly/form")
  @RedirectToInternal("links/new/form")
  public void updateCategoryOnly(MyLinksPeasWebRequestContext context) {
    context.getRequest().setAttribute("updateCategoryOnly", true);
  }

  /**
   * Gets the form HTML block of a link for a modification.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("links/{" + LINK_ID_PARAM + "}/form")
  @RedirectToInternalJsp("linkFormBlock.jsp")
  public void editLink(MyLinksPeasWebRequestContext context) {
    final String linkId = context.getPathVariables().get(LINK_ID_PARAM);
    final LinkDetail link = getManager().getAuthorizedLink(linkId);
    context.getRequest().setAttribute("Link", fromLinkDetail(link, context.uri().ofLink(link)));
    setAllUserCategoriesAttribute(context);
  }

  /**
   * Removes links from the persistence.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("RemoveLinks")
  @RedirectToInternal(VIEW_LINKS_VIEW)
  public void deleteLinks(MyLinksPeasWebRequestContext context) {
    verifyAccessAndExecute(() -> {
      final String[] linkIds = context.getRequest().getParameterValues("linkIds");
      if (linkIds != null) {
        getManager().deleteLinks(linkIds);
      }
    });
  }

  /**
   * Views the links of a component instance.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("ComponentLinks")
  @RedirectToInternal(VIEW_LINKS_VIEW)
  public void viewComponentInstanceLinks(MyLinksPeasWebRequestContext context) {
    setScope(SCOPE_COMPONENT, context);
  }

  /**
   * Views the links of an object hosted by a component instance.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("ObjectLinks")
  @RedirectToInternal(VIEW_LINKS_VIEW)
  public void viewResourceLinks(MyLinksPeasWebRequestContext context) {
    setScope(SCOPE_OBJECT, context);
  }

  private void applyScopeFromRequestIfAny(final MyLinksPeasWebRequestContext context) {
    ofNullable(context.getRequest().getParameterAsInteger(SCOPE_PARAM))
        .ifPresent(s -> setScope(s, context));
  }

  private void setAllUserCategoriesAttribute(final MyLinksPeasWebRequestContext context) {
    if (scope == SCOPE_USER) {
      context.getRequest()
          .setAttribute("AllUserCategories", getManager().getAllCategoriesOfCurrentUser()
              .stream()
              .map(c -> fromCategoryDetail(c, context.uri().ofCategory(c)))
              .collect(Collectors.toList()));
    } else {
      context.getRequest().setAttribute("AllUserCategories", emptyList());
    }
  }

  private void setScope(final int newScope, MyLinksPeasWebRequestContext context) {
    scope = newScope;
    url = null;
    instanceId = null;
    objectId = null;
    if (scope == SCOPE_COMPONENT || scope == SCOPE_OBJECT) {
      instanceId = context.getRequest().getParameter(INSTANCE_ID_PARAM);
      url = context.getRequest().getParameter(URL_RETURN_PARAM);
      if (isDefined(instanceId) && !isDefined(url)) {
        url = getApplicationURL() + getURL(null, instanceId) + "Main";
      }
      if (scope == SCOPE_OBJECT) {
        objectId = context.getRequest().getParameter(OBJECT_ID_PARAM);
      }
    }
  }

  private void verifyAccessAndExecute(final Runnable process) {
    if (scope == SCOPE_COMPONENT || scope == SCOPE_OBJECT) {
      final Optional<SilverpeasRole> silverpeasRole =
          getOrganisationController().getComponentInstance(
              instanceId)
          .map(i -> i.getSilverpeasRolesFor(User.getCurrentRequester()))
          .map(SilverpeasRole::getHighestFrom)
          .filter(SilverpeasRole.ADMIN::equals);
      if (silverpeasRole.isEmpty()) {
        throw new WebApplicationException(UNAUTHORIZED);
      }
    }
    process.run();
  }

  private <T> T verifyCategoryScope(final Supplier<T> getter) {
    final T result;
    if (scope == SCOPE_USER) {
      result = getter.get();
    } else {
      throw new WebApplicationException("Categories are handled only for user favorite links", BAD_REQUEST);
    }
    return result;
  }

  private MyLinksWebManager getManager() {
    return MyLinksWebManager.get();
  }
}
