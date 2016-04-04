/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.workflow.engine.model;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class ConsequenceImplTest {

  public ConsequenceImplTest() {
  }



  /**
   * Test of isVerified method, of class ConsequenceImpl.
   */
  @Test
  public void testIsVerifiedForFLoat() {
    String itemValue = "12.23";
    ConsequenceImpl instance = new ConsequenceImpl();
    instance.setValue("12.23");
    instance.setOperator("=");
    assertThat(instance.isVerified(itemValue), is(true));
    instance.setValue("12.230");
    assertThat(instance.isVerified(itemValue), is(true));
    instance.setValue("12.2300000");
     assertThat(instance.isVerified(itemValue), is(true));
  }

  @Test
  public void testIsVerifiedString() {
    String itemValue = "12-230";
    ConsequenceImpl instance = new ConsequenceImpl();
    instance.setValue("12-230");
    instance.setOperator("=");
    assertThat(instance.isVerified(itemValue), is(true));
    instance.setValue("12-23");
    assertThat(instance.isVerified(itemValue), is(false));
    instance.setOperator("contains");
    assertThat(instance.isVerified(itemValue), is(true));
  }


}
