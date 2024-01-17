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

package org.silverpeas.web.jobdomain.servlets;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.SilverpeasList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.function.Function;

import static org.silverpeas.core.admin.AdminSettings.getDeletionOfRemovedUsersDayDelay;
import static org.silverpeas.core.util.DateUtil.toLocalDate;

/**
 * UI item for a {@link User} instance which {@link User#isRemovedState()} returns true.
 */
public class RemovedUserUIEntity extends UserUIEntity {

  private RemovedUserUIEntity(final User data, final Set<String> selectedIds) {
    super(data, selectedIds);
  }

  /**
   * Converts the given data list into a {@link SilverpeasList} of item wrapping the {@link
   * User}.
   * @param values the list of {@link User}.
   * @return the {@link SilverpeasList} of {@link RemovedUserUIEntity}.
   */
  public static <U extends User> SilverpeasList<RemovedUserUIEntity> convertRemovedUserList(
      final SilverpeasList<U> values, final Set<String> selectedIds) {
    final Function<User, RemovedUserUIEntity> converter = c -> new RemovedUserUIEntity(c,
        selectedIds);
    return values.stream().map(converter).collect(SilverpeasList.collector(values));
  }

  /**
   * Gets the day date of automatic deletion.
   * @return a {@link LocalDate}.
   */
  public LocalDate getAutomaticDeletionDayDate() {
    return toLocalDate(getData().getStateSaveDate()).plusDays(getDeletionOfRemovedUsersDayDelay());
  }

  /**
   * Gets the number of days before automatic deletion.
   * @return an integer.
   */
  public int getAutomaticDeletionDayDelay() {
    final long dayDelay = LocalDate.now().until(getAutomaticDeletionDayDate(), ChronoUnit.DAYS);
    return (int) (dayDelay > 0 ? dayDelay : 0);
  }
}