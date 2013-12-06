/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.token.persistent.service;

import java.util.Date;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.EntityReference;
import org.silverpeas.token.exception.TokenException;
import org.silverpeas.token.persistent.MyEntityReference;
import org.silverpeas.token.persistent.PersistentResourceToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-token.xml", "/spring-token-embedded-datasource.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class TokenServiceTest {

  private static ReplacementDataSet dataSet;

  private final static String EXISTING_TOKEN = "existingToken";
  private final static String UNEXISTING_TOKEN = "unexistingToken";

  private final EntityReference dummyRef = new MyEntityReference("dummy");
  private final EntityReference existingRef = new MyEntityReference("38");
  private final EntityReference newRef = new MyEntityReference("26");

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    final FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(TokenServiceTest.class
        .getResourceAsStream("token-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  @Inject
  private PersistentResourceTokenService tokenService;

  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

  @Before
  public void generalSetUp() throws Exception {
    final IDatabaseConnection myConnection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(myConnection, dataSet);
  }

  @Test
  public void testGet() throws TokenException {

    // Testing from a dummy key (doesn't exist in database)
    PersistentResourceToken token = tokenService.get(dummyRef);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.notExists(), is(true));
    assertThat(token.getResource(MyEntityReference.class), nullValue());

    // Testing from an existing key
    token = tokenService.get(existingRef);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(true));
    assertThat(token.notExists(), is(false));
    assertThat(token.getId(), is(24L));
    assertThat(token.getResource(MyEntityReference.class), is(existingRef));
    assertThat(token.getResource(MyUnknownEntityReference.class), nullValue());
    assertThat(token.getSaveCount(), is(3));
    assertThat(token.getValue(), is("token24"));

    // Testing from an existing token string key
    token = tokenService.get(EXISTING_TOKEN);
    EntityReference expectedResource = new MyEntityReference("7");
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(true));
    assertThat(token.notExists(), is(false));
    assertThat(token.getId(), is(6L));
    assertThat(token.getResource(MyEntityReference.class), is(expectedResource));
    assertThat(token.getResource(MyUnknownEntityReference.class), nullValue());
    assertThat(token.getSaveCount(), is(9));
    assertThat(token.getValue(), is(EXISTING_TOKEN));

    // Testing from an unexisting token string key
    token = tokenService.get(UNEXISTING_TOKEN);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.notExists(), is(true));
    assertThat(token.getResource(MyEntityReference.class), nullValue());

    // Testing from an not valid token key
    token = tokenService.get(new MyEntityReference((null)));
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.notExists(), is(true));
    assertThat(token.getResource(MyEntityReference.class), nullValue());

    // Testing from an unknown token key
    token = tokenService.get(new MyUnknownEntityReference(EXISTING_TOKEN));
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.notExists(), is(true));
    assertThat(token.getResource(MyUnknownEntityReference.class), nullValue());
    assertThat(token.getResource(MyEntityReference.class), nullValue());
  }

  @Test(expected = TokenException.class)
  public void testInitializeFromNotValidKey() throws Exception {
    tokenService.initialize(new MyEntityReference(null));
  }

  @Test(expected = NullPointerException.class)
  public void testInitializeFromNull() throws Exception {
    tokenService.initialize(null);
  }

  @Test
  @Transactional
  public void initializeFromExistingKeyIntoTransaction() throws Exception {

    // Verifying token before initializing
    final PersistentResourceToken token = tokenService.get(existingRef);
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(24L));
    assertThat(token.getResource(MyEntityReference.class), is(existingRef));
    assertThat(token.getSaveCount(), is(3));
    assertThat(token.getValue(), is("token24"));

    // Initializing
    final Date date = token.getSaveDate();
    tokenService.initialize(existingRef);

    // Verifying token after initializing
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(24L));
    assertThat(token.getResource(MyEntityReference.class), is(existingRef));
    assertThat(token.getSaveCount(), is(3));
    assertThat(token.getValue(), not(is("token24")));
    assertThat(token.getValue().length(), greaterThanOrEqualTo(10));
    assertThat(token.getSaveDate().getTime(), is(date.getTime()));
  }

  @Test
  public void initializeFromExistingKey() throws Exception {

    // Verifying token before initializing
    PersistentResourceToken token = tokenService.get(existingRef);
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(24L));
    assertThat(token.getResource(MyEntityReference.class), is(existingRef));
    assertThat(token.getSaveCount(), is(3));
    assertThat(token.getValue(), is("token24"));

    // Initializing
    final Date date = token.getSaveDate();
    token = tokenService.initialize(existingRef);

    // Verifying token after initializing
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(24L));
    assertThat(token.getResource(MyEntityReference.class), is(existingRef));
    assertThat(token.getSaveCount(), is(4));
    assertThat(token.getValue(), not(is("token24")));
    assertThat(token.getValue().length(), greaterThanOrEqualTo(10));
    assertThat(token.getSaveDate().getTime(), greaterThanOrEqualTo(date.getTime()));
  }

  @Test
  @Transactional
  public void testInitializeFromNewKeyIntoTransaction() throws Exception {
    newToken();
  }

  @Test
  public void testInitializeFromNewKey() throws Exception {
    newToken();
  }

  /**
   * Centralization
   */
  private void newToken() throws Exception {

    // Verifying token before initializing
    PersistentResourceToken token = tokenService.get(newRef);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.notExists(), is(true));
    assertThat(token.getResource(MyEntityReference.class), nullValue());

    // Initializing
    final Date date = new Date();
    token = tokenService.initialize(newRef);

    // Verifying token after initializing
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(25L));
    assertThat(token.getResource(MyEntityReference.class), is(newRef));
    assertThat(token.getSaveCount(), is(1));
    assertThat(token.getValue().length(), greaterThanOrEqualTo(10));
    assertThat(token.getSaveDate().getTime(), greaterThanOrEqualTo(date.getTime()));
  }

  @Test
  public void testGetTokenFromUnknownTokenValue() throws Exception {
    PersistentResourceToken token = tokenService.get(UNEXISTING_TOKEN);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.notExists(), is(true));
    assertThat(token.getResource(MyEntityReference.class), nullValue());
  }

  @Test
  public void testGetTokenFromKnownTokenValue() throws Exception {
    final Date expectedDate = new Date();
    PersistentResourceToken token = tokenService.get(EXISTING_TOKEN);
    EntityReference expectedResource = new MyEntityReference("7");
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(true));
    assertThat(token.notExists(), is(false));
    assertThat(token.getId(), is(6L));
    assertThat(token.getResource(MyEntityReference.class), is(expectedResource));
    assertThat(token.getSaveCount(), is(9));
    assertThat(token.getValue(), is(EXISTING_TOKEN));
    assertThat(token.getSaveDate().getTime(), lessThan(expectedDate.getTime()));
  }

  @Test
  public void testGetTokenFromExistingResourceRef() throws Exception {
    final Date date = new Date();
    final PersistentResourceToken token = tokenService.get(existingRef);
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(24L));
    assertThat(token.getResource(MyEntityReference.class), is(existingRef));
    assertThat(token.getSaveCount(), is(3));
    assertThat(token.getValue(), is("token24"));
    assertThat(token.getSaveDate().getTime(), lessThan(date.getTime()));
  }

  @Test
  public void testGetTokenFromNewResourceRef() throws Exception {
    final Date date = new Date();
    PersistentResourceToken token = tokenService.get(newRef);
    assertThat(token, notNullValue());
    assertThat(token.notExists(), is(true));

    token = tokenService.initialize(newRef);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(true));
    assertThat(token.getId(), is(25L));
    assertThat(token.getResource(MyEntityReference.class), is(newRef));
    assertThat(token.getSaveCount(), is(1));
    assertThat(token.getValue().length(), greaterThanOrEqualTo(10));
    assertThat(token.getSaveDate().getTime(), greaterThanOrEqualTo(date.getTime()));
  }

  @Test
  public void testGetTokenFromUnexistingResourceRef() throws Exception {
    final PersistentResourceToken token = tokenService.get(dummyRef);

    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.notExists(), is(true));
    assertThat(token.getResource(MyEntityReference.class), nullValue());
  }

  @Test
  @Transactional
  public void testRemoveIntoTransaction() throws TokenException {
    removeToken();
  }

  @Test
  public void testRemove() throws TokenException {
    removeToken();
  }

  /**
   * Centralization
   */
  private void removeToken() {

    // Removing from an existing key
    PersistentResourceToken token = tokenService.get(existingRef);
    assertThat(token, notNullValue());
    tokenService.remove(existingRef);
    token = tokenService.get(existingRef);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.notExists(), is(true));

    // Removing from an existing token string key
    token = tokenService.get(EXISTING_TOKEN);
    assertThat(token, notNullValue());
    tokenService.remove(token.getResource(MyEntityReference.class));
    token = tokenService.get(EXISTING_TOKEN);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.notExists(), is(true));
  }
}
