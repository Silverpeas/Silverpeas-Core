/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

/**
 * This interface is for services wanting the documents referred by an index entry to be also
 * indexed. Any implementation of this interface has to enrich the index entry with the documents
 * to index. These documents will be then parsed by the indexing engine.
 * </p>
 * Some times, some documents are attached to a contribution to index and it can be then useful to
 * index also the documents themselves. As the indexing engine doesn't know how to get such
 * documents, it delegates this task to the services with this knowledge. For doing, such services
 * have to implement this interface. The indexing engine will then invoke all the implementation
 * but
 * only one must enrich the index entry.
 * @author mmoquillon
 */
public interface DocumentIndexing {

  void updateIndexEntryWithDocuments(FullIndexEntry indexEntry);
}
