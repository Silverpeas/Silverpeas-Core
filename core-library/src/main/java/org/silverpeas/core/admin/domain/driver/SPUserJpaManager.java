package org.silverpeas.core.admin.domain.driver;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.JpaBasicEntityManager;

import java.util.List;

/**
 * @author: ebonnet
 */
public class SPUserJpaManager extends JpaBasicEntityManager<SPUser, UniqueIntegerIdentifier>
    implements SPUserManager {

  public List<SPUser> findByFirstname(final String firstName) {
    return listFromNamedQuery("SPUser.findByFirstname",
        newNamedParameters().add("firstname", firstName));
  }

  public List<SPUser> findByLastname(final String lastName) {
    return listFromNamedQuery("SPUser.findByLastname",
        newNamedParameters().add("lastname", lastName));
  }

  public List<SPUser> findByPhone(final String phone) {
    return listFromNamedQuery("SPUser.findByPhone", newNamedParameters().add("phone", phone));
  }

  public List<SPUser> findByHomephone(final String homePhone) {
    return listFromNamedQuery("SPUser.findByHomephone",
        newNamedParameters().add("homephone", homePhone));
  }

  public List<SPUser> findByCellphone(final String cellphone) {
    return listFromNamedQuery("SPUser.findByCellphone",
        newNamedParameters().add("cellphone", cellphone));
  }

  public List<SPUser> findByFax(final String fax) {
    return listFromNamedQuery("SPUser.findByFax", newNamedParameters().add("fax", fax));
  }

  public List<SPUser> findByAddress(final String address) {
    return listFromNamedQuery("SPUser.findByAddress", newNamedParameters().add("address", address));
  }

  public List<SPUser> findByTitle(final String title) {
    return listFromNamedQuery("SPUser.findByTitle", newNamedParameters().add("title", title));
  }

  public List<SPUser> findByCompany(final String company) {
    return listFromNamedQuery("SPUser.findByCompany", newNamedParameters().add("company", company));
  }

  public List<SPUser> findByPosition(final String position) {
    return listFromNamedQuery("SPUser.findByPosition",
        newNamedParameters().add("position", position));
  }

  public List<SPUser> findByEmail(final String email) {
    return listFromNamedQuery("SPUser.findByEmail", newNamedParameters().add("email", email));
  }

}
