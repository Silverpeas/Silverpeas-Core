package org.silverpeas.core.util;

/**
 * A link to a file that can be accessed through the Web.
 * @author mmoquillon
 */
public class FileLink extends Link {

  private final long size;

  /**
   * Constructs a new link with the specified URL, labels and file size.
   * @param linkUrl the URL of the linked file.
   * @param linkLabel the label to render for that link.
   * @param linkSize the size in byte of the linked file.
   */
  public FileLink(final String linkUrl, final String linkLabel, final long linkSize) {
    super(linkUrl, linkLabel);
    if (linkSize < 0) {
      throw new AssertionError("The size of the linked file must be non-negative");
    }
    this.size = linkSize;
  }

  /**
   * Gets the size of the file referred by this link.
   * @return the size in bytes of the linked file.
   */
  public long getLinkSize() {
    return size;
  }
}
  