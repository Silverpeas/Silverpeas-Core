/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.viewer.web;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The preview entity is a preview instance that is exposed in the web as
 * an entity (web entity).
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentViewEntity extends AbstractPreviewEntity<DocumentViewEntity> {
  private static final long serialVersionUID = 4270519541076741138L;

  @XmlElement(defaultValue = "")
  private int width = 0;

  @XmlElement(defaultValue = "")
  private int height = 0;

  @XmlElement
  private final List<PageViewEntity> pages = new ArrayList<PageViewEntity>();

  /**
   * Creates a new Preview entity from the specified preview.
   * @return the entity representing the specified preview.
   */
  public static DocumentViewEntity createFrom() {
    return new DocumentViewEntity();
  }

  /**
   * Default constructor
   */
  protected DocumentViewEntity() {
  }

  /**
   * Adding a page
   * @param pageView
   */
  protected void addPageView(final PageViewEntity pageView) {
    width = Math.max(width, Integer.valueOf(pageView.getWidth()));
    height = Math.max(height, Integer.valueOf(pageView.getHeight()));
    pages.add(pageView);
  }

  /**
   * @return the width
   */
  protected int getWidth() {
    return width;
  }

  /**
   * @return the height
   */
  protected int getHeight() {
    return height;
  }

  /**
   * @return the pages
   */
  protected List<PageViewEntity> getPages() {
    return pages;
  }
}
