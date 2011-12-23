/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.socialnetwork.user.model;

import com.stratelia.silverpeas.peasCore.SessionInfo;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import java.io.File;
import java.util.Collection;

/**
 * A social network user.
 * @author Bensalem Nabil
 */
public class SNFullUser {

  private  UserFull userFull;
  private  String profilPhoto;
  private  String duration;
  private  boolean connected;
  private String phone;

  public SNFullUser(String userId) {
    userFull = new OrganizationController().getUserFull(userId);
    this.phone=userFull.getValue("phone");

    //return the url of profil Photo
    String avatar = userFull.getLogin() + ".jpg";
    File image = new File(FileRepositoryManager.getAbsolutePath("avatar")
        + File.separatorChar + avatar);
    if (image.exists()) {
      profilPhoto = "/display/avatar/" + avatar;
    } else {
      profilPhoto = "/directory/jsp/icons/Photo_profil.jpg";

    }
    Collection<SessionInfo> sessionInfos = SessionManager.getInstance().getConnectedUsersList();
    for (SessionInfo varSi : sessionInfos) {
      if (varSi.getUserDetail().getId().equals(userId)) {

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


