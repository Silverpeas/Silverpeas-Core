package com.silverpeas.domains.silverpeasdriver;

import org.silverpeas.persistence.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.persistence.repository.jpa.JpaBasicEntityManager;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author: ebonnet
 */
public class SPGroupRepository extends JpaBasicEntityManager<SPGroup, UniqueIntegerIdentifier>
    implements SPGroupDao {

  @Override
  public List<SPGroup> findByName(@Param("name") final String name) {
    return listFromNamedQuery("SPUser.findByName",
        newNamedParameters().add("name", name));
  }

  @Override
  public List<SPGroup> findByDescription(@Param("description") final String description) {
    return listFromNamedQuery("SPUser.findByDescription",
        newNamedParameters().add("description", description));
  }

  @Override
  public List<SPGroup> listAllRootGroups() {
    return listFromNamedQuery("SPUser.listAllRootGroups", newNamedParameters());
  }
}
