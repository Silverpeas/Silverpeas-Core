/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.directory.model;

import java.util.Collection;

import com.silverpeas.socialNetwork.invitation.InvitationService;
import com.silverpeas.socialNetwork.relationShip.RelationShipService;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.SessionInfo;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;

/**
 * A user as a member of something (silverpeas?).
 */
public class Member {

  private UserDetail userDetail = null;
  private boolean connected = false;
  private String duration;

  private void refreshStatus() {
    Collection<SessionInfo> sessionInfos = SessionManager.getInstance().getConnectedUsersList();
    for (SessionInfo varSi : sessionInfos) {
      if (varSi.getUserDetail().equals(userDetail)) {

        this.duration = DateUtil.formatDuration(new java.util.Date().getTime() - varSi.getOpeningTimestamp());
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

  public UserDetail getUserDetail() {
    return userDetail;
  }

  public Member(UserDetail ud) {
    userDetail = ud;
    refreshStatus();
  }

  public String getAccessLevel() {
    return getUserDetail().getAccessLevel();
  }

  public String getMail() {
    return getUserDetail().geteMail();
  }

  public String getFirstName() {
    return getUserDetail().getFirstName();
  }

  public String getId() {
    return getUserDetail().getId();
  }

  public String getLastName() {
    return getUserDetail().getLastName();
  }

  public String getDuration() {
    return duration;
  }

  public boolean isRelationOrInvitation(String myId) {
    RelationShipService relation = new RelationShipService();
    InvitationService invitation = new InvitationService();
    try {
      return relation.isInRelationShip(Integer.parseInt(myId), Integer.parseInt(getId())) ||
          (invitation.getInvitation(Integer.parseInt(myId), Integer.parseInt(getId())) != null);
    } catch (Exception e) {
      SilverTrace.warn("directory", getClass().getSimpleName(), "root.EX_NO_MESSAGE", e);
    }
    return false;
  }

  public String getStatus() {
    if (!StringUtil.isDefined(userDetail.getStatus())) {
      return null;
    }
    return userDetail.getStatus();
  }

}