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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.impl.oak.configuration;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.silverpeas.core.test.unit.UnitTest;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test the Oak repository configuration is correctly loaded from a properties file and this with
 * different configuration parameters for the repository node storage.
 * @author mmoquillon
 */
@UnitTest
class OakRepositoryConfigurationTest {

  @ParameterizedTest
  @DisplayName("Whatever the storage backend set, the segment and the document storage " +
      "configurations are both loaded")
  @ValueSource(strings = {"classpath:/silverpeas-oak.properties",
      "classpath:/silverpeas-oak-segment.properties",
      "classpath:/silverpeas-oak-document.properties"
  })
  void loadDocumentAndSegmentStorageConfigurationsForMemoryStorage(String confPath)
      throws IOException {
    OakRepositoryConfiguration configuration = OakRepositoryConfiguration.load(confPath);
    assertThat(configuration.getSegmentNodeStoreConfiguration(), not(nullValue()));
    assertThat(configuration.getDocumentNodeStoreConfiguration(), not(nullValue()));
  }

  @Test
  @DisplayName("Memory storage setting should be loaded")
  void loadConfForMemoryStorage() throws IOException {
    final String confPath = "classpath:/silverpeas-oak.properties";
    OakRepositoryConfiguration configuration = OakRepositoryConfiguration.load(confPath);
    assertThat(configuration.getStorageType(), Matchers.is(StorageType.MEMORY_NODE_STORE));
  }

  @Test
  @DisplayName("Segment storage configuration should be loaded")
  void loadConfForSegmentStorage() throws IOException {
    final String confPath = "classpath:/silverpeas-oak-segment.properties";
    OakRepositoryConfiguration configuration = OakRepositoryConfiguration.load(confPath);
    assertThat(configuration.getStorageType(), Matchers.is(StorageType.SEGMENT_NODE_STORE));

    SegmentNodeStoreConfiguration segmentStoreConf =
        configuration.getSegmentNodeStoreConfiguration();
    // specific settings
    assertThat(segmentStoreConf.getStoragePath(), is("segmentstore"));
    // default values
    assertThat(segmentStoreConf.getTarMaxSize(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.MAX_TAR_SIZE));
    assertThat(segmentStoreConf.getSegmentCacheSize(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.SEGMENT_CACHE_SIZE));
    assertThat(segmentStoreConf.getStringCacheSize(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.STRING_CACHE_SIZE));
    assertThat(segmentStoreConf.getTemplateCacheSize(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.TEMPLATE_CACHE_SIZE));
    assertThat(segmentStoreConf.getStringDeduplicationCacheSize(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.STRING_DEDUPLICATION_CACHE_SIZE));
    assertThat(segmentStoreConf.getTemplateDeduplicationCacheSize(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.TEMPLATE_DEDUPLICATION_CACHE_SIZE));
    assertThat(segmentStoreConf.getNodeDeduplicationCacheSize(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.NODE_DEDUPLICATION_CACHE_SIZE));
    assertThat(segmentStoreConf.isPauseCompaction(), Matchers.is(true));
    assertThat(segmentStoreConf.getCompactionCRON(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.COMPACTION_CRON));
    assertThat(segmentStoreConf.getBackupFileAgeThreshold(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.COMPACTION_BACKUP_FILE_AGE_THRESHOLD));
    assertThat(segmentStoreConf.getCompactionRetryCount(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.COMPACTION_RETRY_COUNT));
    assertThat(segmentStoreConf.getCompactionForceTimeout(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.COMPACTION_FORCE_TIMEOUT));
    assertThat(segmentStoreConf.getCompactionSizeDeltaEstimation(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.COMPACTION_SIZE_DELTA_ESTIMATION));
    assertThat(segmentStoreConf.isCompactionDisableEstimation(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.COMPACTION_DISABLE_ESTIMATION));
    assertThat(segmentStoreConf.getCompactionMemoryThreshold(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.COMPACTION_MEMORY_THRESHOLD));
    assertThat(segmentStoreConf.getCompactionProgressLog(),
        Matchers.is(SegmentNodeStoreConfiguration.DefaultValues.COMPACTION_PROGRESS_LOG));
  }

  @Test
  @DisplayName("Document storage configuration should be loaded")
  void loadConfForDocumentStorage() throws IOException {
    final String confPath = "classpath:/silverpeas-oak-document.properties";
    OakRepositoryConfiguration configuration = OakRepositoryConfiguration.load(confPath);
    assertThat(configuration.getStorageType(), Matchers.is(StorageType.DOCUMENT_NODE_STORE));

    DocumentNodeStoreConfiguration nodeStoreConf =
        configuration.getDocumentNodeStoreConfiguration();
    // specific settings
    assertThat(nodeStoreConf.getUri(), is("mongodb://localhost:27017"));
    assertThat(nodeStoreConf.getDocumentStoreType(), Matchers.is(
        DocumentNodeStoreConfiguration.DocumentStoreType.MONGO));
    assertThat(nodeStoreConf.getDBName(), is("silverpeas"));
    // default values
    assertThat(nodeStoreConf.getSocketKeepAlive(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.SOCKET_KEEP_ALIVE));
    assertThat(nodeStoreConf.getCacheSize(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.CACHE_SIZE));
    assertThat(nodeStoreConf.getMaxReplicationLag(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.MAX_REPLICATION_LAG));
    assertThat(nodeStoreConf.getVersionGCMaxAge(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.VERSION_GC_MAX_AGE));
    assertThat(nodeStoreConf.getJournalGCMaxAge(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.JOURNAL_GC_MAX_AGE));
    assertThat(nodeStoreConf.getBlobCacheSize(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.BLOB_CACHE_SIZE));
    assertThat(nodeStoreConf.getSubtreesInPersistentCache(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.PERSISTENT_CACHE_CONTENT));
    assertThat(nodeStoreConf.getNodeCachePercentage(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.NODE_CACHE_PERCENTAGE));
    assertThat(nodeStoreConf.getPrevDocCachePercentage(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.PREV_DOC_CACHE_PERCENTAGE));
    assertThat(nodeStoreConf.getChildrenCachePercentage(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.CHILDREN_CACHE_PERCENTAGE));
    assertThat(nodeStoreConf.getDiffCachePercentage(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.DIFF_CACHE_PERCENTAGE));
    assertThat(nodeStoreConf.getCacheSegmentCount(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.CACHE_SEGMENT_COUNT));
    assertThat(nodeStoreConf.getCacheStackMoveDistance(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.CACHE_STACK_MOVE_DISTANCE));
    assertThat(nodeStoreConf.getUpdateNbLimit(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.UPDATE_COUNT_THRESHOLD));
    assertThat(nodeStoreConf.getLeaseCheckMode(),
        Matchers.is(DocumentNodeStoreConfiguration.DefaultValues.LEASE_CHECK_MODE));
  }
}