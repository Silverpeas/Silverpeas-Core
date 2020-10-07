/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.cmis.walkers;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.silverpeas.core.annotation.Service;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A selector of the correct {@link AbstractCmisObjectsTreeWalker} object to use for walking the subtree
 * rooted to a node of a given type in the CMIS objects tree. The selector plays then the role of
 * the delegator to the {@link AbstractCmisObjectsTreeWalker} object that knows how to handle the CMIS
 * object (and hence the Silverpeas object mapped with it) that is implied in the method invocation.
 * <p>
 * The CMIS objects tree is made up of branches whose nodes can be of several types. Each type
 * provides to the node some properties like, for example, the types of nodes it can have as
 * children. So, because walking the subtree rooted to a given node depends of the type of that
 * node, it is required for each type of node to have a specific {@link AbstractCmisObjectsTreeWalker}
 * object. The selector responsibility is to find this walker and then to delegate the method call
 * to it.
 * </p>
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerSelector {

  @Inject @Any
  private Instance<AbstractCmisObjectsTreeWalker> walkers;

  protected CmisObjectsTreeWalker selectByObjectId(final String objectId) {
    return walkers.stream()
        .filter(w -> w.isSupported(objectId))
        .findFirst()
        .orElseThrow(() -> new CmisObjectNotFoundException(
            String.format("Object %s not exposed in the CMIS objects tree", objectId)));
  }
}
  