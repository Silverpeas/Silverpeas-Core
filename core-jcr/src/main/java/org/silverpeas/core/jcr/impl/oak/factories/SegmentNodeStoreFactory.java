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

package org.silverpeas.core.jcr.impl.oak.factories;

import org.apache.jackrabbit.oak.segment.SegmentNodeStore;
import org.apache.jackrabbit.oak.segment.SegmentNodeStoreBuilders;
import org.apache.jackrabbit.oak.segment.compaction.SegmentGCOptions;
import org.apache.jackrabbit.oak.segment.file.FileStore;
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder;
import org.apache.jackrabbit.oak.segment.file.InvalidFileStoreVersionException;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.jcr.impl.ResourcesCloser;
import org.silverpeas.core.jcr.impl.oak.configuration.OakRepositoryConfiguration;
import org.silverpeas.core.jcr.impl.oak.configuration.SegmentNodeStoreConfiguration;
import org.silverpeas.core.jcr.impl.oak.configuration.StorageType;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;

/**
 * Factory of a {@link org.apache.jackrabbit.oak.segment.SegmentNodeStore} instance. This is for the
 * segment storage in Oak. Only a single {@link NodeStore} instance can be created for a given
 * segment storage. If two {@link NodeStore} instances are created for the same storage, then an
 * {@link IllegalStateException} is thrown as only one {@link NodeStore} object can access the
 * underlying storage.
 * @author mmoquillon
 */
public class SegmentNodeStoreFactory implements NodeStoreFactory {

  /**
   * Creates a {@link SegmentNodeStore} instance for the storage on the filesystem defined in the
   * specified configuration. If such a storage doesn't exist, it is created and initialized before
   * being opened. The usage of the storage is locked by the returned {@link NodeStore} instance; so
   * only one {@link SegmentNodeStore} object can be created for a given storage, otherwise an
   * {@link IllegalStateException} is thrown.
   * @param jcrHomePath the absolute path of the home directory of the JCR.
   * @param conf the JCR configuration with the parameters required to either create and initialize
   * or to open the node storage. The segment storage location on the filesystem must be indicated
   * by the property {@code segment.repository.home}.
   * @return a {@link SegmentNodeStore} instance.
   * @see NodeStoreFactory#create(String, OakRepositoryConfiguration)
   */
  @Override
  public NodeStore create(final String jcrHomePath, final OakRepositoryConfiguration conf) {
    if (conf.getStorageType() != StorageType.SEGMENT_NODE_STORE &&
        conf.getStorageType() != StorageType.COMPOSITE_NODE_STORE) {
      return null;
    }

    SegmentNodeStoreConfiguration parameters = conf.getSegmentNodeStoreConfiguration();
    Path storagePath = Path.of(parameters.getStoragePath());
    Path segmentStore =
        storagePath.isAbsolute() ? storagePath : Path.of(jcrHomePath).resolve(storagePath);
    FileStore fs;
    try {
      fs = FileStoreBuilder.fileStoreBuilder(segmentStore.toFile())
          .withMaxFileSize(parameters.getTarMaxSize())
          .withSegmentCacheSize(parameters.getSegmentCacheSize())
          .withStringCacheSize(parameters.getStringCacheSize())
          .withTemplateCacheSize(parameters.getTemplateCacheSize())
          .withStringDeduplicationCacheSize(parameters.getStringDeduplicationCacheSize())
          .withTemplateDeduplicationCacheSize(parameters.getTemplateDeduplicationCacheSize())
          .withNodeDeduplicationCacheSize(parameters.getNodeDeduplicationCacheSize())
          .withGCOptions(SegmentGCOptions.defaultGCOptions()
              .setPaused(parameters.isPauseCompaction())
              .setRetryCount(parameters.getCompactionRetryCount())
              .setForceTimeout(parameters.getCompactionForceTimeout())
              .setGcSizeDeltaEstimation(parameters.getCompactionSizeDeltaEstimation())
              .setEstimationDisabled(parameters.isCompactionDisableEstimation())
              .setMemoryThreshold(parameters.getCompactionMemoryThreshold())
              .setGCLogInterval(parameters.getCompactionProgressLog()))
          .withGCMonitor(new GCLogger())
          .build();
      initializeCompaction(segmentStore, fs, parameters);

      ResourcesCloser.get().register(fs);

    } catch (InvalidFileStoreVersionException | IOException | ParseException e) {
      throw new SilverpeasRuntimeException(e);
    }

    return SegmentNodeStoreBuilders.builder(fs).build();
  }

  private void initializeCompaction(final Path segmentStore, final FileStore fs,
      final SegmentNodeStoreConfiguration parameters) throws ParseException {
    // initialize compaction process
    if (!parameters.isPauseCompaction()) {
      final SegmentNodeStoreCleaner cleaner = SegmentNodeStoreCleaner.get();
      try {
        cleaner.initializeWith(segmentStore, fs, parameters);
        cleaner.execute();
      } catch (SilverpeasException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }
}
