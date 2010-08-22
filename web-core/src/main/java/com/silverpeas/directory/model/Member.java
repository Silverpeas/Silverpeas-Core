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

import com.silverpeas.socialNetwork.invitation.InvitationService;
import com.silverpeas.socialNetwork.relationShip.RelationShipService;
import com.stratelia.silverpeas.peasCore.SessionInfo;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import java.io.File;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author ${user}
 */
public class Member {

  private UserDetail userDetail = null;
  private boolean connected = false;
  private String duration;
  private String profilPhoto;

  public void setUserDetail(UserDetail userDetail) {

    this.userDetail = userDetail;
  }

  public final void refreshStatus() {
    //return the url of profil Photo
    String avatar = getUserDetail().getLogin() + ".jpg";
    File image = new File(FileRepositoryManager.getAbsolutePath("avatar")
        + File.separatorChar + avatar);
    if (image.exists()) {
      profilPhoto = "/display/avatar/" + avatar;
    } else {
      profilPhoto = "/directory/jsp/icons/Photo_profil.jpg";

    }
    Collection<SessionInfo> sessionInfos = SessionManager.getInstance().getConnectedUsersList();
    for (SessionInfo varSi : sessionInfos) {
      if (varSi.m_User.equals(userDetail)) {

        this.duration = DateUtil.formatDuration(new java.util.Date().getTime() - varSi.m_DateBegin);
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

  public String geteMail() {
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

  /**
   * return  the url of  Profil Photo
   *
   * @return String
   * 
   */
  public String getProfilPhoto() {

    return profilPhoto;
  }

  /**
   * Transform the milliseconds duration in hours, minutes and seconds.
   * @param duration in milliseconds
   * @return "xxHyymnzzs" where xx=hours, yy=minutes, zz=seconds
   * 
   */
  private String formatDuration(long duration) {
    long millisPerHour = (long) 60 * (long) 60 * (long) 1000;
    long millisPerMinute = (long) 60 * (long) 1000;
    long secondDuration = ((duration % millisPerHour) % millisPerMinute) / 1000;
    long hourDuration = duration / millisPerHour;
    long minuteDuration = (duration % millisPerHour) / millisPerMinute;


    String dHour = Long.toString(hourDuration) + " h ";
    String dMinute = Long.toString(minuteDuration) + " m ";
    String dSecond = Long.toString(secondDuration) + " s ";


    if (hourDuration < 1) {
      dHour = "";
    }
    if (minuteDuration < 1) {

      dMinute = "";
    } else {
      dSecond = "";
    }


    return dHour + dMinute + dSecond;
  }
  
  public boolean isRelationOrInvitation(String myId){
    RelationShipService relation = new RelationShipService();
    InvitationService invitation = new InvitationService();
    try {
      return relation.isInRelationShip(Integer.parseInt(myId), Integer.parseInt(getId())) ||( invitation.getInvitation(Integer.parseInt(myId), Integer.parseInt(getId()))!=null);
    } catch (NumberFormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return false;
    
  }
}
