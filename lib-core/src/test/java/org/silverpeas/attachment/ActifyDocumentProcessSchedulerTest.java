/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
package org.silverpeas.attachment;

import com.silverpeas.calendar.Date;
import com.silverpeas.calendar.DateTime;
import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MimeTypes;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.attachment.mock.AttachmentServiceMockWrapper;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the scheduling of the tasks on the documents handled by Actify.
 *
 * @author mmoquillon
 */
public class ActifyDocumentProcessSchedulerTest {

  private AttachmentService attachmentService;
  private ClassPathXmlApplicationContext springCtx;

  @BeforeClass
  public static void initActifyDirectories() {
    File dir = new File(ActifyDocumentProcessor.getActifyResultPath());
    if (!dir.exists()) {
      assertThat(dir.mkdirs(), is(true));
    }
    dir = new File(ActifyDocumentProcessor.getActifySourcePath());
    if (!dir.exists()) {
      assertThat(dir.mkdirs(), is(true));
    }
  }

  @Before
  public void setUpTest() {
    springCtx = new ClassPathXmlApplicationContext("/spring-actify.xml");

    attachmentService = AttachmentServiceFactory.getAttachmentService();
    assertThat(attachmentService, not(nullValue()));
    assertThat(attachmentService.getClass().getName(), is(AttachmentServiceMockWrapper.class.
        getName()));
    attachmentService = ((AttachmentServiceMockWrapper) attachmentService).
        getAttachmentServiceMock();
  }

  @After
  public void cleanUpActifyResultDirectory() {
    springCtx.close();
    springCtx.destroy();

    File dir = new File(ActifyDocumentProcessor.getActifyResultPath());
    File[] children = dir.listFiles();
    for (File child : children) {
      FileUtils.deleteQuietly(child);
    }
  }

  @Test
  public void importWithoutAnyFiles() throws Exception {
    TestContext ctx = new TestContext().withoutAnyActifyDocuments();

    ActifyDocumentProcessScheduler scheduler = new ActifyDocumentProcessScheduler();
    Job importer = scheduler.getActifyDocumentImporter();
    importer.execute(inAnyContext());

    assertThat(ctx.noneActifyDocuments(), is(true));
  }

  @Test
  public void importSomeNonVersionedActifyDocuments() throws Exception {
    TestContext ctx = new TestContext().withSomeActifyDocuments(3);

    ActifyDocumentProcessScheduler scheduler = new ActifyDocumentProcessScheduler();
    Job importer = scheduler.getActifyDocumentImporter();
    importer.execute(inAnyContext());

    assertThat(ctx.noneActifyDocuments(), is(true));
    ctx.verifyAttachmentServiceInvocation();
  }

  @Test
  public void importSomeVersionedActifyDocuments() throws Exception {
    TestContext ctx = new TestContext().withSomeVersionedActifyDocuments(3);

    ActifyDocumentProcessScheduler scheduler = new ActifyDocumentProcessScheduler();
    Job importer = scheduler.getActifyDocumentImporter();
    importer.execute(inAnyContext());

    assertThat(ctx.noneActifyDocuments(), is(true));
    ctx.verifyAttachmentServiceInvocation();
  }

  @Test
  public void importSomeActifyDocuments() throws Exception {
    TestContext ctx = new TestContext().withSomeActifyDocuments(3).withSomeVersionedActifyDocuments(
        3);

    ActifyDocumentProcessScheduler scheduler = new ActifyDocumentProcessScheduler();
    Job importer = scheduler.getActifyDocumentImporter();
    importer.execute(inAnyContext());

    assertThat(ctx.noneActifyDocuments(), is(true));
    ctx.verifyAttachmentServiceInvocation();
  }

  @Test
  public void jobSchedulingAtInitialization() throws SchedulerException {
    ActifyDocumentProcessScheduler actifyDocumentProcessor = new ActifyDocumentProcessScheduler();
    actifyDocumentProcessor.initialize();

    Scheduler scheduler = SchedulerFactory.getFactory().getScheduler();
    Job importer = actifyDocumentProcessor.getActifyDocumentImporter();
    Job cleaner = actifyDocumentProcessor.getActifyDocumentCleaner();
    assertThat(scheduler.isJobScheduled(importer.getName()), is(true));
    assertThat(scheduler.isJobScheduled(cleaner.getName()), is(true));

    scheduler.shutdown();
  }

