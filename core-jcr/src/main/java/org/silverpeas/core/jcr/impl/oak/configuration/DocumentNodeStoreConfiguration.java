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

import java.util.List;
import java.util.Properties;

/**
 * <p>
 * Configuration parameters of a document storage. A document storage is dedicated to be used for
 * applications requiring multiple access point to the storage like clustered services.
 * </p>
 * <p>
 * The Oak document storage is a database in which both node data and binaries are stored. Because
 * the database can be of different types, the DocumentNodeStore supports a number of backends, with
 * a storage abstraction called DocumentStore:
 * </p>
 * <ul>
 *   <li>MongoDocumentStore: stores documents in a MongoDB.</li>
 *   <li>RDBDocumentStore: stores documents in a relational data base.</li>
 *   <li>MemoryDocumentStore: keeps documents in memory. This implementation should only be used
 *   for testing purposes and it isn't addressed here.</li>
 * </ul>
 * <h3>The MongoDocumentStore</h1>
 * <p>
 *  The MongoDocumentStore use MongoDB to persist nodes as documents. For production deployments
 *  use a replica-set with at least three mongod instances and a majority write concern. Fewer
 *  than three instances (e.g. two instances and an arbiter) may lead to data loss when the
 *  primary fails.
 * <p>
 * When using MongoDB 3.4 or newer, set the maxStalenessSeconds option in the MongoDB URI to 90.
 * This is an additional safeguard and will prevent reads from a secondary that is too far behind.
 * <p>
 * Initializing a DocumentNodeStore on MongoDB with default values will also use MongoDB to store
 * blobs. While this is convenient for development and tests, the use of MongoDB as a blob store
 * in production is not recommended. MongoDB replicates all changes through a single op-log.
 * Large blobs can lead to a significantly reduced op-log window and cause delay in replicating
 * other changes between the replica-set members. See available blob stores alternatives for
 * production use.
 * <h3>The RDBDocumentStore</h3>
 * <p>
 * The RDBDocumentStore uses relational databases to persist nodes as documents, mainly emulating
 * the native capabilities of MongoDocumentStore. H2DB, PostgreSQL, Microsoft SQL Server, and
 * Oracle are supported.
 * <p>
 * It relies on JDBC, and thus, in general, can not create database instances (that said, certain
 * DBs such as Apache Derby or H2DB can create the database automatically when it's not there yet
 * - consult the DB documentation in general and the JDBC URL syntax specifically).
 * <p>
 * So in general, the administrator will have to take care of creating the database. There are
 * only a few requirements for the database, but these are critical for the correct operation:
 * </p>
 * <ul>
 * <li>character fields must be able to store any Unicode code point - UTF-8 encoding is
 * recommended</li>
 * <li>the collation for character fields needs to sort by Unicode code points</li>
 * <li>BLOBs need to support sizes of ~16MB</li>
 * </ul>
 * @author mmoquillon
 */
public class DocumentNodeStoreConfiguration extends NodeStoreConfiguration {

  /**
   * Default values of the different document node storage configuration parameters. Parameters that
   * aren't set in the configuration file are automatically valued with these defaults values
   * below.
   */
  public static class DefaultValues {
    public static final String DB_URI = "mongodb://localhost:27017";
    public static final String DB_NAME = "oak";
    public static final boolean SOCKET_KEEP_ALIVE = true;
    public static final int CACHE_SIZE = 256;
    public static final int MAX_REPLICATION_LAG = 21600;
    public static final int VERSION_GC_MAX_AGE = 86400;
    public static final long JOURNAL_GC_MAX_AGE = 86400000L;
    public static final int BLOB_CACHE_SIZE = 16;
    public static final List<String> PERSISTENT_CACHE_CONTENT = List.of("/");
    public static final int NODE_CACHE_PERCENTAGE = 35;
    public static final int PREV_DOC_CACHE_PERCENTAGE = 4;
    public static final int CHILDREN_CACHE_PERCENTAGE = 15;
    public static final int DIFF_CACHE_PERCENTAGE = 30;
    public static final int CACHE_SEGMENT_COUNT = 16;
    public static final int CACHE_STACK_MOVE_DISTANCE = 16;
    public static final int UPDATE_COUNT_THRESHOLD = 100000;
    public static final String LEASE_CHECK_MODE = "STRICT";
    public static final String STORE_TYPE = DocumentStoreType.MONGO.name();

    private DefaultValues() {

    }
  }

  /**
   * Specifies the MongoURI required to connect to Mongo Database.
   */
  public String getUri() {
    return getString("document.uri", DefaultValues.DB_URI);
  }

  /**
   * Name of the database in Mongo.
   */
  public String getDBName() {
    return getString("document.db", DefaultValues.DB_NAME);
  }

  /**
   * Enables socket keep-alive for MongoDB connections.
   */
  public boolean getSocketKeepAlive() {
    return getBoolean("document.socketKeepAlive", DefaultValues.SOCKET_KEEP_ALIVE);
  }

