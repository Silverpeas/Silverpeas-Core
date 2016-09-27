package org.silverpeas.core.admin.domain.driver;

import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;

import java.util.List;

/**
 * @author: ebonnet
 */
public class SPGroupJpaRepository extends BasicJpaEntityRepository<SPGroup>
    implements SPGroupRepository {

  @Override
  public List<SPGroup> findByName(final String name) {
    return listFromNamedQuery("SPUser.findByName", newNamedParameters().add("name", name));
  }

  @Override
  public List<SPGroup> findByDescription(final String description) {
    return listFromNamedQuery("SPUser.findByDescription",
        newNamedParameters().add("description", description));
  }

  @Override
  public List<SPGroup> listAllRootGroups() {
    return listFromNamedQuery("SPUser.listAllRootGroups", newNamedParameters());
  }
}
