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

package org.silverpeas.core.contribution.publication.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygManager;
import org.silverpeas.core.contribution.publication.dao.PublicationDAO;
import org.silverpeas.core.contribution.publication.dao.PublicationFatherDAO;
import org.silverpeas.core.contribution.publication.dao.PublicationI18NDAO;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.notification.PublicationEventNotifier;
import org.silverpeas.core.contribution.rating.service.RatingService;
import org.silverpeas.core.index.indexing.model.FileDescription;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.io.media.image.thumbnail.service.ThumbnailService;
import org.silverpeas.core.node.coordinates.service.CoordinatesService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.persistence.jdbc.ConnectionPool;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Checks the index entries produced for a publication having aliases, that is to say placed in
 * other locations than its main one. An alias in the same component instance as the publication
 * only adds a path to the index entry of the publication. An alias in another component instance
 * gets its own index entry, scoped to that instance, and this entry must carry the documents
 * attached to the publication, as those documents are hosted by the component instance of the
 * publication and not by the one of the alias.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PublicationAliasIndexationTest {

  private static final String PUB_ID = "10";
  private static final String HOST_INSTANCE = "kmelia1";
  private static final String OTHER_INSTANCE = "kmelia6";
  private static final PublicationPK PUB_PK = new PublicationPK(PUB_ID, HOST_INSTANCE);
  private static final ResourceReference PUB_REF = new ResourceReference(PUB_ID, HOST_INSTANCE);
  private static final String ATTACHMENT_PATH = "/data/kmelia1/10/wrappers.doc";
  private static final String ALIAS_CREATOR_ID = "1";

  @TestManagedMock
  private IndexEngineProxy indexEngineProxy;

  @TestManagedMock
  private AttachmentService attachmentService;

  @TestManagedMock
  private NodeService nodeService;

  @TestManagedMock
  private CoordinatesService coordinatesService;

  @TestManagedMock
  private RatingService ratingService;

  @TestManagedMock
  private PublicationEventNotifier notifier;

  @TestManagedMock
  private WysiwygManager wysiwygManager;

  @TestManagedMock
  private ThumbnailService thumbnailService;

  @TestManagedMock
  private ConnectionPool connectionPool;

  @TestedBean
  private DefaultPublicationService service;

  private MockedStatic<PublicationDAO> publicationDAO;
  private MockedStatic<PublicationFatherDAO> publicationFatherDAO;
  private MockedStatic<PublicationI18NDAO> publicationI18NDAO;

  @BeforeEach
  void setUp() throws SQLException {
    // the creator of the aliases
    when(UserProvider.get().getUser(ALIAS_CREATOR_ID)).thenReturn(mock(User.class));
    when(connectionPool.getDataSourceConnection()).thenReturn(mock(Connection.class));
    publicationDAO = mockStatic(PublicationDAO.class);
    publicationFatherDAO = mockStatic(PublicationFatherDAO.class);
    publicationI18NDAO = mockStatic(PublicationI18NDAO.class);
    publicationDAO.when(() -> PublicationDAO.selectByPrimaryKey(any(), argThat(PUB_PK::equals)))
        .thenReturn(validPublication());
    when(nodeService.getDetail(any(NodePK.class))).thenAnswer(i -> folder(i.getArgument(0)));
    // the attachment service enriches the given index entry with the documents of the given
    // resource, whatever the component instance the index entry is scoped to
    doAnswer(i -> {
      FullIndexEntry indexEntry = i.getArgument(0);
      ResourceReference owner = i.getArgument(1);
      indexEntry.addFileContent("/data/" + owner.getInstanceId() + "/" + owner.getId() +
          "/wrappers.doc", "UTF-8", "application/msword", "fr");
      return null;
    }).when(attachmentService).updateIndexEntryWithDocuments(any(), any());
  }

  @AfterEach
  void tearDown() {
    publicationDAO.close();
    publicationFatherDAO.close();
    publicationI18NDAO.close();
  }

  @Test
  void anAliasInAnotherInstanceIsIndexedWithTheDocumentsOfThePublication() {
    Location main = mainLocation("0");
    Location alias = alias("8", OTHER_INSTANCE);
    locationsAre(List.of(main, alias));

    service.addAliases(PUB_PK, List.of(alias));

    Map<IndexEntryKey, FullIndexEntry> indexed = indexedEntries();
    FullIndexEntry aliasEntry = indexed.get(indexEntryKey(OTHER_INSTANCE));
    assertThat(aliasEntry.isAlias(), is(true));
    assertThat(aliasEntry.getPaths(), contains("/0/8/"));
    assertThat(filePaths(aliasEntry), contains(ATTACHMENT_PATH));
    verify(attachmentService).updateIndexEntryWithDocuments(eq(aliasEntry),
        argThat(PUB_REF::equals));
  }

  @Test
  void theMainIndexEntryOfThePublicationIsLeftToTheIndexingEngineForItsDocuments() {
    Location main = mainLocation("0");
    Location alias = alias("8", OTHER_INSTANCE);
    locationsAre(List.of(main, alias));

    service.addAliases(PUB_PK, List.of(alias));

    Map<IndexEntryKey, FullIndexEntry> indexed = indexedEntries();
    FullIndexEntry mainEntry = indexed.get(indexEntryKey(HOST_INSTANCE));
    assertThat(mainEntry.isAlias(), is(false));
    assertThat(mainEntry.getPaths(), contains("/0/"));
    assertThat(filePaths(mainEntry), is(empty()));
    verify(attachmentService, never()).updateIndexEntryWithDocuments(eq(mainEntry), any());
  }

  @Test
  void anAliasInTheSameInstanceOnlyAddsAPathToTheIndexEntryOfThePublication() {
    Location main = mainLocation("4");
    Location alias = alias("12", HOST_INSTANCE);
    locationsAre(List.of(main, alias));

    service.addAliases(PUB_PK, List.of(alias));

    Map<IndexEntryKey, FullIndexEntry> indexed = indexedEntries();
    assertThat(indexed.keySet(), contains(indexEntryKey(HOST_INSTANCE)));
    FullIndexEntry entry = indexed.get(indexEntryKey(HOST_INSTANCE));
    assertThat(entry.isAlias(), is(false));
    assertThat(entry.getPaths(), containsInAnyOrder("/0/4/", "/0/12/"));
    assertThat(filePaths(entry), is(empty()));
    verify(attachmentService, never()).updateIndexEntryWithDocuments(any(), any());
  }

  @Test
  void theRemainingAliasInAnotherInstanceKeepsTheDocumentsOfThePublicationOnceAnotherOneRemoved() {
    Location main = mainLocation("0");
    Location removed = alias("8", OTHER_INSTANCE);
    Location remaining = alias("9", OTHER_INSTANCE);
    locationsAre(List.of(main, remaining));

    service.removeAliases(PUB_PK, List.of(removed));

    Map<IndexEntryKey, FullIndexEntry> indexed = indexedEntries();
    assertThat(indexed, not(hasKey(indexEntryKey(HOST_INSTANCE))));
    FullIndexEntry aliasEntry = indexed.get(indexEntryKey(OTHER_INSTANCE));
    assertThat(aliasEntry.isAlias(), is(true));
    assertThat(aliasEntry.getPaths(), contains("/0/9/"));
    assertThat(filePaths(aliasEntry), contains(ATTACHMENT_PATH));
  }

  @Test
  void theIndexEntryOfTheLastRemovedAliasInAnotherInstanceIsDeleted() {
    Location main = mainLocation("0");
    Location removed = alias("8", OTHER_INSTANCE);
    locationsAre(List.of(main));

    service.removeAliases(PUB_PK, List.of(removed));

    verify(indexEngineProxy).delete(indexEntryKey(OTHER_INSTANCE));
    verify(indexEngineProxy, never()).add(any());
  }

  /**
   * Sets the locations of the publication as they are once the operation under test has been
   * persisted into the database.
   */
  private void locationsAre(List<Location> locations) {
    publicationFatherDAO.when(() -> PublicationFatherDAO.getLocations(any(), argThat(PUB_PK::equals)))
        .thenReturn(locations);
    publicationFatherDAO.when(() -> PublicationFatherDAO.getAliases(any(), argThat(PUB_PK::equals)))
        .thenReturn(locations.stream().filter(Location::isAlias).collect(Collectors.toList()));
    publicationFatherDAO.when(
            () -> PublicationFatherDAO.getLocations(any(), argThat(PUB_PK::equals), any()))
        .thenAnswer(i -> locations.stream()
            .filter(l -> l.getInstanceId().equals(i.getArgument(2)))
            .collect(Collectors.toList()));
  }

  private static Location mainLocation(String nodeId) {
    return new Location(nodeId, HOST_INSTANCE);
  }

  private static Location alias(String nodeId, String instanceId) {
    Location alias = new Location(nodeId, instanceId);
    alias.setAsAlias(ALIAS_CREATOR_ID, new Date());
    return alias;
  }

  private static PublicationDetail validPublication() {
    PublicationDetail publication = PublicationDetail.builder("fr")
        .setPk(PUB_PK)
        .setNameAndDescription("Wrappers to the Rescue", "")
        .created(new Date(), "1")
        .updated(new Date(), "1")
        .build();
    publication.setStatus(PublicationDetail.VALID_STATUS);
    publication.setInfoId("0");
    return publication;
  }

  private static NodeDetail folder(NodePK pk) {
    NodeDetail folder = mock(NodeDetail.class);
    when(folder.getFullPath()).thenReturn("/0/" + ("0".equals(pk.getId()) ? "" : pk.getId() + "/"));
    return folder;
  }

  private static IndexEntryKey indexEntryKey(String instanceId) {
    return new IndexEntryKey(instanceId, "Publication", PUB_ID);
  }

  private static List<String> filePaths(FullIndexEntry entry) {
    return entry.getFileContentList().stream()
        .map(FileDescription::getPath)
        .collect(Collectors.toList());
  }

  private Map<IndexEntryKey, FullIndexEntry> indexedEntries() {
    ArgumentCaptor<FullIndexEntry> entries = ArgumentCaptor.forClass(FullIndexEntry.class);
    verify(indexEngineProxy, atLeastOnce()).add(entries.capture());
    return entries.getAllValues().stream()
        .collect(Collectors.toMap(FullIndexEntry::getPK, e -> e));
  }
}
