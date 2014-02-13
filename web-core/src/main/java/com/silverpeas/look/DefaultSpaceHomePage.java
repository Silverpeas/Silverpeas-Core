/**
 * Copyright (C) 2000 - 2014 Silverpeas
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
package com.silverpeas.look;

import java.util.List;

import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class DefaultSpaceHomePage {

  private SpaceInstLight space;
  private List<SpaceInstLight> subSpaces;
  private List<UserDetail> admins;
  private List<ComponentInstLight> apps;
  private List<PublicationDetail> publications;
  private List<PublicationDetail> news;
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
