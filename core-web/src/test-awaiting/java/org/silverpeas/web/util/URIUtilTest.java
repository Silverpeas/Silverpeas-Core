/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.util;

import org.junit.Test;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: Yohann Chastagnier
 * Date: 23/05/13
 */
public class URIUtilTest {

  private final static String URI_BASE = "http://localhost/silverpeas";

  @Test
  public void testBuildURIFromUriInfo() throws Exception {
    UriInfo uriInfoMock = mock(UriInfo.class);
    URI uriMock = new URI(URI_BASE);
    when(uriInfoMock.getBaseUri()).thenReturn(uriMock);
    assertThat(URIBuilder.buildURI(uriInfoMock, "part1", "part2").toString(),
        is(URI_BASE + "/part1/part2"));
  }

  @Test
  public void testBuildURIFromBaseUri() throws Exception {
    assertThat(URIBuilder.buildURI(URI_BASE, "part1", "part2").toString(),
        is(URI_BASE + "/part1/part2"));
  }
}
