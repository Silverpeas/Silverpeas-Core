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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.webactiv.calendar.socialNetwork;

import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.model.SocialInformationType;
import static com.silverpeas.socialNetwork.model.SocialInformationType.*;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.calendar.model.Schedulable;
import com.stratelia.webactiv.util.DateUtil;
import java.util.Date;

/**
 *
 * @author azzedine
 */
public class SocialInformationEvent implements SocialInformation {

  private String classification = "public";
  private final SocialInformationType type = EVENT;
  private Schedulable schedulable = null;

  public SocialInformationEvent(Schedulable schedulable) {

    this.schedulable = schedulable;
    this.classification = schedulable.getClassification().getString();
  }

  @Override
  public String getType() {
    return type.toString();
  }

  @Override
  public String getIcon() {
    if ("private".equals(classification)) {
      return type + "_private.gif";
    }
    return type + "_public.gif";
  }

  @Override
  public String getTitle() {
    return schedulable.getName();
  }

  @Override
  public String getDescription() {
    return schedulable.getDescription();
  }

  @Override
  public String getAuthor() {
    return schedulable.getDelegatorId();
  }

  @Override
  public String getUrl() {

    return URLManager.getURL(URLManager.CMP_AGENDA) + "SelectDay?Day=" + DateUtil.getInputDate(
        getDate(), "FR");
  }

  @Override
  public Date getDate() {
    return schedulable.getStartDate();
  }

  @Override
  public boolean getSocialInformationWasUpdeted() {
    return false;
  }

  @Override
  public int compareTo(SocialInformation o) {
    return getDate().compareTo(o.getDate());
  }
}
