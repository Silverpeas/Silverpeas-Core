/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.domains.silverpeasdriver;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author ehugonnet
 */
@Entity
@Table(name = "domainsp_group")
@NamedQueries( {
    @NamedQuery(name = "SPGroup.findByName", query = "SELECT s FROM SPGroup s WHERE s.name = :name"),
    @NamedQuery(name = "SPGroup.findByDescription", query = "SELECT s FROM SPGroup s WHERE s.description = :description"),
    @NamedQuery(name = "SPGroup.listAllRootGroups", query = "SELECT s FROM SPGroup s WHERE s.parent is null") })
public class SPGroup implements Serializable {

  private static final long serialVersionUID = 287775215176520067L;
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "id")
  private Integer id;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 100)
  @Column(name = "name")
  private String name;
  @Size(max = 400)
  @Column(name = "description")
  private String description;
  @JoinTable(name = "domainsp_group_user_rel", joinColumns = { @JoinColumn(name = "groupid", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "userid", referencedColumnName = "id") })
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
    this.id = id;
  }

  public SPGroup(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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
  public int hashCode() {
    int hash = 0;
    hash += (id != null ? id.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof SPGroup)) {
      return false;
    }
    SPGroup other = (SPGroup) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "com.silverpeas.domains.silverpeasdriver.SPGroup[ id=" + id + " ]";
  }
}
