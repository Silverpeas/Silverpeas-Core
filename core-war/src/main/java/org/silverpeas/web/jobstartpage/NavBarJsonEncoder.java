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

package org.silverpeas.web.jobstartpage;

import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.util.JSONCodec.JSONObject;
import org.silverpeas.core.util.Pair;
import org.silverpeas.web.jobstartpage.control.JobStartPagePeasSessionController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.silverpeas.core.util.CollectionUtil.isNotEmpty;
import static org.silverpeas.core.util.JSONCodec.encodeObject;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.web.jobstartpage.DisplaySorted.TYPE_SPACE;
import static org.silverpeas.web.jobstartpage.DisplaySorted.TYPE_SUBSPACE;

/**
 * JSON encoder dedicated to the spaces and components navigation displayed into Silverpeas's
 * administration.
 * @author silveryocha
 */
public class NavBarJsonEncoder {

  private static final String ID = "id";
  private static final String LABEL = "label";
  private static final String TYPE = "type";
  private static final String NAME = "name";
  private final JobStartPagePeasSessionController controller;

  private NavBarJsonEncoder(final JobStartPagePeasSessionController controller) {
    this.controller = controller;
  }

  public static NavBarJsonEncoder with(JobStartPagePeasSessionController controller) {
    return new NavBarJsonEncoder(controller);
  }

  /**
   * Encodes an object containing the following data:
   * <ul>
   *  <li><strong>rootSpaces</strong>: an array of root space data</li>
   *  <li><strong>currentRootSpace</strong>: the data of current root space if any, undefined otherwise</li>
   *  <li><strong>spacePath</strong>: an array representing the space path to the current sub-space</li>
   *  <li><strong>spaces</strong>: an array of current spaces</li>
   *  <li><strong>applications</strong>: an array of current application instances</li>
   * </ul>
   * @return the String representing
   */
  public String encode() {
    return encodeObject(o -> {
      encodeRootSpaces(o);
      encodeCurrentRootSpace(o);
      encodeSpacePath(o);
      encodeSpaces(o);
      encodeApplications(o);
      return o;
    });
  }

  /**
   * Encodes into the given JSON object the root spaces of the navigation.
   * @param jsonObject the JSON object to fill.
   */
  private void encodeRootSpaces(final JSONObject jsonObject) {
    jsonObject.putJSONArray("rootSpaces", a -> {
      controller.getSpaces()
          .stream()
          .filter(DisplaySorted::isVisible)
          .forEach(s -> a.addJSONObject(so -> encodeSpace(Pair.of(s, true), so)));
      return a;
    });
  }

  /**
   * Encodes if it exists the current root space. It's about the current root space into which
   * the sub spaces are walked through.
   * @param jsonObject the JSON object to fill.
   */
  private void encodeCurrentRootSpace(final JSONObject jsonObject) {
    if (isDefined(controller.getSpaceId())) {
      final SpaceInst s = controller.getSpaceInstById(controller.getSpaceId());
      jsonObject.putJSONObject("currentRootSpace", o -> o
          .put(ID, valueOf(s.getLocalId()))
          .put(LABEL, applyMaintenanceSuffix(s.getId(), s.getName(controller.getLanguage()))));
    }
  }

  /**
   * Encodes if any the full path to the current sub-space.
   * @param jsonObject the JSON object to fill.
   */
  private void encodeSpacePath(final JSONObject jsonObject) {
    if (isDefined(controller.getSubSpaceId())) {
      final List<DisplaySorted> path = new ArrayList<>();
      String parentId = controller.getSubSpaceId();
      while (isDefined(parentId)) {
        final DisplaySorted parent = controller.getManagedSpace(parentId);
        parentId = parent.getParentId();
        if (isDefined(parentId)) {
          path.add(0, parent);
        }
      }
      jsonObject.putJSONArray("spacePath", a -> {
        path.stream()
            .filter(DisplaySorted::isVisible)
            .forEach(s -> a.addJSONObject(so -> encodeSpace(Pair.of(s, false), so)));
        return a;
      });
    }
  }

  /**
   * Encodes into the given JSON object the current spaces to display into the navigation.
   * @param jsonObject the JSON object to fill.
   */
  private void encodeSpaces(final JSONObject jsonObject) {
    final String parentId = defaultStringIfNotDefined(controller.getSubSpaceId(), controller.getSpaceId());
    jsonObject.putJSONArray("spaces", a -> {
      controller.getSubSpaces().stream()
          .filter(s -> s.getParentId().equals(parentId))
          .filter(DisplaySorted::isVisible)
          .forEach(s -> a.addJSONObject(so -> encodeSpace(Pair.of(s, false), so)));
      return a;
    });
  }

  /**
   * Centralization of the encoding of a space.
   * @param space pair containing on the left the identification data and on the right a boolean
   * to indicate if it is a root space.
   * @param jsonObject the JSON object to fill.
   */
  private JSONObject encodeSpace(final Pair<DisplaySorted, Boolean> space,
      final JSONObject jsonObject) {
    return jsonObject
        .put(ID, space.getFirst().getId())
        .put(TYPE, TRUE.equals(space.getSecond()) ? TYPE_SPACE : TYPE_SUBSPACE)
        .put(LABEL, applyMaintenanceSuffix(space.getFirst().getId(), space.getFirst().getName()));
  }

  /**
   * Encodes into the given JSON object the current application instances to display into the
   * navigation.
   * @param jsonObject the JSON object to fill.
   */
  private void encodeApplications(final JSONObject jsonObject) {
    final Collection<DisplaySorted> applications = isDefined(controller.getSubSpaceId()) ?
        controller.getSubSpaceComponents() :
        controller.getSpaceComponents();
    if (isNotEmpty(applications)) {
      jsonObject.putJSONArray("applications", a -> {
        applications.stream()
            .filter(DisplaySorted::isVisible)
            .forEach(i -> a.addJSONObject(so -> so
                .put(ID, i.getId())
                .put(NAME, i.getTypeName())
                .put(LABEL, i.getName())));
        return a;
      });
    }
  }

  /**
   * From given space parameters, returns the right space label against the maintenance status of
   * the space.
   * @param spaceId a space identifier.
   * @param spaceLabel the corresponding i18n space label.
   * @return the space label taking into account the maintenance status of the space.
   */
  private String applyMaintenanceSuffix(final String spaceId, final String spaceLabel) {
    return format("%s%s", spaceLabel, controller.isSpaceInMaintenance(spaceId) ? " (M)" : "");
  }
}
