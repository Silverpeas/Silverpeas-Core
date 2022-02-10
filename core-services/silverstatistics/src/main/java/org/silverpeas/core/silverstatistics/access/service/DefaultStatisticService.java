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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.silverstatistics.access.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silverstatistics.access.dao.HistoryObjectDAO;
import org.silverpeas.core.silverstatistics.access.model.HistoryByUser;
import org.silverpeas.core.silverstatistics.access.model.HistoryCriteria;
import org.silverpeas.core.silverstatistics.access.model.HistoryCriteria.QUERY_ORDER_BY;
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.silverstatistics.access.model.StatisticRuntimeException;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.SilverpeasList;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.silverstatistics.access.dao.HistoryObjectDAO.countByPeriodAndUser;

/**
 * Default implementation of Statistic service layer which manage statistics
 */
@Service
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultStatisticService implements StatisticService, ComponentInstanceDeletion {

  public static final int ACTION_ACCESS = 1;

  protected DefaultStatisticService() {
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public void addStat(String userId, ResourceReference resourceReference, int actionType, String objectType) {
    try (Connection con = getConnection()) {
      HistoryObjectDAO.add(con, userId, resourceReference, actionType, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public void addStat(String userId, SilverpeasContent content) {
    addStat(userId, getForeignPK(content), ACTION_ACCESS, content.getContributionType());
  }

  @Override
  public int getCount(List<ResourceReference> resourceReferences, int action, String objectType) {
    try (Connection con = getConnection()) {
      return HistoryObjectDAO.getCount(con, resourceReferences, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public int getCount(ResourceReference resourceReference, int action, String objectType) {
    try (Connection con = getConnection()) {
      return HistoryObjectDAO.getCount(con, resourceReference, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public int getCount(SilverpeasContent content, int action) {
    return getCount(getForeignPK(content), content.getContributionType());
  }

  @Override
  public int getCount(ResourceReference resourceReference, String objectType) {
    return getCount(resourceReference, ACTION_ACCESS, objectType);
  }

  @Override
  public int getCount(SilverpeasContent content) {
    return getCount(getForeignPK(content), content.getContributionType());
  }

  @Override
  public List<HistoryByUser> getHistoryByUser(ResourceReference resourceReference, int action,
      String objectType) {
    try (Connection con = getConnection()) {
      final HistoryCriteria criteria = new HistoryCriteria(action)
          .onResource(resourceReference)
          .ofType(objectType);
      return HistoryObjectDAO.findByUserByCriteria(con, criteria);
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public SilverpeasList<HistoryObjectDetail> getHistoryByAction(
      final ResourceReference resourceReference, final int action, final String objectType,
      final Collection<String> excludedUserIds, final PaginationPage pagination) {
    try (Connection con = getConnection()) {
      final HistoryCriteria criteria = new HistoryCriteria(action)
          .onResource(resourceReference)
          .ofType(objectType)
          .paginatedBy(pagination);
      if (excludedUserIds != null) {
        criteria
          .byExcludingUsers(excludedUserIds);
      }
      return HistoryObjectDAO.findByCriteria(con, criteria);
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public SilverpeasList<HistoryObjectDetail> getHistoryByObjectAndUser(
      ResourceReference resourceReference, int action, String objectType, String userId,
      final PaginationPage paginationPage, final QUERY_ORDER_BY orderBy) {
    try (Connection con = getConnection()) {
      final HistoryCriteria criteria = new HistoryCriteria(action)
          .onResource(resourceReference)
          .ofType(objectType)
          .aboutUsers(userId)
          .paginatedBy(paginationPage);
      if (orderBy != null) {
        criteria
          .orderedBy(orderBy);
      }
      return HistoryObjectDAO.findByCriteria(con, criteria);
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public List<HistoryByUser> getHistoryByObject(ResourceReference resourceReference, int action,
      String objectType) {
    UserDetail[] allUsers = OrganizationControllerProvider.getOrganisationController()
        .getAllUsers(resourceReference.getInstanceId());
    return getHistoryByObject(resourceReference, action, objectType, allUsers);
  }

  @Override
  public List<HistoryByUser> getHistoryByObject(ResourceReference resourceReference, int action,
      String objectType, List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return getHistoryByObject(resourceReference, action, objectType);
    }
    UserDetail[] users = OrganizationControllerProvider.getOrganisationController()
        .getUserDetails(userIds.toArray(new String[0]));
    return getHistoryByObject(resourceReference, action, objectType, users);
  }

  private List<HistoryByUser> getHistoryByObject(ResourceReference resourceReference, int action,
      String objectType, UserDetail[] users) {
    final List<HistoryByUser> allStatsByUser = new ArrayList<>(getHistoryByUser(resourceReference, action, objectType));
    if (users != null && users.length > 0) {
      final Set<String> userIds = allStatsByUser.stream().map(HistoryByUser::getUserId).collect(Collectors.toSet());
      Arrays.stream(users)
          .filter(u -> !userIds.contains(u.getId()))
          .forEach(u -> allStatsByUser.add(new HistoryByUser(u, null, 0)));
    }
    return allStatsByUser;
  }

  @Override
  public void deleteStats(ResourceReference resourceReference, String objectType) {
    try (Connection con = getConnection()) {
      HistoryObjectDAO.deleteHistoryByObject(con, resourceReference, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public void deleteStats(SilverpeasContent content) {
    deleteStats(getForeignPK(content), content.getContributionType());
  }

  @Override
  public void moveStat(ResourceReference toResourceReference, int actionType, String objectType) {
    try (Connection con = getConnection()) {
      HistoryObjectDAO.move(con, toResourceReference, actionType, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public int getCountByPeriod(List<ResourceReference> refs, int action, String objectType,
      Date startDate, Date endDate) {
    int nb = 0;
    try (Connection con = getConnection()) {
      for (ResourceReference aRef : refs) {
        nb += HistoryObjectDAO.getCountByPeriod(con, aRef, objectType, startDate, endDate);
      }
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
    return nb;
  }

  @Override
  public int getCountByPeriodAndUser(List<ResourceReference> refs, String objectType,
      Date startDate, Date endDate, List<String> userIds) {
    int nb = 0;
    if (!userIds.isEmpty()) {
      final Set<ContributionIdentifier> ids = refs.stream()
          .map(k -> ContributionIdentifier.from(k, objectType))
          .collect(Collectors.toSet());
      try (Connection con = getConnection()) {
        nb += userIds.stream().flatMapToInt(u -> {
          try {
            return countByPeriodAndUser(con, ids, startDate, endDate, u).mapToInt(Pair::getSecond);
          } catch (SQLException e) {
            throw new StatisticRuntimeException(e);
          }
        }).sum();
      } catch (Exception e) {
        throw new StatisticRuntimeException(e);
      }
    }
    return nb;
  }

  @Override
  public int getDistinctCountByPeriod(List<ResourceReference> refs, int action, String objectType,
      Date startDate, Date endDate) {
    try (Connection con = getConnection()) {
      List<String> objectIds =
          HistoryObjectDAO.getListObjectAccessByPeriod(con, refs, objectType, startDate, endDate);
      Set<String> distinctObjectIds = new HashSet<>(objectIds);
      return distinctObjectIds.size();
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public int getDistinctCountByPeriodUser(List<ResourceReference> refs, int action,
      String objectType, Date startDate, Date endDate, List<String> userIds) {
    int nb = 0;
    if (userIds != null && !userIds.isEmpty()) {
      Set<String> distinctObjectIds = new HashSet<>(userIds.size());
      try (Connection con = getConnection()) {
        for (String userId : userIds) {
          List<String> objectIds =
              HistoryObjectDAO.getListObjectAccessByPeriodAndUser(con, refs, objectType, startDate,
                  endDate, userId);
          distinctObjectIds.addAll(objectIds);
        }
        nb = distinctObjectIds.size();
      } catch (Exception e) {
        throw new StatisticRuntimeException(e);
      }
    }
    return nb;
  }

  @Override
  public Collection<HistoryObjectDetail> getLastHistoryOfObjectsForUser(String userId,
      int actionType, String objectType, int nbObjects) {
    try (Connection con = getConnection()) {
      return HistoryObjectDAO
          .getLastHistoryDetailOfObjectsForUser(con, userId, actionType, objectType, nbObjects);
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  @Override
  public <T extends Contribution> Stream<T> filterRead(final Collection<T> contributions,
      final String userId) {
    try (final Connection con = getConnection()) {
      final Map<ContributionIdentifier, T> indexed = contributions.stream()
          .collect(Collectors.toMap(Contribution::getIdentifier, c -> c));
      return countByPeriodAndUser(con, indexed.keySet(), null, null, userId)
          .filter(p -> p.getSecond() > 0)
          .map(p -> indexed.get(p.getFirst()));
    } catch (Exception e) {
      throw new StatisticRuntimeException(e);
    }
  }

  private ResourceReference getForeignPK(SilverpeasContent content) {
    return new ResourceReference(content.getId(), content.getComponentInstanceId());
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try (Connection con = getConnection()) {
      HistoryObjectDAO.deleteStatsOfComponent(con, componentInstanceId);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(
          "A failure occurred when deleting the statistics relative to the component instance " +
              componentInstanceId, e);
    }
  }
}
