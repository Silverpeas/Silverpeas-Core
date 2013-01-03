/*
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
package org.silverpeas.token.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

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
import org.silverpeas.token.TokenStringKey;
import org.silverpeas.token.constant.TokenType;
import org.silverpeas.token.exception.TokenException;
import org.silverpeas.token.model.Token;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yohann Chastagnier
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-token.xml", "/spring-token-embedded-datasource.xml" })
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class TokenServiceTest {

  private static ReplacementDataSet dataSet;

  private final static String EXISTING_TOKEN = "existingToken";
  private final static String UNEXISTING_TOKEN = "unexistingToken";

  private final TestTokenKey dummyKey = new TestTokenKey("dummy");
  private final TestTokenKey existingKey = new TestTokenKey("38");
  private final TestTokenKey newKey = new TestTokenKey("26");

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    final FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet =
        new ReplacementDataSet(builder.build(TokenServiceTest.class
            .getResourceAsStream("token-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  @Inject
  private TokenService tokenService;

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
    Token token = tokenService.get(dummyKey);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.getType(), is(TokenType.UNKNOWN));

    // Testing from an existing key
    token = tokenService.get(existingKey);
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(24L));
    assertThat(token.getResourceId(), is("38"));
    assertThat(token.getSaveCount(), is(3));
    assertThat(token.getValue(), is("token24"));

    // Testing from an existing token string key
    token = tokenService.get(TokenStringKey.from(EXISTING_TOKEN));
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(6L));
    assertThat(token.getResourceId(), is("7"));
    assertThat(token.getSaveCount(), is(9));
    assertThat(token.getValue(), is(EXISTING_TOKEN));

    // Testing from an unexisting token string key
    token = tokenService.get(TokenStringKey.from(UNEXISTING_TOKEN));
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.getType(), is(TokenType.UNKNOWN));

    // Testing from an not valid token key
    token = tokenService.get(new TestTokenKeyNotValid());
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.getType(), is(TokenType.UNKNOWN));

    // Testing from an unknown token key
    token = tokenService.get(new TestTokenKeyUnknown());
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.getType(), is(TokenType.UNKNOWN));
  }

  @Test(expected = TokenException.class)
  public void testInitializeFromNotValidKey() throws Exception {
    tokenService.initialize(new TestTokenKeyNotValid());
  }

  @Test(expected = TokenException.class)
  public void testInitializeFromUnknownTokenType() throws Exception {
    tokenService.initialize(new TestTokenKeyUnknown());
  }

  @Test(expected = TokenException.class)
  public void testInitializeFromExistingToken() throws Exception {
    tokenService.initialize(TokenStringKey.from(EXISTING_TOKEN));
  }

  @Test(expected = TokenException.class)
  public void testInitializeFromUnexistingToken() throws Exception {
    tokenService.initialize(TokenStringKey.from(UNEXISTING_TOKEN));
  }

  @Test
  @Transactional
  public void initializeFromExistingKeyIntoTransaction() throws Exception {

    // Verifying token before initializing
    final Token token = tokenService.get(existingKey);
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(24L));
    assertThat(token.getResourceId(), is("38"));
    assertThat(token.getSaveCount(), is(3));
    assertThat(token.getValue(), is("token24"));

    // Initializing
    final String toString = ((Object) token).toString();
    final Date date = token.getSaveDate();
    tokenService.initialize(existingKey);

    // Verifying token after initializing
    assertThat(token, notNullValue());
    assertThat(toString, is(((Object) token).toString()));
    assertThat(token.getId(), is(24L));
    assertThat(token.getResourceId(), is("38"));
    assertThat(token.getSaveCount(), is(3));
    assertThat(token.getValue(), not(is("token24")));
    assertThat(token.getValue().length(), greaterThanOrEqualTo(10));
    assertThat(token.getSaveDate().getTime(), is(date.getTime()));
  }

  @Test
  public void initializeFromExistingKey() throws Exception {

    // Verifying token before initializing
    Token token = tokenService.get(existingKey);
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(24L));
    assertThat(token.getResourceId(), is("38"));
    assertThat(token.getSaveCount(), is(3));
    assertThat(token.getValue(), is("token24"));

    // Initializing
    final String toString = ((Object) token).toString();
    final Date date = token.getSaveDate();
    token = tokenService.initialize(existingKey);

    // Verifying token after initializing
    assertThat(token, notNullValue());
    assertThat(toString, not(is(((Object) token).toString())));
    assertThat(token.getId(), is(24L));
    assertThat(token.getResourceId(), is("38"));
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
    Token token = tokenService.get(newKey);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
    assertThat(token.getType(), is(TokenType.UNKNOWN));

    // Initializing
    final String toString = ((Object) token).toString();
    final Date date = new Date();
    token = tokenService.initialize(newKey);

    // Verifying token after initializing
    assertThat(token, notNullValue());
    assertThat(toString, not(is(((Object) token).toString())));
    assertThat(token.getId(), is(25L));
    assertThat(token.getResourceId(), is("26"));
    assertThat(token.getSaveCount(), is(1));
    assertThat(token.getValue().length(), greaterThanOrEqualTo(10));
    assertThat(token.getSaveDate().getTime(), greaterThanOrEqualTo(date.getTime()));
  }

  @Test(expected = TokenException.class)
  public void testGetInitializedFromNotValidKey() throws Exception {
    tokenService.getInitialized(new TestTokenKeyNotValid());
  }

  @Test(expected = TokenException.class)
  public void testGetInitializedFromUnknownTokenType() throws Exception {
    tokenService.getInitialized(new TestTokenKeyUnknown());
  }

  @Test(expected = TokenException.class)
  public void testGetInitializedFromUnexistingToken() throws Exception {
    tokenService.getInitialized(TokenStringKey.from(UNEXISTING_TOKEN));
  }

  @Test
  public void testGetInitializedFromExistingToken() throws Exception {
    final Date date = new Date();
    final Token token = tokenService.getInitialized(TokenStringKey.from(EXISTING_TOKEN));

    // No initialization
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(6L));
    assertThat(token.getResourceId(), is("7"));
    assertThat(token.getSaveCount(), is(9));
    assertThat(token.getValue(), is(EXISTING_TOKEN));
    assertThat(token.getSaveDate().getTime(), lessThan(date.getTime()));
  }

  @Test
  public void testGetInitializedFromExistingKey() throws Exception {
    final Date date = new Date();
    final Token token = tokenService.getInitialized(existingKey);

    // No initialization
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(24L));
    assertThat(token.getResourceId(), is("38"));
    assertThat(token.getSaveCount(), is(3));
    assertThat(token.getValue(), is("token24"));
    assertThat(token.getSaveDate().getTime(), lessThan(date.getTime()));
  }

  @Test
  public void testGetInitializedFromNewKey() throws Exception {
    final Date date = new Date();
    final Token token = tokenService.getInitialized(newKey);

    // Initialization
    assertThat(token, notNullValue());
    assertThat(token.getId(), is(25L));
    assertThat(token.getResourceId(), is("26"));
    assertThat(token.getSaveCount(), is(1));
    assertThat(token.getValue().length(), greaterThanOrEqualTo(10));
    assertThat(token.getSaveDate().getTime(), greaterThanOrEqualTo(date.getTime()));
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
    Token token = tokenService.get(existingKey);
    assertThat(token, notNullValue());
    tokenService.remove(existingKey);
    token = tokenService.get(existingKey);
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));

    // Removing from an existing token string key
    token = tokenService.get(TokenStringKey.from(EXISTING_TOKEN));
    assertThat(token, notNullValue());
    tokenService.remove(TokenStringKey.from(EXISTING_TOKEN));
    token = tokenService.get(TokenStringKey.from(EXISTING_TOKEN));
    assertThat(token, notNullValue());
    assertThat(token.exists(), is(false));
  }
}
