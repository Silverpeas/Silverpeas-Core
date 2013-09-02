/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.comment.service.notification;

import com.silverpeas.SilverpeasContent;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.Date;

/**
 * A silverpeas content to use in tests.
 */
public class Classified implements SilverpeasContent {
  
  private static final long serialVersionUID = -5162002893531236283L;
  private String id;
  private String instanceId;
  private UserDetail author;
  private String title;
  
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
    return author;
  }
  
  public Classified createdBy(final UserDetail author) {
    this.author = author;
    return this;
  }

  @Override
  public String getTitle() {
    return title;
  }
  
  @Override
  public String getDescription() {
    return "";
  }

  public Classified entitled(String title) {
    this.title = title;
    return this;
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
  public String getSilverpeasContentId() {
    return "";
  }
  
}
