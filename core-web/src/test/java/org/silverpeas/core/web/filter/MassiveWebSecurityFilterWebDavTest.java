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
package org.silverpeas.core.web.filter;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Regression tests for WebDAV document upload detection.
 */
public class MassiveWebSecurityFilterWebDavTest {

    @Test
    public void webDavDocumentUploadIsNotInspectedAsAnApplicationEntity() {
        assertThat(MassiveWebSecurityFilter.isWebDavDocumentUpload("PUT", "/repository"), is(true));
        assertThat(MassiveWebSecurityFilter.isWebDavDocumentUpload("PUT", "/repository2000"), is(true));
        assertThat(MassiveWebSecurityFilter.isWebDavDocumentUpload("put", "/repository"), is(true));
        assertThat(MassiveWebSecurityFilter.isWebDavDocumentUpload("PUT", "/repository-other"), is(false));
        assertThat(MassiveWebSecurityFilter.isWebDavDocumentUpload("PUT", "/services"), is(false));
        assertThat(MassiveWebSecurityFilter.isWebDavDocumentUpload("PROPFIND", "/repository"), is(false));
        assertThat(MassiveWebSecurityFilter.isWebDavDocumentUpload("PROPPATCH", "/repository"), is(false));
        assertThat(MassiveWebSecurityFilter.isWebDavDocumentUpload("LOCK", "/repository"), is(false));
        assertThat(MassiveWebSecurityFilter.isWebDavDocumentUpload("POST", "/services"), is(false));
    }

}
