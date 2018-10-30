/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.silverpeas.core.io.temp.TestLastModifiedDateFileTask;
import org.silverpeas.core.io.upload.TestUploadSession;
import org.silverpeas.core.io.upload.TestUploadSessionFile;
import org.silverpeas.core.process.io.file.TestAbstractFileHandler;
import org.silverpeas.core.process.io.file.TestFileHandler;
import org.silverpeas.core.process.io.file.TestHandledFile;
import org.silverpeas.core.test.UnitTest;

/**
 * This test suite is to sequence unit tests that includes access to the filesystem and that could
 * use the same file structure. Because of that and of a possibly file cache use, it is required to
 * run them sequentially in order to avoid file context overriding.
 * @author miguel
 */
@UnitTest
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestUploadSession.class,
    TestUploadSessionFile.class,
    TestAbstractFileHandler.class,
    TestHandledFile.class,
    TestFileHandler.class,
    TestLastModifiedDateFileTask.class
})
public class FilesIncludingTestSuite {
}
