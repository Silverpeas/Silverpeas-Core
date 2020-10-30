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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.util.SilverpeasList;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Extension of {@link SelectableUIEntity} which handles simple cases of {@link Contribution} UI
 * selection.<br>
 * Of course, if some stuffs are missing into this implementation, it is highly recommended to
 * extend this class.
 * @param <C> the type of the {@link Contribution}.
 * @author silveryocha
 */
public class SimpleContributionUIEntity<C extends Contribution> extends SelectableUIEntity<C> {

  @SuppressWarnings("WeakerAccess")
  protected SimpleContributionUIEntity(final C data, final Set<String> selectedIds) {
    super(data, selectedIds);
  }

  /**
   * Initializes an item from the given {@link Contribution} and a list of selected ids as string.
   * @param contribution the contribution instance.
   * @param selectedIds the selected identifiers.
   * @param <D> the type of the {@link Contribution}.
   * @param <W> the type of the item {@link Contribution} wrapper.
   * @return the initialized item data wrapper.
   */
  @SuppressWarnings("unchecked")
  public static <D extends Contribution, W extends SimpleContributionUIEntity<D>> W convert(
      final D contribution, final Set<String> selectedIds) {
    return (W) new SimpleContributionUIEntity<>(contribution, selectedIds);
  }

  /**
   * Converts the given data list into a {@link SilverpeasList} of item wrapping the {@link
   * Contribution}.
   * @param contributionList the list of {@link Contribution}.
   * @param <D> the type of the {@link Contribution}.
   * @param <W> the type of the item {@link Contribution} wrapper.
   * @return the {@link SilverpeasList} of wrapped data item.
   */
  public static <D extends Contribution, W extends SimpleContributionUIEntity<D>>
  SilverpeasList<W> convertList(
      final List<D> contributionList, final Set<String> selectedIds) {
    SilverpeasList<D> list = SilverpeasList.wrap(contributionList);
    final Function<D, W> converter = c -> convert(c, selectedIds);
    return contributionList.stream().map(converter).collect(SilverpeasList.collector(list));
  }

  @Override
  public String getId() {
    return getData().getIdentifier().getLocalId();
  }
}
