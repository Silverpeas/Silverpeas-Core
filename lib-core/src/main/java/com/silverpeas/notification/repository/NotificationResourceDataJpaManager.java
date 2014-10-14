package com.silverpeas.notification.repository;

import com.silverpeas.notification.model.NotificationResourceData;
import org.silverpeas.persistence.model.identifier.UniqueLongIdentifier;
import org.silverpeas.persistence.repository.jpa.JpaBasicEntityManager;
import org.silverpeas.util.persistence.TypedParameter;
import org.silverpeas.util.persistence.TypedParameterUtil;

import javax.inject.Singleton;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: ebonnet
 */
@Singleton
public class NotificationResourceDataJpaManager
    extends JpaBasicEntityManager<NotificationResourceData, UniqueLongIdentifier>
    implements NotificationResourceDataManager {

  @Override
  public long deleteResources() {
    return deleteFromNamedQuery("NotificationResourceData.deleteResources", newNamedParameters());
  }

  /**
   * Get existing resource
   * @param resourceId the resource identifier
   * @param resourceType the resource type
   * @param componentInstanceId the component instance identifier
   * @return
   */
  @Override
  public NotificationResourceData getExistingResource(final String resourceId,
      final String resourceType, final String componentInstanceId) {

    // Parameters
    final List<TypedParameter<?>> parameters = new ArrayList<>();

    // Query
    final StringBuilder query = new StringBuilder("from NotificationResourceData where");
    query.append(" resourceId = :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "resourceId", resourceId));
    query.append(" and resourceType = :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "resourceType", resourceType));
    query.append(" and componentInstanceId = :");
    query.append(TypedParameterUtil
        .addNamedParameter(parameters, "componentInstanceId", componentInstanceId));

    // Typed query
    final TypedQuery<NotificationResourceData> tq =
        getEntityManager().createQuery(query.toString(), NotificationResourceData.class);

    // Parameters
    TypedParameterUtil.computeNamedParameters(tq, parameters);

    // Result
    final List<NotificationResourceData> resources = tq.getResultList();
    if (resources.size() == 1) {
      return resources.get(0);
    }
    return null;
  }
}
