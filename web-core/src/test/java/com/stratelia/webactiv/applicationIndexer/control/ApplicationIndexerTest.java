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
 * "http://www.silverpeas.com/legal/licensing"
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

  private static ApplicationIndexer instance;

  public ApplicationIndexerTest() {
  }

  @BeforeClass
  public static void setUpMockInstance() {
    MainSessionController mainSessionController = mock(MainSessionController.class);
    instance = new ApplicationIndexer(mainSessionController);
  }

  /**
   * Test of firstLetterToUpperCase method, of class ApplicationIndexer.
   */
  @Test
  public void testFirstLetterToUpperCase() {
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
    assertThat(instance.firstLetterToLowerCase(null), nullValue());
    assertThat(instance.firstLetterToLowerCase(""), is(""));
    assertThat(instance.firstLetterToLowerCase("Cat"), is("cat"));
    assertThat(instance.firstLetterToLowerCase("cat"), is("cat"));
    assertThat(instance.firstLetterToLowerCase("CAt"), is("cAt"));
  }

  @Test
  public void testGetPackageName() {
    ComponentInst kmelia = mock(ComponentInst.class);
    when(kmelia.getName()).thenReturn("Kmelia");
    assertThat(instance.getPackage(kmelia), is("kmelia"));

    ComponentInst toolbox = mock(ComponentInst.class);
    when(toolbox.getName()).thenReturn("toolbox");
    assertThat(instance.getPackage(toolbox), is("kmelia"));

    ComponentInst bookmark = mock(ComponentInst.class);
    when(bookmark.getName()).thenReturn("bookmark");
    assertThat(instance.getPackage(bookmark), is("webSites"));

    ComponentInst pollingStation = mock(ComponentInst.class);
    when(pollingStation.getName()).thenReturn("pollingStation");
    assertThat(instance.getPackage(pollingStation), is("survey"));

    ComponentInst webPages = mock(ComponentInst.class);
    when(webPages.getName()).thenReturn("webPages");
    assertThat(instance.getPackage(webPages), is("webpages"));

    ComponentInst mydb = mock(ComponentInst.class);
    when(mydb.getName()).thenReturn("MyDB");
    assertThat(instance.getPackage(mydb), is("mydb"));
  }
  
  @Test
  public void testGetClassName() {
    ComponentInst kmelia = mock(ComponentInst.class);
    when(kmelia.getName()).thenReturn("Kmelia");
    assertThat(instance.getClassName(kmelia), is("Kmelia"));

    ComponentInst toolbox = mock(ComponentInst.class);
    when(toolbox.getName()).thenReturn("toolbox");
    assertThat(instance.getClassName(toolbox), is("Kmelia"));

    ComponentInst bookmark = mock(ComponentInst.class);
    when(bookmark.getName()).thenReturn("bookmark");
    assertThat(instance.getClassName(bookmark), is("WebSites"));

    ComponentInst pollingStation = mock(ComponentInst.class);
    when(pollingStation.getName()).thenReturn("pollingStation");
    assertThat(instance.getClassName(pollingStation), is("Survey"));

    ComponentInst webPages = mock(ComponentInst.class);
    when(webPages.getName()).thenReturn("webPages");
    assertThat(instance.getClassName(webPages), is("WebPages"));

    ComponentInst mydb = mock(ComponentInst.class);
    when(mydb.getName()).thenReturn("MyDB");
    assertThat(instance.getClassName(mydb), is("MyDB"));
  }
  
  
  @Test
  public void testGetIndexer() {
    ComponentInst stub = mock(ComponentInst.class);
    when(stub.getName()).thenReturn("stub");
    assertThat(instance.getClassName(stub), is("Stub"));
    assertThat(instance.getPackage(stub), is("stub"));
    ComponentIndexerInterface result = instance.getIndexer(stub);
    assertThat(result, is(notNullValue()));
    assertThat(result.getClass().getName(), is("com.stratelia.webactiv.stub.StubIndexer"));
  }
}
