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

package org.silverpeas.core.webapi.wbe;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeUser;

import java.util.StringJoiner;

/**
 * Handles the edition context of a file.
 * @author silveryocha
 */
public class WbeFileEditionContext {

  private final WbeUser user;

  private final WbeFileWrapper file;

  protected WbeFileEditionContext(final WbeUser user, final WbeFileWrapper file) {
    this.user = user;
    this.file = file;
  }

  /**
   * Gets the initiator of the edition
   * @return a {@link User} representing the initiator of the edition.
   */
  public WbeUser getUser() {
    return user;
  }

  /**
   * Gets the WBE file the context is linked to.
   * @return a {@link WbeFile} instance.
   */
  public WbeFile getFile() {
    return file;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", WbeFileEditionContext.class.getSimpleName() + "[", "]")
        .add("user=" + user).add("file=" + file).toString();
  }
}
