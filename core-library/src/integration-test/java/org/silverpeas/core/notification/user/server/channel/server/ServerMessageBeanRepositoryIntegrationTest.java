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

package org.silverpeas.core.notification.user.server.channel.server;

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
public class ServerMessageBeanRepositoryIntegrationTest {

  private static final String TABLE_CREATION =
      "/org/silverpeas/core/notification/user/server/channel/server/create_table.sql";
  private static final Operation MESSAGES_SETUP = Operations.insertInto("ST_ServerMessage")
      .columns("id", "userId", "header", "subject", "body", "sessionId", "type")
      .values(0, 0, null, null, "Toto chez les papoos", "1234", null)
      .values(1, 0, null, null, "Lolo a des lolos", "1234", null)
      .values(2, 0, null, null, "Bidule a faim", "30", null)
      .build();
  private static final Operation UNIQUE_ID_SETUP = Operations.insertInto("UniqueId")
      .columns("maxId", "tableName")
      .values(2, "ST_ServerMessage")
      .build();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION)
      .loadInitialDataSetFrom(MESSAGES_SETUP, UNIQUE_ID_SETUP);

  @Inject
  private ServerMessageBeanRepository repository;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(ServerMessageBeanRepositoryIntegrationTest.class)
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures()
        .addClasses(ServerMessageBeanFinder.class)
        .testFocusedOn(
            war -> war.addClasses(ServerMessageBean.class, ServerMessageBeanRepository.class))
        .build();
  }

  @Test
  @Ignore
  public void emptyTest() {
  }

  @Test
  public void testFindFirstExistingMessageByUserIdAndSessionId() {
    ServerMessageBean bean = repository.findFirstMessageByUserIdAndSessionId("0", "1234");
    assertThat(bean, notNullValue());
    assertThat(bean.getId(), is("0"));
    assertThat(bean.getBody(), is("Toto chez les papoos"));
    assertThat(bean.getSessionId(), is("1234"));
  }

  @Test
  public void testFindFirstMessageByNonExistingUserIdAndSessionId() {
    ServerMessageBean bean = repository.findFirstMessageByUserIdAndSessionId("1", "1234");
    assertThat(bean, nullValue());
  }

  @Test
  public void testFindFirstMessageByUserIdAndNonExistingSessionId() {
    ServerMessageBean bean = repository.findFirstMessageByUserIdAndSessionId("0", "BCD");
    assertThat(bean, nullValue());
  }

  @Test
  public void testDeleteAllMessagesByUserIdAndSessionId() {
    Transaction.performInOne(() -> {
      repository.deleteAllMessagesByUserIdAndSessionId("0", "1234");
      return null;
    });

    long count = ServerMessageBeanFinder.count();
    assertThat(count, is(1L));
    List<ServerMessageBean> beans = ServerMessageBeanFinder.getSomeByQuery(
        "from ServerMessageBean where userId = 0 and sessionId = '1234'");
    assertThat(beans.isEmpty(), is(true));
  }

  @Test
  public void testDeleteAllMessagesByNonExistingUserIdAndSessionId() {
    Transaction.performInOne(() -> {
      repository.deleteAllMessagesByUserIdAndSessionId("8", "1234");
      return null;
    });

    long count = ServerMessageBeanFinder.count();
    assertThat(count, is(3L));
  }

  @Test
  public void testDeleteAllMessagesByUserIdAndNonExistingSessionId() {
    Transaction.performInOne(() -> {
      repository.deleteAllMessagesByUserIdAndSessionId("0", "BCD");
      return null;
    });

    long count = ServerMessageBeanFinder.count();
    assertThat(count, is(3L));
  }

  @Test
  public void persistANewMessage() {
    ServerMessageBean expected = new ServerMessageBean();
    expected.setUserId(42L);
    expected.setBody("Hello every body!");
    expected.setSessionId("AQWS");
    Transaction.performInOne(() -> repository.save(expected));

    ServerMessageBean actual = ServerMessageBeanFinder.getById(Long.parseLong(expected.getId()));
    assertThat(actual, is(expected));
  }
}