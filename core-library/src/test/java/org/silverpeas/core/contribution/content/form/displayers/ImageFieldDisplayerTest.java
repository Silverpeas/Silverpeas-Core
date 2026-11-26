/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.form.displayers;

import org.apache.commons.fileupload.FileItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the update of an image field of a form from the values submitted by the user: the image
 * can be either a file uploaded in the form (an attachment) or a link to an image of a gallery.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ImageFieldDisplayerTest {

  private static final String FIELD_NAME = "shortcutIcon1";
  private static final String GALLERY_IMAGE =
      "/silverpeas/GalleryInWysiwyg/dummy?ImageId=12&ComponentId=gallery3";
  private static final String OTHER_GALLERY_IMAGE =
      "/silverpeas/GalleryInWysiwyg/dummy?ImageId=13&ComponentId=gallery3";
  private static final String ATTACHMENT_ID = "a9b8c7d6-0000-1111-2222-333344445555";

  @TestManagedMock
  AttachmentService attachmentService;

  private final FieldTemplate template = mock(FieldTemplate.class);
  private final PagesContext context = mock(PagesContext.class);
  private final FileField field = new FileField();

  @BeforeEach
  void setUp() {
    field.setName(FIELD_NAME);
    when(context.getComponentId()).thenReturn("webPages1");
    when(context.getContentLanguage()).thenReturn("fr");
    when(context.isCreation()).thenReturn(false);
  }

  @Test
  void anImageOfAGalleryRemovedByTheUserIsNoMoreInTheField() throws Exception {
    field.setAttachmentId(GALLERY_IMAGE);

    update(GALLERY_IMAGE, AbstractFileFieldDisplayer.Operation.DELETION);

    assertThat(field.getAttachmentId(), is(nullValue()));
    verify(attachmentService, never()).deleteAttachment(any());
  }

  @Test
  void anImageOfAGalleryLeftUnchangedIsKeptInTheField() throws Exception {
    field.setAttachmentId(GALLERY_IMAGE);

    update(GALLERY_IMAGE, AbstractFileFieldDisplayer.Operation.UPDATE);

    assertThat(field.getAttachmentId(), is(GALLERY_IMAGE));
  }

  @Test
  void anImageOfAGalleryReplacedByAnotherOneIsReplacedInTheField() throws Exception {
    field.setAttachmentId(GALLERY_IMAGE);

    update(OTHER_GALLERY_IMAGE, AbstractFileFieldDisplayer.Operation.UPDATE);

    assertThat(field.getAttachmentId(), is(OTHER_GALLERY_IMAGE));
    verify(attachmentService, never()).deleteAttachment(any());
  }

  @Test
  void anUploadedImageRemovedByTheUserIsDeletedAndNoMoreInTheField() throws Exception {
    SimpleDocument image = mock(SimpleDocument.class);
    when(attachmentService.searchDocumentById(any(SimpleDocumentPK.class), anyString()))
        .thenReturn(image);
    field.setAttachmentId(ATTACHMENT_ID);

    update(ATTACHMENT_ID, AbstractFileFieldDisplayer.Operation.DELETION);

    assertThat(field.getAttachmentId(), is(nullValue()));
    verify(attachmentService).deleteAttachment(image);
  }

  @Test
  void anUploadedImageReplacedByAnImageOfAGalleryIsDeleted() throws Exception {
    SimpleDocument image = mock(SimpleDocument.class);
    when(attachmentService.searchDocumentById(any(SimpleDocumentPK.class), anyString()))
        .thenReturn(image);
    field.setAttachmentId(ATTACHMENT_ID);

    update(GALLERY_IMAGE, AbstractFileFieldDisplayer.Operation.UPDATE);

    assertThat(field.getAttachmentId(), is(GALLERY_IMAGE));
    verify(attachmentService).deleteAttachment(image);
  }

  @Test
  void anUploadedImageLeftUnchangedIsKeptInTheField() throws Exception {
    field.setAttachmentId(ATTACHMENT_ID);

    update(ATTACHMENT_ID, AbstractFileFieldDisplayer.Operation.UPDATE);

    assertThat(field.getAttachmentId(), is(ATTACHMENT_ID));
    verify(attachmentService, never()).deleteAttachment(any());
  }

  /**
   * Updates the field with the values submitted by the user without uploading any file: the
   * identifier of the image set in the field (an attachment identifier or a link to an image of a
   * gallery) and the operation asked on it.
   */
  private void update(final String imageId, final AbstractFileFieldDisplayer.Operation operation)
      throws FormException, UnsupportedEncodingException {
    List<FileItem> items = List.of(
        formField(FIELD_NAME + Field.FILE_PARAM_NAME_SUFFIX, imageId),
        formField(FIELD_NAME + AbstractFileFieldDisplayer.OPERATION_KEY, operation.name()));
    new ImageFieldDisplayer().update(items, field, template, context);
  }

  private static FileItem formField(final String name, final String value)
      throws UnsupportedEncodingException {
    FileItem item = mock(FileItem.class);
    when(item.isFormField()).thenReturn(true);
    when(item.getFieldName()).thenReturn(name);
    when(item.getString(anyString())).thenReturn(value);
    when(item.getString()).thenReturn(value);
    return item;
  }
}
