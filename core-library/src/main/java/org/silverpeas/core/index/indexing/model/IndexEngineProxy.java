/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing.model;

import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;

/**
 * A proxy to the Indexing Engine. It delegates all the call to the underlying indexing engine.
 */
@Singleton
public final class IndexEngineProxy {

  /**
   * The IndexEngine class is only used via static methods and no IndexEngine object will ever be
   * constructed.
   */
  private IndexEngineProxy() {
  }

  /**
   * Adds the specified entry in the indexes.
   * @param entry the index to add.
   */
  public void add(FullIndexEntry entry) {
    IndexerTask.addIndexEntry(entry);
  }

  /**
   * Removes from the indexes the entry identified by the specified key.
   * @param entryKey the key of the entry in the indexes.
   */
  public void delete(IndexEntryKey entryKey) {
    IndexerTask.removeIndexEntry(entryKey);
  }

  /**
   * Removes from the index the entries matching the specified scope.
   * @param scope the scope of the entries in the index.
   */
  public void deleteByScope(String scope) {
    IndexerTask.removeIndexEntriesByScope(scope);
  }

  /**
   * Removes all the index entries.
   */
  public void deleteAll() {
    IndexerTask.removeAllIndexEntries();
  }

  public static IndexEngineProxy get() {
    return ServiceProvider.getSingleton(IndexEngineProxy.class);
  }

  /**
   * Adds an entry index.
   */
  public static void addIndexEntry(FullIndexEntry indexEntry) {
    get().add(indexEntry);
  }

  /**
   * Removes an entry index.
   */
  public static void removeIndexEntry(IndexEntryKey indexEntry) {
    get().delete(indexEntry);
  }

  /**
   * Removes an entry index.
   */
  public static void removeScopedIndexEntries(String scope) {
    get().deleteByScope(scope);
  }

  /**
   * Removes all entry indexes.
   */
  public static void removeAllIndexEntries() {
    get().deleteAll();
  }
}
