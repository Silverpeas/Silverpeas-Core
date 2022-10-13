/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.socialnetwork.provider;

import org.silverpeas.core.date.Period;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;

import java.util.Date;
import java.util.List;

import static java.time.ZoneId.systemDefault;
import static org.silverpeas.core.date.TemporalConverter.asDate;
import static org.silverpeas.core.date.TemporalConverter.asOffsetDateTime;

/**
 * A provider of social information by the type of such information to get. It delegates the task
 * to the concrete social information provider that works on the type passed as argument.
 * @author Bensalem Nabil
 */
public interface SocialInformationProviderSwitcher {

  List<SocialInformation> getSocialInformationsList(SocialInformationType socialInformationType,
      SocialInfoContext context) ;

  List<SocialInformation> getSocialInformationsListOfMyContacts(
      SocialInformationType socialInformationType, SocialInfoContext context);

  class SocialInfoContext {
    private final String userId;
    private final Date begin;
    private final Date end;
    private List<String> contactIds = null;
    private String classification = "";

    public SocialInfoContext(final String userId, final Period period) {
      this.userId = userId;
      this.begin = asDate(asOffsetDateTime(period.getStartDate()).atZoneSameInstant(systemDefault()));
      this.end = asDate(asOffsetDateTime(period.getEndDate()).atZoneSameInstant(systemDefault()));
    }

    public SocialInfoContext withContactIds(final List<String> contactIds) {
      this.contactIds = contactIds;
      return this;
    }

    public SocialInfoContext withClassification(final String classification) {
      this.classification = classification;
      return this;
    }

    public String getUserId() {
      return userId;
    }

    public Date getBeginDate() {
      return begin;
    }

    public Date getEndDate() {
      return end;
    }

    public List<String> getContactIds() {
      return contactIds;
    }

    public String getClassification() {
      return classification;
    }
  }
}