  /**
   * Cache size in MB. This is distributed among various caches used in DocumentNodeStore.
   */
  public int getCacheSize() {
    return getInteger("document.cache", DefaultValues.CACHE_SIZE);
  }

  /**
   * Determines the duration in seconds beyond which it can be safely assumed that state on
   * secondary would be consistent with primary, and it's safe to read from them. (See OAK-1645). By
   * default 6 hours.
   */
  public int getMaxReplicationLag() {
    return getInteger("document.maxReplicationLagInSecs", DefaultValues.MAX_REPLICATION_LAG);
  }

  /**
   * Oak uses MVCC model to store the data. So each update to a node results in new version getting
   * created. This duration controls how much old revision data should be kept. For example if a
   * node is deleted at time T1 then its content would only be marked deleted at revision for T1 but
   * its content would not be removed. Only when a Revision GC is run then its content would be
   * removed and that too only after (currentTime -T1 > versionGcMaxAgeInSecs).
   */
  public int getVersionGCMaxAge() {
    return getInteger("document.versionGCMaxAgeInSecs", DefaultValues.VERSION_GC_MAX_AGE);
  }

  /**
   * Journal entries older than journalGCMaxAge can be removed by the journal garbage collector. The
   * maximum age is specified in milliseconds. By default, 24 hours.
   */
  public long getJournalGCMaxAge() {
    return getLong("document.journalGCMaxAge", DefaultValues.JOURNAL_GC_MAX_AGE);
  }

  /**
   * DocumentNodeStore when running with Mongo will use MongoBlobStore by default unless a custom
   * BlobStore is configured. In such scenario the size of in memory cache in MB for the frequently
   * used blobs can be configured via blobCacheSize.
   */
  public int getBlobCacheSize() {
    return getInteger("document.blobCacheSize", DefaultValues.BLOB_CACHE_SIZE);
  }

  /**
   * List of paths defining the subtrees to cache.
   */
  public List<String> getSubtreesInPersistentCache() {
    return getList("document.persistentCacheIncludes", DefaultValues.PERSISTENT_CACHE_CONTENT);
  }

  /**
   * Percentage of cache allocated for nodeCache.
   */
  public int getNodeCachePercentage() {
    return getInteger("document.nodeCachePercentage", DefaultValues.NODE_CACHE_PERCENTAGE);
  }

  /**
   * Percentage of cache allocated for prevDocCache.
   */
  public int getPrevDocCachePercentage() {
    return getInteger("document.prevDocCachePercentage", DefaultValues.PREV_DOC_CACHE_PERCENTAGE);
  }

  /**
   * Percentage of cache allocated for childrenCache.
   */
  public int getChildrenCachePercentage() {
    return getInteger("document.childrenCachePercentage", DefaultValues.CHILDREN_CACHE_PERCENTAGE);
  }

  /**
   * Percentage of cache allocated for diffCache.
   */
  public int getDiffCachePercentage() {
    return getInteger("document.diffCachePercentage", DefaultValues.DIFF_CACHE_PERCENTAGE);
  }

  /**
   * The number of segments in the LIRS cache.
   */
  public int getCacheSegmentCount() {
    return getInteger("document.cacheSegmentCount", DefaultValues.CACHE_SEGMENT_COUNT);
  }

  /**
   * The delay to move entries to the head of the queue in the LIRS cache.
   */
  public int getCacheStackMoveDistance() {
    return getInteger("document.cacheStackMoveDistance", DefaultValues.CACHE_STACK_MOVE_DISTANCE);
  }

  /**
   * The number of updates kept in memory until changes are written to a branch in the
   * DocumentStore.
   */
  public int getUpdateNbLimit() {
    return getInteger("document.updateLimit", DefaultValues.UPDATE_COUNT_THRESHOLD);
  }

  /**
   * The lease check mode. STRICT is the default and will stop the DocumentNodeStore as soon as the
   * lease expires. LENIENT will give the background lease update a chance to renew the lease even
   * when the lease expired. This mode is only recommended for development, e.g. when debugging an
   * application and the lease may expire when the JVM is stopped at a breakpoint.
   */
  public String getLeaseCheckMode() {
    return getString("document.leaseCheckMode", DefaultValues.LEASE_CHECK_MODE);
  }

  /**
   * "MONGO" for MongoDocumentStore, “RDB” for RDBDocumentStore. Latter will require a configured
   * Sling DataSource called oak.
   */
  public DocumentStoreType getDocumentStoreType() {
    return DocumentStoreType.valueOf(getString("document.storeType", DefaultValues.STORE_TYPE));
  }

  DocumentNodeStoreConfiguration(Properties props) {
    super(props);
  }

  /**
   * The type of document-based datasource.
   */
  public enum DocumentStoreType {
    /**
     * The datasource is MongoDB.
     */
    MONGO,
    /**
     * The datasource is a relational database.
     */
    RDB;

    @Override
    public String toString() {
      String name = name();
      return name.charAt(0) + name.substring(1).toLowerCase();
    }
  }

}
