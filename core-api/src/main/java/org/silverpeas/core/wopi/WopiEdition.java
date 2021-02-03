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

package org.silverpeas.core.wopi;

/**
 * Represents the preparation of a wopi edition.
 * <p>
 *   This object provides the following data:
 *   <ul>
 *     <li>a {@link WopiFile}, the aimed file by the edition</li>
 *     <li>a {@link WopiUser}, the editor</li>
 *     <li>a client base URL that permits to access the online editor that takes in charge de
 *     {@link WopiFile#mimeType()}</li>
 *   </ul>
 * </p>
 * @author silveryocha
 */
public class WopiEdition {

  private WopiFile file;
  private WopiUser user;
  private String clientBaseUrl;

  protected WopiEdition(final WopiFile file, final WopiUser user, final String clientBaseUrl) {
    this.file = file;
    this.user = user;
    this.clientBaseUrl = clientBaseUrl;
  }

  /**
   * Gets the WOPI file of the edition.
   * @return a {@link WopiFile} instance.
   */
  public WopiFile getFile() {
    return file;
  }

  /**
   * Gets the WOPI user which is editing the file.
   * @return a {@link WopiUser} instance.
   */
  public WopiUser getUser() {
    return user;
  }

  /**
   * The client base URL is the base URL which permits to load the WOPI editor which takes in
   * charge the {@link WopiFile} into the WEB browser.
   * @return an URL as string.
   */
  public String getClientBaseUrl() {
    return clientBaseUrl;
  }
}
