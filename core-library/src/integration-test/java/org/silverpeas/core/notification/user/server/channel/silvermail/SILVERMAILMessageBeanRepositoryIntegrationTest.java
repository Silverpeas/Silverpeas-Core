/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.notification.user.server.channel.silvermail;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class SILVERMAILMessageBeanRepositoryIntegrationTest {

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
        SILVERMAILMessageBeanRepositoryIntegrationTest.class)
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures()
        .testFocusedOn(war -> war.addClasses(SILVERMAILMessageBean.class,
            SILVERMAILMessageBeanRepository.class))
        .build();
  }

  @Test
  @Ignore
  public void emptyTest() {
  }

  @Test
  public void findMessagesByUserIdAndFolderId() {
    List<SILVERMAILMessageBean> msgs = repository.findMessageByUserIdAndFolderId("109", "0", -1);
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
  public void findMessagesByUserIdAndFolderIdAndByRead() {
    List<SILVERMAILMessageBean> msgs = repository.findMessageByUserIdAndFolderId("109", "0", 1);
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
  public void findMessagesByNonexisingUserIdAndFolderId() {
    List<SILVERMAILMessageBean> msgs = repository.findMessageByUserIdAndFolderId("42", "0", -1);
    assertThat(msgs.isEmpty(), is(true));
  }

  @Test
  public void findMessagesByUserIdAnNonExistingdFolderId() {
    List<SILVERMAILMessageBean> msgs = repository.findMessageByUserIdAndFolderId("109", "3", -1);
    assertThat(msgs.isEmpty(), is(true));
  }

  @Test
  public void persistANewMessage() {
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

    SILVERMAILMessageBean actual =
        SILVERMAILMessageBeanFinder.getById(Long.parseLong(expected.getId()));
    assertThat(actual, notNullValue());
    assertThat(actual, is(expected));
  }

}