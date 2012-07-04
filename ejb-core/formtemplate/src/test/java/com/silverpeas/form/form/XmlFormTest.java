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

package com.silverpeas.form.form;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.PagesContext;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.mock.web.MockJspWriter;

import javax.naming.InitialContext;
import javax.servlet.jsp.JspWriter;
import javax.sql.DataSource;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static com.silverpeas.util.PathTestUtil.SEPARATOR;
import static com.silverpeas.util.PathTestUtil.TARGET_DIR;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class XmlFormTest {

  private static PublicationTemplate template;

  public XmlFormTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    DataSource ds = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement pstmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);
    when(ds.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(pstmt);
    when(pstmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(Boolean.FALSE);
    InitialContext context = new InitialContext();
    context.bind(JNDINames.FORMTEMPLATE_DATASOURCE, ds);
    PublicationTemplateManager.templateDir = TARGET_DIR + SEPARATOR + "test-classes"
            + SEPARATOR + "templateRepository";
    PublicationTemplateManager.mappingPublicationTemplateFilePath = TARGET_DIR + SEPARATOR
            + "test-classes" + SEPARATOR + "templateRepository" + SEPARATOR + "mapping"
            + SEPARATOR + "templateFilesMapping.xml";
    PublicationTemplateManager.mappingRecordTemplateFilePath = TARGET_DIR + SEPARATOR
            + "test-classes" + SEPARATOR + "templateRepository" + SEPARATOR + "mapping"
            + SEPARATOR + "templateMapping.xml";
    template = PublicationTemplateManager.getInstance().loadPublicationTemplate("MyForm.xml");
    DBUtil.clearTestInstance();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  /**
   * Test of toString method, of class XmlForm.
   */
  @Test
  public void testToString() {
  }

  /**
   * Test of display method, of class XmlForm.
   */
  @Test
  public void testDisplay() throws Exception {
    XmlForm form = new XmlForm(template.getRecordTemplate());
    DataRecord data = template.getRecordSet().getEmptyRecord();
    PagesContext pagesContext = mock(PagesContext.class);
    StringWriter out = new StringWriter();
    JspWriter jw = new MockJspWriter(out);
    form.display(jw, pagesContext, data);
  }
}
