/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class DocumentTemplateRestrictionFilterTest {

  @TestManagedMock
  private OrganizationController controller;

  private List<DocumentTemplate> documentTemplates;

  @TestedBean
  private DocumentTemplateRestrictionFilter filter;

  @BeforeEach
  void setup() {
    final DocumentTemplate notRestricted = new DocumentTemplate();
    final DocumentTemplate restricted = new DocumentTemplate();
    restricted.setRestrictedToSpaceIds(List.of("WA26", "WA25"));
    documentTemplates = List.of(notRestricted, restricted);
    when(controller.getPathToComponent(anyString())).thenAnswer(a -> {
      final String instanceId = a.getArgument(0);
      final List<String> spaceIds;
      if ("kmelia1".equals(instanceId)) {
        spaceIds = List.of("25", "24");
      } else {
        spaceIds = List.of("38", "37");
      }
      return spaceIds.stream().map(i -> {
        final SpaceInstLight spaceInstLight = new SpaceInstLight();
        spaceInstLight.setLocalId(Integer.parseInt(i));
        return spaceInstLight;
      }).collect(toList());
    });
  }

  @DisplayName("Filtering when no filter is set excludes document templates with restriction should work")
  @Test
  void filterWithoutInstanceContext() {
    final List<DocumentTemplate> filteredDocumentTemplates = getFilteredDocumentTemplates();
    assertThat(filteredDocumentTemplates.size(), is(1));
    assertThat(filteredDocumentTemplates.stream()
        .map(DocumentTemplate::getRestrictedToSpaceIds)
        .allMatch(List::isEmpty), is(true));
  }

  @DisplayName("Filtering on instance id hosted by space included into restriction of document template should work")
  @Test
  void filterWithInstanceContextHostedBySpaceIndicatedIntoRestriction() {
    filter.setInstanceId("kmelia1");
    final List<DocumentTemplate> filteredDocumentTemplates = getFilteredDocumentTemplates();
    assertThat(filteredDocumentTemplates.size(), is(2));
    assertThat(filteredDocumentTemplates.stream()
        .map(DocumentTemplate::getRestrictedToSpaceIds)
        .anyMatch(not(List::isEmpty)), is(true));
  }

  @DisplayName("Filtering on instance id hosted by space not included into restriction of document template should work")
  @Test
  void filterWithInstanceContextHostedBySpaceNotIndicatedIntoRestriction() {
    filter.setInstanceId("kmelia2");
    final List<DocumentTemplate> filteredDocumentTemplates = getFilteredDocumentTemplates();
    assertThat(filteredDocumentTemplates.size(), is(1));
    assertThat(filteredDocumentTemplates.stream()
        .map(DocumentTemplate::getRestrictedToSpaceIds)
        .allMatch(List::isEmpty), is(true));
  }

  private List<DocumentTemplate> getFilteredDocumentTemplates() {
    return documentTemplates.stream().filter(filter::applyOn).collect(toList());
  }
}