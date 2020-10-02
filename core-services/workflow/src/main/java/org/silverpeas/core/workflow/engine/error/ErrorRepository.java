package org.silverpeas.core.workflow.engine.error;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import java.util.List;

/**
 * Created by Nicolas on 07/06/2017.
 */
@Repository
public class ErrorRepository extends BasicJpaEntityRepository<WorkflowErrorImpl> {

  public List<WorkflowErrorImpl> getByProcessInstanceId(String id) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("id", Integer.parseInt(id));
    return listFromNamedQuery("processInstance.findErrors", parameters);
  }

  public void deleteByProcessInstanceId(String id) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("id", Integer.parseInt(id));
    deleteFromNamedQuery("processInstance.deleteErrors", parameters);
  }
}