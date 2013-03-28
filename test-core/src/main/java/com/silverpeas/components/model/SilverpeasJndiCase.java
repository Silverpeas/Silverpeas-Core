/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.components.model;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.dbcp.BasicDataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.io.IOUtils;
import org.dbunit.IDatabaseTester;
import org.dbunit.JndiBasedDBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import com.silverpeas.jndi.SimpleMemoryContextFactory;

/**
 * @author ehugonnet
 */
public class SilverpeasJndiCase extends JndiBasedDBTestCase {

  private String jndiName = "";
  private final String dataSetFileName;
  private final String ddlFile;
  private final DatabaseOperation tearDownOperation;
  private BasicDataSource datasource;

  public SilverpeasJndiCase(String dataSetFileName, String ddlFile) {
    this(dataSetFileName, ddlFile, DatabaseOperation.DELETE_ALL);
  }

  public SilverpeasJndiCase(String dataSetFileName, String ddlFile,
      DatabaseOperation tearDownOperation) {
    this.dataSetFileName = dataSetFileName;
    this.ddlFile = ddlFile;
    this.tearDownOperation = tearDownOperation;    
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  public void shudown() throws SQLException {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    if (datasource != null) {
      datasource.close();
    }
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  protected DatabaseOperation getTearDownOperation() throws Exception {
    return tearDownOperation;
  }

  @Override
  protected DatabaseOperation getSetUpOperation() throws Exception {
    return DatabaseOperation.CLEAN_INSERT;
  }

  @Override
  protected String getLookupName() {
    return jndiName;
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream(this.dataSetFileName);
    try {
      ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(in));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  public void configureJNDIDatasource() throws IOException, NamingException, Exception {
    InitialContext ic = new InitialContext();
    if (datasource == null) {
      Properties props = new Properties();
      props.load(SilverpeasJndiCase.class.getClassLoader().getResourceAsStream("jdbc.properties"));
      datasource = (BasicDataSource) BasicDataSourceFactory.createDataSource(props);
      jndiName = props.getProperty("jndi.name");
      ic.rebind(jndiName, datasource);
    }
  }

  @Override
  public IDatabaseTester getDatabaseTester() throws Exception {
    return super.getDatabaseTester();
  }

  public String getDdlFile() {
    return this.ddlFile;
  }
}
