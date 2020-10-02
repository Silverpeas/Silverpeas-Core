package org.silverpeas.core.workflow.engine.instance;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.util.SilverpeasList;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Collection;

/**
 * Created by Nicolas on 05/06/2017.
 */
@Repository
@Singleton
public class ProcessInstanceRepository extends BasicJpaEntityRepository<ProcessInstanceImpl> {

  @Override
  @Transactional
  public ProcessInstanceImpl getById(final String id) {
    return super.getById(id).fetchAll();
  }

  @Override
  @Transactional
  public SilverpeasList<ProcessInstanceImpl> getById(final Collection<String> ids) {
    return fetchProcessInstanceData(super.getById(ids));
  }

  private SilverpeasList<ProcessInstanceImpl> fetchProcessInstanceData(
      SilverpeasList<ProcessInstanceImpl> processInstances) {
    processInstances.forEach(ProcessInstanceImpl::fetchAll);
    return processInstances;
  }
}
