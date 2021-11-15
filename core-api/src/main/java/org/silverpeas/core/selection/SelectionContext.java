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

package org.silverpeas.core.selection;

/**
 * The context of a selection by a user of a resource in Silverpeas. It explains in what aim a
 * resource has been put into the {@link SelectionBasket}, in other words for what operation the
 * selection has been done. As such it can contain additional information required by the operation
 * to be performed against the selected resource. By specifying a context to a selection, the
 * consumer of the selected resources in the basket can then either filter the resources on which it
 * has to operate or apply a different behaviour according to their selection context.
 * @author mmoquillon
 */
public interface SelectionContext {

}
