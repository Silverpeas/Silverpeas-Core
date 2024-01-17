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
package org.silverpeas.core.io.temp;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.Charsets;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.io.temp.TemporaryWorkspaceTranslation.startWithTranslationDescriptorPrefix;
import static org.silverpeas.core.util.file.FileRepositoryManager.getTemporaryPath;

/**
 * In the tests below, the last modification time is asserted with split-second accuracy as the
 * test running system is unable to be consistent over the time with the time measurements.
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
class TemporaryWorkspaceTranslationTest {

  private static final String TRANSLATION_ID_KEY = "__sptrans_id=";
  private static final String SILVERPEAS_TRANSLATION_PREFIX = "__sptrans_";
  private final static String TEST_WORKSPACE_ID = "workspaceId";
  private File tempPath;

  @BeforeEach
  @AfterEach
  public void cleanTest() {
    FileUtils.deleteQuietly(new File(getTemporaryPath()));
  }

  @BeforeEach
  public void setup() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    tempPath = new File(getTemporaryPath());
  }

  @Test
  void testWorkspaceCreateAndExist() throws Exception {
    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    assertThat(test.getRootPath().exists(), is(false));
    assertThat(test.exists(), is(false));
    assertThat(test.lastModified(), is(0L));

    long createTime = System.currentTimeMillis() / 1000;
    await(1001);
    test.create();

    File descriptor = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    File workspace = test.getRootPath();
    assertThat(descriptor, not(is(workspace)));
    assertThat(tempPath.listFiles(), arrayContainingInAnyOrder(descriptor, workspace));
    assertThat(test.getRootPath().exists(), is(true));
    assertThat(test.exists(), is(true));
    assertThat(test.lastModified() / 1000, greaterThanOrEqualTo(createTime));
    assertThat(readFileToString(descriptor, Charsets.UTF_8),
        is(TRANSLATION_ID_KEY + workspace.getName()));

    test.create();
    test.create();

    assertThat(tempPath.listFiles(), arrayContainingInAnyOrder(descriptor, workspace));
    assertThat(test.getRootPath().exists(), is(true));
    assertThat(test.exists(), is(true));
    assertThat(test.lastModified() / 1000, greaterThanOrEqualTo(createTime));
    assertThat(readFileToString(descriptor, Charsets.UTF_8),
        is(TRANSLATION_ID_KEY + workspace.getName()));
  }

  @Test
  void testWorkspaceExist() throws Exception {
    File descriptorSrc = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    File workspaceSrc = new File(tempPath, "totototototototototototototototototototototo");
    FileUtils.writeStringToFile(descriptorSrc,
        TRANSLATION_ID_KEY + "totototototototototototototototototototototo", Charsets.UTF_8);
    //noinspection ResultOfMethodCallIgnored
    workspaceSrc.mkdirs();

    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    File descriptor = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    File workspace = test.getRootPath();
    assertThat(descriptor, not(is(workspace)));
    assertThat(descriptor, is(descriptorSrc));
    assertThat(workspace, is(workspaceSrc));
    assertThat(test.get("key"), nullValue());
  }

  @Test
  void testWorkspaceExistWithAdditionalData() throws Exception {
    File descriptorSrc = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    File workspaceSrc = new File(tempPath, "titititititititititititititititititititititi");
    FileUtils.writeStringToFile(descriptorSrc,
        TRANSLATION_ID_KEY + "titititititititititititititititititititititi", Charsets.UTF_8);
    FileUtils.writeStringToFile(descriptorSrc,
        "\nkey=" + Base64.encodeBase64String(SerializationUtils.serialize("value")),
        Charsets.UTF_8, true);
    //noinspection ResultOfMethodCallIgnored
    workspaceSrc.mkdirs();

    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    File descriptor = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    File workspace = test.getRootPath();
    assertThat(descriptor, not(is(workspace)));
    assertThat(descriptor, is(descriptorSrc));
    assertThat(workspace, is(workspaceSrc));
    assertThat(test.get("key"), is("value"));
  }

  @Test
  void testWorkspaceNotExistBecauseDescriptorDoesNotExist() {
    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    await(1);
    test.create();

    File descriptor = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    File workspace = test.getRootPath();
    assertThat(descriptor, not(is(workspace)));

    //noinspection ResultOfMethodCallIgnored
    descriptor.delete();

    assertThat(test.getRootPath().exists(), is(true));
    assertThat(test.exists(), is(false));
    assertThat(test.lastModified(), is(0L));

    long createTime = System.currentTimeMillis() / 1000;
    test.create();

    assertThat(tempPath.listFiles(), arrayContainingInAnyOrder(descriptor, workspace));
    assertThat(test.getRootPath().exists(), is(true));
    assertThat(test.exists(), is(true));
    assertThat(test.lastModified(), greaterThan(0L));
    assertThat(test.lastModified() / 1000, greaterThanOrEqualTo(createTime));
  }

  @Test
  void testWorkspaceNotExistBecauseRootWorkspaceDoesNotExist() {
    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    System.currentTimeMillis();
    await(1);
    test.create();

    File descriptor = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    File workspace = test.getRootPath();

    //noinspection ResultOfMethodCallIgnored
    workspace.delete();

    assertThat(test.getRootPath().exists(), is(false));
    assertThat(test.exists(), is(false));
    assertThat(test.lastModified(), is(0L));

    long createTime = System.currentTimeMillis() / 1000;
    await(1001);
    test.create();

    assertThat(descriptor, not(is(workspace)));
    assertThat(tempPath.listFiles(), arrayContainingInAnyOrder(descriptor, workspace));
    assertThat(test.getRootPath().exists(), is(true));
    assertThat(test.exists(), is(true));
    assertThat(test.lastModified() / 1000, greaterThan(createTime));
  }

  @Test
  void testWorkspaceRemove() {
    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    await(1);
    test.create();

    File descriptor = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    File workspace = test.getRootPath();
    assertThat(descriptor, not(is(workspace)));

    assertThat(test.exists(), is(true));
    test.remove();

    assertThat(tempPath.listFiles(), emptyArray());
    assertThat(test.getRootPath().exists(), is(false));
    assertThat(test.exists(), is(false));
    assertThat(test.lastModified(), is(0L));
  }

  @Test
  void testWorkspaceRemoveAndCreateAgain() {
    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    await(1);
    test.create();

    String workspaceName = test.getRootPath().getName();
    assertThat(test.exists(), is(true));

    test.remove();
    test.create();

    assertThat(test.exists(), is(true));
    assertThat(test.getRootPath().getName(), not(is(workspaceName)));
  }

  @Test
  void testStartWithTranslationDescriptorPrefix() {
    assertThat(startWithTranslationDescriptorPrefix(null), is(false));
    assertThat(startWithTranslationDescriptorPrefix(""), is(false));
    assertThat(startWithTranslationDescriptorPrefix(" "), is(false));
    assertThat(startWithTranslationDescriptorPrefix(TEST_WORKSPACE_ID), is(false));
    assertThat(
        startWithTranslationDescriptorPrefix(SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID),
        is(true));
    assertThat(startWithTranslationDescriptorPrefix(
        "/" + SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID), is(true));
    assertThat(startWithTranslationDescriptorPrefix(
        "folder/" + SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID), is(false));
    assertThat(startWithTranslationDescriptorPrefix(
        "-" + SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID), is(false));
    assertThat(startWithTranslationDescriptorPrefix(
        SILVERPEAS_TRANSLATION_PREFIX + "/" + TEST_WORKSPACE_ID), is(true));
  }

  @Test
  void testWorkspacePutKeyValueOnNotCreatedWorkspace() {
    File descriptor = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    Serializable serializable = new SerializableClass();
    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    test.put("key", "value");
    test.put("otherKey", serializable);
    assertThat(test.get("key"), is("value"));
    assertThat(test.get("otherKey"), not(sameInstance(serializable)));
    assertThat(test.get("otherKey").toString(), is(serializable.toString()));
    assertThat(descriptor.exists(), is(false));

    test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    assertThat(descriptor.exists(), is(false));
    assertThat(test.get("key"), nullValue());
    assertThat(test.get("otherKey"), nullValue());
  }

  @Test
  void testWorkspacePutKeyValueOnNotCreateWorkspaceAndReload() throws Exception {
    File descriptor = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    Serializable serializable = new SerializableClass();
    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    test.create();
    assertThat(descriptor.exists(), is(true));

    test.put("key", "value");
    test.put("otherKey", serializable);
    assertThat(test.get("key"), is("value"));
    assertThat(test.get("otherKey"), not(sameInstance(serializable)));
    assertThat(test.get("otherKey").toString(), is(serializable.toString()));

    assertThat(FileUtils.readFileToString(descriptor, Charsets.UTF_8),
        containsString(TRANSLATION_ID_KEY));
    assertThat(FileUtils.readFileToString(descriptor, Charsets.UTF_8), containsString("key="));
    assertThat(FileUtils.readFileToString(descriptor, Charsets.UTF_8), containsString("otherKey="));

    test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    assertThat(test.get("key"), notNullValue());
    assertThat(test.get("otherKey"), notNullValue());
    assertThat(test.get("key"), is("value"));
    assertThat(test.get("otherKey"), not(sameInstance(serializable)));
    assertThat(test.get("otherKey").toString(), is(serializable.toString()));
  }

  @Test
  void testWorkspacePutNullObject() {
    File descriptor = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    test.create();
    test.put("key", null);
    assertThat(descriptor.exists(), is(true));
    assertThat(test.get("key"), nullValue());

    test.put("key", "value");
    assertThat(test.get("key"), notNullValue());

    test.put("key", null);
    assertThat(test.get("key"), nullValue());
  }

  @Test
  void testWorkspaceGetObjectThatDoesNotExist() {
    File descriptor = new File(tempPath, (SILVERPEAS_TRANSLATION_PREFIX + TEST_WORKSPACE_ID));
    TemporaryWorkspaceTranslation test = TemporaryWorkspaceTranslation.from(TEST_WORKSPACE_ID);
    test.create();
    assertThat(descriptor.exists(), is(true));
    assertThat(test.get("otherKey"), nullValue());
  }

  private static void await(long timeInMillis) {
    Awaitility.await().pollDelay(timeInMillis, TimeUnit.MILLISECONDS).until(() -> true);
  }

  /**
   * @author Yohann Chastagnier
   */
  public static class SerializableClass implements Serializable {
    private static final long serialVersionUID = 6097050519496876662L;

    private static final String stringValue = "serializedString";
    private final File fileValue = new File("test/serializedFile");

    @Override
    public String toString() {
      return stringValue + "|" + fileValue.getPath();
    }
  }
}