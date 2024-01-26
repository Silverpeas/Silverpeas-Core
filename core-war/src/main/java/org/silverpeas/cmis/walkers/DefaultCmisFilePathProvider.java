/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.cmis.walkers;

import org.silverpeas.cmis.Filtering;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.core.cmis.model.CmisFile;
import org.silverpeas.core.cmis.model.CmisFilePath;
import org.silverpeas.core.cmis.model.CmisFilePathProvider;
import org.silverpeas.core.cmis.model.Space;

import javax.inject.Inject;

@Technical
@Provider
public class DefaultCmisFilePathProvider implements CmisFilePathProvider {

  @Inject
  private TreeWalkerSelector selector;

  @Override
  public CmisFilePath getPath(final CmisFile file) {
    CmisFilePathImpl path = new CmisFilePathImpl();
    return walkUpPath(file, path);
  }

  private CmisFilePathImpl walkUpPath(final CmisFile file, CmisFilePathImpl path) {
    if (Space.ROOT_ID.asString().equals(file.getId())) {
      return path;
    } else {
      path.addNodeLabel(file.getLabel());
      CmisFile parent = (CmisFile) selector.selectByObjectIdOrFail(file.getParentId())
          .getObjectData(file.getParentId(), new Filtering());
      return walkUpPath(parent, path);
    }
  }

  static class CmisFilePathImpl implements CmisFilePath {

    private final StringBuilder path = new StringBuilder();

    public void addNodeLabel(final String node) {
      path.insert(0, node)
          .insert(0, PATH_SEPARATOR);
    }

    @Override
    public String toString() {
      return path.toString();
    }
  }
}
