/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;

import java.util.List;

/**
 * @author ebonnet
 */
@Repository
public class SPUserJpaRepository extends BasicJpaEntityRepository<SPUser>
    implements SPUserRepository {

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
