/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.persistence.jdbc.bean;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.silverpeas.core.persistence.jdbc.bean.BeanCriteria.OPERATOR.*;

/**
 * Integration tests on the {@link SilverpeasBeanDAO} operations.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
@SuppressWarnings("deprecation")
public class SilverpeasBeanDAOIT {

  private static final String TABLE_CREATION_SCRIPT = "create-table.sql";
  private static final String DATASET_SQL_SCRIPT = "test-dao-dataset.sql";

  private SilverpeasBeanDAO<PersonBean> dao;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(DATASET_SQL_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SilverpeasBeanDAOIT.class)
        .addClasses(WAPrimaryKey.class)
        .addPackages(true, "org.silverpeas.core.persistence.jdbc.bean")
        .build();
  }

  @Before
  public void setUpDAO() throws PersistenceException {
    dao = SilverpeasBeanDAOFactory.getDAO(PersonBean.class);
    assertThat(dao, is(notNullValue()));
  }

  @Test
  public void getAnExistingPerson() throws PersistenceException {
    PersonPK pk = new PersonPK("1");
    PersonBean actual = dao.findByPrimaryKey(pk);
    assertThat(actual, is(notNullValue()));
    assertThat(actual.getFirstName(), is("John"));
    assertThat(actual.getLastName(), is("Doo"));
    assertThat(actual.getAge(), is(26));
    assertThat(actual.getAddress(), is("Street Tooper, Argentat"));
    assertThat(actual.getWebSiteURL(), is("/mywebsite/Doo_John"));
  }

  @Test
  public void removeAnExistingPerson() throws PersistenceException {
    PersonPK pk = new PersonPK("1");
    dao.remove(pk);
    assertThat(isPersonExists(pk), is(false));
  }

  @Test
  public void removeAnExistingPersonByItsName() throws PersistenceException {
    dao.removeBy(BeanCriteria.addCriterion("firstName", "John"));
    assertThat(isPersonExists(new PersonPK("1")), is(false));
    assertThat(isPersonExists(new PersonPK("2")), is(true));
    assertThat(isPersonExists(new PersonPK("3")), is(true));
    assertThat(isPersonExists(new PersonPK("4")), is(true));
  }

  @Test
  public void removeAnExistingPersonByItsAddress() throws PersistenceException {
    PersonPK pk = new PersonPK("1");
    dao.removeBy(BeanCriteria.addCriterion("address", LIKE, "%Tooper%"));
    assertThat(isPersonExists(pk), is(false));
    assertThat(isPersonExists(new PersonPK("2")), is(true));
    assertThat(isPersonExists(new PersonPK("3")), is(true));
    assertThat(isPersonExists(new PersonPK("4")), is(true));
  }

  @Test
  public void removeAnExistingPersonByItsNameAndAddress() throws PersistenceException {
    dao.removeBy(BeanCriteria.addCriterion("firstName", "John")
        .and("address", LIKE, "%Tooper%"));
    assertThat(isPersonExists(new PersonPK("1")), is(false));
    assertThat(isPersonExists(new PersonPK("2")), is(true));
    assertThat(isPersonExists(new PersonPK("3")), is(true));
    assertThat(isPersonExists(new PersonPK("4")), is(true));
  }

  @Test
  public void removeAnExistingPersonByItsNameOrAddress() throws PersistenceException {
    dao.removeBy(BeanCriteria.addCriterion("firstName", "Toto")
        .or("address", LIKE, "%Tooper%"));
    assertThat(isPersonExists(new PersonPK("1")), is(false));
    assertThat(isPersonExists(new PersonPK("2")), is(false));
    assertThat(isPersonExists(new PersonPK("3")), is(true));
    assertThat(isPersonExists(new PersonPK("4")), is(true));
  }

  @Test
  public void removeExistingPersonsByItsAge() throws PersistenceException {
    dao.removeBy(BeanCriteria.addCriterion("age", GREATER_OR_EQUAL, 26));
    assertThat(isPersonExists(new PersonPK("1")), is(false));
    assertThat(isPersonExists(new PersonPK("2")), is(false));
    assertThat(isPersonExists(new PersonPK("3")), is(true));
    assertThat(isPersonExists(new PersonPK("4")), is(false));
  }

  @Test
  public void removeAllPersons() throws PersistenceException {
    dao.removeBy(BeanCriteria.emptyCriteria());
    assertThat(isPersonExists(new PersonPK("1")), is(false));
    assertThat(isPersonExists(new PersonPK("2")), is(false));
    assertThat(isPersonExists(new PersonPK("3")), is(false));
    assertThat(isPersonExists(new PersonPK("4")), is(false));
  }

  @Test
  public void removeSomePersons() throws PersistenceException {
    dao.removeBy(BeanCriteria.addCriterion("id", IN, List.of(1, 2, 3)));
    assertThat(isPersonExists(new PersonPK("1")), is(false));
    assertThat(isPersonExists(new PersonPK("2")), is(false));
    assertThat(isPersonExists(new PersonPK("3")), is(false));
    assertThat(isPersonExists(new PersonPK("4")), is(true));
  }

  @Test
  public void removePersonsInAlpesJUGAsso() throws PersistenceException {
    dao.removeBy(BeanCriteria.emptyCriteria().andSubQuery("id", IN,
        "personId FROM SB_Membership", BeanCriteria.addCriterion("assoId", 1)));
    assertThat(isPersonExists(new PersonPK("3")), is(false));
    assertThat(isPersonExists(new PersonPK("4")), is(false));
    assertThat(isPersonExists(new PersonPK("1")), is(true));
    assertThat(isPersonExists(new PersonPK("2")), is(true));
  }

  @Test
  public void findAnExistingPersonByItsName() throws PersistenceException {
    var persons = new ArrayList<>(dao.findBy(BeanCriteria.addCriterion("firstName",
        "John")));
    assertThat(persons.size(), is(1));
    assertThat(persons.get(0).getFirstName(), is("John"));
    assertThat(persons.get(0).getPK().getId(), is("1"));
  }

  @Test
  public void findAnExistingPersonByItsAddress() throws PersistenceException {
    var persons = new ArrayList<>(dao.findBy(BeanCriteria.addCriterion("address", LIKE,
        "%Tooper%")));
    assertThat(persons.size(), is(1));
    assertThat(persons.get(0).getAddress(), is("Street Tooper, Argentat"));
    assertThat(persons.get(0).getPK().getId(), is("1"));
  }

  @Test
  public void findAnExistingPersonByItsNameAndAddress() throws PersistenceException {
    var persons = new ArrayList<>(dao.findBy(BeanCriteria.addCriterion("firstName", "John")
        .and("address", LIKE, "%Tooper%")));
    assertThat(persons.size(), is(1));
    assertThat(persons.get(0).getFirstName(), is("John"));
    assertThat(persons.get(0).getAddress(), is("Street Tooper, Argentat"));
    assertThat(persons.get(0).getPK().getId(), is("1"));
  }

  @Test
  public void findAnExistingPersonByItsNameOrAddress() throws PersistenceException {
    BeanCriteria criteria = BeanCriteria.addCriterion("firstName", "Toto")
        .or("address", LIKE, "%Tooper%");
    criteria.setAscOrderBy("id");
    var persons = new ArrayList<>(dao.findBy(criteria));
    assertThat(persons.size(), is(2));
    assertThat(persons.get(0).getPK().getId(), is("1"));
    assertThat(persons.get(1).getPK().getId(), is("2"));
  }

  @Test
  public void findAnExistingPersonByItsAge() throws PersistenceException {
    var criteria = BeanCriteria.addCriterion("age", 26);
    var persons = new ArrayList<>(dao.findBy(criteria));
    assertThat(persons.size(), is(1));
    assertThat(persons.get(0).getPK().getId(), is("1"));
  }

  @Test
  public void findExistingPersonsByTheirAge() throws PersistenceException {
    var criteria = BeanCriteria.addCriterion("age", GREATER_OR_EQUAL, 26);
    criteria.setAscOrderBy("id");
    var persons = new ArrayList<>(dao.findBy(criteria));
    assertThat(persons.size(), is(3));
    assertThat(persons.get(0).getPK().getId(), is("1"));
    assertThat(persons.get(1).getPK().getId(), is("2"));
    assertThat(persons.get(2).getPK().getId(), is("4"));
  }

  @Test
  public void findAllExistingPersons() throws PersistenceException {
    var criteria = BeanCriteria.emptyCriteria();
    criteria.setAscOrderBy("id");
    var persons = new ArrayList<>(dao.findBy(criteria));
    assertThat(persons.size(), is(4));
    assertThat(persons.get(0).getPK().getId(), is("1"));
    assertThat(persons.get(1).getPK().getId(), is("2"));
    assertThat(persons.get(2).getPK().getId(), is("3"));
    assertThat(persons.get(3).getPK().getId(), is("4"));
  }

  @Test
  public void findSomeExistingPersons() throws PersistenceException {
    var criteria = BeanCriteria.addCriterion("id", IN, List.of(1, 2, 3));
    criteria.setAscOrderBy("id");
    var persons = new ArrayList<>(dao.findBy(criteria));
    assertThat(persons.size(), is(3));
    assertThat(persons.get(0).getPK().getId(), is("1"));
    assertThat(persons.get(1).getPK().getId(), is("2"));
    assertThat(persons.get(2).getPK().getId(), is("3"));
  }

  @Test
  public void findExistingPersonsInAlpesJUGAsso() throws PersistenceException {
    var criteria = BeanCriteria.emptyCriteria().andSubQuery("id", IN,
        "personId FROM SB_Membership", BeanCriteria.addCriterion("assoId", 1));
    criteria.setAscOrderBy("id");
    var persons = new ArrayList<>(dao.findBy(criteria));
    assertThat(persons.size(), is(2));
    assertThat(persons.get(0).getPK().getId(), is("3"));
    assertThat(persons.get(1).getPK().getId(), is("4"));
  }

  @Test
  public void findExistingPersonsByAgeRangeAndBySomeNames() throws PersistenceException {
    var criteria = BeanCriteria.addCriterion("age", GREATER_OR_EQUAL, 26)
            .and(BeanCriteria.addCriterion("firstName", "John").or("firstName", "Toto"));
    criteria.setAscOrderBy("id");
    var persons = new ArrayList<>(dao.findBy(criteria));
    assertThat(persons.size(), is(2));
    assertThat(persons.get(0).getPK().getId(), is("1"));
    assertThat(persons.get(1).getPK().getId(), is("2"));
  }

  private boolean isPersonExists(PersonPK pk) throws PersistenceException {
    try(Connection connection = DBUtil.openConnection();
        PreparedStatement statement =
            connection.prepareStatement("SELECT id FROM " + pk.getTableName() + " WHERE id = ?")) {
      statement.setInt(1, Integer.parseInt(pk.getId()));
      try(ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next();
      }
    } catch (SQLException e ) {
      throw new PersistenceException(e);
    }
  }
}
  