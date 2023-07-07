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
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.StringUtil;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;

import static java.util.Optional.of;
import static java.util.function.Predicate.not;
import static org.silverpeas.core.ui.DisplayI18NHelper.getDefaultLanguage;
import static org.silverpeas.core.ui.DisplayI18NHelper.getLanguages;
import static org.silverpeas.core.util.ResourceLocator.getLocalizationBundle;

/**
 * This is the main implementation of the mechanism in charge of the localized data providing.
 * <p>
 *   Each implementation MUST give the component context (by its technical name) and the locale
 *   in which localized data have to be retrieved.
 * </p>
 * <p>
 *   At instantiation, the bundle property file dedicated to a component is loaded if any. All
 *   component bundles MUST be situated into $SILVERPEAS_HOME/properties root location. For a
 *   component the bundle property path MUST be templated like following:<br/>
 *   <code>xmlcomponents/[case sensitive component technical name]_[locale].properties</code><br/>
 *   About kmelia component for example:<br/>
 *   {@code xmlcomponents/kmelia_en.properties}
 * </p>
 * <p>
 *   When a localized data is get, it is first looked up from bundle property file, and if
 *   not found, it is then looked up from XML component descriptor file.
 * </p>
 * <p>
 *   If no localized data is found, neither from bundle property file, neither from XML component
 *   descriptor, then the localized data into default language is retrieved. If no localized data
 *   is retrieved at the end a {@link MissingResourceException} exception is thrown.
 * </p>
 * <p>
 *   The potential labels the bundle resource handles:
 *   <ul>
 *     <li>{@code label}: the label of the component</li>
 *     <li>{@code description}: the description of the component</li>
 *     <li>{@code suite}: the suite of the component</li>
 *     <li>{@code profile.[case sensitive user role, 'admin' for example].label}: the label of profile handled by the component</li>
 *     <li>{@code profile.[case sensitive user role, 'admin' for example].help}: the help about profile handled by the component</li>
 *     <li>{@code parameter.[case sensitive parameter name].label}: the label of a component parameter not included into a parameter group</li>
 *     <li>{@code parameter.[case sensitive parameter name].help}: the help about a component parameter not included into a parameter group</li>
 *     <li>{@code parameter.[case sensitive parameter name].warning}: the warning about a component parameter not included into a parameter group</li>
 *     <li>{@code parameter.[case sensitive parameter name].option.[case sensitive option name].name}: the name of an option about a component parameter not included into a parameter group</li>
 *   </ul>
 *   When a parameter is included into a group of parameters, the following key prefix MUST be used:<br/>
 *   {@code parameterGroup.[case sensitive name of group parameter].}<br/>
 *   For example:
 *   {@code parameterGroup.folders.parameter.delegatedTopicManagement.label} where {@code folders}
 *   is the name of the parameter group and {@code delegatedTopicManagement} is the parameter
 *   name.
 *   <p>Only one group of parameters into an XML component descriptor could have no name. In that
 *   case, "noname" is used as group name.</p>
 * </p>
 * <p>
 *   When all localized data are specified into a bundle property file, then if a {@link Warning}
 *   MUST be handled on a {@link Parameter}, don't forget to write an empty warning TAG {@code
 *   <warning />} into the XML description of the parameter. If not declared into XML descriptor
 *   the localized data specified into bundle resource are not taken into account and no
 *   {@link Warning} is then handled for the {@link Parameter}.
 * </p>
 * @author silveryocha
 */
abstract class ComponentLocalization {

  private final SilverpeasComponent component;
  private final String lang;
  private final LocalizationBundle bundle;


  public ComponentLocalization(final ComponentLocalization bundle) {
    this.component = bundle.component;
    this.lang = bundle.lang;
    this.bundle = bundle.bundle;
  }

  public ComponentLocalization(final SilverpeasComponent component, String lang) {
    this.component = component;
    this.lang = lang;
    this.bundle = getLocalizationBundle("xmlcomponents." + getComponentName(), getLanguage());
  }

  public String getComponentName() {
    return component.getName();
  }

  public String getLanguage() {
    return lang;
  }

  /**
   * Gets the localized data first into bundle property files if any, then into map of translations.
   * @param bundleKey the bundle key.
   * @param messages the map of translations extracted from XML component descriptors.
   * @return the localized data as string.
   * @throws MissingResourceException if no translation can be found from the different resource
   * locations.
   */
  protected String getLocalized(final String bundleKey, final Map<String, String> messages) {
    return of(bundle)
        .filter(LocalizationBundle::exists)
        .map(b -> {
          try {
            return b.getString(bundleKey);
          } catch (MissingResourceException ignore) {
            return null;
          }
        })
        .filter(StringUtil::isDefined)
        .orElseGet(() -> {
          if (messages.containsKey(lang)) {
            return messages.get(lang);
          }
          if (messages.containsKey(getDefaultLanguage())) {
            return messages.get(getDefaultLanguage());
          }
          return getLanguages().stream()
              .filter(not(lang::equals).and(not(getDefaultLanguage()::equals)))
              .map(messages::get)
              .filter(Objects::nonNull)
              .findFirst()
              .orElseThrow(() -> new MissingResourceException(String.format(
                  "Can't find localization into %s with key %s, or into %s XML descriptor",
                  bundle.getBaseBundleName() + "_" + getLanguage() + ".properties", bundleKey,
                  getComponentName()), this.getClass().getName(), bundleKey));
        });
  }
}
