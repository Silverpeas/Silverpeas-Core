package org.silverpeas.core.variables;

import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class DefaultVariablesRepository extends SilverpeasJpaEntityRepository<Variable>
    implements VariablesRepository {

  public List<Variable> getAllVariables() {
    NamedParameters params = newNamedParameters();
    return listFromNamedQuery("allVariables", params);
  }

}