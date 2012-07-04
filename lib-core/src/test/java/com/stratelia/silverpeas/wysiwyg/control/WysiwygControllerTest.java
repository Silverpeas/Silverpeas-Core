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

package com.stratelia.silverpeas.wysiwyg.control;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class WysiwygControllerTest {

  public WysiwygControllerTest() {
  }

  /**
   * Test of finNode method, of class WysiwygController.
   */
  @Test
  public void testFinNode() {
    String path = "c:\\\\silverpeas_data\\\\webSite17\\\\id\\\\rep1\\\\rep2\\\\rep3";
    String componentId = "webSite17";
    String result = WysiwygController.finNode(path, componentId);
    assertThat(result, is("id\\rep1\\rep2\\rep3"));
    
    path = "c:\\silverpeas_data\\webSite17\\id\\rep1\\rep2\\rep3";
    componentId = "webSite17";
    result = WysiwygController.finNode(path, componentId);
    assertThat(result, is("id\\rep1\\rep2\\rep3"));

    path = "/var/silverpeas_data/webSite17/id/rep1/rep2/rep3";
    componentId = "webSite17";
    result = WysiwygController.finNode(path, componentId);
    assertThat(result, is("id/rep1/rep2/rep3"));
  }

  /**
   * Test of finNode2 method, of class WysiwygController.
   */
  @Test
  public void testFinNode2() {
    String path = "c:\\\\silverpeas_data\\\\webSite17\\\\id\\\\rep1\\\\rep2\\\\rep3";
    String componentId = "webSite17";
    String result = WysiwygController.finNode2(path, componentId);
    assertThat(result, is("rep1\\rep2\\rep3"));

    path = "/var/silverpeas_data/webSite17/id/rep1/rep2/rep3";
    componentId = "webSite17";
    result = WysiwygController.finNode2(path, componentId);
    assertThat(result, is("rep1/rep2/rep3"));
  }

  /**
   * Test of getNodePath method, of class WysiwygController.
   */
  @Test
  public void testGetNodePath() {
    String currentPath = "c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3\\rep1\\rep11\\";
    String componentId = "webSite17";
    String result = WysiwygController.getNodePath(currentPath, componentId);
    assertThat(result, is("c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3"));
    currentPath = "c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3";
    result = WysiwygController.getNodePath(currentPath, componentId);
    assertThat(result, is("c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3"));
  }
  
  /**
   * Test of getNodePath method, of class WysiwygController.
   */
  @Test
  public void testGetNodePathOnLinux() {
    String currentPath = "/home/ehugonnet/programs/silverpeas/data/web/website.war/webSites45/1";
    String componentId = "webSites45";
    String result = WysiwygController.getNodePath(currentPath, componentId);
    assertThat(result, is("/home/ehugonnet/programs/silverpeas/data/web/website.war/webSites45/1"));
    currentPath = "/home/ehugonnet/programs/silverpeas/data/web/website.war/webSites45/1/repertoire1/repertoire2/";
    result = WysiwygController.getNodePath(currentPath, componentId);
    assertThat(result, is("/home/ehugonnet/programs/silverpeas/data/web/website.war/webSites45/1"));
  }

  /**
   * Test of ignoreAntiSlash method, of class WysiwygController.
   */
  @Test
  public void testIgnoreSlashAndAntislash() {
    String chemin = "\\\\rep1\\rep2\\rep3";
    String result = WysiwygController.ignoreSlashAndAntislash(chemin);
    assertThat(result, is("rep1\\rep2\\rep3"));
    
    chemin = "\\rep1\\rep2\\rep3";
    result = WysiwygController.ignoreSlashAndAntislash(chemin);
    assertThat(result, is("rep1\\rep2\\rep3"));
    
    chemin = "/rep1/rep2/rep3";
    result = WysiwygController.ignoreSlashAndAntislash(chemin);
    assertThat(result, is("rep1/rep2/rep3"));
  }
  
  
  /**
   * Test of ignoreAntiSlash method, of class WysiwygController.
   */
  @Test
  public void testIgnoreSlash() {
    String chemin = "\\\\rep1\\rep2\\rep3";
    String result = WysiwygController.ignoreSlash(chemin);
    assertThat(result, is("\\\\rep1\\rep2\\rep3"));
    
    chemin = "//rep1/rep2/rep3";
    result = WysiwygController.ignoreSlash(chemin);
    assertThat(result, is("rep1/rep2/rep3"));
    
    chemin = "/rep1/rep2/rep3";
    result = WysiwygController.ignoreSlash(chemin);
    assertThat(result, is("rep1/rep2/rep3"));
  }
  
  /**
   * Test of ignoreAntiSlash method, of class WysiwygController.
   */
  @Test
  public void testSupprDoubleAntiSlash() {
    String chemin = "\\\\rep1\\rep2\\\\rep3";
    String result = WysiwygController.supprDoubleAntiSlash(chemin);
    assertThat(result, is("\\rep1\\rep2\\rep3"));
    
    chemin = "\\rep1\\rep2\\rep3";
    result = WysiwygController.supprDoubleAntiSlash(chemin);
    assertThat(result, is("\\rep1\\rep2\\rep3"));
    
    chemin = "/rep1/rep2/rep3";
    result = WysiwygController.supprDoubleAntiSlash(chemin);
    assertThat(result, is("/rep1/rep2/rep3"));
  }

 
}
