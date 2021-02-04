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

/**
 * Represents the preparation of a Web Browser Edition.
 * <p>
 *   This object provides the following data:
 *   <ul>
 *     <li>a {@link WbeFile}, the aimed file by the edition</li>
 *     <li>a {@link WbeUser}, the editor</li>
 *   </ul>
 * </p>
 * @author silveryocha
 */
public abstract class WbeEdition {

  private WbeFile file;
  private WbeUser user;

  protected WbeEdition(final WbeFile file, final WbeUser user) {
    this.file = file;
    this.user = user;
  }

  /**
   * Gets the WBE file of the edition.
   * @return a {@link WbeFile} instance.
   */
  public WbeFile getFile() {
    return file;
  }

  /**
   * Gets the WBE user which is editing the file.
   * @return a {@link WbeUser} instance.
   */
  public WbeUser getUser() {
    return user;
  }
}
