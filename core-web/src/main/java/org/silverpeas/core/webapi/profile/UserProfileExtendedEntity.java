/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.profile;

import org.silverpeas.core.admin.user.model.UserFull;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The complete profile of a user that is web entity in the WEB. It is a web entity representing the
 * profile of a user that can be serialized into a given media type (JSON, XML). It is a decorator
 * that decorates a UserFull object with additional properties concerning its exposition in the WEB.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UserProfileExtendedEntity extends UserProfileEntity {

  private static final long serialVersionUID = 3852818013944136146L;

  /**
   * Decorates the specified user full details with the required WEB exposition features.
   *
   * @param user the user full details to decorate.
   * @return a web entity representing the profile of a user.
   */
  public static UserProfileExtendedEntity fromUser(final UserFull user) {
    return new UserProfileExtendedEntity(user);
  }
  private UserFull user = null;

  private UserProfileExtendedEntity(final UserFull user) {
    super(user);
    this.user = user;
  }

  @Override
  public UserProfileExtendedEntity withAsUri(final URI userUri) {
    super.withAsUri(userUri);
    return this;
  }

  @XmlElement
  public Map<String, String> getMoreLabelData() {
    final String[] propertyNames = user.getPropertiesNames();
    final Map<String, String> labels = user.getSpecificLabels(getLanguage());
    final Map<String, String> result = new LinkedHashMap<>();
    for (final String propertyName : propertyNames) {
      result.put(propertyName, labels.get(propertyName));
    }
    return result;
  }

  @XmlElement
  public Map<String, String> getMoreData() {
    final String[] propertyNames = user.getPropertiesNames();
    final Map<String, String> result = new LinkedHashMap<>();
    for (final String propertyName : propertyNames) {
      result.put(propertyName, user.getValue(propertyName));
    }
    return result;
  }

  public UserFull toUserFull() {
    return user;
  }

  protected UserProfileExtendedEntity() {
    user = new UserFull();
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof UserProfileExtendedEntity) {
      return user.equals(((UserProfileExtendedEntity) other).user);
    } else {
      return user.equals(other);
    }
  }

  @Override
  public int hashCode() {
    return user.hashCode();
  }
}
