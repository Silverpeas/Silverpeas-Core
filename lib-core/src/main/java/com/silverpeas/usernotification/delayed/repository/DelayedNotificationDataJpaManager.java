package com.silverpeas.usernotification.delayed.repository;

import com.silverpeas.usernotification.delayed.constant.DelayedNotificationFrequency;
import com.silverpeas.usernotification.delayed.model.DelayedNotificationData;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.silverpeas.persistence.model.identifier.UniqueLongIdentifier;
import org.silverpeas.persistence.repository.jpa.JpaBasicEntityManager;
import org.silverpeas.util.persistence.TypedParameter;
import org.silverpeas.util.persistence.TypedParameterUtil;

import javax.inject.Singleton;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author: ebonnet
 */
@Singleton
public class DelayedNotificationDataJpaManager
    extends JpaBasicEntityManager<DelayedNotificationData, UniqueLongIdentifier>
    implements DelayedNotificationDataManager {

  @Override
  public List<Integer> findAllUsersToBeNotified(final Collection<Integer> aimedChannels) {
    return getEntityManager()
        .createNamedQuery("DelayedNotificationData.findDistinctUserByChannel", Integer.class)
        .getResultList();
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
        newNamedParameters().add("ids", ids));
  }


  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.delayed.repository.DelayedNotificationRepositoryCustom#
   * findUsersToBeNotified(java.util.Set, java.util.Set, boolean)
   */
  public List<Integer> findUsersToBeNotified(final Set<NotifChannel> aimedChannels,
      final Set<DelayedNotificationFrequency> aimedFrequencies,
      final boolean isThatUsersWithNoSettingHaveToBeNotified) {

    // Parameters
    final List<TypedParameter<?>> parameters = new ArrayList<TypedParameter<?>>();

    // Query
    final StringBuilder query = new StringBuilder();
    query.append("select distinct d.userId from DelayedNotificationData d ");
    query.append("  left outer join d.delayedNotificationUserSetting p ");
    query.append("where d.channel in (:");
    query.append(TypedParameterUtil
        .addNamedParameter(parameters, "channels", NotifChannel.toIds(aimedChannels)));
    query.append(") and ( ");
    query.append("  (p.id is not null and p.frequency in (:");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "frequencies",
        DelayedNotificationFrequency.toCodes(aimedFrequencies))).append(")) ");
    if (isThatUsersWithNoSettingHaveToBeNotified) {
      query.append("  or p.id is null ");
    }
    query.append(") ");

    // Typed query
    final TypedQuery<Integer> typedQuery =
        getEntityManager().createQuery(query.toString(), Integer.class);

    // Parameters
    TypedParameterUtil.computeNamedParameters(typedQuery, parameters);

    // Result
    return typedQuery.getResultList();
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.delayed.repository
   * .DelayedNotificationRepositoryCustom#findResource(com.silverpeas.
   * notification.model.NotificationResourceData)
   */
  public List<DelayedNotificationData> findDelayedNotification(
      final DelayedNotificationData delayedNotification) {

    // Parameters
    final List<TypedParameter<?>> parameters = new ArrayList<TypedParameter<?>>();

    // Query
    final StringBuilder query = new StringBuilder("from DelayedNotificationData where");
    query.append(" userId = :");
    query.append(TypedParameterUtil
        .addNamedParameter(parameters, "userId", delayedNotification.getUserId()));
    query.append(" and fromUserId = :");
    query.append(TypedParameterUtil
        .addNamedParameter(parameters, "fromUserId", delayedNotification.getFromUserId()));
    query.append(" and channel = :");
    query.append(TypedParameterUtil
        .addNamedParameter(parameters, "channel", delayedNotification.getChannel().getId()));
    query.append(" and action = :");
    query.append(TypedParameterUtil
        .addNamedParameter(parameters, "action", delayedNotification.getAction().getId()));
    query.append(" and language = :");
    query.append(TypedParameterUtil
        .addNamedParameter(parameters, "language", delayedNotification.getLanguage()));
    Date date = delayedNotification.getCreationDate();
    if (date == null) {
      date = new Date();
    }
    query.append(" and creationDate between :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "creationDateMin",
        DateUtils.setSeconds(DateUtils.setMilliseconds(date, 0), 0), TemporalType.TIMESTAMP));
    query.append(" and :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "creationDateMax",
        DateUtils.setSeconds(DateUtils.setMilliseconds(date, 999), 59), TemporalType.TIMESTAMP));
    query.append(" and notificationResourceId = :");
    query.append(TypedParameterUtil
        .addNamedParameter(parameters, "resourceId", delayedNotification.getResource()));

    // resourceDescription parameter
    if (StringUtils.isNotBlank(delayedNotification.getMessage())) {
      query.append(" and message = :");
      query.append(TypedParameterUtil
          .addNamedParameter(parameters, "message", delayedNotification.getMessage()));
    } else {
      query.append(" and message is null");
    }

    // Typed query
    final TypedQuery<DelayedNotificationData> typedQuery =
        getEntityManager().createQuery(query.toString(), DelayedNotificationData.class);

    // Parameters
    TypedParameterUtil.computeNamedParameters(typedQuery, parameters);

    // Result
    return typedQuery.getResultList();
  }
}
