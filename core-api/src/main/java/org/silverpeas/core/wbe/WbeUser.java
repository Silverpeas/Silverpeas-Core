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

package org.silverpeas.core.wbe;

import org.silverpeas.core.admin.user.model.User;

import java.time.OffsetDateTime;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Representation of a WbeUser. It associates finally a Silverpeas's user with a Web Browser Edition access token.
 * <p>
 * An access token is created on instance creation.
 * </p>
 * @author silveryocha
 */
public abstract class WbeUser {
  private final String spSessionId;
  private final User user;
  private String accessToken;
  private OffsetDateTime lastEditionDate;

  protected WbeUser(final String spSessionId, final User user) {
    this.spSessionId = spSessionId;
    this.user = user;
    renewAccessToken();
  }

  /**
   * Gets the WBE user identifier.
   * @return an identifier as string.
   */
  public abstract String getId();

  /**
   * Gets the date of last edition.
   * @return an {@link OffsetDateTime} instance.
   */
  public OffsetDateTime getLastEditionDate() {
    return this.lastEditionDate;
  }

  /**
   * Gets identifier of the Silverpeas's session the WBE user is linked to.
   * @return a Silverpeas's session identifier.
   */
  public String getSilverpeasSessionId() {
    return spSessionId;
  }

  /**
   * Gets the Silverpeas's user.
   * @return a {@link User} instance.
   */
  public User asSilverpeas() {
    return user;
  }

  /**
   * Gets the WBE access token linked to the Silverpeas's user.
   * @return a string representing a WBE access token.
   */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * Updates the last edition date with the current date and time.
   */
  public void setLastEditionDateAtNow() {
    this.lastEditionDate = OffsetDateTime.now();
  }

  public User getUser() {
    return user;
  }

  /**
   * Renews access token.
   */
  private void renewAccessToken() {
    accessToken = UUID.randomUUID().toString();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", WbeUser.class.getSimpleName() + "[", "]")
        .add("spSessionId=" + getSilverpeasSessionId()).add("user=" + getId())
        .add("accessToken='" + getAccessToken() + "'")
        .add("lastEditionDate='" + getLastEditionDate() + "'").toString();
  }
}
