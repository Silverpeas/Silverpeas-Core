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
package org.silverpeas.core.io.upload;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.cache.model.SimpleCache;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.cache.service.CacheAccessorProvider.getSessionCacheAccessor;
import static org.silverpeas.core.util.file.FileRepositoryManager.getTemporaryPath;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class TestUploadSession {

  private static final String SESSION_CACHE_KEY = "@@@_" + UploadSession.class.getName();
  private static final String UPLOAD_SESSION_CACHE_KEY_PREFIX = "@@@_instance_for_";

  private SessionInfo si;

  @TestManagedMock
  private ComponentAccessControl accessControllerMock;
  @TestManagedMock
  private OrganizationController organisationControllerMock;

  @BeforeEach
  @AfterEach
  void cleanTest() {

    // Test
    FileUtils.deleteQuietly(new File(getTemporaryPath()));
    SimpleCache cache = getSessionCacheAccessor().getCache();
    if (cache != null) {
      cache.remove(SESSION_CACHE_KEY);
    }
  }

  @BeforeEach
  void setup() {
    UserDetail user = new UserDetail();
    user.setId("32");
    si = new SessionInfoForTest(null, user);
  }

  @SuppressWarnings("unchecked")
  @Test
  void verifySessionCache() {
    assertThat(getSessionCacheAccessor().getCache().get(SESSION_CACHE_KEY), nullValue());

    UploadSession uploadSession1 = UploadSession.from("   ");
    assertThat((Set<String>) getSessionCacheAccessor().getCache().get(SESSION_CACHE_KEY, Set.class),
        containsInAnyOrder(uploadSession1.getId()));
    assertThat(getSessionCacheAccessor().getCache()
            .get(UPLOAD_SESSION_CACHE_KEY_PREFIX + uploadSession1.getId(), UploadSession.class),
        sameInstance(uploadSession1));

    UploadSession uploadSession2 = UploadSession.from("anId");
    uploadSession2.getUploadSessionFile("path/test/name");
    assertThat(new File(getTemporaryPath(), uploadSession2.getId()).exists(), is(true));

    Set<String> uploadSessionIds =
        getSessionCacheAccessor().getCache().get(SESSION_CACHE_KEY, Set.class);
    assertThat(uploadSessionIds, notNullValue());
    assertThat(uploadSessionIds, containsInAnyOrder(uploadSession1.getId(), "anId"));
    assertThat(getSessionCacheAccessor().getCache()
            .get(UPLOAD_SESSION_CACHE_KEY_PREFIX + "anId", UploadSession.class),
        sameInstance(uploadSession2));

    uploadSession1.getUploadSessionFile("newPath/newTest/newName");
    assertThat(new File(getTemporaryPath(), uploadSession1.getId()).exists(), is(true));

    uploadSessionIds = getSessionCacheAccessor().getCache().get(SESSION_CACHE_KEY, Set.class);
    assertThat(uploadSessionIds, notNullValue());
    assertThat(uploadSessionIds, containsInAnyOrder("anId", uploadSession1.getId()));

    UploadSession.clearFrom(si);

    assertThat(getSessionCacheAccessor().getCache().get(SESSION_CACHE_KEY), nullValue());
    assertThat(new File(getTemporaryPath(), uploadSession1.getId()).exists(), is(false));
    assertThat(getSessionCacheAccessor().getCache()
            .get(UPLOAD_SESSION_CACHE_KEY_PREFIX + uploadSession1.getId(), UploadSession.class),
        nullValue());
    assertThat(new File(getTemporaryPath(), uploadSession2.getId()).exists(), is(false));
    assertThat(getSessionCacheAccessor().getCache()
            .get(UPLOAD_SESSION_CACHE_KEY_PREFIX + "anId", UploadSession.class),
        nullValue());
  }

  @Test
  void fromBehaviorAccordingToSessionCache() {
    UploadSession uploadSession = UploadSession.from("anId");
    UploadSession sameUploadSession = UploadSession.from("anId");
    UploadSession otherUploadSession = UploadSession.from("anOtherId");
    assertThat(sameUploadSession, sameInstance(uploadSession));
    assertThat(otherUploadSession, not(sameInstance(uploadSession)));
  }

  @Test
  void registerWithNewUploadSession() {
    UploadSessionFile uploadSessionFile = initializeUploadSessionAndRegisterFile();
    UploadSessionFile otherUploadSessionFile =
        uploadSessionFile.getUploadSession().getUploadSessionFile("newPath/newFile");
    assertThat(uploadSessionFile.getServerFile().exists(), is(false));
    assertThat(otherUploadSessionFile.getServerFile().exists(), is(false));
  }

  @Test
  void registerWithExistingUploadSession() {
    UploadSessionFile uploadSessionFile = initializeUploadSessionAndRegisterFile("existingId");
    UploadSessionFile otherUploadSessionFile =
        uploadSessionFile.getUploadSession().getUploadSessionFile("   /newFile");
    assertThat(uploadSessionFile.getServerFile().exists(), is(false));
    assertThat(otherUploadSessionFile.getServerFile().exists(), is(false));
  }

  @Test
  void write() throws Exception {
    UploadSessionFile uploadSessionFile = initializeUploadSessionAndRegisterFile();
    assertThat(uploadSessionFile.getServerFile().exists(), is(false));
    final StringBuilder sb = new StringBuilder();
    ByteArrayInputStream bais = new ByteArrayInputStream("writing test...".getBytes()) {
      @Override
      public void close() throws IOException {
        super.close();
        sb.append(true);
      }
    };
    assertThat(sb.toString(), isEmptyString());
    uploadSessionFile.write(bais);
    assertThat(sb.toString(), is("true"));
    assertThat(FileUtils.readFileToString(uploadSessionFile.getServerFile()),
        is("writing test..."));
  }

  @Test
  void registerSameFileButWritingIsInProgress() throws Exception {
    UploadSessionFile uploadSessionFile = initializeUploadSessionAndRegisterFile();
    UploadSessionFile sameUploadSessionFile =
        uploadSessionFile.getUploadSession().getUploadSessionFile("path/test/name");
    assertThat(uploadSessionFile.getServerFile().exists(), is(false));
    assertThat(sameUploadSessionFile.getServerFile().exists(), is(false));

    // This next section, as it exists a current file writing, it is not possible to write an
    // other upload with same path.
    uploadSessionFile.getUploadSession().markFileWritingInProgress(uploadSessionFile);
    FileUtils.write(uploadSessionFile.getServerFile(), "File exists...");
    assertThat(uploadSessionFile.getServerFile().exists(), is(true));

    UploadSessionFile sameUploadSessionFile2 =
        uploadSessionFile.getUploadSession().getUploadSessionFile("path/test/name");
    assertThat(sameUploadSessionFile2.getServerFile().getPath(),
        is(new File(getUploadSessionFile(uploadSessionFile.getUploadSession()), "path/test/name")
            .getPath()));
    assertThrows(IOException.class, () -> sameUploadSessionFile2.write(new ByteArrayInputStream("writing test...".getBytes())));

  }

  @Test
  void registerSameFileAndNoWritingIsInProgress() throws Exception {
    UploadSessionFile uploadSessionFile = initializeUploadSessionAndRegisterFile();
    UploadSessionFile sameUploadSessionFile =
        uploadSessionFile.getUploadSession().getUploadSessionFile("path/test/name");
    assertThat(uploadSessionFile.getServerFile().exists(), is(false));
    assertThat(sameUploadSessionFile.getServerFile().exists(), is(false));

    // This next section, as it does not exist a current file writing, it is possible to write an
    // upload with same path.
    FileUtils.write(uploadSessionFile.getServerFile(), "File exists...");
    assertThat(uploadSessionFile.getServerFile().exists(), is(true));
    sameUploadSessionFile =
        uploadSessionFile.getUploadSession().getUploadSessionFile("path/test/name");
    assertThat(sameUploadSessionFile.getServerFile().getPath(),
        is(new File(getUploadSessionFile(uploadSessionFile.getUploadSession()), "path/test/name")
            .getPath()));
    sameUploadSessionFile.write(new ByteArrayInputStream("writing test...".getBytes()));
  }

  @Test
  void remove() throws Exception {
    UploadSessionFile uploadSessionFile = initializeUploadSessionAndRegisterFile();
    assertThat(uploadSessionFile.getServerFile().exists(), is(false));
    boolean removed = uploadSessionFile.getUploadSession().remove("path/test/name");
    assertThat(removed, is(false));
    assertThat(uploadSessionFile.getServerFile().exists(), is(false));
    FileUtils.touch(uploadSessionFile.getServerFile());
    assertThat(uploadSessionFile.getServerFile().exists(), is(true));
    assertThat(uploadSessionFile.getServerFile().getPath(),
        is(new File(getUploadSessionFile(uploadSessionFile.getUploadSession()), "path/test/name")
            .getPath()));

    // This next section, as it exists a current file writing, it is not possible to remove the
    // server file.
    uploadSessionFile.getUploadSession().markFileWritingInProgress(uploadSessionFile);
    removed = uploadSessionFile.getUploadSession().remove("path/test/name");
    assertThat(removed, is(false));
    assertThat(uploadSessionFile.getServerFile().exists(), is(true));

    // This next section, it is possible
    uploadSessionFile.getUploadSession().markFileWritingDone(uploadSessionFile);
    removed = uploadSessionFile.getUploadSession().remove("path/test/name");
    assertThat(removed, is(true));
    assertThat(uploadSessionFile.getServerFile().exists(), is(false));
  }

  @Test
  void isUserAuthorizedWithoutComponentIdAndUserAccess() {
    UploadSession uploadSession = initializeUploadSessionAndRegisterFile().getUploadSession();
    assertThat(uploadSession.isUserAuthorized("instanceId"), is(false));
  }

  @Test
  void isUserAuthorizedWithoutUserAccessButWithComponentId() {
    UploadSession uploadSession = initializeUploadSessionAndRegisterFile().getUploadSession();
    assertThat(uploadSession.isUserAuthorized("instanceId"), is(false));
  }

  @Test
  void isUserAuthorizedWithUserAccessAndComponentId() {
    UploadSession uploadSession = initializeUploadSessionAndRegisterFile().getUploadSession();
    when(accessControllerMock.isUserAuthorized(anyString(), anyString())).thenReturn(true);
    assertThat(uploadSession.isUserAuthorized("instanceId"), is(true));
  }

  @Test
  void isUserAuthorizedWithUserAccessButWithoutComponentId() {
    UploadSession uploadSession =
        initializeUploadSessionAndRegisterFile().getUploadSession().forComponentInstanceId("");
    when(accessControllerMock.isUserAuthorized(anyString(), anyString())).thenReturn(true);
    assertThat(uploadSession.isUserAuthorized("instanceId"), is(false));
  }

  @Test
  void isUserAuthorizedWithUserAccessButWrongComponentId() {
    UploadSession uploadSession = initializeUploadSessionAndRegisterFile().getUploadSession();
    when(accessControllerMock.isUserAuthorized(anyString(), anyString())).thenReturn(true);
    assertThat(uploadSession.isUserAuthorized("wrongInstanceId"), is(false));
  }

  @Test
  void getComponentInstanceParameterValue() {
    UploadSession uploadSession = initializeUploadSessionAndRegisterFile().getUploadSession();
    when(organisationControllerMock.getComponentParameterValue(anyString(), anyString()))
        .thenReturn("toto");
    assertThat(uploadSession.getComponentInstanceParameterValue("anyParameter"), is("toto"));
    verify(organisationControllerMock, times(1))
        .getComponentParameterValue(anyString(), anyString());
  }

  @Test
  void getComponentInstanceParameterValueButNoComponentId() {
    UploadSession uploadSession =
        initializeUploadSessionAndRegisterFile().getUploadSession().forComponentInstanceId("");
    when(organisationControllerMock.getComponentParameterValue(anyString(), anyString()))
        .thenReturn("toto");
    assertThat(uploadSession.getComponentInstanceParameterValue("anyParameter"), nullValue());
    verify(organisationControllerMock, times(0))
        .getComponentParameterValue(anyString(), anyString());
  }

  @Test
  void getComponentInstanceParameterValueWithTwoDifferentParameters() {
    UploadSession uploadSession = initializeUploadSessionAndRegisterFile().getUploadSession();
    when(organisationControllerMock.getComponentParameterValue(anyString(), anyString()))
        .then(invocation -> "aParameter".equals(invocation.getArguments()[1]) ? "toto" : "titi");
    assertThat(uploadSession.getComponentInstanceParameterValue("aParameter"), is("toto"));
    assertThat(uploadSession.getComponentInstanceParameterValue("otherParameter"), is("titi"));
    verify(organisationControllerMock, times(2))
        .getComponentParameterValue(anyString(), anyString());
  }

  @Test
  void getComponentInstanceParameterValueWithTwoDifferentParametersCalledSeveralTimes() {
    UploadSession uploadSession = initializeUploadSessionAndRegisterFile().getUploadSession();
    when(organisationControllerMock.getComponentParameterValue(anyString(), anyString()))
        .then(invocation -> "aParameter".equals(invocation.getArguments()[1]) ? "toto" : "titi");
    assertThat(uploadSession.getComponentInstanceParameterValue("aParameter"), is("toto"));
    assertThat(uploadSession.getComponentInstanceParameterValue("aParameter"), is("toto"));
    assertThat(uploadSession.getComponentInstanceParameterValue("aParameter"), is("toto"));
    assertThat(uploadSession.getComponentInstanceParameterValue("aParameter"), is("toto"));
    assertThat(uploadSession.getComponentInstanceParameterValue("aParameter"), is("toto"));
    assertThat(uploadSession.getComponentInstanceParameterValue("aParameter"), is("toto"));
    assertThat(uploadSession.getComponentInstanceParameterValue("aParameter"), is("toto"));
    assertThat(uploadSession.getComponentInstanceParameterValue("otherParameter"), is("titi"));
    assertThat(uploadSession.getComponentInstanceParameterValue("otherParameter"), is("titi"));
    assertThat(uploadSession.getComponentInstanceParameterValue("otherParameter"), is("titi"));
    assertThat(uploadSession.getComponentInstanceParameterValue("otherParameter"), is("titi"));
    assertThat(uploadSession.getComponentInstanceParameterValue("otherParameter"), is("titi"));
    assertThat(uploadSession.getComponentInstanceParameterValue("otherParameter"), is("titi"));
    assertThat(uploadSession.getComponentInstanceParameterValue("otherParameter"), is("titi"));
    verify(organisationControllerMock, times(2))
        .getComponentParameterValue(anyString(), anyString());
  }

  /**
   * Creates an upload session and register a file.
   */
  private UploadSessionFile initializeUploadSessionAndRegisterFile() {
    return initializeUploadSessionAndRegisterFile(null);
  }

  /**
   * Creates an upload session from the given identifier and register a file.
   */
  private UploadSessionFile initializeUploadSessionAndRegisterFile(String uploadSessionId) {
    UploadSession uploadSession =
        UploadSession.from(uploadSessionId).forComponentInstanceId("instanceId");
    assertThat(new File(getTemporaryPath()).listFiles(), nullValue());
    UploadSessionFile uploadSessionFile = uploadSession.getUploadSessionFile("path/test/name");
    assertThat(new File(getTemporaryPath()).listFiles(), arrayWithSize(1));
    assertThat(getUploadSessionFile(uploadSession).exists(), is(true));
    assertThat(uploadSessionFile.getServerFile().exists(), is(false));
    return uploadSessionFile;
  }

  private File getUploadSessionFile(UploadSession uploadSession) {
    return new File(getTemporaryPath(), uploadSession.getId());
  }
}