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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.tracking;

import org.silverpeas.core.admin.user.model.User;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * An action performed on a contribution that is tracked.
 * @author mmoquillon
 */
@Embeddable
public class TrackedAction implements Serializable {

  @Column(name = "action_type", nullable = false, length = 40)
  @Enumerated(EnumType.STRING)
  private TrackedActionType type;
  @Column(name = "action_date", nullable = false)
  private Instant dateTime;
  @Column(name = "action_by", nullable = false, length = 50)
  private String userId;

  protected TrackedAction() {
    // for JPA
  }

  TrackedAction(final TrackedActionType type, final Instant dateTime, final User user) {
    this.type = type;
    this.dateTime = dateTime;
    this.userId = user.getId();
  }

  /**
   * Gets the type of the action performed on the contribution.
   * @return a {@link TrackedActionType} instance.
   */
  public TrackedActionType getType() {
    return type;
  }

  /**
   * Gets the date time at which the action has been performed.
   * @return a date time in UTC.
   */
  public OffsetDateTime getDateTime() {
    return OffsetDateTime.ofInstant(dateTime, ZoneOffset.UTC);
  }

  /**
   * Gets the user having performed this action.
   * @return a {@link User} instance.
   */
  public User getUser() {
    return User.getById(userId);
  }
}