  @Test
  public void purgeEmptyActifySourceDirectory() throws Exception {
    TestContext ctx = new TestContext().withoutAnyCADDocuments();

    ActifyDocumentProcessScheduler scheduler = new ActifyDocumentProcessScheduler();
    Job cleaner = scheduler.getActifyDocumentCleaner();
    cleaner.execute(inAnyContext());

    assertThat(ctx.noneCADDocuments(), is(true));
  }

  @Test
  public void purgeActifySourceDirectoryWithSomeCADDocuments() throws Exception {
    TestContext ctx = new TestContext().withSomeCADDocuments(5);

    ActifyDocumentProcessScheduler scheduler = new ActifyDocumentProcessScheduler();
    Job cleaner = scheduler.getActifyDocumentCleaner();
    cleaner.execute(inAnyContext());

    assertThat(ctx.noneCADDocuments(), is(true));
  }

  @Test
  public void purgeActifySourceDirectoryWithSomeVersionedCADDocuments() throws Exception {
    TestContext ctx = new TestContext().withSomeVersionedCADDocuments(5);

    ActifyDocumentProcessScheduler scheduler = new ActifyDocumentProcessScheduler();
    Job cleaner = scheduler.getActifyDocumentCleaner();
    cleaner.execute(inAnyContext());

    assertThat(ctx.noneCADDocuments(), is(true));
  }

  private JobExecutionContext inAnyContext() {
    return JobExecutionContext.createWith("A test job", DateTime.now());
  }

  /**
   * Context on the unit tests. It allocates and prepares all the resources required to the tests.
   */
  private class TestContext {

    private static final String ACTIFY_DOCUMENT_PREFIX = "doc-";

    private final String publicationId;
    private final String instanceId;
    private boolean versioned = false;
    private int actifyDocuments = 0;

    public TestContext() {
      this.publicationId = "42";
      this.instanceId = "kmelia42";
    }

    public TestContext(String publicationId, String componentInstanceId) {
      this.publicationId = publicationId;
      this.instanceId = componentInstanceId;
    }

    public TestContext withoutAnyCADDocuments() {
      File dir = new File(ActifyDocumentProcessor.getActifySourcePath());
      if (dir.exists()) {
        File[] children = dir.listFiles();
        for (File child : children) {
          FileUtils.deleteQuietly(child);
        }
      }
      return this;
    }

    public TestContext withoutAnyActifyDocuments() {
      File dir = new File(ActifyDocumentProcessor.getActifyResultPath());
      if (dir.exists()) {
        File[] children = dir.listFiles();
        for (File child : children) {
          FileUtils.deleteQuietly(child);
        }
      }
      return this;
    }

    public TestContext withSomeActifyDocuments(int count) throws IOException {
      String directory = "a_" + getComponentInstanceId() + "_" + getPublicationId();
      createActifyDocumentsIn(directory, count);
      prepareMock();
      return this;
    }

    public TestContext withSomeVersionedActifyDocuments(int count) throws IOException {
      this.versioned = true;
      String directory = "v_" + getComponentInstanceId() + "_" + getPublicationId();
      createActifyDocumentsIn(directory, count);
      prepareMock();
      return this;
    }

    public TestContext withSomeCADDocuments(int count) throws IOException {
      String directory = "a_" + getComponentInstanceId() + "_" + getPublicationId();
      createCADDocumentsIn(directory, count);
      return this;
    }

    public TestContext withSomeVersionedCADDocuments(int count) throws IOException {
      String directory = "v_" + getComponentInstanceId() + "_" + getPublicationId();
      createCADDocumentsIn(directory, count);
      return this;
    }

    public String getPublicationId() {
      return publicationId;
    }

    public String getComponentInstanceId() {
      return instanceId;
    }

    public boolean isVersioned() {
      return versioned;
    }

