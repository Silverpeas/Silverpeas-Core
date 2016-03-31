package org.silverpeas.core.notification.user.delayed.repository;

import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationData;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.JpaBasicEntityManager;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import javax.inject.Singleton;
import javax.persistence.TemporalType;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency.toCodes;
import static org.silverpeas.core.notification.user.client.constant.NotifChannel.toIds;
import static org.apache.commons.lang3.time.DateUtils.addSeconds;

@Singleton
public class DelayedNotificationDataJpaManager
    extends JpaBasicEntityManager<DelayedNotificationData, UniqueLongIdentifier>
    implements DelayedNotificationDataManager {

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
    query.append("  left outer join d.delayedNotificationUserSetting p ");
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
