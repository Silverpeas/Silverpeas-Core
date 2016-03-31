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

package org.silverpeas.core.notification.user.server.channel.popup;

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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class POPUPMessageBeanRepositoryIntegrationTest {

  private static final String TABLE_CREATION =
      "/org/silverpeas/core/notification/user/server/channel/popup/create_table.sql";
  private static final Operation MESSAGES_SETUP = Operations.insertInto("ST_PopupMessage")
      .columns("id", "userId", "body", "senderId", "senderName", "answerAllowed", "source", "url",
          "msgDate", "msgTime")
      .values(0, 0, "Toto chez les papoos", "42", "Toto", "0", "src", "url", "2014/10/31", "10:30")
      .values(1, 0, "Bidule a faim", "30", "Tartempion", "0", null, null, "2014/11/01", "09:30")
      .build();
  private static final Operation UNIQUE_ID_SETUP = Operations.insertInto("UniqueId")
      .columns("maxId", "tableName")
      .values(1, "ST_PopupMessage")
      .build();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION)
      .loadInitialDataSetFrom(MESSAGES_SETUP, UNIQUE_ID_SETUP);

  @Inject
  private POPUPMessageBeanRepository repository;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(POPUPMessageBeanRepositoryIntegrationTest.class)
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures()
        .addClasses(POPUPMessageBeanFinder.class)
        .testFocusedOn(
            war -> war.addClasses(POPUPMessageBean.class, POPUPMessageBeanRepository.class))
        .build();
  }

  @Test
  @Ignore
  public void emptyTest() {
  }

  @Test
  public void findFirstExistingMessageByUserId() {
    POPUPMessageBean bean = repository.findFirstMessageByUserId("0");
    assertThat(bean, notNullValue());
    assertThat(bean.getUserId(), is(0L));
    assertThat(bean.getBody(), is("Toto chez les papoos"));
    assertThat(bean.getSenderId(), is("42"));
    assertThat(bean.getSenderName(), is("Toto"));
    assertThat(bean.isAnswerAllowed(), is(false));
    assertThat(bean.getSource(), is("src"));
    assertThat(bean.getUrl(), is("url"));
    assertThat(bean.getMsgDate(), is("2014/10/31"));
    assertThat(bean.getMsgTime(), is("10:30"));
  }

  @Test
  public void findFirstNonExistingMessageByUserId() {
    POPUPMessageBean bean = repository.findFirstMessageByUserId("1");
    assertThat(bean, nullValue());
  }

  @Test
  public void deleteExistingMessagesByUserIdAndSenderId() {
    Transaction.performInOne(() -> {
      repository.deleteMessagesByUserIdAndSenderId("0", "42");
      return null;
    });

    List<POPUPMessageBean> actuals =
        POPUPMessageBeanFinder.getSomeByQuery("SELECT m FROM POPUPMessageBean m");
    assertThat(actuals.size(), is(1));
    assertThat(actuals.get(0).getId(), is("1"));
  }

  @Test
  public void deleteMessagesByNonExistingUserIdAndSenderId() {
    Transaction.performInOne(() -> {
      repository.deleteMessagesByUserIdAndSenderId("1", "42");
      return null;
    });

    long count = POPUPMessageBeanFinder.count();
    assertThat(count, is(2L));
  }

  @Test
  public void deleteMessagesByUserIdAndNonExistingSenderId() {
    Transaction.performInOne(() -> {
      repository.deleteMessagesByUserIdAndSenderId("0", "22");
      return null;
    });

    long count = POPUPMessageBeanFinder.count();
    assertThat(count, is(2L));
  }

  @Test
  public void persistANewMessage() {
    POPUPMessageBean expected = new POPUPMessageBean();
    expected.setUserId(42L);
    expected.setSenderId("0");
    expected.setSenderName("Administrateur");
    expected.setBody("The body of the message");
    expected.setAnswerAllowed(false);
    expected.setSource("source");
    expected.setUrl("urlLink");
    expected.setMsgDate("2014/11/02");
    expected.setMsgTime("11:10");
    Transaction.performInOne(() -> repository.save(expected));

    POPUPMessageBean actual = POPUPMessageBeanFinder.getById(Long.valueOf(expected.getId()));
    assertThat(actual, notNullValue());
    assertThat(actual, is(expected));
  }

}