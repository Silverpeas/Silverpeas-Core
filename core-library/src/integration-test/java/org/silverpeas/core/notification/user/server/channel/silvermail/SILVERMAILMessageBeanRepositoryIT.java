/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.notification.user.server.channel.silvermail;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.util.SQLRequester;

import javax.inject.Inject;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class SILVERMAILMessageBeanRepositoryIT {

  private static final String TABLE_CREATION =
      "/org/silverpeas/core/notification/user/server/channel/silvermail/create_table.sql";
  private static final Operation MESSAGES_SETUP = Operations.insertInto("ST_SilverMailMessage")
      .columns("id", "userId", "folderId", "header", "senderName", "subject", "body", "source",
          "url", "dateMsg", "readen")
      .values(314, 109, 0, null, "Toto Loufoc", "Abonnement", "1082",
          "Espace Commerciale - Documentation Commerciale",
          "http://localhost:8000/silverpeas/Publication/379", "2014/05/27", 0)
      .values(315, 109, 0, null, "Administrateur", "Abonnement", "1086",
          "Espace Commerciale - Documentation Commerciale",
          "http://localhost:8000/silverpeas/Publication/385", "2014/05/27", 1)
      .values(322, 0, 0, null, "Toto Loufoc", "Validation d'une idée", "1139",
          "Espace collaboratif - Boite à idées",
          "http://localhost:8000/silverpeas/autoRedirect.jsp?domainId=0&goto=bidule", "2014/04/06",
          1)
      .values(360, 26, 0, null, "Toto Loufoc", "Validation d'une idée", "1139",
          "Espace collaboratif - Boite à idées",
          "http://localhost:8000/silverpeas/autoRedirect.jsp?domainId=0&goto=bidule", "2014/04/06",
          0)
      .build();
  private static final Operation UNIQUE_ID_SETUP = Operations.insertInto("UniqueId")
      .columns("maxId", "tableName")
      .values(2, "ST_ServerMessage")
      .build();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION)
      .loadInitialDataSetFrom(MESSAGES_SETUP, UNIQUE_ID_SETUP);

  @Inject
  private SILVERMAILMessageBeanRepository repository;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(
        SILVERMAILMessageBeanRepositoryIT.class)
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures()
        .testFocusedOn(war -> war.addClasses(SILVERMAILMessageBean.class,
            SILVERMAILMessageBeanRepository.class, SilvermailCriteria.class,
            SilvermailCriteriaProcessor.class, JPQLQueryBuilder.class))
        .build();
  }

  @Before
  public void verifyData() throws Exception {
    assertThat(getUserNotificationTableLines(), hasSize(4));
    assertThat(getUserNotificationReadTableLines(), hasSize(2));
  }

  @Test
  @Ignore
  public void emptyTest() {
  }

  @Test
  public void findMessagesByUserIdAndFolderId() {
    SilvermailCriteria criteria = SilvermailCriteria.get();
    criteria.aboutUser("109").into("0");
    List<SILVERMAILMessageBean> msgs = repository.findByCriteria(criteria);
    assertThat(msgs.size(), is(2));

    assertThat(msgs.get(0).getId(), is("315"));
    assertThat(msgs.get(0).getUserId(), is(109L));
    assertThat(msgs.get(0).getFolderId(), is(0L));
    assertThat(msgs.get(0).getSenderName(), is("Administrateur"));
    assertThat(msgs.get(0).getSubject(), is("Abonnement"));
    assertThat(msgs.get(0).getBody(), is("1086"));
    assertThat(msgs.get(0).getSource(), is("Espace Commerciale - Documentation Commerciale"));
    assertThat(msgs.get(0).getUrl(), is("http://localhost:8000/silverpeas/Publication/385"));
    assertThat(msgs.get(0).getDateMsg(), is("2014/05/27"));
    assertThat(msgs.get(0).getReaden(), is(1));

    assertThat(msgs.get(1).getId(), is("314"));
    assertThat(msgs.get(1).getUserId(), is(109L));
    assertThat(msgs.get(1).getFolderId(), is(0L));
    assertThat(msgs.get(1).getSenderName(), is("Toto Loufoc"));
    assertThat(msgs.get(1).getSubject(), is("Abonnement"));
    assertThat(msgs.get(1).getBody(), is("1082"));
    assertThat(msgs.get(1).getSource(), is("Espace Commerciale - Documentation Commerciale"));
    assertThat(msgs.get(1).getUrl(), is("http://localhost:8000/silverpeas/Publication/379"));
    assertThat(msgs.get(1).getDateMsg(), is("2014/05/27"));
    assertThat(msgs.get(1).getReaden(), is(0));
  }

  @Test
  public void countMessagesByUserIdAndFolderId() {
    SilvermailCriteria criteria = SilvermailCriteria.get();
    criteria.aboutUser("109").into("0");
    long count = repository.countByCriteria(criteria);
    assertThat(count, is(2L));
  }

  @Test
  public void findMessagesByUserIdAndFolderIdAndByRead() {
    SilvermailCriteria criteria = SilvermailCriteria.get();
    criteria.aboutUser("109").into("0").read();
    List<SILVERMAILMessageBean> msgs = repository.findByCriteria(criteria);
    assertThat(msgs.size(), is(1));

    assertThat(msgs.get(0).getId(), is("315"));
    assertThat(msgs.get(0).getUserId(), is(109L));
    assertThat(msgs.get(0).getFolderId(), is(0L));
    assertThat(msgs.get(0).getSenderName(), is("Administrateur"));
    assertThat(msgs.get(0).getSubject(), is("Abonnement"));
    assertThat(msgs.get(0).getBody(), is("1086"));
    assertThat(msgs.get(0).getSource(), is("Espace Commerciale - Documentation Commerciale"));
    assertThat(msgs.get(0).getUrl(), is("http://localhost:8000/silverpeas/Publication/385"));
    assertThat(msgs.get(0).getDateMsg(), is("2014/05/27"));
    assertThat(msgs.get(0).getReaden(), is(1));
  }

  @Test
  public void countMessagesByUserIdAndFolderIdAndByRead() {
    SilvermailCriteria criteria = SilvermailCriteria.get();
    criteria.aboutUser("109").into("0").read();
    long count = repository.countByCriteria(criteria);
    assertThat(count, is(1L));
  }

  @Test
  public void findMessagesByNonExistingUserIdAndFolderId() {
    SilvermailCriteria criteria = SilvermailCriteria.get();
    criteria.aboutUser("42").into("0");
    List<SILVERMAILMessageBean> msgs = repository.findByCriteria(criteria);
    assertThat(msgs.isEmpty(), is(true));
  }


  @Test
  public void countMessagesByNonExistingUserIdAndFolderId() {
    SilvermailCriteria criteria = SilvermailCriteria.get();
    criteria.aboutUser("42").into("0");
    long count = repository.countByCriteria(criteria);
    assertThat(count, is(0L));
  }

  @Test
  public void findMessagesByUserIdAnNonExistingFolderId() {
    SilvermailCriteria criteria = SilvermailCriteria.get();
    criteria.aboutUser("109").into("3");
    List<SILVERMAILMessageBean> msgs = repository.findByCriteria(criteria);
    assertThat(msgs.isEmpty(), is(true));
  }

  @Test
  public void countMessagesByUserIdAnNonExistingFolderId() {
    SilvermailCriteria criteria = SilvermailCriteria.get();
    criteria.aboutUser("109").into("3");
    long count = repository.countByCriteria(criteria);
    assertThat(count, is(0L));
  }

  @Test
  public void persistANewMessage() throws Exception {
    SILVERMAILMessageBean expected = new SILVERMAILMessageBean();
    expected.setUserId(100);
    expected.setSenderName("Boloo");
    expected.setFolderId(0);
    expected.setSubject("Abonnement");
    expected.setBody("13452");
    expected.setUrl("http://localhost:8000/silverpeas/Publication/1023");
    expected.setSource("Espace Commerciale - Documentation Commerciale");
    expected.setDateMsg("2014/11/02");
    expected.setReaden(0);
    Transaction.performInOne(() -> repository.save(expected));

    assertThat(getUserNotificationTableLines(), hasSize(5));
    SILVERMAILMessageBean actual =
        SILVERMAILMessageBeanFinder.getById(Long.parseLong(expected.getId()));
    assertThat(actual, notNullValue());
    assertThat(actual, is(expected));
  }

  @Test
  public void markAsReadAllMessagesByUserIdAndFolderId() throws Exception {
    long nbUpdated = Transaction
        .performInOne(() -> repository.markAsReadAllMessagesByUserIdAndFolderId("109", "0"));
    assertThat(nbUpdated, is(1L));
    assertThat(getUserNotificationReadTableLines(), hasSize(3));
  }

  @Test
  public void markAsReadMessagesByUserIdAndById() throws Exception {
    long nbUpdated = Transaction.performInOne(() -> repository
        .markAsReadMessagesByUserIdAndByIds("109", asList("314", "315", "322", "360")));
    assertThat(nbUpdated, is(1L));
    assertThat(getUserNotificationReadTableLines(), hasSize(3));
  }

  @Test
  public void markAsReadMessagesByUserIdAndByIdButNoIdGiven() throws Exception {
    long nbUpdated = Transaction.performInOne(() -> repository
        .markAsReadMessagesByUserIdAndByIds("109", emptyList()));
    assertThat(nbUpdated, is(0L));
    assertThat(getUserNotificationReadTableLines(), hasSize(2));
  }

  @Test
  public void deleteAllMessagesByUserIdAndFolderId() throws Exception {
    long nbDeleted = Transaction
        .performInOne(() -> repository.deleteAllMessagesByUserIdAndFolderId("109", "0"));
    assertThat(nbDeleted, is(2L));
    assertThat(getUserNotificationTableLines(), hasSize(2));
    assertThat(getUserNotificationReadTableLines(), hasSize(1));
  }

  @Test
  public void deleteMessagesByUserIdAndById() throws Exception {
    long nbDeleted = Transaction.performInOne(
        () -> repository.deleteMessagesByUserIdAndByIds("109", asList("315", "322", "360")));
    assertThat(nbDeleted, is(1L));
    assertThat(getUserNotificationTableLines(), hasSize(3));
  }

  @Test
  public void deleteMessagesByUserIdAndByIdButNoIdGiven() throws Exception {
    long nbDeleted = Transaction.performInOne(
        () -> repository.deleteMessagesByUserIdAndByIds("109", emptyList()));
    assertThat(nbDeleted, is(0L));
    assertThat(getUserNotificationTableLines(), hasSize(4));
  }

  private List<SQLRequester.ResultLine> getUserNotificationTableLines() throws Exception {
    return dbSetupRule.mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.createSelect("* from ST_SilverMailMessage"));
  }

  private List<SQLRequester.ResultLine> getUserNotificationReadTableLines() throws Exception {
    return dbSetupRule.mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.createSelect("* from ST_SilverMailMessage where readen = 1"));
  }
}