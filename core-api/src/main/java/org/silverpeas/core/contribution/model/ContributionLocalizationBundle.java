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

package org.silverpeas.core.contribution.model;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.MissingResourceException;
import java.util.Optional;

import static org.apache.commons.lang3.ArrayUtils.toArray;

/**
 * {@link LocalizationBundle} dedicated to {@link Contribution} instances.
 * <p>
 * Useful to gets localized labels, for example, according to the type of a contribution and also
 * according to the component which is handling the contribution.
 * </p
 * <p>It exists two possible bundle repositories:</p>
 * <ul>
 * <li>a main one: <code>org.silverpeas.contribution.multilang.contribution.properties</code></li>
 * <li>an optional component specific one: <code>org.silverpeas.[component name].multilang
 * .contribution.properties</code></li>
 * </ul>
 * Returning the value of:
 * <ul>
 * <li>the component specific bundle if it defines the key</li>
 * <li>the main bundle otherwise</li>
 * </ul>
 * If the key is not defined by any of bundles, then a {@link MissingResourceException} is thrown.
 * @author silveryocha
 */
public class ContributionLocalizationBundle {

  private static final String NO_PROPERTY = "CONTRIBUTION_LOCALIZATION_BUNDLED#NO_PROPERTY";

  private final Contribution contribution;
  private final LocalizationBundle mainBundle;
  private final LocalizationBundle componentBundle;

  /**
   * Hidden constructor.
   */
  private ContributionLocalizationBundle(final Contribution contribution,
      final LocalizationBundle mainBundle, final LocalizationBundle componentBundle) {
    this.contribution = contribution;
    this.mainBundle = mainBundle;
    this.componentBundle = componentBundle;
  }

  /**
   * Gets the {@link ContributionLocalizationBundle} for a {@link Contribution} adapted from a
   * specified language.
   * @param contribution a {@link Contribution} instance.
   * @param language the aimed language for bundles.
   * @return an initialized {@link ContributionLocalizationBundle} instance.
   */
  public static ContributionLocalizationBundle getByInstanceAndLanguage(Contribution contribution,
      String language) {
    LocalizationBundle main = ResourceLocator
        .getLocalizationBundle("org.silverpeas.contribution.multilang.contribution", language);
    final String componentName = SilverpeasComponentInstance
        .getComponentName(contribution.getIdentifier().getComponentInstanceId());
    final Optional<LocalizationBundle> specific = ResourceLocator.getOptionalLocalizationBundle(
        "org.silverpeas." + componentName.toLowerCase() + ".multilang.contribution", language);
    return new ContributionLocalizationBundle(contribution, main, specific.orElse(null));
  }

  /**
   * Gets the title of the contribution, according to its type, for UI messages.
   * @return a formatted string.
   */
  public String getUiMessageTitleByType() {
    return getUiMessageTitleByTypeAndProperty(NO_PROPERTY);
  }

  /**
   * Gets the title of the contribution, according to its type and a property, for UI messages.
   * @return a formatted string.
   */
  public String getUiMessageTitleByTypeAndProperty(String property) {
    return getByTypeAndPropertyOrJustByType(property,
        Pair.of("ui.message.label", toArray(contribution.getTitle())));
  }

  private String getByTypeAndPropertyOrJustByType(String property,
      Pair<String, Object[]> keySuffixAndParameters) {
    final String contributionType = contribution.getIdentifier().getType();
    final String keySuffix = keySuffixAndParameters.getLeft();
    final Object[] parameters = keySuffixAndParameters.getRight();
    final String keyBase = "contribution." + contributionType + ".";
    if (!NO_PROPERTY.equals(property)) {
      // TYPE AND PROPERTY CASE
      try {
        return getStringWithParams(keyBase + property + "." + keySuffix, parameters);
      } catch (MissingResourceException mre) {
        SilverLogger.getLogger(this).silent(mre);
      }
    }
    // JUST TYPE CASE
    return getStringWithParams(keyBase + keySuffix, parameters);
  }

  private String getStringWithParams(final String key, Object... params) {
    if (componentBundle != null) {
      try {
        return componentBundle.getStringWithParams(key, params);
      } catch (MissingResourceException mre) {
        SilverLogger.getLogger(this).silent(mre).debug(mre.getMessage());
      }
    }
    return mainBundle.getStringWithParams(key, params);
  }
}
