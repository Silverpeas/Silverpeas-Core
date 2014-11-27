/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.domains.silverpeasdriver;

import com.ninja_squad.dbsetup.operation.Operation;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.DomainDriver;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.IsInstanceOf;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.silverpeas.DataSetTest;
import org.silverpeas.authentication.encryption.PasswordEncryption;
import org.silverpeas.authentication.encryption.PasswordEncryptionProvider;
import org.silverpeas.authentication.encryption.UnixSHA512Encryption;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.test.WarBuilder4LibCore;
import org.silverpeas.test.rule.DbUnitLoadingRule;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class SPUserDaoTest extends DataSetTest {

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(this, "create-database.sql", "spuser-dataset.xml");


  @Override
  protected Operation getDbSetupOperations() {
    return null;
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarFor(SPUserJpaManager.class).addCommonBasicUtilities()
        .addSilverpeasExceptionBases().addJpaPersistenceFeatures().testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "com.silverpeas.domains.silverpeasdriver");
          warBuilder.addClasses(DomainDriver.class, AbstractDomainDriver.class,
              PasswordEncryptionProvider.class, UnixSHA512Encryption.class,
              PasswordEncryption.class);
          warBuilder.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml");
          warBuilder.addAsResource("META-INF/services/test-org.silverpeas.util.BeanContainer",
              "META-INF/services/org.silverpeas.util.BeanContainer");
          warBuilder.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        }).build();
  }

  @Inject
  private SPUserJpaManager dao;

  static final int PASSWORD_MAX_SIZE = 123;


  public SPUserDaoTest() {
  }

  @Test
  @Transactional
  public void testNewValidUser() {
    Transaction.performInOne(() -> {
      SPUser tartempion = getTartempion();
      dao.saveAndFlush(tartempion);
      return null;
    });
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test(expected = ConstraintViolationException.class)
  @Transactional
  public void testNewUserWithInvalidPassword() {
    try {
      Transaction.performInOne(() -> {
        SPUser tartempion = getTartempion();
        tartempion.setPassword(tartempion.getPassword() + "b");
        dao.saveAndFlush(tartempion);
        return null;
      });
    } catch (Exception e) {
      expectedException
          .expectCause(is(IsInstanceOf.<Throwable>instanceOf(ConstraintViolationException.class)));
      throw new ConstraintViolationException("Transactional wrap ConstraintViolation", null);
    }
  }

  /**
   * Test of findByFirstname method, of class SPUserDao.
   */
  @Test
  @Transactional
  public void testReadByPrimaryKey() {
    Transaction.performInOne(() -> {
      SPUser bart = dao.getById("1000");
      Set<SPGroup> groups = bart.getGroups();
      assertThat(groups, is(notNullValue()));
      assertThat(groups, hasSize(2));
      assertThat(groups.contains((new SPGroup(5000))), is(true));
      assertThat(groups.contains((new SPGroup(5001))), is(true));
      assertThat(bart, is(notNullValue()));
      assertThat(bart.getFirstname(), is("bart"));
      assertThat(bart.getLastname(), is("simpson"));
      assertThat(bart.getId(), is("1000"));
      assertThat(bart.getPhone(), is("047669084"));
      assertThat(bart.getAddress(), is("18 rue des aiguinards"));
      assertThat(bart.getTitle(), is("student"));
      assertThat(bart.getCompany(), is("Simpson's family"));
      assertThat(bart.getEmail(), is("bart.simpson@silverpeas.org"));
      assertThat(bart.getPosition(), is("elder"));
      assertThat(bart.isPasswordValid(), is(true));
      assertThat(bart.getLogin(), is("bart.simpson"));
      assertThat(bart.getPassword(), is("bart"));
      return null;
    });
  }

  /**
   * Test of findByFirstname method, of class SPUserDao.
   */
  @Test
  public void testFindByFirstname() {
    String firstName = "bart";
    List<SPUser> result = dao.findByFirstname(firstName);
    assertThat(result, hasSize(1));
    SPUser bart = result.get(0);
    assertIsBart(bart);
    firstName = "krusty";
    result = dao.findByFirstname(firstName);
    assertThat(result, hasSize(1));
    SPUser krusty = result.get(0);
    assertIsKrusty(krusty);
    firstName = RandomStringUtils.random(15);
    result = dao.findByFirstname(firstName);
    assertThat(result, hasSize(0));
  }

  /**
   * Test of findByLastname method, of class SPUserDao.
   */
  @Test
  public void testFindByLastname() {
    String lastName = "simpson";
    List<SPUser> result = dao.findByLastname(lastName);
    assertThat(result, hasSize(2));
    SPUser bart = result.get(0);
    assertIsBart(bart);
    SPUser lisa = result.get(1);
    assertIsLisa(lisa);
    lastName = RandomStringUtils.random(15);
    result = dao.findByLastname(lastName);
    assertThat(result, hasSize(0));
  }

  /**
   * Test of findByPhone method, of class SPUserDao.
   */
  @Test
  public void testFindByPhone() {
    String phone = "0146221498";
    List<SPUser> result = dao.findByPhone(phone);
    assertThat(result, hasSize(1));
    SPUser krusty = result.get(0);
    assertIsKrusty(krusty);
    phone = RandomStringUtils.random(15);
    result = dao.findByPhone(phone);
    assertThat(result, hasSize(0));
  }

  /**
   * Test of findByAddress method, of class SPUserDao.
   */
  /* @Test
   public void testFindByAddress() {
   System.out.println("findByAddress");
   String address = "";

   List expResult = null;
   List result = dao.findByAddress(address);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of findByTitle method, of class SPUserDao.
   */
  /*@Test
   public void testFindByTitle() {
   System.out.println("findByTitle");
   String title = "";

   List expResult = null;
   List result = dao.findByTitle(title);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/

  /**
   * Test of findByCompany method, of class SPUserDao.
   */
  @Test
  public void testFindByCompany() {
    String position = "elder";
    List<SPUser> result = dao.findByPosition(position);
    assertThat(result, hasSize(1));
    assertIsBart(result.get(0));
  }

  /**
   * Test of findByPosition method, of class SPUserDao.
   */
  @Test
  public void testFindByPosition() {
    String position = "elder";
    List<SPUser> result = dao.findByPosition(position);
    assertThat(result, hasSize(1));
    assertIsBart(result.get(0));

    position = "benjamin";
    result = dao.findByPosition(position);
    assertThat(result, hasSize(2));
    assertIsKrusty(result.get(0));
    assertIsLisa(result.get(1));
    position = RandomStringUtils.random(15);
    result = dao.findByPosition(position);
    assertThat(result, hasSize(0));
  }

  void assertIsLisa(SPUser lisa) {
    assertThat(lisa.getFirstname(), is("lisa"));
    assertThat(lisa.getId(), is("1001"));
    assertThat(lisa.getLastname(), is("simpson"));
    assertThat(lisa.getPhone(), is("047669084"));
    assertThat(lisa.getAddress(), is("18 rue des aiguinards"));
    assertThat(lisa.getTitle(), is("saxo player"));
    assertThat(lisa.getCompany(), is("Simpson's family"));
    assertThat(lisa.getEmail(), is("lisa.simpson@silverpeas.org"));
    assertThat(lisa.isPasswordValid(), is(true));
    assertThat(lisa.getLogin(), is("lisa.simpson"));
    assertThat(lisa.getPassword(), is("lisa"));
    assertThat(lisa.getPosition(), is("benjamin"));
  }

  void assertIsBart(SPUser bart) {
    assertThat(bart.getFirstname(), is("bart"));
    assertThat(bart.getId(), is("1000"));
    assertThat(bart.getLastname(), is("simpson"));
    assertThat(bart.getPosition(), is("elder"));
    assertThat(bart.getPhone(), is("047669084"));
    assertThat(bart.getAddress(), is("18 rue des aiguinards"));
    assertThat(bart.getTitle(), is("student"));
    assertThat(bart.getCompany(), is("Simpson's family"));
    assertThat(bart.getEmail(), is("bart.simpson@silverpeas.org"));
    assertThat(bart.isPasswordValid(), is(true));
    assertThat(bart.getLogin(), is("bart.simpson"));
    assertThat(bart.getPassword(), is("bart"));
  }

  private void assertIsKrusty(SPUser krusty) {
    assertThat(krusty.getFirstname(), is("krusty"));
    assertThat(krusty.getId(), is("1010"));
    assertThat(krusty.getPhone(), is("0146221498"));
    assertThat(krusty.getLastname(), is("theklown"));
    assertThat(krusty.getPosition(), is("benjamin"));
    assertThat(krusty.getAddress(), is("18 rue des aiguinards"));
    assertThat(krusty.getTitle(), is("Klown"));
    assertThat(krusty.getCompany(), is("Krusty Show"));
    assertThat(krusty.getPosition(), is("benjamin"));
    assertThat(krusty.getBoss(), is("krusty"));
    assertThat(krusty.getLogin(), is("krusty.theklown"));
    assertThat(krusty.getPassword(), is("krusty"));
    assertThat(krusty.isPasswordValid(), is(true));
    assertThat(krusty.getLoginmail(), is(nullValue()));
    assertThat(krusty.getEmail(), is("krusty.theklown@silverpeas.org"));
  }

  private SPUser getTartempion() {
    PasswordEncryption encryption = PasswordEncryptionProvider.getDefaultPasswordEncryption();
    StringBuilder passwordBuilder = new StringBuilder(encryption.encrypt("tartempion"));
    for (; passwordBuilder.length() < PASSWORD_MAX_SIZE; ) {
      passwordBuilder.append("a");
    }

    SPUser tartempion = new SPUser();
    tartempion.setId(2000);
    tartempion.setFirstname("toto");
    tartempion.setLastname("tartempion");
    tartempion.setPosition("elder");
    tartempion.setPhone("047669084");
    tartempion.setAddress("18 rue des aiguinards");
    tartempion.setTitle("student");
    tartempion.setCompany("Tartempion's family");
    tartempion.setEmail("toto.tartempion@silverpeas.org");
    tartempion.setPasswordValid(true);
    tartempion.setLogin("tartempion");
    tartempion.setPassword(passwordBuilder.toString());
    return tartempion;
  }
}