    /**
     * In the case of versioned documents, only the last one generated by Actify is considered as
     * existing and then requiring an update (a new version) in the tests.
     */
    public void verifyAttachmentServiceInvocation() {
      int creationCount = isVersioned() ? actifyDocuments - 1 : actifyDocuments;
      int updateCount = isVersioned() ? 1 : 0;

      ArgumentCaptor<SimpleDocument> creationDoc = ArgumentCaptor.forClass(SimpleDocument.class);
      ArgumentCaptor<File> creationFile = ArgumentCaptor.forClass(File.class);
      verify(attachmentService, times(creationCount)).createAttachment(creationDoc.capture(),
          creationFile.capture(), eq(false));

      ArgumentCaptor<SimpleDocument> updateDoc = ArgumentCaptor.forClass(SimpleDocument.class);
      ArgumentCaptor<File> updateFile = ArgumentCaptor.forClass(File.class);
      ArgumentCaptor<UnlockContext> unlockCtx = ArgumentCaptor.forClass(UnlockContext.class);
      verify(attachmentService, times(updateCount)).
          updateAttachment(updateDoc.capture(), updateFile.capture(), eq(false), eq(false));
      verify(attachmentService, times(updateCount)).unlock(unlockCtx.capture());

      for (SimpleDocument document : creationDoc.getAllValues()) {
        assertThat(document.getPk().getInstanceId().equals(instanceId) && document.getForeignId().
            equals(publicationId), is(true));
      }
      for (SimpleDocument document : updateDoc.getAllValues()) {
        assertThat(document.getPk().getInstanceId().equals(instanceId) && document.getForeignId().
            equals(publicationId), is(true));
      }

      List<String> filenames = new ArrayList<String>();
      for (int i = 0; i < creationCount; i++) {
        filenames.add(ACTIFY_DOCUMENT_PREFIX + i + ".3d");
      }
      for (File aFile : creationFile.getAllValues()) {
        assertThat(filenames.contains(aFile.getName()), is(true));
      }
      if (updateCount > 0) {
        assertThat(updateFile.getValue().getName().equals(ACTIFY_DOCUMENT_PREFIX + (actifyDocuments
            - 1) + ".3d"), is(true));
        assertThat(unlockCtx.getValue().isUpload(), is(true));
      }
    }

    private boolean noneActifyDocuments() {
      File resultDir = new File(ActifyDocumentProcessor.getActifyResultPath());
      return !resultDir.exists() || resultDir.list().length == 0;
    }

    private boolean noneCADDocuments() {
      File sourceDir = new File(ActifyDocumentProcessor.getActifySourcePath());
      return !sourceDir.exists() || sourceDir.list().length == 0;
    }

    /**
     * With the versioned documents, only the last Actify one is considered as already existing and
     * then should require a new version (an update).
     */
    private void prepareMock() {
      if (isVersioned()) {
        String lastFilename = ACTIFY_DOCUMENT_PREFIX + (actifyDocuments - 1) + ".3d";
        SimpleAttachment existingAttachment = new SimpleAttachment(lastFilename, null,
            null, null, 30, MimeTypes.SPINFIRE_MIME_TYPE, null, Date.yesterday(), null);
        SimpleDocument existingDocument = new SimpleDocument(new SimpleDocumentPK(UUID.randomUUID().
            toString(), instanceId), publicationId, 0, versioned, existingAttachment);
        when(attachmentService.findExistingDocument(any(SimpleDocumentPK.class), eq(lastFilename),
            any(ForeignPK.class), (String) eq(null))).thenReturn(existingDocument);
      }
    }

    private void createActifyDocumentsIn(String directory, int count) throws IOException {
      getFileInActifyResultFolder(directory).mkdirs();
      int total = this.actifyDocuments + count;
      for (int i = this.actifyDocuments; i < total; i++) {
        String filename = ACTIFY_DOCUMENT_PREFIX + i + ".3d";
        File actifyDocument = getFileInActifyResultFolder(directory, filename);
        boolean created = actifyDocument.createNewFile();
        assertThat(created, is(true));
      }
      this.actifyDocuments = total;
    }

    private void createCADDocumentsIn(String directory, int count) throws IOException {
      getFileInActifySourceFolder(directory).mkdirs();
      int total = this.actifyDocuments + count;
      for (int i = this.actifyDocuments; i < total; i++) {
        String filename = ACTIFY_DOCUMENT_PREFIX + i + ".dwf";
        File actifyDocument = getFileInActifySourceFolder(directory, filename);
        boolean created = actifyDocument.createNewFile();
        assertThat(created, is(true));
      }
      this.actifyDocuments = total;
    }

    private File getFileInActifyResultFolder(String... pathNode) {
      String path = "";
      for (String aNode : pathNode) {
        path += File.separator + aNode;
      }
      return new File(ActifyDocumentProcessor.getActifyResultPath() + path);
    }

    private File getFileInActifySourceFolder(String... pathNode) {
      String path = "";
      for (String aNode : pathNode) {
        path += File.separator + aNode;
      }
      return new File(ActifyDocumentProcessor.getActifySourcePath() + path);
    }
  }
}
