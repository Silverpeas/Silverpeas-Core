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

package com.stratelia.webactiv.util;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ehugonnet
 */
public class DateUtilTest {

    public DateUtilTest() {
    }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

 

  /**
   * Test of formatDuration method, of class DateUtil.
   */
  @Test
  public void testFormatDuration() {
    System.out.println("formatDuration");
    long duration = 0l;
    String expResult = "0s";
    String result = DateUtil.formatDuration(duration);
    assertEquals("Duration of 0s", expResult, result);
    
    
    duration = 10000l;
    expResult = "10s";
    result = DateUtil.formatDuration(duration);
    assertEquals("Duration of 10 secondes ",expResult, result);
    
    duration = 60000l;
    expResult = "1m00s";
    result = DateUtil.formatDuration(duration);
    assertEquals("Duration of 1 minute",expResult, result);
    
    duration = 305000l;
    expResult = "5m05s";
    result = DateUtil.formatDuration(duration);
    assertEquals("Duration of 5 minutes and 5 seconds",expResult, result);
    
    
    duration = 3600000l;
    expResult = "01h00m00s";
    result = DateUtil.formatDuration(duration);
    assertEquals("Duration of 1 hour",expResult, result);
    
    duration = 3600000l + 15*60000l + 30000l;
    expResult = "01h15m30s";
    result = DateUtil.formatDuration(duration);
    assertEquals("Duration of 1 hour 15 minutes and 30 seconds",expResult, result);
    
    duration = 36000000l + 15*60000l + 15000l;
    expResult = "10h15m15s";
    result = DateUtil.formatDuration(duration);
    assertEquals("Duration of 10 hours 15 minutes and 15 seconds",expResult, result);
  }

 
}