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

package org.silverpeas.core.contribution.contentcontainer.content;

import java.util.Iterator;

/**
 * The interface for all the SilverContent (filebox+, ..)
 * @deprecated use instead {@link org.silverpeas.core.contribution.model.Contribution} and
 * {@link org.silverpeas.core.contribution.model.ContributionContent} interfaces.
 */
public interface SilverContentInterface {
  public String getName();

  public String getName(String language);

  public String getDescription();

  public String getDescription(String language);

  public String getURL();

  public String getId();

  public String getInstanceId();

  public String getDate();

  public String getSilverCreationDate(); // added by ney. 16/05/2004.

  public String getIconUrl();

  public String getCreatorId();

  public Iterator<String> getLanguages();
}