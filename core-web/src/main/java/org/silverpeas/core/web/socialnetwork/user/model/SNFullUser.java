/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.web.socialnetwork.user.model;

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;

import java.io.File;
import java.util.Collection;

/**
 * A social network user.
 * @author Bensalem Nabil
 */
public class SNFullUser {

  private UserFull userFull;
  private String profilPhoto;
  private String duration;
  private boolean connected;
  private String phone;

  public SNFullUser(String userId) {
    userFull = OrganizationControllerProvider.getOrganisationController().getUserFull(userId);
    this.phone = userFull.getValue("phone");

    // return the url of profil Photo
    String avatar = userFull.getLogin() + ".jpg";
    File image = new File(FileRepositoryManager.getAbsolutePath("avatar")
        + File.separatorChar + avatar);
    if (image.exists()) {
      profilPhoto = "/display/avatar/" + avatar;
    } else {
      profilPhoto = "/directory/jsp/icons/Photo_profil.jpg";

    }
    SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
    Collection<SessionInfo> sessionInfos = sessionManagement.getConnectedUsersList();
    for (SessionInfo varSi : sessionInfos) {
      if (varSi.getUserDetail().getId().equals(userId)) {

        this.duration =
            DateUtil.formatDuration(new java.util.Date().getTime() - varSi.getOpeningTimestamp());
        this.connected = true;
        return;
      }
    }
    this.duration = "";
    this.connected = false;
  }

  public boolean isConnected() {
    return connected;
  }

  public String getDuration() {
    return duration;
  }

  public String getProfilPhoto() {
    return profilPhoto;
  }

  public UserFull getUserFull() {
    return userFull;
  }

  public String getPhone() {
    return phone;
  }

}
