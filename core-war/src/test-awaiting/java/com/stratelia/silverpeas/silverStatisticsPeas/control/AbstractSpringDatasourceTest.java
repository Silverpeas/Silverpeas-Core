/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.silverStatisticsPeas.control;

import java.io.InputStream;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.silverpeas.jndi.SimpleMemoryContextFactory;

import org.silverpeas.util.DBUtil;

import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author ehugonnet
 */
public abstract class AbstractSpringDatasourceTest {

  private static ClassPathXmlApplicationContext springContext;
  private static DataSource datasource;

  @BeforeClass
  public static void setUpClass() throws Exception {
    DBUtil.clearTestInstance();
    SimpleMemoryContextFactory.setUpAsInitialContext();
    configureJNDIDatasource();
  }

  private static void configureJNDIDatasource() throws Exception {
    springContext = new ClassPathXmlApplicationContext("/spring-silverpeas.xml",
        "/com/stratelia/silverpeas/silverStatisticsPeas/control/spring-h2-datasource.xml");
    datasource = springContext.getBean("dataSource", DataSource.class);
    InitialContext ic = new InitialContext();
    ic.bind("java:/datasources/silverpeas-jdbc", datasource);
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    InitialContext ic = new InitialContext();
    ic.unbind("java:/datasources/silverpeas-jdbc");
    springContext.close();
    DBUtil.clearTestInstance();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  @Before
  public void prepareData() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(datasource.getConnection());
    try {
      DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    } finally {
      connection.close();
    }
  }

  @After
  public void cleanData() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(datasource.getConnection());
    try {
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    } finally {
      connection.close();
    }
  }

  protected IDataSet getDataSet() throws Exception {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    InputStream in = AbstractSpringDatasourceTest.class.getResourceAsStream(getDatasetFileName());
    try {
      ReplacementDataSet dataSet = new ReplacementDataSet(builder.build(in));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  public abstract String getDatasetFileName();

}
