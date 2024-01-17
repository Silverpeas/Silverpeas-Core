/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.comment.service.notification;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A silverpeas content to use in tests.
 */
public class Classified implements SilverpeasContent {

  private static final long serialVersionUID = -5162002893531236283L;
  private final String id;
  private final String instanceId;
  private final List<String> unauthorizedUsers = new ArrayList<>();

  public Classified(String id, String instanceId) {
    this.id = id;
    this.instanceId = instanceId;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getComponentInstanceId() {
    return instanceId;
  }

  @Override
  public UserDetail getCreator() {
    return null;
  }

  @Override
  public String getTitle() {
    return "";
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public Date getCreationDate() {
    return new Date();
  }

  @Override
  public String getContributionType() {
    return "Classified";
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return !unauthorizedUsers.contains(user.getId());
  }

  @Override
  public String getSilverpeasContentId() {
    return "";
  }

  @Override
  public User getLastUpdater() {
    return getCreator();
  }

  @Override
  public Date getLastUpdateDate() {
    return getCreationDate();
  }
}
