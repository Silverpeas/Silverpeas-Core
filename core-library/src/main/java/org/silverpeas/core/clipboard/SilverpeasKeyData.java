/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.clipboard;

import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

/**
 * Data about the contribution or the resource put in a clipboard.
 */
public class SilverpeasKeyData implements Serializable {

  private static final long serialVersionUID = 6975015837634530711L;
  private static final String ID_KEY = "ID";
  private static final String INSTANCE_ID_KEY = "COMPONENT";
  private static final String TITLE_KEY = "TITLE";
  private static final String AUTHOR_KEY = "AUTHOR";
  private static final String DESC_KEY = "DESC";
  private static final String TEXT_KEY = "TEXT"; // rendu HTML de l'object
  private static final String TYPE_KEY = "TYPE";
  private static final String LINK_KEY = "LINK";

  private Date creationDate = null;
  private final Properties keyData;

  /**
   * Constructs a new {@link SilverpeasKeyData} instance for the specified Silverpeas resource.
   * The resource isn't a contribution and it doesn't belong to a component instance.
   * @param id the unique identifier of the resource.
   */
  public SilverpeasKeyData(String id) {
    keyData = new Properties();
    keyData.setProperty(ID_KEY, id);
  }

  /**
   * Constructs a new {@link SilverpeasKeyData} instance for the specified contribution.
   * @param id the unique identifier of the contribution.
   * @param componentInstanceId the unique identifier of the component instance to which the
   * contribution belongs.
   */
  public SilverpeasKeyData(String id, String componentInstanceId) {
    keyData = new Properties();
    keyData.setProperty(ID_KEY, id);
    keyData.setProperty(INSTANCE_ID_KEY, componentInstanceId);
  }

  public void setTitle(String title) {
    keyData.setProperty(TITLE_KEY, title);
  }

  public void setAuthor(String author) {
    keyData.setProperty(AUTHOR_KEY, author);
  }

  public void setCreationDate(Date date) {
    creationDate = date;
  }

  public void setDesc(String desc) {
    keyData.setProperty(DESC_KEY, desc);
  }

  public void setText(String text) {
    keyData.setProperty(TEXT_KEY, text);
  }

  public void setType(String type) {
    keyData.setProperty(TYPE_KEY, type);
  }

  public void setLink(String link) {
    keyData.setProperty(LINK_KEY, link);
  }

  public void setProperty(String key, String value) throws SKDException {
    if (keyData.containsKey(key)) {
      throw new SKDException("The property already contains the key " + key);
    } else {
      keyData.setProperty(key, value);
    }
  }

  @NonNull
  public String getId() {
    return keyData.getProperty(ID_KEY);
  }

  @Nullable
  public String getComponentInstanceId() {
    return keyData.getProperty(INSTANCE_ID_KEY);
  }

  @NonNull
  public String getTitle() {
    return keyData.getProperty(TITLE_KEY);
  }

  public String getAuthor() {
    return keyData.getProperty(AUTHOR_KEY);
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public String getDesc() {
    return keyData.getProperty(DESC_KEY);
  }

  public String getText() {
    return keyData.getProperty(TEXT_KEY);
  }

  @NonNull
  public String getType() {
    return keyData.getProperty(TYPE_KEY);
  }

  @NonNull
  public String getLink() {
    return keyData.getProperty(LINK_KEY);
  }

  @Nullable
  public String getProperty(String key) {
    return keyData.getProperty(key);
  }

}
