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

package com.silverpeas.importExport.report;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class HtmlExportGeneratorTest {

  /**
   * Test of encode method, of class HtmlExportGenerator.
   */
  @Test
  public void testEncode() {
    String result = HtmlExportGenerator.encode(null);
    assertThat("", is(result));
    result = HtmlExportGenerator.encode("\t\t");
    assertThat("&nbsp;&nbsp;&nbsp;&nbsp;", is(result));
    result = HtmlExportGenerator.encode("\t\n");
    assertThat("&nbsp;&nbsp;<br/>", is(result));
    result = HtmlExportGenerator.encode("mise en œuvre");
    assertThat("mise en &oelig;uvre", is(result));
    result = HtmlExportGenerator.encode("c'est cet été");
    assertThat("c'est cet &eacute;t&eacute;", is(result));
  }

  /**
   * Test of getHtmlStyle method, of class HtmlExportGenerator.
   */
  @Test
  public void testGetHtmlStyle() {
    String expResult = "<link href='treeview/display.css' type='text/css' rel='stylesheet'/>";
    String result = HtmlExportGenerator.getHtmlStyle();
    assertThat(expResult, is(result));
  }

  @Test
  public void testWriteEnTeteSommaire() {
    String expResult = "<div class='numberOfDocument'>c'est cet &eacute;t&eacute;</div>";
    HtmlExportGenerator instance = new HtmlExportGenerator(null, null);
    String result = instance.writeEnTeteSommaire("c'est cet été");
    assertThat(expResult, is(result));
  }

  @Test
  public void tesGetBeginningOfPage() {
    String expResult = "<html><head><title>c'est cet été</title><meta http-equiv='Content-Type' "
        + "content='text/html; charset=UTF-8'/>" + HtmlExportGenerator.getHtmlStyle() + "</head>";
    HtmlExportGenerator instance = new HtmlExportGenerator(null, null);
    String result = instance.getBeginningOfPage("c'est cet été", false);
    assertThat(expResult, is(result));
    
    expResult = "<html><head><title>c'est cet été</title><meta http-equiv='Content-Type' "
        + "content='text/html; charset=UTF-8'/>" + HtmlExportGenerator.getHtmlStyle() 
        + "<script type='text/javascript' src='treeview/TreeView.js' language='Javascript'>"
        + "</script><script type='text/javascript' src='treeview/TreeViewElements.js' "
        + "language='Javascript'></script><link href='treeview/treeview.css' type='text/css' "
        + "rel='stylesheet'/></head>";
    result = instance.getBeginningOfPage("c'est cet été", true);
    assertThat(expResult, is(result));
  }
}