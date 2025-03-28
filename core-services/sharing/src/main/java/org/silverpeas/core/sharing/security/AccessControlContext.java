/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.sharing.security;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.node.model.NodeDetail;

/**
 * The context of a control access on a resource shared by a ticket. The context refers the
 * resource for which the access control has to be performed.
 * @author mmoquillon
 */
public class AccessControlContext {

  private NodeDetail node;
  private PublicationDetail publication;
  private SimpleDocument document;

  public static AccessControlContext about(NodeDetail node) {
    AccessControlContext context = new AccessControlContext();
    context.node = node;
    return context;
  }

  public static AccessControlContext about(PublicationDetail publication) {
    AccessControlContext context = new AccessControlContext();
    context.publication = publication;
    return context;
  }

  public static AccessControlContext about(SimpleDocument document) {
    AccessControlContext context = new AccessControlContext();
    context.document = document;
    return context;
  }

  public NodeDetail getNode() {
    return node;
  }

  public PublicationDetail getPublication() {
    return publication;
  }

  public SimpleDocument getDocument() {
    return document;
  }

  public boolean isAboutNode() {
    return node != null;
  }

  public boolean isAboutPublication() {
    return publication != null;
  }

  public boolean isAboutDocument() {
    return document != null;
  }
}
  