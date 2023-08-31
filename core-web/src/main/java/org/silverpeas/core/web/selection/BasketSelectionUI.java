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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.selection;

import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.img;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.SpaceWithSubSpacesAndComponents;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.PUBLISHER;
import static org.silverpeas.core.util.ResourceLocator.getGeneralLocalizationBundle;
import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;

/**
 * This class permits to manager some UI behaviors against the current connected user and about
 * the basket selection feature.
 * @author silveryocha
 */
public class BasketSelectionUI {

  private BasketSelectionUI() {
    // hidden constructor
  }

  /**
   * Gets the HTML snippet that permits to a user to click on to put an element into basket
   * selection.
   * <p>
   * If the snippet is useless for current user, then EMPTY string is returned.
   * </p>
   * @param callback the javascript callback.
   * @param userLanguage the language of current user.
   * @return the HTML snippet if necessary, empty otherwise.
   */
  public static String getPutIntoBasketSelectionHtmlSnippet(final String callback,
      final String userLanguage) {
    if (displayPutIntoBasketSelectionShortcut()) {
      final img basketImg = new img(getApplicationURL() + "/util/icons/add-basket.png");
      final a basketLink = new a();
      basketLink.setTitle(getGeneralLocalizationBundle(userLanguage).getString("GML.putInBasket"));
      basketLink.setClass("add-to-basket-selection");
      basketLink.setHref("javascript:void(0)");
      basketLink.setOnClick(callback);
      basketLink.addElement(basketImg);
      return basketLink.toString();
    }
    return EMPTY;
  }

  /**
   * Allows to handle the display of the shortcut to put quickly elements into basket selection.
   * <p>
   * This condition is computed one time only per session, at first method access.
   * </p>
   * @return true if it can be displayed, false otherwise.
   */
  public static boolean displayPutIntoBasketSelectionShortcut() {
    final SimpleCache cache = CacheAccessorProvider.getSessionCacheAccessor().getCache();
    final String key = "displayPutIntoBasketSelectionShortcut@" + User.getCurrentUser().getId();
    return cache.computeIfAbsent(key, Boolean.class,
        BasketSelectionUI::computeDisplayPutIntoBasketSelectionShortcut);
  }

  private static boolean computeDisplayPutIntoBasketSelectionShortcut() {
    final OrganizationController controller = OrganizationController.get();
    final String userId = User.getCurrentUser().getId();
    final SpaceWithSubSpacesAndComponents fullTreeview;
    try {
      fullTreeview = controller.getFullTreeviewOnComponentName(userId, "infoLetter");
    } catch (AdminException e) {
      SilverLogger.getLogger(BasketSelectionUI.class).warn(e);
      return false;
    }
    final List<SilverpeasComponentInstance> newslettersInstances =
        fullTreeview.componentInstanceSelector()
        .fromAllSpaces()
        .select();
    return controller.getUserProfilesByComponentId(userId, newslettersInstances.stream()
        .map(SilverpeasComponentInstance::getId)
        .collect(Collectors.toList()))
        .entrySet()
        .stream()
        .map(Map.Entry::getValue)
        .flatMap(Collection::stream)
        .anyMatch(p -> Optional.ofNullable(SilverpeasRole.fromString(p))
            .filter(r -> r.isGreaterThanOrEquals(PUBLISHER))
            .isPresent());
  }
}
