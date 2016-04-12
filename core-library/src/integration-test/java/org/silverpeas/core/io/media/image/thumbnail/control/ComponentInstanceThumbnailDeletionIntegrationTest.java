package org.silverpeas.core.io.media.image.thumbnail.control;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class ComponentInstanceThumbnailDeletionIntegrationTest {

  private static final int COMPONENT_INSTANCE_NB_THUMBNAILS = 5;

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/io/media/image/thumbnail/create-database.sql";
  private static final String DATASET_SCRIPT =
      "test-thumbnail-component-instance-deletion-data.sql";

  private Map<String, File> componentInstanceRepos = new HashMap<>();

  private ComponentInstanceDeletion thumbnailController;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore
        .onWarForTestClass(ComponentInstanceThumbnailDeletionIntegrationTest.class)
        .addDatabaseToolFeatures().addSilverpeasExceptionBases().addFileRepositoryFeatures()
        .addProcessFeatures().addApacheFileUploadFeatures()
        .addComponentInstanceDeletionFeatures()
        .testFocusedOn(war -> war
            .addPackages(true, "org.silverpeas.core.io.media.image.thumbnail")
            .addAsResource("org/silverpeas/core/io/media/image/thumbnail/create-database.sql"))
        .addAsResource("org/silverpeas/publication/publicationSettings.properties")
        .build();
  }

  @Before
  public void setup() throws Exception {
    thumbnailController = ServiceProvider.getService(ThumbnailController.class);

    for (String componentInstanceId : new String[]{"kmelia144", "kmelia160"}) {
      File componentInstanceRepo =
          new File(ThumbnailController.getImageDirectory(componentInstanceId));
      componentInstanceRepos.put(componentInstanceId, componentInstanceRepo);

      for (int i = 0; i < COMPONENT_INSTANCE_NB_THUMBNAILS; i++) {
        FileUtils.touch(new File(componentInstanceRepo, String.format("File_%1$s.jpg", (i + 1))));
      }

      SilverLogger.getLogger(this)
          .info("{0}ImagePath={1} with files {2}", componentInstanceId, componentInstanceRepo,
              Arrays.asList(componentInstanceRepo.listFiles()).stream().map(file -> file.getName())
                  .collect(Collectors.joining(", ")));
    }
  }

  @Test
  public void verifyingTestData() throws Exception {
    assertThat("Thumbnails", getThumbnails(), contains(
        "kmelia144 | 109 | 1 | 1382111141665.png",
        "kmelia144 | 112 | 1 | 1382111428789.png",
        "kmelia157 | 110 | 1 | 1382111283564.png",
        "kmelia157 | 111 | 1 | 1382111415607.png",
        "kmelia159 | 137 | 1 | 1384265587117.JPG",
        "kmelia159 | 171 | 1 | 1383066767571.jpg",
        "kmelia160 | 119 | 1 | 1382743216119.jpg",
        "kmelia160 | 124 | 1 | 1382796005525.jpg",
        "kmelia160 | 131 | 1 | 1382802694057.jpg",
        "kmelia160 | 132 | 1 | 1382955289211.jpg",
        "kmelia166 | 167 | 1 | 1383059151941.jpg",
        "kmelia188 | 211 | 1 | 1391424414501.png",
        "kmelia188 | 214 | 1 | 1391508876097.jpg",
        "kmelia188 | 317 | 1 | /silverpeas/GalleryInWysiwyg/dummy?ImageId=9e548e2d-8cc9-4865-993c-da07c6774c6d&ComponentId=gallery283&UseOriginal=false",
        "kmelia343 | 480 | 1 | 1422267307101.png"));

    for (Map.Entry<String, File> componentInstanceEntry : componentInstanceRepos.entrySet()) {
      assertThat(componentInstanceEntry.getKey(), componentInstanceEntry.getValue().listFiles(),
          arrayWithSize(COMPONENT_INSTANCE_NB_THUMBNAILS));
    }
  }

  @Test
  public void nothingShouldBeDeletedOnDeletionOfUnknownComponentInstanceId() throws Exception {
    thumbnailController.delete("kmeliaUnknown");
    verifyingTestData();
  }

  @Test
  public void dataAboutKmelia144ShouldBeDeletedAndNotThoseOfKmelia160() throws Exception {
    thumbnailController.delete("kmelia144");

    assertThat("Thumbnails", getThumbnails(), contains(
        "kmelia157 | 110 | 1 | 1382111283564.png",
        "kmelia157 | 111 | 1 | 1382111415607.png",
        "kmelia159 | 137 | 1 | 1384265587117.JPG",
        "kmelia159 | 171 | 1 | 1383066767571.jpg",
        "kmelia160 | 119 | 1 | 1382743216119.jpg",
        "kmelia160 | 124 | 1 | 1382796005525.jpg",
        "kmelia160 | 131 | 1 | 1382802694057.jpg",
        "kmelia160 | 132 | 1 | 1382955289211.jpg",
        "kmelia166 | 167 | 1 | 1383059151941.jpg",
        "kmelia188 | 211 | 1 | 1391424414501.png",
        "kmelia188 | 214 | 1 | 1391508876097.jpg",
        "kmelia188 | 317 | 1 | /silverpeas/GalleryInWysiwyg/dummy?ImageId=9e548e2d-8cc9-4865-993c-da07c6774c6d&ComponentId=gallery283&UseOriginal=false",
        "kmelia343 | 480 | 1 | 1422267307101.png"));

    File deletedComponentInstanceRepo = componentInstanceRepos.remove("kmelia144");
    assertThat(deletedComponentInstanceRepo.exists(), is(false));

    for (Map.Entry<String, File> componentInstanceEntry : componentInstanceRepos.entrySet()) {
      assertThat(componentInstanceEntry.getKey(), componentInstanceEntry.getValue().listFiles(),
          arrayWithSize(COMPONENT_INSTANCE_NB_THUMBNAILS));
    }
  }

  /**
   * Returns the list of thumbnails (sb_thumbnail_thumbnail table).
   * @return list of strings which the schema is:
   * [instanceid]-[objectid]-[objecttype]-[originalattachmentname]
   * @throws Exception
   */
  private List<String> getThumbnails() throws Exception {
    return JdbcSqlQuery.createSelect(
        "instanceid, objectid, objecttype, originalattachmentname from sb_thumbnail_thumbnail")
        .addSqlPart("order by instanceid, objectid")
        .execute(row -> row.getString(1) + " | " + row.getInt(2) + " | " + row.getInt(3) + " | " +
            row.getString(4));
  }
}
