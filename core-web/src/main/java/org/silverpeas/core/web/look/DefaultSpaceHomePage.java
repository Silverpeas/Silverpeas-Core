/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.look;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

import java.util.Collections;
import java.util.List;

public class DefaultSpaceHomePage {

  private SpaceInstLight space;
  private List<SpaceInstLight> subSpaces = Collections.EMPTY_LIST;
  private List<UserDetail> admins = Collections.EMPTY_LIST;
  private List<ComponentInstLight> apps = Collections.EMPTY_LIST;
  private List<PublicationDetail> publications = Collections.EMPTY_LIST;
  private List<PublicationDetail> news = Collections.EMPTY_LIST;
  private String nextEventsURL;

  public SpaceInstLight getSpace() {
    return space;
  }

  public void setSpace(SpaceInstLight space) {
    this.space = space;
  }

  public List<PublicationDetail> getPublications() {
    return publications;
  }

  public void setPublications(List<PublicationDetail> publications) {
    this.publications = publications;
  }

  public List<PublicationDetail> getNews() {
    return news;
  }

  public void setNews(List<PublicationDetail> news) {
    this.news = news;
  }

  public List<SpaceInstLight> getSubSpaces() {
    return subSpaces;
  }

  public void setSubSpaces(List<SpaceInstLight> subSpaces) {
    this.subSpaces = subSpaces;
  }

  public List<ComponentInstLight> getApps() {
    return apps;
  }

  public void setApps(List<ComponentInstLight> apps) {
    this.apps = apps;
  }

  public List<UserDetail> getAdmins() {
    return admins;
  }

  public void setAdmins(List<UserDetail> admins) {
    this.admins = admins;
  }

  public String getNextEventsURL() {
    return nextEventsURL;
  }

  public void setNextEventsURL(String nextEventsURL) {
    this.nextEventsURL = nextEventsURL;
  }
}
