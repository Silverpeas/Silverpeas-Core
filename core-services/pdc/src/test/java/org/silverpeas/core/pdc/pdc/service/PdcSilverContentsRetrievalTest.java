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
package org.silverpeas.core.pdc.pdc.service;

import jakarta.enterprise.inject.Instance;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentPeas;
import org.silverpeas.core.contribution.contentcontainer.content.DefaultGlobalSilverContentProcessor;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContentProcessor;
import org.silverpeas.core.contribution.contentcontainer.content.ManagedContribution;
import org.silverpeas.core.contribution.contentcontainer.content.SilverpeasContentManager;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.admin.component.model.SilverpeasComponentInstance.getIdentity;

/**
 * Unit tests on the retrieval of the contents classified on the PdC from their identifiers: the
 * contents of a component instance are converted into {@link GlobalSilverContent} instances by
 * the processor dedicated to the component, or by the default processor for the components
 * without a dedicated one.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PdcSilverContentsRetrievalTest {

  private static final String USER_ID = "2";
  private static final String ALMANACH_INSTANCE_ID = "almanach12";
  private static final String KMELIA_INSTANCE_ID = "kmelia3";
  private static final List<Integer> SILVER_CONTENT_IDS = List.of(42);

  private final ContentManagementEngine contentMgtEngine = mock(ContentManagementEngine.class);
  @SuppressWarnings("unchecked")
  private final Instance<GlobalSilverContentProcessor> processors = mock(Instance.class);
  private GlobalPdcManager pdcManager;

  @BeforeEach
  void setUpThePdcManager(@TestManagedMock I18n i18n) throws Exception {
    when(i18n.getDefaultLanguage()).thenReturn("fr");
    pdcManager = new GlobalPdcManager();
    FieldUtils.writeField(pdcManager, "contentMgtEngine", contentMgtEngine, true);
    FieldUtils.writeField(pdcManager, "contentProcessors", processors, true);
  }

  @Test
  void theContentsOfAComponentAreConvertedByItsOwnProcessor() throws Exception {
    aContentIn(KMELIA_INSTANCE_ID);
    final GlobalSilverContent expected = mock(GlobalSilverContent.class);
    final GlobalSilverContentProcessor kmeliaProcessor = aProcessorFor("kmelia", expected);
    whenTheProcessorsAre(kmeliaProcessor, new DefaultGlobalSilverContentProcessor());

    final List<GlobalSilverContent> contents =
        pdcManager.getSilverContentsByIds(SILVER_CONTENT_IDS, USER_ID);

    assertThat(contents, contains(expected));
  }

  @Test
  void theContentsOfAComponentWithoutItsOwnProcessorAreConvertedByTheDefaultOne()
      throws Exception {
    aContentIn(ALMANACH_INSTANCE_ID);
    final GlobalSilverContent expected = mock(GlobalSilverContent.class);
    final GlobalSilverContentProcessor defaultProcessor = aProcessorFor("default", expected);
    whenTheProcessorsAre(defaultProcessor);

    final List<GlobalSilverContent> contents =
        pdcManager.getSilverContentsByIds(SILVER_CONTENT_IDS, USER_ID);

    assertThat(contents, contains(expected));
  }

  @Test
  void theContentsOfAComponentAreStillConvertedWhenNoProcessorIsFound() throws Exception {
    aContentIn(ALMANACH_INSTANCE_ID);
    whenTheProcessorsAre();

    final List<GlobalSilverContent> contents =
        pdcManager.getSilverContentsByIds(SILVER_CONTENT_IDS, USER_ID);

    assertThat(contents.stream().map(GlobalSilverContent::getId).toList(), contains("7"));
  }

  @Test
  void theContentsOfTheOtherComponentsAreKeptWhenTheConversionOfAComponentContentsFails()
      throws Exception {
    aContentIn(ALMANACH_INSTANCE_ID, KMELIA_INSTANCE_ID);
    final GlobalSilverContent expected = mock(GlobalSilverContent.class);
    final GlobalSilverContentProcessor kmeliaProcessor = aProcessorFor("kmelia", expected);
    final GlobalSilverContentProcessor failingProcessor =
        mock(GlobalSilverContentProcessor.class);
    when(failingProcessor.relatedToComponent()).thenReturn("default");
    // the failure occurs while the stream of the converted contents is consumed
    when(failingProcessor.asGlobalSilverContent(anyList())).thenAnswer(i -> Stream.of("7")
        .map(id -> {
          throw new IllegalStateException("Conversion failure of the content " + id);
        }));
    whenTheProcessorsAre(kmeliaProcessor, failingProcessor);

    final List<GlobalSilverContent> contents =
        pdcManager.getSilverContentsByIds(SILVER_CONTENT_IDS, USER_ID);

    assertThat(contents, contains(expected));
  }

  private void aContentIn(final String... instanceIds) throws Exception {
    final List<ResourceReference> references = Stream.of(instanceIds)
        .map(instanceId -> new ResourceReference("7", instanceId))
        .toList();
    when(contentMgtEngine.getResourceReferencesByContentIds(SILVER_CONTENT_IDS))
        .thenReturn(references);
    for (final ResourceReference reference : references) {
      aContentManagerFor(reference);
    }
  }

  private void aContentManagerFor(final ResourceReference reference) throws Exception {
    final String instanceId = reference.getComponentInstanceId();
    final User creator = mock(User.class);
    when(creator.getId()).thenReturn(USER_ID);
    final ManagedContribution content = mock(ManagedContribution.class);
    when(content.getId()).thenReturn("7");
    when(content.getComponentInstanceId()).thenReturn(instanceId);
    when(content.getName()).thenReturn("A content");
    when(content.getDescription()).thenReturn("");
    when(content.getCreator()).thenReturn(creator);
    when(content.getCreationDate()).thenReturn(new Date());
    when(content.getLanguages()).thenReturn(List.of());

    final SilverpeasContentManager contentManager = mock(SilverpeasContentManager.class);
    when(contentManager.getSilverContentByReference(List.of(reference), USER_ID))
        .thenReturn(List.of(content));
    final ContentPeas contentPeas = mock(ContentPeas.class);
    when(contentPeas.getContentManager()).thenReturn(contentManager);
    when(contentMgtEngine.getContentPeasByComponentName(
        getIdentity(instanceId).getComponentName())).thenReturn(contentPeas);
  }

  private GlobalSilverContentProcessor aProcessorFor(final String componentName,
      final GlobalSilverContent result) {
    final GlobalSilverContentProcessor processor = mock(GlobalSilverContentProcessor.class);
    when(processor.relatedToComponent()).thenReturn(componentName);
    when(processor.asGlobalSilverContent(anyList())).thenAnswer(i -> Stream.of(result));
    return processor;
  }

  private void whenTheProcessorsAre(final GlobalSilverContentProcessor... someProcessors) {
    when(processors.stream()).thenAnswer(i -> Stream.of(someProcessors));
  }
}
