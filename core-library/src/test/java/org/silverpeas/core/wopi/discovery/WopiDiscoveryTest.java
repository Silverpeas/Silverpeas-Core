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

package org.silverpeas.core.wopi.discovery;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author silveryocha
 */
class WopiDiscoveryTest {

  @Test
  void libreOfficeDiscoveryXmlContent() throws IOException {
    final WopiDiscovery discovery;
    try (InputStream in = getDiscoveryXmlStream()){
      discovery = WopiDiscovery.load(in);
    }
    assertThat(discovery, notNullValue());
    final NetZone netZone = discovery.getNetZone();
    assertThat(netZone, notNullValue());
    assertThat(netZone.getName(), is("external-http"));
    final List<App> apps = netZone.getApps();
    assertThat(apps, notNullValue());
    assertThat(apps, not(empty()));
    final App app = apps.get(0);
    assertThat(app, notNullValue());
    assertThat(app.getName(), is("image/svg+xml"));
    final List<Action> actions = app.getActions();
    assertThat(actions, notNullValue());
    assertThat(actions, not(empty()));
    Action action = actions.get(0);
    assertThat(action.getExt(), is("svg"));
    assertThat(action.getName(), is("view"));
    assertThat(action.getUrlsrc(), is("http://CLIENT_HOST/loleaflet/b889fbb/loleaflet.html?"));
    action = actions.get(1);
    assertThat(action.getExt(), is("xsvg"));
    assertThat(action.getName(), is("edit"));
    assertThat(action.getUrlsrc(), is("http://CLIENT_HOST/loleaflet/b889fbb/loleaflet.html?"));
  }

  private InputStream getDiscoveryXmlStream() {
    return WopiDiscoveryTest.class.getClassLoader()
        .getResourceAsStream("org/silverpeas/wopi/discovery.xml");
  }
}