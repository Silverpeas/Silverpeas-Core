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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStoreBuilder;
import org.apache.jackrabbit.oak.plugins.document.LeaseCheckMode;
import org.apache.jackrabbit.oak.plugins.document.Path;
import org.apache.jackrabbit.oak.plugins.document.mongo.MongoDocumentNodeStoreBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.jcr.impl.ResourcesCloser;
import org.silverpeas.core.jcr.impl.oak.configuration.DocumentNodeStoreConfiguration;
import org.silverpeas.core.jcr.impl.oak.configuration.OakRepositoryConfiguration;
import org.silverpeas.core.jcr.impl.oak.configuration.StorageType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory of a {@link org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore} instance. This
 * is for the document storage in Oak. Currently, only the MongoDB as document-based database is
 * supported.
 * @author mmoquillon
 */
public class DocumentNodeStoreFactory implements NodeStoreFactory {

  private final Map<DocumentNodeStoreConfiguration.DocumentStoreType, Function<DocumentNodeStoreConfiguration, DocumentNodeStore>>
      nodeStoreBuilders = Map.of(
      DocumentNodeStoreConfiguration.DocumentStoreType.MONGO, this::createMongoNodeStore,
      DocumentNodeStoreConfiguration.DocumentStoreType.RDB, c -> {
        throw new NotSupportedException(
            "The relational database backend isn't currently supported as document-based " +
                "datasource");
      }
  );

  @Override
  public NodeStore create(final String jcrHomePath, final OakRepositoryConfiguration conf) {
    if (conf.getStorageType() != StorageType.DOCUMENT_NODE_STORE &&
        conf.getStorageType() != StorageType.COMPOSITE_NODE_STORE) {
      return null;
    }

    DocumentNodeStoreConfiguration docNodeConf = conf.getDocumentNodeStoreConfiguration();
    return nodeStoreBuilders.getOrDefault(docNodeConf.getDocumentStoreType(), c -> null)
        .apply(docNodeConf);
  }

  private DocumentNodeStore createMongoNodeStore(final DocumentNodeStoreConfiguration conf) {
    DocumentNodeStoreBuilder<?> builder =
        MongoDocumentNodeStoreBuilder.newMongoDocumentNodeStoreBuilder()
            // we use here a Guava executor (as it is by default by the builder). Perhaps we
            // should use an executor from our own org.silverpeas.core.thread.ManagedThreadPool
            // technical bean in which the threads pool of the underlying JEE server is used.
            .setExecutor(MoreExecutors.newDirectExecutorService())
            .setMongoDB(conf.getUri(), conf.getDBName(), conf.getBlobCacheSize())
            .setSocketKeepAlive(conf.getSocketKeepAlive())
            .memoryCacheSize(conf.getCacheSize())
            .memoryCacheDistribution(
                conf.getNodeCachePercentage(),
                conf.getPrevDocCachePercentage(),
                conf.getChildrenCachePercentage(),
                conf.getDiffCachePercentage())
            .setCacheSegmentCount(conf.getCacheSegmentCount())
            .setCacheStackMoveDistance(conf.getCacheStackMoveDistance())
            .setMaxReplicationLag(conf.getMaxReplicationLag(), TimeUnit.SECONDS)
            .setJournalGCMaxAge(conf.getJournalGCMaxAge())
            .setRevisionGCMaxAge(conf.getVersionGCMaxAge())
            .setLeaseCheckMode(LeaseCheckMode.valueOf(conf.getLeaseCheckMode()))
            .setNodeCachePathPredicate(createCachePredicate(conf))
            .setUpdateLimit(conf.getUpdateNbLimit());
    DocumentNodeStore store = builder.build();

    ResourcesCloser.get().register(store::dispose);

    return store;
  }

  @SuppressWarnings("Guava")
  private Predicate<Path> createCachePredicate(final DocumentNodeStoreConfiguration config) {
    List<String> subtrees = config.getSubtreesInPersistentCache();
    if (subtrees.isEmpty()) {
      return Predicates.alwaysTrue();
    }
    if (subtrees.contains("/")) {
      return Predicates.alwaysTrue();
    }

    final List<Path> subtreePaths = subtrees.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .map(Path::fromString)
        .collect(Collectors.toList());

    return p -> p != null && subtreePaths.stream().anyMatch(subtree -> subtree.isAncestorOf(p));
  }
}
