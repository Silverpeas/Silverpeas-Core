/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.util.DateUtil;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

/**
 * The interface for all the SilverContent (filebox+, ..)
 * @deprecated use instead {@link org.silverpeas.core.contribution.model.Contribution} and
 * {@link org.silverpeas.core.contribution.model.ContributionContent} interfaces.
 */
@Deprecated
public interface SilverContentInterface extends SilverpeasContent {
  String getName();

  String getName(String language);

  String getDescription(String language);

  String getURL();

  String getInstanceId();

  String getDate();

  // added by ney. 16/05/2004.
  String getSilverCreationDate();

  String getIconUrl();

  String getCreatorId();

  Collection<String> getLanguages();

  /**
   * {@link SilverpeasContent} default implementations.
   */

  @Override
  default String getComponentInstanceId() {
    return getInstanceId();
  }

  @Override
  default User getCreator() {
    return getCreatorId() != null ? User.getById(getCreatorId()) : null;
  }

  @Override
  default User getLastModifier() {
    return getCreator();
  }

  @Override
  default Date getCreationDate() {
    try {
      return getSilverCreationDate() != null ? DateUtil.parseDate(getSilverCreationDate()) : null;
    } catch (ParseException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @Override
  default Date getLastModificationDate() {
    try {
      return getDate() != null ? DateUtil.parseDate(getDate()) : null;
    } catch (ParseException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}