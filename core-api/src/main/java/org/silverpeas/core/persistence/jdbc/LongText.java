/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.persistence.jdbc;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.Mutable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.silverpeas.core.persistence.jdbc.DBUtil.openConnection;

public class LongText {
  private static final int PART_SIZE_MAX = 1998;
  private static final String INSERT_LONG_TEXT =
      "insert into ST_LongText (id, orderNum, bodyContent) values (?, ?, ?)";

  /**
   * Hidden constructor.
   */
  private LongText() {
  }

  public static int addLongText(String theText) throws SQLException {
    int theId = DBUtil.getNextId("ST_LongText", "id");
    int orderNum = 0;

    try (Connection connection = openConnection();
         PreparedStatement stmt = connection.prepareStatement(INSERT_LONG_TEXT)) {
      if ((theText == null) || (theText.length() <= 0)) {
        int i = 1;
        stmt.setInt(i++, theId);
        stmt.setInt(i++, orderNum);
        stmt.setString(i, "");
        stmt.executeUpdate();
      } else {
        while (orderNum * PART_SIZE_MAX < theText.length()) {
          final String partText;
          if ((orderNum + 1) * PART_SIZE_MAX < theText.length()) {
            partText = theText.substring(orderNum * PART_SIZE_MAX, (orderNum + 1) * PART_SIZE_MAX);
          } else {
            partText = theText.substring(orderNum * PART_SIZE_MAX);
          }
          int i = 1;
          stmt.setInt(i++, theId);
          stmt.setInt(i++, orderNum);
          stmt.setString(i, partText);
          stmt.executeUpdate();
          orderNum++;
        }
      }
    }
    return theId;
  }

  public static String getLongText(int longTextId) {
    try {
      final StringBuilder content = new StringBuilder();
      JdbcSqlQuery.createSelect("bodyContent from ST_LongText")
          .where("id = ?", longTextId)
          .addSqlPart("order by orderNum")
          .executeUnique(row -> {
            content.append(row.getString(1));
            return null;
          });
      return content.toString();
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Lists long texts which are indexed by their ids.
   * @param longTextIds the long text ids.
   * @return the long texts indexed by their ids.
   */
  public static Map<Integer, String> listLongTexts(Collection<Integer> longTextIds) {
    try {
      final int idIndex = 1;
      final int contentIndex = idIndex + 1;
      final Mutable<Integer> previousId = Mutable.empty();
      final StringBuilder content = new StringBuilder();
      final Map<Integer, String> result = new HashMap<>();
      JdbcSqlQuery.createSelect("id, bodyContent from ST_LongText")
          .where("id").in(longTextIds)
          .addSqlPart("order by id, orderNum")
          .execute(row -> {
            final int id = row.getInt(idIndex);
            previousId.filter(i-> i != id).ifPresent(i -> {
              result.put(i, content.toString());
              content.setLength(0);
            });
            previousId.set(id);
            content.append(row.getString(contentIndex));
            return null;
          });
      previousId.ifPresent(i -> result.put(i, content.toString()));
      return result;
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  public static void removeLongText(int longTextId) {
    try {
      JdbcSqlQuery.createDeleteFor("ST_LongText").where("id = ?", longTextId).execute();
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}
