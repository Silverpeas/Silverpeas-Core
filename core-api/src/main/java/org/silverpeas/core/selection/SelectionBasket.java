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

import org.silverpeas.core.SilverpeasResource;
import org.silverpeas.core.cache.service.CacheServiceProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A basket of a selection of Silverpeas resources. It is specific to each user and lives along the
 * current session of the user; it expires with the session of the user. A resource can be present
 * only one time in the basket whatever its selection context. So putting it again in the basket
 * doesn't add it a second time but update its position in the basket so that it will be the first
 * one to be popped.
 * <p>
 * The basket behaves like an indexed stack of {@link SelectionEntry} instances, each of them
 * related to a given Silverpeas resource selected by the current user. Each entries, and hence each
 * resource, is then indexed by their position in the stack, that is to say by the ordered way they
 * were put into the basket: the first resource put in the basket is at the tail of the stack, the
 * second resource just atop of the first resource, and so on. The last resource is hence at the
 * head of the stack, and therefore it will be the first one to be popped. Because the resources put
 * in the basket are indexed by their position, they can be gotten by their index.
 * </p>
 * @author mmoquillon
 */
public class SelectionBasket implements Serializable {

  private static final String CACHE_KEY = "###SELECTION_BASKET###";

  /**
   * Gets the basket of resources selected by the current user. The basket is
   * @return the current instance of {@link SelectionBasket} in the scope of the session of the
   * user.
   */
  public static SelectionBasket get() {
    return CacheServiceProvider.getSessionCacheService()
        .getCache()
        .computeIfAbsent(CACHE_KEY, SelectionBasket.class, SelectionBasket::new);
  }

  private final List<SelectionEntry<SilverpeasResource>> selection = new ArrayList<>();

  private SelectionBasket() {
  }

  /**
   * Puts the specified Silverpeas resource into this basket of selected resources to transfer
   * information about it. The resource can be present only one time in the basket whatever the
   * context of its selection; putting it another time is just moving it in atop of all others
   * selected resources in the basket with the new selection context.
   * @param resource a resource to put.
   * @return the basket enriched with the specified resource.
   */
  public <T extends SilverpeasResource> SelectionBasket put(final T resource) {
    return put(resource, new SelectionContext());
  }

  /**
   * Puts the specified Silverpeas resource into this basket of selected resources for the given
   * selection context . The resource can be present only one time in the basket whatever the
   * context of its selection; putting it another time is just moving it in atop of all others
   * selected resources in the basket with the new selection context.
   * @param resource a resource to put.
   * @return the basket enriched with the specified resource.
   */
  @SuppressWarnings("unchecked")
  public <T extends SilverpeasResource> SelectionBasket put(final T resource,
      final SelectionContext context) {
    Objects.requireNonNull(resource);
    Objects.requireNonNull(context);
    SelectionEntry<T> entry = new SelectionEntry<>(resource, context);
    int index = selection.indexOf(entry);
    if (index != 0) {
      if (index > 0) {
        selection.remove(index);
      }
      selection.add(0, (SelectionEntry<SilverpeasResource>) entry);
    }
    return this;
  }

  /**
   * Pops the last resource put in the basket; the resource atop of the basket. The resource just
   * below it becomes then the first resource (position at 0).
   * @return maybe the last resource put in the basket and then removes it from the basket. If the
   * basket is empty, then nothing is returned.
   */
  public <T extends SilverpeasResource> Optional<SelectionEntry<T>> pop() {
    return removeAt(0);
  }

  /**
   * Gets a functional stream on the selected resources this basket contains.
   * @return a stream on {@link SilverpeasResource} objects of the basket.
   */
  public Stream<SelectionEntry<SilverpeasResource>> getSelectedResources() {
    return selection.stream();
  }

  /**
   * Gets the resource that is at the specified position in this basket.
   * @param index the position number, starting by 0 meaning the head of the basket.
   * @return maybe the selected resource positioned at the given position. If the index is out of
   * range of the capacity of the basket, then nothing is returned.
   */
  @SuppressWarnings("unchecked")
  public <T extends SilverpeasResource> Optional<SelectionEntry<T>> getAt(final int index) {
    return index < 0 || index >= selection.size() ?
        Optional.empty() :
        Optional.of((SelectionEntry<T>) selection.get(index));
  }

  /**
   * Removes the resource that is at the specified position in the basket. The position of the
   * resources following the removed one are then shifted to one place.
   * @param index the position number, starting by 0 meaning the head of the basket.
   * @return the selected resource positioned at the given position and then remove it from the
   * basket. If the index is out of range of the capacity of the basket, then nothing is returned.
   */
  @SuppressWarnings("unchecked")
  public <T extends SilverpeasResource> Optional<SelectionEntry<T>> removeAt(final int index) {
    return index < 0 || index >= selection.size() ?
        Optional.empty() :
        Optional.of((SelectionEntry<T>) selection.remove(index));
  }

  public <T extends SilverpeasResource> void remove(final T resource) {
    SelectionEntry<T> entry = new SelectionEntry<>(resource, new SelectionContext());
    selection.remove(entry);
  }

  /**
   * Clears this basket of all the resources it contains.
   * @return the basket emptied.
   */
  public SelectionBasket clear() {
    selection.clear();
    return this;
  }

  /**
   * Is the basket empty?
   * @return true if there is no resources in this basket. False otherwise.
   */
  public boolean isEmpty() {
    return selection.isEmpty();
  }

  /**
   * Gets the number of resources in this basket.
   * @return the number of selected resources.
   */
  public int count() {
    return selection.size();
  }
}
