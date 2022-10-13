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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.selection;

import org.silverpeas.core.NotFoundException;
import org.silverpeas.core.SilverpeasResource;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.I18nContribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.selection.SelectionContext;
import org.silverpeas.core.selection.SelectionEntry;
import org.silverpeas.core.web.rs.WebEntity;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Objects;

/**
 * An entry in the basket. Any items in the basket are wrapped by an entry that maps the item, id
 * est the Silverpeas resource, to a context that indicates for what reason the resource was put in
 * the basket. When an item is put or get from the
 * {@link org.silverpeas.core.selection.SelectionBasket},
 * it is always done through a {@link SelectionBasketEntry} instance.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SelectionBasketEntry implements WebEntity {

  private URI uri;
  @XmlElement(required = true)
  private BasketItem item;

  private SelectionContext context = new SelectionContext(SelectionContext.Reason.TRANSFER);

  public static SelectionBasketEntry from(SelectionEntry<SilverpeasResource> entry) {
    Objects.requireNonNull(entry);
    SilverpeasResource resource = entry.getResource();
    BasketItem item = BasketItem.from(resource);
    return new SelectionBasketEntry(item, entry.getContext());
  }

  protected SelectionBasketEntry() {
  }

  public SelectionBasketEntry(final BasketItem item, final SelectionContext context) {
    this.item = item;
    this.context = context;
  }

  public SelectionBasketEntry(final BasketItem item) {
    this.item = item;
    this.context = new SelectionContext(SelectionContext.Reason.TRANSFER);
  }

  /**
   * Gets the URI through which this entry can be retrieved through the selection basket Web API.
   * @return the URI at which this entry, and hence the selected Silverpeas resource, can be found.
   */
  @Override
  public URI getURI() {
    return uri;
  }

  public SelectionBasketEntry withURI(URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Gets the item in the basket to which this entry is related.
   * @return a Web entity representation of a Silverpeas resource.
   */
  public BasketItem getItem() {
    return item;
  }

  /**
   * Gets the context for what the related item has been put into the basket.
   * @return the context of selecting the Silverpeas resource represented here by the item.
   */
  public SelectionContext getContext() {
    return context;
  }

  /**
   * Reloads the data of the represented Silverpeas resource. By calling this method, you ensure the
   * representation of the resource is up-to-date. If the resource supports the l10n, then the data
   * get is in the language of the current user behind the request; by doing this, you ensure the
   * data of the resource will be presented to the user in his language.
   * @return itself
   */
  public SelectionBasketEntry reload() {
    String language = User.getCurrentRequester().getUserPreferences().getLanguage();
    try {
      SilverpeasResource resource = getItem().toResource();
      if (resource instanceof I18nContribution) {
        LocalizedContribution contribution = ((I18nContribution) resource).getLocalizationIn(language);
        this.item = BasketItem.from(contribution);
      } else {
        this.item = BasketItem.from(resource);
      }
      return this;
    } catch (NotFoundException e) {
      throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SelectionBasketEntry that = (SelectionBasketEntry) o;
    return item.equals(that.item);
  }

  @Override
  public int hashCode() {
    return Objects.hash(item);
  }
}
