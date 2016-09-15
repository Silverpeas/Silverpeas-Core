package org.silverpeas.core.notification.user.repository;

import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class NotificationResourceDataJpaRepository
    extends BasicJpaEntityRepository<NotificationResourceData>
    implements NotificationResourceDataRepository {

  @Override
  public long deleteResources() {
    return deleteFromNamedQuery("NotificationResourceData.deleteResources", noParameter());
  }

  @Override
  public NotificationResourceData getExistingResource(final String resourceId,
      final String resourceType, final String componentInstanceId) {

    // Parameters
    NamedParameters parameters = newNamedParameters();

    // Query
    final StringBuilder query = new StringBuilder("from NotificationResourceData where");
    query.append(" resourceId = :");
    query.append(parameters.add("resourceId", resourceId).getLastParameterName());
    query.append(" and resourceType = :");
    query.append(parameters.add("resourceType", resourceType).getLastParameterName());
    query.append(" and componentInstanceId = :");
    query.append(parameters.add("componentInstanceId", componentInstanceId).getLastParameterName());

    // Result
    final List<NotificationResourceData> resources =
        listFromJpqlString(query.toString(), parameters, NotificationResourceData.class);
    if (resources.size() == 1) {
      return resources.get(0);
    }
    return null;
  }
}
