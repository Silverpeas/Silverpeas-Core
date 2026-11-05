/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.comment.dao.jdbc;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class RichCommentPersistenceTest {
  private final JDBCCommentRequester requester = new JDBCCommentRequester();

  @Test
  void newSchemaPersistsRichAndPlainFormsAndLegacyUpdatesClearTheRichSource() throws Exception {
    try (var connection = database(); var ids = mockStatic(DBUtil.class); var users = mockStatic(User.class)) {
      execute(connection, "004/create_table.sql");
      var author = mock(User.class);
      when(author.getId()).thenReturn("2");
      users.when(User::getCurrentRequester).thenReturn(author);
      ids.when(() -> DBUtil.getNextId("sb_comment_comment", "commentId")).thenReturn(1);
      var comment = new Comment(new CommentId("kmelia1", "0"), "2", "Publication",
          new ResourceReference("16", "kmelia1"), new Date());
      comment.setRichText("**Hello** @[Élodie](3) 🚀");
      var saved = requester.saveComment(connection, comment);
      assertEquals(comment.getRichText(), saved.getRichText());
      assertEquals("Hello @Élodie 🚀", saved.getMessage());
      requester.moveComments(connection, "Publication", new ResourceReference("16", "kmelia1"),
          "Publication", new ResourceReference("17", "kmelia2"));
      var moved = requester.getComment(connection, new CommentId("kmelia2", "1"));
      assertEquals(saved.getRichText(), moved.getRichText());
      assertEquals("17", moved.getResourceReference().getLocalId());
      moved.setMessage("**literal legacy update**");
      requester.updateComment(connection, moved);
      var plain = requester.getComment(connection, moved.getIdentifier());
      assertNull(plain.getRichText());
      assertEquals("**literal legacy update**", plain.getMessage());
    }
  }

  @Test
  void upgradingThePreviousSchemaPreservesHistoricalTextWithoutGuessingItsFormat() throws Exception {
    try (var connection = database()) {
      execute(connection, "003/create_table.sql");
      try (var sql = connection.createStatement()) {
        sql.executeUpdate("INSERT INTO SB_Comment_Comment VALUES (1,2,'2026/11/05',NULL,'**historical**','kmelia1','Publication','16')");
      }
      execute(connection, "up003/alter_table.sql");
      var comment = requester.getComment(connection, new CommentId("kmelia1", "1"));
      assertEquals("**historical**", comment.getMessage());
      assertNull(comment.getRichText());
      comment.setRichText("**Updated**");
      requester.updateComment(connection, comment);
      assertEquals("Updated", requester.getComment(connection, comment.getIdentifier()).getMessage());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void postgresqlUpgradeIsRepeatableAndPreservesAManuallyAddedColumn(boolean alreadyExists) throws Exception {
    try (var connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL");
         var statement = connection.createStatement()) {
      execute(connection, "003/create_table.sql");
      statement.executeUpdate("INSERT INTO SB_Comment_Comment VALUES (1,2,'2026/11/05',NULL,'Historical','kmelia1','Publication','16')");
      if (alreadyExists) {
        statement.execute("ALTER TABLE SB_Comment_Comment ADD commentRichText TEXT");
        statement.executeUpdate("UPDATE SB_Comment_Comment SET commentRichText='**Preserved**'");
      }
      try (var input = getClass().getResourceAsStream("/migrations/db/postgresql/comment/up003/alter_table.sql")) {
        assertNotNull(input);
        var script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        statement.execute(script);
        statement.execute(script);
      }
      try (var rows = statement.executeQuery("SELECT commentComment, commentRichText FROM SB_Comment_Comment")) {
        assertTrue(rows.next());
        assertEquals("Historical", rows.getString(1));
        assertEquals(alreadyExists ? "**Preserved**" : null, rows.getString(2));
      }
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"h2", "mssql", "oracle", "postgresql"})
  void upgradeScriptsExistAtThePathsResolvedByTheInstaller(String database) throws Exception {
    var factory = DocumentBuilderFactory.newDefaultInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    try (var descriptor = getClass().getResourceAsStream("/migrations/modules/comment-migration.xml")) {
      assertNotNull(descriptor);
      var upgrades = factory.newDocumentBuilder().parse(descriptor).getElementsByTagName("upgrade");
      for (int index = 0; index < upgrades.getLength(); index++) {
        var upgrade = (Element) upgrades.item(index);
        var scripts = upgrade.getElementsByTagName("script");
        for (int scriptIndex = 0; scriptIndex < scripts.getLength(); scriptIndex++) {
          var script = (Element) scripts.item(scriptIndex);
          var path = "/migrations/db/" + database + "/comment/up"
              + upgrade.getAttribute("fromVersion") + "/" + script.getAttribute("name");
          try (var input = getClass().getResourceAsStream(path)) {
            assertNotNull(input, "Installer cannot resolve " + path);
          }
        }
      }
    }
  }

  private Connection database() throws Exception {
    return DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
  }

  private void execute(Connection connection, String script) throws Exception {
    try (var input = getClass().getResourceAsStream("/migrations/comment/" + script);
         var statement = connection.createStatement()) {
      assertNotNull(input);
      for (var sql : new String(input.readAllBytes(), StandardCharsets.UTF_8).split(";")) {
        if (!sql.isBlank()) { statement.execute(sql); }
      }
    }
  }
}
