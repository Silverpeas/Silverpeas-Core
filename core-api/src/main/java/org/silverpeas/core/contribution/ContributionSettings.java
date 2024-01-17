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

package org.silverpeas.core.contribution;

import org.silverpeas.core.contribution.tracking.TrackedApplications;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import java.util.stream.Stream;

/**
 * The different settings that are applied on the contributions, whatever their concrete type.
 * These settings allow to customize some feature behaviours on the contributions.
 * @author silveryocha
 */
public class ContributionSettings {

  private static final SettingBundle SETTINGS = ResourceLocator.getSettingBundle(
      "org.silverpeas.contribution.settings.contribution");

  /**
   * Hidden constructor.
   */
  private ContributionSettings() {
  }

  /**
   * Indicates if the behavior of asking to user if its modification is a minor one is enabled or
   * not.
   * @return true if enabled, false otherwise.
   */
  public static Stream<String> streamComponentNamesWithMinorModificationBehaviorEnabled() {
    return Stream.of(
        SETTINGS.getList("contribution.modification.behavior.minor.componentNames", new String[]{}))
        .filter(StringUtil::isDefined);
  }

  /**
   * Gets the applications in Silverpeas for which the contributions have to be tracked for change.
   * Only the contributions that support the tracking for modifications will be taken in charge (for
   * example the publications).
   * @return a {@link TrackedApplications} instance. If {@link TrackedApplications#NONE}, no
   * tracking for contributions modifications are operated (the tracking for change is disabled).
   * If {@link TrackedApplications#ALL}, the tracking for contributions modifications (those
   * that support such a tracking) is enabled for all the applications in Silverpeas.
   */
  public static TrackedApplications getApplicationsTrackedForModifications() {
    return TrackedApplications.track(
        streamComponentNamesWithMinorModificationBehaviorEnabled().toArray(String[]::new));
  }
}
