/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.look.delegate;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.look.SilverpeasLook;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.admin.space.UserFavoriteSpaceService;
import org.silverpeas.core.admin.space.UserFavoriteSpaceServiceProvider;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.space.model.UserFavoriteSpaceVO;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.admin.service.OrganizationController;

/**
 * @author Yohann Chastagnier
 */
public class LookWebDelegate {

  private OrganizationController organizationController;

  private final UserDetail user;
  private final UserPreferences userPreference;

  private final LookHelper lookHelper;
  private final GraphicElementFactory gef;

  private List<UserFavoriteSpaceVO> userFavoriteSpaces = null;
  private List<String> userFavoriteSpaceIds = null;
  private UserFavoriteSpaceService userFavoriteSpaceService;

  /**
   * Gets the favorite value for the given space
   * @param space
   * @param forceGettingFavorite forcing the user favorite space search even if the favorite feature
   * is disabled
   * @return
   */
  public String getUserFavorite(final SpaceInstLight space, final boolean forceGettingFavorite) {
    String favorite = "false";
    if (forceGettingFavorite || UserMenuDisplay.DISABLE != getUserMenuDisplay()) {
      if (getUserFavoriteSpaceIds().contains(String.valueOf(space.getLocalId()))) {
        favorite = "true";
      } else if (getUserFavoriteSpaceService().containsFavoriteSubSpace(space,
          getUserFavoriteSpaces(), getUserId())) {
        favorite = "contains";
      }
    }
    return favorite;
  }

  /**
   * Adds a space to user favorites
   * @param space
   */
  public void addToUserFavorites(final SpaceInstLight space) {
    getUserFavoriteSpaceService().addUserFavoriteSpace(new UserFavoriteSpaceVO(getUser(), space));
    clearFavoriteCache();
  }

  /**
   * Removess a space from user favorites
   * @param space
   */
  public void removeFromUserFavorites(final SpaceInstLight space) {
    getUserFavoriteSpaceService().removeUserFavoriteSpace(new UserFavoriteSpaceVO(getUser(),
        space));
    clearFavoriteCache();
  }

  /**
   * Gets the right look.
   * @param space
   * @return the space style according to the space hierarchy
   */
  public String getLook(SpaceInstLight space) {
    String look = space.getLook();
    while (!space.isRoot() && !StringUtil.isDefined(look)) {
      space = getOrganisationController().getSpaceInstLightById(space.getFatherId());
      look = space.getLook();
    }
    if (!StringUtil.isDefined(look)) {
      look = getGraphicalElements().getDefaultLookName();
    }
    return look;
  }

  /**
   * Gets the right URL wallpaper
   * @return
   */
  public String getWallpaper(final SpaceInstLight space) {
    final String wallpaper =
        SilverpeasLook.getSilverpeasLook().getWallpaperOfSpace(String.valueOf(space.getLocalId()));
    return wallpaper == null ? "" : wallpaper;
  }

  /**
   * Gets the right URL CSS
   * @return
   */
  public String getCSS(final SpaceInstLight space) {
    final String css = SilverpeasLook.getSilverpeasLook().getCSSOfSpace(String.valueOf(space.getLocalId()));
    return css == null ? "" : css;
  }

  /**
   * Gets the user display menu behaviour
   * @return
   */
  private UserMenuDisplay getUserMenuDisplay() {
    return getHelper().getDisplayUserMenu();
  }

  /**
   * Gets the favorite space of the user
   * @return
   */
  private List<UserFavoriteSpaceVO> getUserFavoriteSpaces() {
    if (userFavoriteSpaces == null) {
      userFavoriteSpaces =
          getUserFavoriteSpaceService().getListUserFavoriteSpace(getUserId());
    }
    return userFavoriteSpaces;
  }

  /**
   * Gets the favorite space ids of the user
   * @return
   */
  private List<String> getUserFavoriteSpaceIds() {
    if (userFavoriteSpaceIds == null) {
      userFavoriteSpaceIds = new ArrayList<String>();
      for (final UserFavoriteSpaceVO favoriteUserSpace : getUserFavoriteSpaces()) {
        userFavoriteSpaceIds.add("" + favoriteUserSpace.getSpaceId());
      }
    }
    return userFavoriteSpaceIds;
  }

  /**
   * Clearing cache of favorites
   */
  private void clearFavoriteCache() {
    userFavoriteSpaces = null;
    userFavoriteSpaceIds = null;
  }

  /**
   * Easy way to instance the look service provider
   * @param user
   * @param userPreference
   * @param request
   * @return
   */
  public static LookWebDelegate getInstance(final UserDetail user,
      final UserPreferences userPreference, final HttpServletRequest request) {
    return new LookWebDelegate(user, userPreference, request);
  }

  /**
   * Hidden constructor
   * @param user
   * @param userPreference
   * @param request
   */
  private LookWebDelegate(final UserDetail user, final UserPreferences userPreference,
      final HttpServletRequest request) {
    this.user = user;
    this.userPreference = userPreference;
    lookHelper = (LookHelper) request.getSession().getAttribute(LookHelper.SESSION_ATT);
    gef =
        (GraphicElementFactory) request.getSession().getAttribute(
            GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    if (lookHelper != null) {
      initializeUserMenuDisplay(request);
    }
  }

  /**
   * Initializes from a specified request parameter the bookMark management
   * @param request
   */
  private void initializeUserMenuDisplay(final HttpServletRequest request) {
    UserMenuDisplay displayMode = getHelper().getDisplayUserMenu();
    if (getHelper().isMenuPersonalisationEnabled()) {
      if (StringUtil.isDefined(request.getParameter("UserMenuDisplayMode"))) {
        displayMode = UserMenuDisplay.valueOf(request.getParameter("UserMenuDisplayMode"));
      } else if (userPreference.getDisplay().isNotDefault() &&
          !UserMenuDisplay.ALL.equals(displayMode) &&
          !UserMenuDisplay.BOOKMARKS.equals(displayMode)) {
        // Initializes displayMode from user preferences
        displayMode = userPreference.getDisplay();
      }
    }
    getHelper().setDisplayUserMenu(displayMode);
  }

  /**
   * @return
   */
  private UserDetail getUser() {
    return user;
  }

  /**
   * @return
   */
  private String getUserId() {
    return getUser().getId();
  }

  /**
   * Gets the look helper
   * @return
   */
  public LookHelper getHelper() {
    return lookHelper;
  }

  /**
   * Gets the graphical element factory
   * @return
   */
  private GraphicElementFactory getGraphicalElements() {
    return gef;
  }

  private OrganizationController getOrganisationController() {
    if (organizationController == null) {
      organizationController =
          OrganizationControllerProvider.getOrganisationController();
    }
    return organizationController;
  }

  private UserFavoriteSpaceService getUserFavoriteSpaceService() {
    if (userFavoriteSpaceService == null) {
      userFavoriteSpaceService = UserFavoriteSpaceServiceProvider.getUserFavoriteSpaceService();
    }
    return userFavoriteSpaceService;
  }
}
