/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.documenttemplate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.extention.TestedBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.documenttemplate.DocumentTemplateTestUtil.DEFAULT_JSON;
import static org.silverpeas.core.documenttemplate.DocumentTemplateTestUtil.createTemplateInstance;
import static org.silverpeas.core.documenttemplate.JsonDocumentTemplate.decode;
import static org.silverpeas.core.util.Charsets.UTF_8;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class DocumentTemplateServiceTest {

  private static final  DocumentTemplate DEFAULT_TEMPLATE = new DocumentTemplate(decode(DEFAULT_JSON), "txt");
  private static final String SIMPLE_CONTENT = "Simple content";

  @TestManagedMock
  private DocumentTemplateRepository repository;

  @TestedBean
  private DefaultDocumentTemplateService service;

  @DisplayName("Putting document template without id creates a new document template into repository")
  @Test
  void create() {
    final DocumentTemplate newTemplate = new DocumentTemplate(DEFAULT_TEMPLATE);
    newTemplate.setId(null);
    final ByteArrayInputStream content = new ByteArrayInputStream(SIMPLE_CONTENT.getBytes(UTF_8));
    assertThat(newTemplate.getId(), nullValue());
    service.put(newTemplate, content);
    final ArgumentCaptor<DocumentTemplate> dt = forClass(DocumentTemplate.class);
    final ArgumentCaptor<InputStream> is = forClass(InputStream.class);
    verify(repository, times(0)).streamAll();
    verify(repository, times(1)).create(dt.capture(), is.capture());
    verify(repository, times(0)).update(Mockito.any(DocumentTemplate.class), Mockito.any(InputStream.class));
    verify(repository, times(0)).delete(Mockito.any(DocumentTemplate.class));
    assertThat(dt.getValue(), sameInstance(newTemplate));
    assertThat(newTemplate.getId(), notNullValue());
    assertThat(is.getValue(), sameInstance(content));
  }

  @DisplayName("Creating document template without a position set automatically the postion")
  @Test
  void createWithoutPosition() {
    final DocumentTemplate newTemplate = new DocumentTemplate(DEFAULT_TEMPLATE);
    newTemplate.setId(null);
    newTemplate.setPosition(-1);
    final ByteArrayInputStream content = new ByteArrayInputStream(SIMPLE_CONTENT.getBytes(UTF_8));
    assertThat(newTemplate.getId(), nullValue());
    service.put(newTemplate, content);
    assertThat(newTemplate.getPosition(), is(0));
    final ArgumentCaptor<DocumentTemplate> dt = forClass(DocumentTemplate.class);
    final ArgumentCaptor<InputStream> is = forClass(InputStream.class);
    verify(repository, times(1)).streamAll();
    verify(repository, times(1)).create(dt.capture(), is.capture());
    verify(repository, times(0)).update(Mockito.any(DocumentTemplate.class), Mockito.any(InputStream.class));
    verify(repository, times(0)).delete(Mockito.any(DocumentTemplate.class));
    assertThat(dt.getValue(), sameInstance(newTemplate));
    assertThat(newTemplate.getId(), notNullValue());
    assertThat(is.getValue(), sameInstance(content));
  }

  @DisplayName("Putting document template with an id updates the document template into repository")
  @Test
  void update() {
    final DocumentTemplate updateTemplate = new DocumentTemplate(DEFAULT_TEMPLATE);
    final ByteArrayInputStream content = new ByteArrayInputStream(SIMPLE_CONTENT.getBytes(UTF_8));
    final String idBeforeUpdate = updateTemplate.getId();
    assertThat(idBeforeUpdate, notNullValue());
    service.put(updateTemplate, content);
    final ArgumentCaptor<DocumentTemplate> dt = forClass(DocumentTemplate.class);
    final ArgumentCaptor<InputStream> is = forClass(InputStream.class);
    verify(repository, times(0)).streamAll();
    verify(repository, times(0)).create(Mockito.any(DocumentTemplate.class), Mockito.any(InputStream.class));
    verify(repository, times(1)).update(dt.capture(), is.capture());
    verify(repository, times(0)).delete(Mockito.any(DocumentTemplate.class));
    assertThat(dt.getValue(), sameInstance(updateTemplate));
    assertThat(updateTemplate.getId(), notNullValue());
    assertThat(updateTemplate.getId(), is(idBeforeUpdate));
    assertThat(is.getValue(), sameInstance(content));
  }

  @DisplayName("Removing document template deletes the document template from repository")
  @Test
  void delete() {
    final DocumentTemplate deleteTemplate = new DocumentTemplate(DEFAULT_TEMPLATE);
    final String idBeforeUpdate = deleteTemplate.getId();
    assertThat(idBeforeUpdate, notNullValue());
    service.remove(deleteTemplate);
    final ArgumentCaptor<DocumentTemplate> dt = forClass(DocumentTemplate.class);
    verify(repository, times(0)).streamAll();
    verify(repository, times(0)).create(Mockito.any(DocumentTemplate.class), Mockito.any(InputStream.class));
    verify(repository, times(0)).update(Mockito.any(DocumentTemplate.class), Mockito.any(InputStream.class));
    verify(repository, times(1)).delete(dt.capture());
    assertThat(dt.getValue(), sameInstance(deleteTemplate));
    assertThat(deleteTemplate.getId(), notNullValue());
    assertThat(deleteTemplate.getId(), is(idBeforeUpdate));
  }

  @DisplayName("Listing all document templates should be sorted on position attribute")
  @Test
  void listAll() {
    final List<DocumentTemplate> unsortedList = List.of(createTemplateInstance(3),
        createTemplateInstance(2), createTemplateInstance(5), createTemplateInstance(1),
        createTemplateInstance(4));
    assertThat(
        unsortedList.stream().map(DocumentTemplate::getPosition).collect(Collectors.toList()),
        contains(3, 2, 5, 1, 4));
    when(repository.streamAll()).thenReturn(unsortedList.stream());
    final List<DocumentTemplate> sortedList = service.listAll();
    assertThat(
        sortedList.stream().map(DocumentTemplate::getPosition).collect(Collectors.toList()),
        contains(1, 2, 3, 4, 5));
  }
}