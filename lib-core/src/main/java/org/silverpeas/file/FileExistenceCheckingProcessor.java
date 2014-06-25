package org.silverpeas.file;

import javax.annotation.PostConstruct;
import javax.inject.Named;

/**
 * A processor to check the existence of a {@link org.silverpeas.file.SilverpeasFile}. It the
 * file doesn't exist, then {@¢ode NO_FILE} is returned.
 * @author mmoquillon
 */
@Named("fileExistenceCheckingProcessor")
public class FileExistenceCheckingProcessor implements SilverpeasFileProcessor {

  /**
   * Registers itself among the SilverpeasFileFactory instance.
   */
  @PostConstruct
  public void registerItself() {
    SilverpeasFileProvider fileFactory = SilverpeasFileProvider.getInstance();
    fileFactory.addProcessor(this);
  }

  @Override
  public String processBefore(final String path) {
    return path;
  }

  @Override
  public SilverpeasFile processAfter(final SilverpeasFile file) {
    SilverpeasFile validatedFile = file;
    if (!validatedFile.exists() && !validatedFile.isFile()) {
      validatedFile = SilverpeasFile.NO_FILE;
    }
    return validatedFile;
  }
}
