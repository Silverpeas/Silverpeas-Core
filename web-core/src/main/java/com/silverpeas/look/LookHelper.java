/**
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

package com.silverpeas.look;

import com.silverpeas.personalization.UserMenuDisplay;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.ation.model.PublicationDetail;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.util.ResourceLocator;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * A LookHelper is an utility class aiming to facilitate the access of the current Web
 * navigation of the user in the Silverpeas Web interface as well of some settings on the Web
 * displaying. As such a LookHelper is instantiated once per user session.
 */
interface LookHelper {

  /**
   * Creates a new look helper instance and put it into the current user session.
   * @param session a user HTTP session.
   * @return a new look helper.
   */
  static LookHelper newLookHelper(HttpSession session) {
    LookHelper lookHelper = new LookSilverpeasV5Helper(session);
    session.setAttribute(LookHelper.SESSION_ATT, lookHelper);
    return lookHelper;
  }

  /**
   * Gets the look helper actually set in the user session.
   * @param session an HTTP user session.
   * @return a look helper or null if no one was registered into the current user session.
   */
  static LookHelper getLookHelper(HttpSession session) {
    return (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);
  }

  String getURLOfLastVisitedCollaborativeSpace();

  String SESSION_ATT = "Silverpeas_LookHelper";

  String getSpaceId();

  void setSpaceId(String spaceId);

  String getSubSpaceId();

  void setSubSpaceId(String subSpaceId);

  String getComponentId();

  void setComponentId(String componentId);

  boolean isMenuPersonalisationEnabled();

  /**
   * @param spaceId can be id of a space or a subspace
   */
  void setSpaceIdAndSubSpaceId(String spaceId);

  void setComponentIdAndSpaceIds(String spaceId,
      String subSpaceId, String componentId);

  @Deprecated
  void init(MainSessionController mainSessionController,
      ResourceLocator resources);

  String getUserFullName(String userId);

  String getUserFullName();

  String getUserId();

  String getLanguage();

  boolean isAnonymousUser();

  boolean displayPDCInNavigationFrame();

  boolean displayPDCFrame();

  boolean displayContextualPDC();

  boolean displaySpaceIcons();

  String getSpaceId(String componentId);

  String getWallPaper(String spaceId);

  int getNBConnectedUsers();

  boolean isAnonymousAccess();

  boolean getSettings(String key);

  boolean getSettings(String key, boolean defaultValue);

  String getSettings(String key, String defaultValue);

  int getSettings(String key, int defaultValue);

  String getString(String key);

  boolean isBackOfficeVisible();

  List<TopItem> getTopItems();

  List<String> getTopSpaceIds();

  String getMainFrame();

  void setMainFrame(String mainFrame);

  String getSpaceWallPaper();

  String getComponentURL(String componentId);

  String getDate();

  String getDefaultSpaceId();

  List<PublicationDetail> getLatestPublications(String spaceId, int nbPublis);

  List<PublicationDetail> getValidPublications(NodePK nodePK);

  UserMenuDisplay getDisplayUserMenu();

  void setDisplayUserMenu(UserMenuDisplay userMenuDisplayMode);

  boolean isEnableUFSContainsState();

  boolean isDisplayPDCInHomePage();

  String getSpaceWithCSSToApply();

  DefaultSpaceHomePage getSpaceHomePage(String spaceId);

  TickerSettings getTickerSettings();

  UserDetail getUserDetail();

}