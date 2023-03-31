/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

import org.apache.jackrabbit.oak.api.Blob;
import org.apache.jackrabbit.oak.api.CommitFailedException;
import org.apache.jackrabbit.oak.segment.SegmentNodeStore;
import org.apache.jackrabbit.oak.segment.SegmentNodeStoreBuilders;
import org.apache.jackrabbit.oak.segment.compaction.SegmentGCOptions;
import org.apache.jackrabbit.oak.segment.file.FileStore;
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder;
import org.apache.jackrabbit.oak.segment.file.InvalidFileStoreVersionException;
import org.apache.jackrabbit.oak.spi.commit.CommitHook;
import org.apache.jackrabbit.oak.spi.commit.CommitInfo;
import org.apache.jackrabbit.oak.spi.commit.Observable;
import org.apache.jackrabbit.oak.spi.commit.Observer;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.jcr.impl.oak.configuration.OakRepositoryConfiguration;
import org.silverpeas.core.jcr.impl.oak.configuration.SegmentNodeStoreConfiguration;
import org.silverpeas.core.jcr.impl.oak.configuration.StorageType;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

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
          .build();
    } catch (InvalidFileStoreVersionException | IOException e) {
      throw new SilverpeasRuntimeException(e);
    }
    return new SegmentNodeStoreWrapper(SegmentNodeStoreBuilders.builder(fs).build(), fs);
  }

  /**
   * Disposes the specified {@link SegmentNodeStore}. It unlock the underlying segment storage so it
   * can be reused by another {@link SegmentNodeStore} instance. It is mandatory to dispose the
   * storage at application shutdown otherwise the storage couldn't be anymore reused by the
   * application and it is yet locked by a previous, not more existing, {@link SegmentNodeStore}
   * instance.
   * @param store the {@link NodeStore} instance to dispose.
   * @see NodeStoreFactory#dispose(NodeStore)
   */
  @Override
  public void dispose(final NodeStore store) {
    if (store instanceof SegmentNodeStoreWrapper) {
      ((SegmentNodeStoreWrapper) store).dispose();
    } else {
      throw new IllegalArgumentException(
          "The specified store isn't a SegmentStore managed by Silverpeas");
    }
  }

  /**
   * Wrapper of the {@link SegmentNodeStore} created by Oak over a {@link FileStore} to manage the
   * content of a JCR. This wrapper is to keep in memory both the {@link SegmentNodeStore} and the
   * {@link FileStore} objects used as backend for the JCR in order to close them once their usage
   * isn't more required (id est when the application is shutdown). Indeed, the {@link FileStore}
   * maintains a lock to ensure it is the single entry point to the content of the JCR. Hence, to
   * open another {@link FileStore}, it is required the lock was previously removed.
   */
  private static class SegmentNodeStoreWrapper implements NodeStore, Observable {

    private final SegmentNodeStore sns;
    private final FileStore fs;

    public SegmentNodeStoreWrapper(final SegmentNodeStore segmentNodeStore, FileStore fileStore) {
      this.fs = fileStore;
      this.sns = segmentNodeStore;
    }

    public void dispose() {
      this.fs.close();
    }

    @Override
    public Closeable addObserver(final Observer observer) {
      return sns.addObserver(observer);
    }

    @Override
    @Nonnull
    public NodeState getRoot() {
      return sns.getRoot();
    }

    @Override
    @Nonnull
    public NodeState merge(
        @Nonnull final NodeBuilder builder,
        @Nonnull final CommitHook commitHook,
        @Nonnull final CommitInfo info) throws CommitFailedException {
      return sns.merge(builder, commitHook, info);
    }

    @Override
    @Nonnull
    public NodeState rebase(
        @Nonnull final NodeBuilder builder) {
      return sns.rebase(builder);
    }

    @Override
    public NodeState reset(
        @Nonnull final NodeBuilder builder) {
      return sns.reset(builder);
    }

    @Override
    @Nonnull
    public Blob createBlob(final InputStream stream)
        throws IOException {
      return sns.createBlob(stream);
    }

    @Override
    public Blob getBlob(@Nonnull final String reference) {
      return sns.getBlob(reference);
    }

    @Override
    @Nonnull
    public String checkpoint(final long lifetime,
        @Nonnull final Map<String, String> properties) {
      return sns.checkpoint(lifetime, properties);
    }

    @Override
    @Nonnull
    public String checkpoint(final long lifetime) {
      return sns.checkpoint(lifetime);
    }

    @Override
    @Nonnull
    public Map<String, String> checkpointInfo(
        @Nonnull final String checkpoint) {
      return sns.checkpointInfo(checkpoint);
    }

    @Override
    @Nonnull
    public Iterable<String> checkpoints() {
      return sns.checkpoints();
    }

    @Override
    public NodeState retrieve(
        @Nonnull final String checkpoint) {
      return sns.retrieve(checkpoint);
    }

    @Override
    public boolean release(@Nonnull final String checkpoint) {
      return sns.release(checkpoint);
    }
  }
}
