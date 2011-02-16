/*
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.comment.web;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.personalization.control.ejb.PersonalizationBm;
import com.stratelia.webactiv.personalization.control.ejb.PersonalizationBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.io.Serializable;
import java.rmi.RemoteException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import static com.silverpeas.util.StringUtil.*;

/**
 * The entity representing the author of a comment. It is a user that has written a comment and
 * that is exposed in the web as an entity (a web entity). As such, it publishes only some of its
 * attributes.
 */
@XmlRootElement
public class CommentAuthorEntity implements Serializable {

  private static final long serialVersionUID = 1L;
  @XmlElement(defaultValue = "")
  private String fullName;
  @XmlElement(required = true)
  private String id;
  @XmlElement(defaultValue = "")
  private String avatar;
  @XmlElement(defaultValue = "")
  private String language = "";

  /**
   * Creates an new comment author entity from the specified user.
   * @param user the user to entitify.
   * @return the comment writer.
   */
  public static CommentAuthorEntity fromUser(final UserDetail user) {
    return new CommentAuthorEntity(user);
  }

  /**
   * Gets the relative path of the user avatar.
   * @return the relative path of the URI refering the user avatar.
   */
  public String getAvatar() {
    return avatar;
  }

  /**
   * Gets the unique identifier of the author.
   * @return the user identifier.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the full name of the author (both the first name and the last name).
   * @return the user full name.
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * Gets the prefered language of the author.
   * @return the language code of the author according to the ISO 639-1 standard
   * (for example fr for french).
   */
  public String getLanguage() {
    return this.language;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CommentAuthorEntity other = (CommentAuthorEntity) obj;
    if (isDefined(id) && isDefined(other.getId())) {
      return id.equals(other.getId());
    } else {
      return fullName.equals(other.getFullName()) && avatar.equals(other.getAvatar());
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    if (isDefined(id)) {
      hash = 59 * hash + this.id.hashCode();
    } else {
      hash = 59 * hash + (this.fullName != null ? this.fullName.hashCode() : 0);
      hash = 59 * hash + (this.avatar != null ? this.avatar.hashCode() : 0);
    }
    return hash;
  }

  /**
   * Gets a user detail from this entity.
   * @return a UserDetail instance.
   */
  public UserDetail toUser() {
    UserDetail user = new UserDetail();
    user.setId(id);
    if (isDefined(fullName)) {
      int separatorBetweenFirstAndLastName = fullName.indexOf(" ");
      user.setFirstName(fullName.substring(0,
          separatorBetweenFirstAndLastName));
      user.setLastName(fullName.substring(separatorBetweenFirstAndLastName + 1));
    }
    return user;
  }

  private CommentAuthorEntity(final UserDetail userDetail) {
    this.fullName = userDetail.getDisplayedName();
    this.id = userDetail.getId();
    this.avatar = userDetail.getAvatar();
    PersonalizationBm prefs = getUserPreferences();
    if (prefs != null) {
      try {
        this.language = prefs.getFavoriteLanguage();
      } catch (RemoteException ex) {
        SilverTrace.warn("comment", getClass().getSimpleName(), "root.NO_EX_MESSAGE", ex);
      }
    } else {
      language = "";
    }
  }

  /**
   * Creates an empty comment author.
   */
  protected CommentAuthorEntity() {
  }

  /**
   * Gets the preference of this author.
   * @return the preferences of the user or null if its preferences cannot be retrieved.
   */
  @XmlTransient
  public final PersonalizationBm getUserPreferences() {
    PersonalizationBm persoBm = null;
    try {
      PersonalizationBmHome personalizationBmHome = (PersonalizationBmHome) EJBUtilitaire.
          getEJBObjectRef(JNDINames.PERSONALIZATIONBM_EJBHOME,
          PersonalizationBmHome.class);
      persoBm = personalizationBmHome.create();
      persoBm.setActor(this.id);
    } catch (Exception e) {
      persoBm = null;
    }
    return persoBm;
  }
}
