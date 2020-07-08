package org.silverpeas.core.variables;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.List;

@Repository
@Singleton
public class DefaultVariablesRepository extends SilverpeasJpaEntityRepository<Variable>
    implements VariablesRepository {

  public List<Variable> getAllVariables() {
    NamedParameters params = newNamedParameters();
    return listFromNamedQuery("allVariables", params);
  }

  @Override
  public List<Variable> getAllCurrentVariables() {
    NamedParameters params = newNamedParameters().add("today", LocalDate.now());
    return listFromNamedQuery("currentVariables", params);
  }
}