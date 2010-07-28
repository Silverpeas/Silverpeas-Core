/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.silverpeas.socialNetwork.user.model;

import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import java.io.File;

/**
 *
 * @author Bensalem Nabil
 */
public class SNContactUser {
  private String userId;
  private String firstName;
  private String lastName;
  private String profilPhoto;

  public SNContactUser(String userId) {

    UserDetail userDetail=new OrganizationController().getUserDetail(userId);
    this.userId=userId;
    this.lastName=userDetail.getLastName();
    this.firstName=userDetail.getFirstName();

    //return the url of profil Photo
    String avatar = userDetail.getLogin() + ".jpg";
    File image = new File(FileRepositoryManager.getAbsolutePath("avatar")
        + File.separatorChar + avatar);
    if (image.exists()) {
      profilPhoto = "/display/avatar/" + avatar;
    } else {
      profilPhoto = "/directory/jsp/icons/Photo_profil.jpg";

    }
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getProfilPhoto() {
    return profilPhoto;
  }

  public String getUserId() {
    return userId;
  }

}
