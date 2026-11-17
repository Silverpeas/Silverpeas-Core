/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

package org.silverpeas.core.admin.configuration;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.kernel.logging.SilverLogger;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

/** Successful console changes, without recording configuration values or credentials. */
@Service
public class ConfigurationChangeLog {

  static final String NAMESPACE = "silverpeas.core.admin.configuration.audit";

  /** Records the keys actually changed by a successful properties replacement. */
  public void propertiesChanged(User actor, ConfigurationFile before, ConfigurationFile after) {
    var keys = after.properties().keySet().stream()
        .filter(key -> !Objects.equals(before.properties().get(key), after.properties().get(key)))
        .sorted().toList();
    if (!keys.isEmpty()) {
      record(actor, "UPDATE_PROPERTIES", event -> event.put("file", "properties/" + after.path())
          .put("revisionBefore", before.revision()).put("revisionAfter", after.revision())
          .putJSONArray("keys", array -> {
            keys.forEach(array::add);
            return array;
          }));
    }
  }

  /** Records the keys written to an existing or newly created installation XML. */
  public void settingsPersisted(User actor, ConfigurationFile selection,
      CustomSettingsTarget before, CustomSettingsTarget after) {
    if (!before.revision().equals(after.revision())) {
      record(actor, before.exists() ? "UPDATE_CUSTOM_SETTINGS" : "CREATE_CUSTOM_SETTINGS",
          event -> event.put("file", "configuration/silverpeas/" + after.name())
              .put("source", "properties/" + selection.path())
              .put("sourceRevision", selection.revision())
              .put("revisionBefore", before.revision()).put("revisionAfter", after.revision())
              .putJSONArray("keys", array -> {
                selection.properties().keySet().stream().sorted().forEach(array::add);
                return array;
              }));
    }
  }

  private void record(User actor, String operation, Consumer<JSONCodec.JSONObject> details) {
    var entry = JSONCodec.encodeObject(event -> {
      event.put("timestamp", Instant.now().toString())
          .put("operation", operation).put("userId", actor.getId())
          .put("login", actor.getLogin()).put("domainId", actor.getDomainId());
      details.accept(event);
      return event;
    });
    // JSON escaping keeps user-controlled names and keys on a single physical log line.
    SilverLogger.getLogger(NAMESPACE).info("{0}", entry);
  }
}
