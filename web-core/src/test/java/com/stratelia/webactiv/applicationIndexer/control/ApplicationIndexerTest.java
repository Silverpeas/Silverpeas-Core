/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.applicationIndexer.control;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;


/**
 *
 * @author ehugonnet
 */
public class ApplicationIndexerTest {
  
  public ApplicationIndexerTest() {
  }


  /**
   * Test of firstLetterToUpperCase method, of class ApplicationIndexer.
   */
  @Test
  public void testFirstLetterToUpperCase() {
    MainSessionController mainSessionController = mock(MainSessionController.class);
    ApplicationIndexer instance = new ApplicationIndexer(mainSessionController);
    assertThat(instance.firstLetterToUpperCase(null), nullValue());
    assertThat(instance.firstLetterToUpperCase(""), is(""));
    assertThat(instance.firstLetterToUpperCase("Cat"), is("Cat"));
    assertThat(instance.firstLetterToUpperCase("cat"), is("Cat"));    
    assertThat(instance.firstLetterToUpperCase("cAt"), is("CAt"));
  }

  /**
   * Test of firstLetterToLowerCase method, of class ApplicationIndexer.
   */
  @Test
  public void testFirstLetterToLowerCase() {
    MainSessionController mainSessionController = mock(MainSessionController.class);
    ApplicationIndexer instance = new ApplicationIndexer(mainSessionController);
    assertThat(instance.firstLetterToLowerCase(null), nullValue());
    assertThat(instance.firstLetterToLowerCase(""), is(""));
    assertThat(instance.firstLetterToLowerCase("Cat"), is("cat"));
    assertThat(instance.firstLetterToLowerCase("cat"), is("cat"));    
    assertThat(instance.firstLetterToLowerCase("CAt"), is("cAt"));
  }

}
