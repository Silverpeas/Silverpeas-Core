/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.notification.user.delayed.repository;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationData;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import javax.inject.Singleton;
import javax.persistence.TemporalType;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.time.DateUtils.addSeconds;
import static org.silverpeas.core.notification.user.client.constant.NotifChannel.toIds;
import static org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency
    .toCodes;

@Singleton
public class DelayedNotificationDataJpaRepository
    extends BasicJpaEntityRepository<DelayedNotificationData>
    implements DelayedNotificationDataRepository {

  @Override
  public List<Integer> findAllUsersToBeNotified(final Collection<Integer> aimedChannels) {
    return listFromNamedQuery("DelayedNotificationData.findDistinctUserByChannel",
        newNamedParameters().add("channels", aimedChannels), Integer.class);
  }

  @Override
  public List<DelayedNotificationData> findByUserId(final int userId,
      final Collection<Integer> aimedChannels) {
    return listFromNamedQuery("DelayedNotificationData.findByUserId",
        newNamedParameters().add("userId", userId).add("channels", aimedChannels));
  }

  @Override
  public long deleteByIds(final Collection<Long> ids) {
    return deleteFromNamedQuery("DelayedNotificationData.deleteByIds",
        newNamedParameters().add("ids", UniqueLongIdentifier.fromLongs(ids)));
  }

  @Override
  public List<Integer> findUsersToBeNotified(final Set<NotifChannel> aimedChannels,
      final Set<DelayedNotificationFrequency> aimedFrequencies,
      final boolean isThatUsersWithNoSettingHaveToBeNotified) {

    // Parameters
    NamedParameters namedParameters = newNamedParameters();

    // Query
    final StringBuilder query = new StringBuilder();
        query.append("select distinct d.userId from DelayedNotificationData d ");
    query.append("left outer join DelayedNotificationUserSetting p on ");
    query.append("  d.userId = p.userId and d.channel = p.channel ");
    query.append("where d.channel in (:");
    query.append(namedParameters.add("channels", toIds(aimedChannels)).getLastParameterName());
    query.append(") and ( ");
    query.append("  (p.id is not null and p.frequency in (:");
    query.append(
        namedParameters.add("frequencies", toCodes(aimedFrequencies)).getLastParameterName())
        .append(")) ");
    if (isThatUsersWithNoSettingHaveToBeNotified) {
      query.append("  or p.id is null ");
    }
    query.append(") ");

    // Result
    return listFromJpqlString(query.toString(), namedParameters, Integer.class);
  }

  @Override
  public List<DelayedNotificationData> findDelayedNotification(
      final DelayedNotificationData delayedNotification) {

    // Parameters
    NamedParameters namedParameters = newNamedParameters();

    // Query
    final StringBuilder query = new StringBuilder("from DelayedNotificationData where");
    query.append(" userId = :");
    query.append(
        namedParameters.add("userId", delayedNotification.getUserId()).getLastParameterName());
    query.append(" and fromUserId = :");
    query.append(namedParameters.add("fromUserId", delayedNotification.getFromUserId())
        .getLastParameterName());
    query.append(" and channel = :");
    query.append(namedParameters.add("channel", delayedNotification.getChannel().getId())
        .getLastParameterName());
    query.append(" and action = :");
    query.append(namedParameters.add("action", delayedNotification.getAction().getId())
        .getLastParameterName());
    query.append(" and language = :");
    query.append(
        namedParameters.add("language", delayedNotification.getLanguage()).getLastParameterName());
    Date date = delayedNotification.getCreationDate();
    if (date == null) {
      date = new Date();
    }
    query.append(" and creationDate between :");
    query.append(
        namedParameters.add("creationDateMin", addSeconds(date, -45), TemporalType.TIMESTAMP)
            .getLastParameterName());
    query.append(" and :");
    query.append(
        namedParameters.add("creationDateMax", addSeconds(date, 45), TemporalType.TIMESTAMP)
            .getLastParameterName());
    query.append(" and notificationResourceId = :");
    query.append(namedParameters.add("resourceId", delayedNotification.getResource())
        .getLastParameterName());

    // resourceDescription parameter
    if (StringUtils.isNotBlank(delayedNotification.getMessage())) {
      query.append(" and message = :");
      query.append(
          namedParameters.add("message", delayedNotification.getMessage()).getLastParameterName());
    } else {
      query.append(" and message is null");
    }

    // Result
    return listFromJpqlString(query.toString(), namedParameters, DelayedNotificationData.class);
  }
}
