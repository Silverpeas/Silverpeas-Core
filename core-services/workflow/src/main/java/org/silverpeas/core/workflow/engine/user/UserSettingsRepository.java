package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.workflow.api.user.UserSettings;

/**
 * Created by Nicolas on 30/05/2017.
 */
@Repository
public class UserSettingsRepository extends BasicJpaEntityRepository<UserSettingsImpl> {

  public UserSettings getByUserIdAndComponentId(String userId, String componentId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("userId", userId).add("componentId", componentId);
    SilverpeasList<UserSettingsImpl>
        manySettings = listFromNamedQuery("findByUserAndComponent", parameters);
    if (!manySettings.isEmpty()) {
      return manySettings.get(0);
    }
    return null;
  }

}
