/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.web.jobdomain;

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.web.http.UnescapeHtml;

import javax.ws.rs.FormParam;

/**
 * This class is a user data container which the data are retrieved from an HTTP request that
 * deals with user data creation or modification.<br/>
 * To get a loaded container, use {@link RequestParameterDecoder#decode(HttpRequest, Class)}.
 * @author Yohann Chastagnier
 */
public class UserRequestData {

  /**
   * The id of the user (in case of modify action).
   */
  @FormParam("Iduser")
  private String id;

  /**
   * The login of the user (in case of cration action).
   */
  @FormParam("userLogin")
  @UnescapeHtml
  private String login;

  /**
   * The last name of the user.
   */
  @FormParam("userLastName")
  @UnescapeHtml
  private String lastName;

  /**
   * The first name of the user.
   */
  @FormParam("userFirstName")
  @UnescapeHtml
  private String firstName;

  /**
   * The email fo the user.
   */
  @FormParam("userEMail")
  @UnescapeHtml
  private String email;

  /**
   * Indicates if a mail must be sent in order to indicate to the user its password.
   */
  @FormParam("sendEmail")
  private boolean sendEmail;

  /**
   * The access level of the user.
   */
  @FormParam("userAccessLevel")
  private UserAccessLevel accessLevel;

  /**
   * Indicates if the password is valid.
   */
  @FormParam("userPasswordValid")
  private boolean passwordValid;

  /**
   * The password of the user.
   */
  @FormParam("userPassword")
  @UnescapeHtml
  private String password;

  /**
   * The group identifier the user is associated to.
   */
  @FormParam("GroupId")
  private String groupId;

  /**
   * The preferred language of the user.
   */
  @FormParam("SelectedUserLanguage")
  private String language;

  /**
   * The indicator that enables the limitation on the maximum number of recipient the user can
   * notify manually.
   */
  @FormParam("userManualNotifReceiverLimitEnabled")
  private boolean userManualNotifReceiverLimitEnabled;

  /**
   * The value of the limitation on the maximum number of recipient the user can notify manually.
   */
  @FormParam("userManualNotifReceiverLimitValue")
  private Integer userManualNotifReceiverLimitValue;

  /**
   * Applies the data on the specified new instance of a user.<br/>
   * Following data are not set:
   * <ul>
   * <li>user id</li>
   * <li>the user password validity</li>
   * <li>the user password</li>
   * </ul>
   * @param newUser the instance of the new user.
   */
  public void applyDataOnNewUser(UserDetail newUser) {
    newUser.setLogin(getLogin());
    setCommonDataToUser(newUser);
  }

  /**
   * Applies the data on the specified instance of an existing user.<br/>
   * Following data are not set:
   * <ul>
   * <li>user id</li>
   * <li>user login</li>
   * </ul>
   * @param existingUser the instance of the existing user.
   */
  public void applyDataOnExistingUser(UserFull existingUser) {
    if (existingUser.isPasswordAvailable()) {
      existingUser.setPasswordValid(isPasswordValid());
      existingUser.setPassword(getPassword());
    }
    setCommonDataToUser(existingUser);
  }

  /**
   * Centralization.
   */
  private void setCommonDataToUser(UserDetail user) {
    user.setLastName(getLastName());
    user.setFirstName(getFirstName());
    user.seteMail(getEmail());
    user.setAccessLevel(getAccessLevel());
    user.setUserManualNotificationUserReceiverLimit(getUserManualNotifReceiverLimitValue());
  }

  /*
  GETTER & SETTERS
   */

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(final String login) {
    this.login = login;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public boolean isSendEmail() {
    return sendEmail;
  }

  public void setSendEmail(final boolean sendEmail) {
    this.sendEmail = sendEmail;
  }

  public UserAccessLevel getAccessLevel() {
    return accessLevel;
  }

  public void setAccessLevel(final UserAccessLevel accessLevel) {
    this.accessLevel = accessLevel;
  }

  public boolean isPasswordValid() {
    return passwordValid;
  }

  public void setPasswordValid(final boolean passwordValid) {
    this.passwordValid = passwordValid;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(final String groupId) {
    this.groupId = groupId;
  }

  public String getLanguage() {
    return DisplayI18NHelper.verifyLanguage(language);
  }

  public void setLanguage(final String language) {
    this.language = language;
  }

  protected boolean getUserManualNotifReceiverLimitEnabled() {
    return userManualNotifReceiverLimitEnabled;
  }

  public void setUserManualNotifReceiverLimitEnabled(
      final boolean userManualNotifReceiverLimitEnabled) {
    this.userManualNotifReceiverLimitEnabled = userManualNotifReceiverLimitEnabled;
  }

  public Integer getUserManualNotifReceiverLimitValue() {
    Integer limit = 0;
    if (getUserManualNotifReceiverLimitEnabled()) {
      limit = userManualNotifReceiverLimitValue;
    }
    return limit;
  }

  @Override
  public String toString() {
    return (id != null ? ("userId=" + id + " ") : "") + "userLogin=" + login + " userLastName=" +
        lastName + " userFirstName=" + firstName + " userEMail=" + email + " userAccessLevel=" +
        accessLevel;
  }
}
