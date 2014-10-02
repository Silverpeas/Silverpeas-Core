package com.silverpeas.domains.silverpeasdriver;

import org.silverpeas.persistence.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.persistence.repository.jpa.JpaBasicEntityManager;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author: ebonnet
 */
public class SPUserRepository
    extends JpaBasicEntityManager<SPUser, UniqueIntegerIdentifier> implements SPUserDao {

  public List<SPUser> findByFirstname(@Param("firstname") final String firstName) {
    return listFromNamedQuery("SPUser.findByFirstname",
        newNamedParameters().add("firstname", firstName));
  }

  public List<SPUser> findByLastname(@Param("lastname") final String lastName) {
    return listFromNamedQuery("SPUser.findByLastname",
        newNamedParameters().add("firstname", lastName));
  }

  public List<SPUser> findByPhone(@Param("phone") final String phone) {
    return listFromNamedQuery("SPUser.findByPhone", newNamedParameters().add("phone", phone));
  }

  public List<SPUser> findByHomephone(@Param("homephone") final String homePhone) {
    return listFromNamedQuery("SPUser.findByHomephone",
        newNamedParameters().add("homephone", homePhone));
  }

  public List<SPUser> findByCellphone(@Param("cellphone") final String cellphone) {
    return listFromNamedQuery("SPUser.findByHomephone",
        newNamedParameters().add("cellphone", cellphone));
  }

  public List<SPUser> findByFax(@Param("fax") final String fax) {
    return listFromNamedQuery("SPUser.findByFax", newNamedParameters().add("fax", fax));
  }

  public List<SPUser> findByAddress(@Param("address") final String address) {
    return listFromNamedQuery("SPUser.findByAddress",
        newNamedParameters().add("address", address));
  }

  public List<SPUser> findByTitle(@Param("title") final String title) {
    return listFromNamedQuery("SPUser.findByTitle",
        newNamedParameters().add("title", title));
  }

  public List<SPUser> findByCompany(@Param("company") final String company) {
    return listFromNamedQuery("SPUser.findByCompany",
        newNamedParameters().add("address", company));
  }

  public List<SPUser> findByPosition(@Param("position") final String position) {
    return listFromNamedQuery("SPUser.findByPosition",
        newNamedParameters().add("address", position));
  }

  public List<SPUser> findByEmail(@Param("email") final String email) {
    return listFromNamedQuery("SPUser.findByEmail",
        newNamedParameters().add("address", email));
  }

}
