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
package org.silverpeas.core.web.util;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.authorization.ComponentAccessControl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * The applications a WYSIWYG editor can take images or files from, the image banks and the file
 * banks. They are given to the editor with its configuration, so that the editor doesn't request
 * them at its initialization.
 */
public final class WysiwygBanks {

  private static final String IMAGE_BANK_PARAMETER = "viewInWysiwyg";
  private static final String FILE_BANK_PARAMETER = "publicFiles";
  private static final String ENABLED = "yes";

  /**
   * An application acting as a bank of images or of files.
   * @param name the name of the application (kmelia, gallery, ...).
   * @param id the local identifier of the application instance.
   * @param label the label of the application instance.
   */
  public record Bank(String name, String id, String label) {
  }

  private WysiwygBanks() {
  }

  /**
   * Gets the image banks accessible to the specified user.
   * @param user a user, or null if there is no user.
   * @param language the language of the labels.
   * @return the image banks accessible to the user, an empty list if none or without any user.
   */
  public static List<Bank> imageBanksFor(final User user, final String language) {
    return banksFor(IMAGE_BANK_PARAMETER, user, language);
  }

  /**
   * Gets the file banks accessible to the specified user.
   * @param user a user, or null if there is no user.
   * @param language the language of the labels.
   * @return the file banks accessible to the user, an empty list if none or without any user.
   */
  public static List<Bank> fileBanksFor(final User user, final String language) {
    return banksFor(FILE_BANK_PARAMETER, user, language);
  }

  private static List<Bank> banksFor(final String parameter, final User user,
      final String language) {
    if (user == null) {
      return List.of();
    }
    final Map<String, ComponentInstLight> banks = OrganizationController.get()
        .getComponentsWithParameterValue(parameter, ENABLED)
        .stream()
        .collect(toMap(ComponentInstLight::getId, Function.identity(), (a, b) -> a,
            LinkedHashMap::new));
    return ComponentAccessControl.get()
        .filterAuthorizedByUser(banks.keySet(), user.getId())
        .map(banks::get)
        .map(app -> new Bank(app.getName(), String.valueOf(app.getLocalId()),
            app.getLabel(language)))
        .toList();
  }
}
