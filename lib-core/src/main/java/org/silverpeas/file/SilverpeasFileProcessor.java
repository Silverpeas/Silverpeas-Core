package org.silverpeas.file;

import javax.ws.rs.Produces;

/**
 * A processor of a {@link org.silverpeas.file.SilverpeasFile} instance. It performs some peculiar
 * tasks according to a file path in order to apply some additional business or technical rules
 * on the asked file.
 * @author mmoquillon
 */
public interface SilverpeasFileProcessor {

  public static enum ProcessingContext {
    /**
     * The processing is about the getting of a file.
     */
    GETTING,
    /**
     * The processing is about the writing of the content into a file.
     */
    WRITING,
    /**
     * The processing is about the deletion of a file.
     */
    DELETION,
    /**
     * The processing is about the copy of a file.
     */
    COPY,
    /**
     * The processing is about the moving of a file.
     */
    MOVING
  }

  /**
   * Processes the specified path and returns the new path of the SilverpeasFile to get. This method
   * is triggered before retrieving the SilverpeasFile matching a given file path. If nothing
   * should be done with the path, then just returns the path passed as argument.
   * @param path the path of the asked file.
   * @param context the processing context.
   * @return either the specified path or a new path of the asked file.
   */
  public String processBefore(String path, ProcessingContext context);

  /**
   * Processes the specified SilverpeasFile and returns the new one. This method is triggered after
   * retrieving the SilverpeasFile. If nothing should be done with the path, then just returns the
   * SilverpeasFile instance passed as argument.
   * @param file the SilverpeasFile to process.
   * @param context the processing context.
   * @return either the specified one or a new one.
   */
  public SilverpeasFile processAfter(SilverpeasFile file, ProcessingContext context);
}
