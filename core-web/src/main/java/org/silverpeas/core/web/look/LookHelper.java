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

package org.silverpeas.core.web.look;

import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.util.SettingBundle;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * A LookHelper is an utility class aiming to facilitate the access of the current Web
 * navigation of the user in the Silverpeas Web interface as well of some settings on the Web
 * displaying. As such a LookHelper is instantiated once per user session.
 */
public abstract class LookHelper {

  public static final String SESSION_ATT = "Silverpeas_LookHelper";

  /**
   * Creates a new look helper instance and put it into the current user session.
   * @param session a user HTTP session.
   * @return a new look helper.
   */
  public static LookHelper newLookHelper(HttpSession session) {
    LookHelper lookHelper = new LookSilverpeasV5Helper(session);
    session.setAttribute(LookHelper.SESSION_ATT, lookHelper);
    return lookHelper;
  }

  /**
   * Gets the look helper actually set in the user session.
   * @param session an HTTP user session.
   * @return a look helper or null if no one was registered into the current user session.
   */
  public static LookHelper getLookHelper(HttpSession session) {
    return (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);
  }

  public abstract String getURLOfLastVisitedCollaborativeSpace();

  public abstract String getSpaceId();

  public abstract void setSpaceId(String spaceId);

  public abstract String getSubSpaceId();

  public abstract void setSubSpaceId(String subSpaceId);

  public abstract String getComponentId();

  public abstract void setComponentId(String componentId);

  public abstract boolean isMenuPersonalisationEnabled();

  /**
   * @param spaceId can be id of a space or a subspace
   */
  public abstract void setSpaceIdAndSubSpaceId(String spaceId);

  public abstract void setComponentIdAndSpaceIds(String spaceId,
      String subSpaceId, String componentId);

  @Deprecated
  public abstract void init(MainSessionController mainSessionController,
      SettingBundle resources);

  public abstract String getUserFullName(String userId);

  public abstract String getUserFullName();

  public abstract String getUserId();

  public abstract String getLanguage();

  public abstract boolean isAnonymousUser();

  public abstract boolean displayPDCInNavigationFrame();

  public abstract boolean displayPDCFrame();

  public abstract boolean displayContextualPDC();

  public abstract boolean displaySpaceIcons();

  public abstract String getSpaceId(String componentId);

  public abstract String getWallPaper(String spaceId);

  public abstract int getNBConnectedUsers();

  public abstract boolean isAnonymousAccess();

  public abstract boolean getSettings(String key);

  public abstract boolean getSettings(String key, boolean defaultValue);

  public abstract String getSettings(String key, String defaultValue);

  public abstract int getSettings(String key, int defaultValue);

  public abstract String getString(String key);

  public abstract boolean isBackOfficeVisible();

  public abstract List<TopItem> getTopItems();

  public abstract List<String> getTopSpaceIds();

  public abstract String getMainFrame();

  public abstract void setMainFrame(String mainFrame);

  public abstract String getSpaceWallPaper();

  public abstract String getComponentURL(String componentId);

  public abstract String getDate();

  public abstract String getDefaultSpaceId();

  public abstract List<PublicationDetail> getLatestPublications(String spaceId, int nbPublis);

  public abstract List<PublicationDetail> getValidPublications(NodePK nodePK);

  public abstract UserMenuDisplay getDisplayUserMenu();

  public abstract void setDisplayUserMenu(UserMenuDisplay userMenuDisplayMode);

  public abstract boolean isEnableUFSContainsState();

  public abstract boolean isDisplayPDCInHomePage();

  public abstract String getSpaceWithCSSToApply();

  public abstract DefaultSpaceHomePage getSpaceHomePage(String spaceId);

  public abstract TickerSettings getTickerSettings();

  public abstract UserDetail getUserDetail();

}