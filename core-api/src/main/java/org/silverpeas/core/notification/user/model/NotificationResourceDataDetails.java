/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.notification.user.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * Labels and gender container linked to a {@link NotificationResourceData}.
 * <p>
 * It permits to register and provide labels according to a locale.
 * </p>
 * @author silveryocha
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NotificationResourceDataDetails {

  private Boolean feminineGenderResource;
  private Map<String, Map<String, String>> localizations;

  /**
   * Is the linked {@link NotificationResourceData} a about a feminine gender resource or
   * masculine one ?
   * @return true if feminine gender, false otherwise.
   */
  public boolean isFeminineGenderResource() {
    return feminineGenderResource == null || feminineGenderResource;
  }

  @XmlElement
  public NotificationResourceDataDetails setFeminineGenderResource(
      final boolean feminineGenderResource) {
    this.feminineGenderResource = feminineGenderResource ? null : Boolean.FALSE;
    return this;
  }

  @XmlElement
  Map<String, Map<String, String>> getLocalizations() {
    if (localizations != null) {
      localizations.entrySet().removeIf(l -> {
        l.getValue().entrySet().removeIf(e -> isNotDefined(e.getValue()));
        return l.getValue().isEmpty();
      });
      if (localizations.isEmpty()) {
        localizations = null;
      }
    }
    return localizations;
  }

  @XmlElement
  void setLocalizations(final Map<String, Map<String, String>> localizations) {
    this.localizations = localizations;
  }

  /**
   * Gets a localized data according to the given language and the specified key.
   * @param language a language.
   * @param key a key.
   * @return a label as string.
   */
  public String getLocalized(final String language, final String key) {
    String value = null;
    if (localizations != null) {
      final Map<String, String> localizedData = getLocalizedData(language);
      value = localizedData.get(key);
    }
    return value;
  }

  /**
   * Puts a localized data according to the given language and the specified key.
   * @param language a language.
   * @param key a key.
   * @param value a value associated to the key.
   * @return itself.
   */
  public NotificationResourceDataDetails putLocalized(final String language, final String key,
      final String value) {
    if (localizations == null) {
      localizations = new LinkedHashMap<>();
    }
    final Map<String, String> localizedData = getLocalizedData(language);
    localizedData.put(key, value);
    return this;
  }

  /**
   * Merges the labels from other to itself.
   * @param other the other {@link NotificationResourceDataDetails} instance.
   */
  public void merge(final NotificationResourceDataDetails other) {
    if (other != null && other.localizations != null) {
      other.localizations.forEach((l, t) -> t.forEach((k, v) -> putLocalized(l, k, v)));
    }
  }

  private Map<String, String> getLocalizedData(final String language) {
    return localizations.computeIfAbsent(language, l -> new LinkedHashMap<>());
  }
}
