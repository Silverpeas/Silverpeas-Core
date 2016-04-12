/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain.driver;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaCustomEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

/**
 * @author ehugonnet
 */
@Entity
@Table(name = "domainsp_group")
@NamedQueries({
    @NamedQuery(name = "SPGroup.findByName",
        query = "SELECT s FROM SPGroup s WHERE s.name = :name"),
    @NamedQuery(name = "SPGroup.findByDescription",
        query = "SELECT s FROM SPGroup s WHERE s.description = :description"),
    @NamedQuery(name = "SPGroup.listAllRootGroups",
        query = "SELECT s FROM SPGroup s WHERE s.parent is null")})
public class SPGroup extends AbstractJpaCustomEntity<SPGroup, UniqueIntegerIdentifier>
    implements Serializable {

  private static final long serialVersionUID = 287775215176520067L;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 100)
  @Column(name = "name")
  private String name;
  @Size(max = 400)
  @Column(name = "description")
  private String description;
  @JoinTable(name = "domainsp_group_user_rel",
      joinColumns = {@JoinColumn(name = "groupid", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "userid", referencedColumnName = "id")})
  @ManyToMany
  private Set<SPUser> users;
  @OneToMany(mappedBy = "parent")
  private Set<SPGroup> subGroups;
  @JoinColumn(name = "supergroupid", referencedColumnName = "id")
  @ManyToOne
  private SPGroup parent;

  public SPGroup() {
  }

  public SPGroup(Integer id) {
    setId(id);
  }

  public SPGroup(Integer id, String name) {
    this(id);
    this.name = name;
  }

  public void setId(Integer id) {
    setId(String.valueOf(id));
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Set<SPUser> getUsers() {
    return users;
  }

  public void setUsers(Set<SPUser> users) {
    this.users = users;
  }

  public Set<SPGroup> getSubGroups() {
    return subGroups;
  }

  public void setSubGroups(Set<SPGroup> subGroups) {
    this.subGroups = subGroups;
  }

  public SPGroup getParent() {
    return parent;
  }

  public void setParent(SPGroup parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    return "org.silverpeas.core.admin.domain.driver.SPGroup[ id=" + getId() + " ]";
  }
}
