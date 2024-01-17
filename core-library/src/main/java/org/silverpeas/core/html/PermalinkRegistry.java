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
package org.silverpeas.core.html;

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.URLUtil;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.synchronizedList;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Register of all permalink prefixes available into Silverpeas.
 * <p>
 *   By default, all {@link URLUtil.Permalink#getURLPrefix()} values are registered.
 * </p>
 */
@Singleton
public class PermalinkRegistry {

  private final List<String> urlPartRegistry = synchronizedList(new ArrayList<>());

  public static PermalinkRegistry get() {
    return ServiceProvider.getSingleton(PermalinkRegistry.class);
  }

  @PostConstruct
  protected void setupDefaults() {
    Stream.of(URLUtil.Permalink.values()).forEach(p -> addUrlPart(p.getURLPrefix()));
  }

  /**
   * Hidden constructor.
   */
  private PermalinkRegistry() {
  }

  public Stream<String> streamAllUrlParts() {
    return urlPartRegistry.stream();
  }

  /**
   * Adds a part url of a permalink.
   * @param urlPart a string value.
   */
  public void addUrlPart(final String urlPart) {
    if (isDefined(urlPart)) {
      urlPartRegistry.add("/" + urlPart.replace("/", "") + "/");
    }
  }

  /**
   * Indicates if the given URL is compliant with a permalink definition.
   * @param url an URL as string.
   * @return true is the given URL is compliant, false otherwise.
   */
  public boolean isCompliant(String url) {
    return url != null && urlPartRegistry.stream().anyMatch(url::contains);
  }
}
