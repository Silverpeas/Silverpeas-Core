/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.io.upload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedMocks;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
@TestManagedMocks(OrganizationController.class)
class TestUploadSessionFile {

  @BeforeEach
  void setup() {
    UserDetail user = new UserDetail();
    user.setId("32");
    new SessionInfoForTest(null, user);
  }

  @Test
  void createInstance() {
    UploadSession uploadSession = UploadSession.from("toto");
    File serverFile = new File("");
    UploadSessionFile uploadSessionFile =
        new UploadSessionFile(uploadSession, "path/filename", serverFile);
    assertThat(uploadSessionFile.getUploadSession(), is(uploadSession));
    assertThat(uploadSessionFile.getFullPath(), is("path/filename"));
    assertThat(uploadSessionFile.getServerFile(), is(serverFile));
  }
}