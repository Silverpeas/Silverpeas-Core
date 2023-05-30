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
package org.silverpeas.core.admin.component.model;

/**
 * This localized warning allows to provide data of {@link Warning} instances on the Silverpeas's
 * user interfaces without having to manage the user language, which is carried by the localized
 * warning itself..
 * <p>
 *   As a {@link Warning} instance is part of a {@link Parameter}, {@link LocalizedWarning} is
 *   part of {@link LocalizedParameter}.
 * </p>
 * @author silveryocha
 */
public class LocalizedWarning extends ComponentLocalization {

  private final String bundleKeyPrefix;
  private final Warning warning;

  LocalizedWarning(LocalizedParameter bundle, Warning warning) {
    super(bundle);
    this.bundleKeyPrefix = bundle.getBundleKeyPrefix();
    this.warning = warning;
  }

  /**
   * @see Warning#isAlways()
   */
  public boolean isAlways() {
    return warning.isAlways();
  }

  /**
   * Gets the localized value of a {@link Warning} according to the language specified on
   * {@link LocalizedWarning} instantiation.
   * @return a localized value as string.
   * @see Warning#getMessages()
   */
  public String getValue() {
    return getLocalized(bundleKeyPrefix + ".warning", warning.getMessages());
  }
}