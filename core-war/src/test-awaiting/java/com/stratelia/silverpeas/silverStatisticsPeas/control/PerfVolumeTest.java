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

import java.io.File;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.silverpeas.silverstatistics.volume.DirectoryVolumeService;

import com.silverpeas.jndi.SimpleMemoryContextFactory;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author ehugonnet
 */
public class PerfVolumeTest {

  private String userId = "9";
  private int forLoop = 10;

  @Test
  @Ignore
  public void computeDataFromFs() throws Exception {
    String dataHomeDir = "/media/DATA/opt/silverpeas/data/workspaces/";
    File dataDirectory = new File(dataHomeDir);
    long total = 0L;
    DirectoryVolumeService service = new DirectoryVolumeService(dataDirectory);
    for (int i = 0; i < forLoop; i++) {
      long start = System.currentTimeMillis();
      service.getVolumes(userId);
      total = total + (System.currentTimeMillis() - start);
    }
    System.out.println("Mean duration for " + forLoop + " is " + (total / forLoop)
        + " for scanning mode");
  }




  @BeforeClass
  public static void setupDataSource() throws NamingException {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    BasicDataSource ds = new BasicDataSource();
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setUsername("postgres");
    ds.setPassword("postgres");
    ds.setUrl("jdbc:postgresql://localhost:5432/extranet");
    InitialContext ic = new InitialContext();
    ic.bind("java:/datasources/silverpeas-jdbc", ds);
    ic.bind("java:/datasources/DocumentStoreDS", ds);
  }

  @AfterClass
  public static void teardownDataSource() throws NamingException, SQLException {
    InitialContext ic = new InitialContext();
    BasicDataSource ds = (BasicDataSource) ic.lookup("java:/datasources/silverpeas-jdbc");
    ds.close();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }
}