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

package org.silverpeas.core.web.index.components;

import org.silverpeas.core.admin.component.model.ComponentInst;

/**
 * Indexation of the data managed by a given component instance. Each Application in Silverpeas
 * should provide an implementation of this interface as it knows how to index it own data. This
 * implementation must be qualified by a unique name starting by the application name and ending by
 * the <code>QUALIFIER_SUFFIX</code> constant value (aka annotated by {@kink javax.inject.Named}).
 * The implementation will be then lookable by the index engine by their qualification name.
 */
public interface ComponentIndexation {

  String QUALIFIER_SUFFIX = "Indexation";

  /**
   * Indexes the data managed by the specified component instance.
   * @param componentInst the instance of the component managing the data to index or to reindex.
   * @throws Exception if an error occurs during the indexation.
   */
  void index(ComponentInst componentInst) throws Exception;
}