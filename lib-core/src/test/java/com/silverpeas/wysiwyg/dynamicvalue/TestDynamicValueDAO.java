/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.wysiwyg.dynamicvalue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.silverpeas.wysiwyg.dynamicvalue.dao.DynamicValueDAO;
import com.silverpeas.wysiwyg.dynamicvalue.model.DynamicValue;
import com.silverpeas.wysiwyg.dynamicvalue.pool.ConnectionPoolFactory;

/**
 * class test for com.silverpeas.wysiwyg.dynamicvalue.dao.DynamicValueDAO
 */
public class TestDynamicValueDAO extends AbstractBaseDynamicValue {

  /**
   * Test method for
   * {@link com.silverpeas.wysiwyg.dynamicvalue.dao.DynamicValueDAO#getValidDynamicValue(java.sql.Connection, java.lang.String)}
   * .
   * @throws Exception
   */
  @Test
  public final void testGetValidDynamicValue() throws Exception {
    Connection connection = ConnectionPoolFactory.getConnection();
    DynamicValue value = DynamicValueDAO.getValidDynamicValue(connection, "java_version");
    assertEquals("jdk1.6.0_17", value.getValue());
  }

  /**
   * Test method for
   * {@link com.silverpeas.wysiwyg.dynamicvalue.dao.DynamicValueDAO#getAllValidDynamicValue(java.sql.Connection)}
   * .
   */
  @Test
  public final void testGetAllValidDynamicValue() throws SQLException {
    Connection connection = ConnectionPoolFactory.getConnection();
    List<DynamicValue> list = DynamicValueDAO.getAllValidDynamicValue(connection);
    assertEquals(2, list.size());
    int i = 0;
    for (DynamicValue dynamicValue : list) {
      if (i == 0) {
        assertEquals("jdk1.6.0_17", dynamicValue.getValue());
      }
      if (i == 1) {
        assertEquals("version 2.3", dynamicValue.getValue());
      }
      i++;
    }
  }

  /**
   * Test method for
   * {@link com.silverpeas.wysiwyg.dynamicvalue.dao.DynamicValueDAO#searchValidDynamicValue(java.sql.Connection, java.lang.String)}
   * .
   * @throws SQLException
   */
  @Test
  public final void testSearchValidDynamicValue() throws SQLException {
    Connection connection = ConnectionPoolFactory.getConnection();
    List<DynamicValue> list = DynamicValueDAO.searchValidDynamicValue(connection, "ja");
    assertEquals(list.size(), 1);
    for (DynamicValue dynamicValue : list) {

      assertEquals("jdk1.6.0_17", dynamicValue.getValue());

    }
  }

}
