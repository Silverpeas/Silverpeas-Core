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
package org.silverpeas.core.admin.component.model;

import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;

import static java.util.Optional.of;
import static java.util.function.Predicate.not;
import static org.silverpeas.core.ui.DisplayI18NHelper.getDefaultLanguage;
import static org.silverpeas.core.ui.DisplayI18NHelper.getLanguages;
import static org.silverpeas.kernel.bundle.ResourceLocator.getLocalizationBundle;

/**
 * This is the main implementation of the mechanism in charge of the localized data for the
 * Silverpeas component definitions (component descriptors), both multi-instantiable components,
 * personal components, and workflows.
 * <p>
 * Each implementation MUST give the component context (by its technical name) and the locale in
 * which localized data have to be retrieved.
 * </p>
 * <p>
 * At instantiation, the bundle file dedicated to a component is loaded if any. All
 * component bundles MUST be located into the {@code $SILVERPEAS_HOME/properties/xmlcomponents} root
 * directory. The location in this root directory of the different kinds of Silverpeas components
 * have to satisfy the following rule:
 * </p>
 * <ul>
 *  <li>For typical multi-instantiable components: directly at the root of the root directory;</li>
 *  <li>For specifically personal components: in the {@code personals} subfolder;</li>
 *  <li>For workflows components: in the {@code workflows} subfolder.</li>
 * </ul>
 * <p>
 * For a given component the bundle name MUST be templated like following:
 * </p>
 * <pre>
 *   {@code [case-sensitive component technical name]_[locale].properties}
 * </pre>
 * <p>
 *   For example, the bundle of a Kmelia component localized for English-speaking people must be:
 * </p>
 * <pre>
 *   {@code kmelia_en.properties}
 * </pre>
 * <p>
 * When a localized data is gotten, it is first looked up in the bundle file, and if not
 * found (either the bundle doesn't exist or the data is missing in the bundle), it is then
 * looked up directly from the XML component descriptor file.
 * </p>
 * <p>
 * If a localized data for a given locale isn't found, neither in the bundle file nor in the XML
 * component descriptor, then it is looked for the default locale. If eventually no such data is
 * found, a {@link MissingResourceException} exception is thrown.
 * </p>
 * <p>
 * The possible localized data that can be defined into a bundle resource are:
 * </p>
 *   <ul>
 *     <li>{@code label}: the label of the component</li>
 *     <li>{@code description}: the description of the component</li>
 *     <li>{@code suite}: the suite of the component</li>
 *     <li>{@code profile.[case-sensitive user role, 'admin' for example].label}: the label of
 *     profile handled by the component</li>
 *     <li>{@code profile.[case-sensitive user role, 'admin' for example].help}: the help about
 *     profile handled by the component</li>
 *     <li>{@code parameter.[case-sensitive parameter name].label}: the label of a component
 *     parameter not included into a parameter group</li>
 *     <li>{@code parameter.[case-sensitive parameter name].help}: the help about a component
 *     parameter not included into a parameter group</li>
 *     <li>{@code parameter.[case-sensitive parameter name].warning}: the warning about a
 *     component parameter not included into a parameter group</li>
 *     <li>{@code parameter.[case-sensitive parameter name].option.[case sensitive option name]
 *     .name}:
 *     the name of an option about a component parameter not included into a parameter group</li>
 *   </ul>
 *   <p>
 *   When a parameter is included into a group of parameters, the following key prefix is
 *   used:
 *   </p>
 *   <pre>
 *   {@code parameterGroup.[case-sensitive name of group parameter]}
 *   </pre>
 *   <p>
 *   For example:
 *   </p>
 *   <pre>
 *   {@code parameterGroup.folders.parameter.delegatedTopicManagement.label}
 *   </pre>
 *   <p>
 *   where {@code folders} is the name of the parameter group and {@code delegatedTopicManagement}
 *   is the parameter name.
 *   </p>
 *   <p>Only one group of parameters into an XML component descriptor could have no name. In such a
 *   case, {@code noname} is used as group name.</p>
 * </p>
 * <p>
 *   When all the localized data are specified in the bundle, then, in the case a {@link Warning}
 *   has to be handled on a {@link Parameter}, don't forget to write an empty warning tag {@code
 *   <warning />} within the XML definition of the parameter in the XML descriptor. If it isn't
 *   declared in the XML
 *   descriptor, the localized data in the bundle won't be taken into account and no
 *   {@link Warning} will be then handled for the given {@link Parameter}.
 * </p>
 *
 * @author silveryocha
 */
abstract class ComponentLocalization {

  private final SilverpeasComponent component;
  private final String lang;
  private final LocalizationBundle bundle;


  protected ComponentLocalization(final ComponentLocalization bundle) {
    this.component = bundle.component;
    this.lang = bundle.lang;
    this.bundle = bundle.bundle;
  }

  protected ComponentLocalization(final SilverpeasComponent component, String lang) {
    this.component = component;
    this.lang = lang;
    this.bundle = getLocalizationBundle(getBundleName(component), getLanguage());
  }

  public String getComponentName() {
    return component.getName();
  }

  public String getLanguage() {
    return lang;
  }

  /**
   * Gets the localized data first into bundle property files if any, then into map of
   * translations.
   *
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

  private String getBundleName(SilverpeasComponent component) {
    final String baseName = "xmlcomponents";
    String bundleName;
    if (component.isPersonal()) {
      bundleName = baseName + ".personals." + component.getName();
    } else if (component.isWorkflow()) {
      bundleName = baseName + ".workflows." + component.getName();
    } else {
      bundleName = baseName + "." + component.getName();
    }
    return bundleName;
  }
}
