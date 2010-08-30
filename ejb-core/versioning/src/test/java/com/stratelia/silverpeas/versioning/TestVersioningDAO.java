/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.stratelia.silverpeas.versioning;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.versioning.ejb.VersioningDAO;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.Worker;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

public class TestVersioningDAO extends AbstractTestDao {

  private static final String INSTANCE_ID = "kmelia60";
  private Date checkoutdate;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.YEAR, 2008);
    calend.set(Calendar.MONTH, Calendar.MAY);
    calend.set(Calendar.DAY_OF_MONTH, 15);
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    checkoutdate = calend.getTime();
  }

  @Override
  protected String getDatasetFileName() {
    return "test-versioning-dataset.xml";
  }

  @Test
  public void testGetDocumentById() throws Exception {
    DocumentPK pk = new DocumentPK(1, null, INSTANCE_ID);
    Document doc = VersioningDAO.getDocument(getConnection().getConnection(), pk);
    assertNotNull(doc);
    assertEquals(pk, doc.getPk());
    assertEquals("SimpleDocument", doc.getName());
    assertEquals("simple document", doc.getDescription());
    assertEquals(Document.STATUS_CHECKINED, doc.getStatus());
    assertEquals(1, doc.getOwnerId());
    assertEquals(checkoutdate, doc.getLastCheckOutDate());
    assertEquals("", doc.getAdditionalInfo());
    assertEquals(new ForeignPK("4", INSTANCE_ID), doc.getForeignKey());
    assertEquals(0, doc.getTypeWorkList());
    assertEquals(0, doc.getCurrentWorkListOrder());
  }

  @Test
  public void testGetDocument() throws Exception {
    DocumentPK pk = new DocumentPK(1, null, INSTANCE_ID);
    Document doc = VersioningDAO.getDocument(getConnection().getConnection(), pk);
    assertNotNull(doc);
    assertEquals(pk, doc.getPk());
    assertEquals("SimpleDocument", doc.getName());
    assertEquals("simple document", doc.getDescription());
    assertEquals(Document.STATUS_CHECKINED, doc.getStatus());
    assertEquals(1, doc.getOwnerId());
    assertEquals(checkoutdate, doc.getLastCheckOutDate());
    assertEquals("", doc.getAdditionalInfo());
    assertEquals(new ForeignPK("4", INSTANCE_ID), doc.getForeignKey());
    assertEquals(0, doc.getTypeWorkList());
    assertEquals(0, doc.getCurrentWorkListOrder());
     assertEquals(INSTANCE_ID, doc.getInstanceId());
  }

  @Test
  public void testGetDocumentsByForeignKey() throws Exception {
    ForeignPK fpk = new ForeignPK("4", INSTANCE_ID);
    List<Document> docs = VersioningDAO.getDocuments(getConnection().getConnection(), fpk);
    assertNotNull(docs);
    assertEquals(3, docs.size());

    Document doc1 = new Document();
    doc1.setPk(new DocumentPK(1, null, INSTANCE_ID));
    doc1.setInstanceId(INSTANCE_ID);
    doc1.setWorkList(new ArrayList<Worker>());
    doc1.setName("SimpleDocument");
    doc1.setDescription("simple document");
    doc1.setStatus(Document.STATUS_CHECKINED);
    doc1.setOwnerId(1);
    doc1.setLastCheckOutDate(checkoutdate);
    doc1.setAdditionalInfo("");
    doc1.setForeignKey(new ForeignPK("4", INSTANCE_ID));
    doc1.setTypeWorkList(0);
    doc1.setCurrentWorkListOrder(0);

    Document doc2 = new Document();
    doc2.setPk(new DocumentPK(2, null, INSTANCE_ID));
    doc2.setInstanceId(INSTANCE_ID);
    doc2.setWorkList(new ArrayList<Worker>());
    doc2.setName("FreeDocument");
    doc2.setDescription("free document");
    doc2.setStatus(Document.STATUS_CHECKINED);
    doc2.setOwnerId(2);
    doc2.setLastCheckOutDate(checkoutdate);
    doc2.setAdditionalInfo("");
    doc2.setForeignKey(new ForeignPK("4", INSTANCE_ID));
    doc2.setTypeWorkList(0);
    doc2.setCurrentWorkListOrder(0);


    Document doc3 = new Document();
    doc3.setPk(new DocumentPK(3, null, INSTANCE_ID));
    doc3.setInstanceId(INSTANCE_ID);
    doc3.setWorkList(new ArrayList<Worker>());
    doc3.setName("CheckedDocument");
    doc3.setDescription("locked document");
    doc3.setStatus(Document.STATUS_CHECKOUTED);
    doc3.setOwnerId(2);
    doc3.setLastCheckOutDate(checkoutdate);
    doc3.setAdditionalInfo("");
    doc3.setForeignKey(new ForeignPK("4", INSTANCE_ID));
    doc3.setTypeWorkList(0);
    doc3.setCurrentWorkListOrder(0);

    assertThat("Result shoud contain the document 1", docs, hasItem(doc1));
    assertThat("Result shoud contain the document 2", docs, hasItem(doc2));
    assertThat("Result shoud contain the document 3", docs, hasItem(doc3));

  }
}
