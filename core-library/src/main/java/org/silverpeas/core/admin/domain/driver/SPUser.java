/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

/**
 * @author ehugonnet
 */
@Entity
@Table(name = "domainsp_user")
@NamedQueries({
    @NamedQuery(name = "SPUser.findByFirstname",
        query = "SELECT s FROM SPUser s WHERE lower(s.firstname) like lower(:firstname)"),
    @NamedQuery(name = "SPUser.findByLastname",
        query = "SELECT s FROM SPUser s WHERE lower(s.lastname) like lower(:lastname)"),
    @NamedQuery(name = "SPUser.findByPhone",
        query = "SELECT s FROM SPUser s WHERE lower(s.phone) like lower(:phone)"),
    @NamedQuery(name = "SPUser.findByHomephone",
        query = "SELECT s FROM SPUser s WHERE lower(s.homephone) like lower(:homephone)"),
    @NamedQuery(name = "SPUser.findByCellphone",
        query = "SELECT s FROM SPUser s WHERE lower(s.cellphone) like lower(:cellphone)"),
    @NamedQuery(name = "SPUser.findByFax",
        query = "SELECT s FROM SPUser s WHERE lower(s.fax) like lower(:fax)"),
    @NamedQuery(name = "SPUser.findByAddress",
        query = "SELECT s FROM SPUser s WHERE lower(s.address) like lower(:address)"),
    @NamedQuery(name = "SPUser.findByTitle",
        query = "SELECT s FROM SPUser s WHERE lower(s.title) like lower(:title)"),
    @NamedQuery(name = "SPUser.findByCompany",
        query = "SELECT s FROM SPUser s WHERE lower(s.company) like lower(:company)"),
    @NamedQuery(name = "SPUser.findByPosition",
        query = "SELECT s FROM SPUser s WHERE lower(s.position) like lower(:position)"),
    @NamedQuery(name = "SPUser.findByBoss",
        query = "SELECT s FROM SPUser s WHERE lower(s.boss) like lower(:boss)"),
    @NamedQuery(name = "SPUser.findByLogin",
        query = "SELECT s FROM SPUser s WHERE s.login = :login"),
    @NamedQuery(name = "SPUser.findByPassword",
        query = "SELECT s FROM SPUser s WHERE s.password = :password"),
    @NamedQuery(name = "SPUser.findByPasswordvalid",
        query = "SELECT s FROM SPUser s WHERE s.passwordvalid = :passwordvalid"),
    @NamedQuery(name = "SPUser.findByLoginmail",
        query = "SELECT s FROM SPUser s WHERE s.loginmail = :loginmail"),
    @NamedQuery(name = "SPUser.findByEmail",
        query = "SELECT s FROM SPUser s WHERE s.email = :email")})
public class SPUser extends BasicJpaEntity<SPUser, UniqueIntegerIdentifier>
    implements Serializable {

  private static final long serialVersionUID = 2645559023438948622L;

  @Size(max = 100)
  @Column(name = "firstname")
  private String firstname;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 100)
  @Column(name = "lastname")
  private String lastname;
  // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$",
  // message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or
  // fax number consider using this annotation to enforce field validation
  @Size(max = 20)
  @Column(name = "phone")
  private String phone;
  @Size(max = 20)
  @Column(name = "homephone")
  private String homephone;
  @Size(max = 20)
  @Column(name = "cellphone")
  private String cellphone;
  // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$",
  // message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or
  // fax number consider using this annotation to enforce field validation
  @Size(max = 20)
  @Column(name = "fax")
  private String fax;
  @Size(max = 500)
  @Column(name = "address")
  private String address;
  @Size(max = 100)
  @Column(name = "title")
  private String title;
  @Size(max = 100)
  @Column(name = "company")
  private String company;
  @Size(max = 100)
  @Column(name = "position")
  private String position;
  @Size(max = 100)
  @Column(name = "boss")
  private String boss;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 50)
  @Column(name = "login")
  private String login;
  @Size(max = 123)
  @Column(name = "password")
  private String password;
  @Basic(optional = false)
  @NotNull
  @Column(name = "passwordvalid")
  private char passwordvalid;
  @Size(max = 100)
  @Column(name = "loginmail")
  private String loginmail;
  // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@
  // (?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
  // message="Invalid email")//if the field contains email address consider using this annotation to
  // enforce field validation
  @Size(max = 100)
  @Column(name = "email")
  private String email;
  @ManyToMany(mappedBy = "users")
  private Set<SPGroup> groups;

  public SPUser() {
    this.title = "";
    this.company = "";
    this.position = "";
    this.boss = "";
    this.phone = "";
    this.homephone = "";
    this.fax = "";
    this.cellphone = "";
    this.address = "";
    this.loginmail = "";
    this.password = "";
    this.passwordvalid = 'N';
    this.lastname = "";
    this.login = "";
  }

  public SPUser(Integer id) {
    this();
    setId(id);
  }

  public SPUser(Integer id, String lastname, String login, char passwordvalid) {
    this(id);
    this.lastname = lastname;
    this.login = login;
    this.passwordvalid = passwordvalid;
  }

  public void setId(Integer id) {
    setId(String.valueOf(id));
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getHomephone() {
    return homephone;
  }

  public void setHomephone(String homephone) {
    this.homephone = homephone;
  }

  public String getCellphone() {
    return cellphone;
  }

  public void setCellphone(String cellphone) {
    this.cellphone = cellphone;
  }

  public String getFax() {
    return fax;
  }

  public void setFax(String fax) {
    this.fax = fax;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getBoss() {
    return boss;
  }

  public void setBoss(String boss) {
    this.boss = boss;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isPasswordValid() {
    return 'Y' == passwordvalid || 'y' == passwordvalid;
  }

  public void setPasswordValid(boolean passwordvalid) {
    if (passwordvalid) {
      this.passwordvalid = 'Y';
    } else {
      this.passwordvalid = 'N';
    }
  }

  public String getLoginmail() {
    return loginmail;
  }

  public void setLoginmail(String loginmail) {
    this.loginmail = loginmail;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Set<SPGroup> getGroups() {
    return groups;
  }

  public void setGroups(Set<SPGroup> groups) {
    this.groups = groups;
  }

  @Override
  public String toString() {
    return "org.silverpeas.core.admin.domain.driver.SPUser[ id=" + getId() + " ]";
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }
}
