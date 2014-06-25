package org.silverpeas.file;

/**
 * A processor of a {@link org.silverpeas.file.SilverpeasFile} instance. It performs some peculiar
 * tasks according to a file path in order to apply some additional business or technical rules
 * on the asked file.
 * @author mmoquillon
 */
public interface SilverpeasFileProcessor {

  /**
   * Processes the specified path and returns the new path of the SilverpeasFile to get. This method
   * is triggered before retrieving the SilverpeasFile matching a given file path. If nothing
   * should be done with the path, then just returns the path passed as argument.
   * @param path the path of the asked file.
   * @return either the specified path or a new path of the asked file.
   */
  public String processBefore(String path);

  /**
   * Processes the specified SilverpeasFile and returns the new one. This method is triggered after
   * retrieving the SilverpeasFile. If nothing should be done with the path, then just returns the
   * SilverpeasFile instance passed as argument.
   * @param file the SilverpeasFile to process.
   * @return either the specified one or a new one.
   */
  public SilverpeasFile processAfter(SilverpeasFile file);
}
