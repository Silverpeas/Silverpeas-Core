package org.silverpeas.core.notification.user;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.FileLink;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.memory.MemoryUnit;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Web link to an attachment of a contribution in Silverpeas to render in a notification message.
 * It extends the {@link FileLink} class by providing useful methods for templates.
 * @author mmoquillon
 */
public class AttachmentLink extends FileLink {
  private final String language;

  /**
   * Gets all the web links to all of the documents attached to the specified contribution in
   * Silverpeas and for the given language.
   * @param resource a reference to the contribution in Silverpeas.
   * @param language the ISO 631-1 code of the language in which the attachments have to be get.
   * @return a list of Web links to the attachments. If the contribution have no attachments, then
   * an empty list is returned.
   */
  public static List<AttachmentLink> getForContribution(final ResourceReference resource,
      final String language) {
    final AttachmentService attachmentService = AttachmentService.get();
    return attachmentService.listDocumentsByForeignKey(resource, language).stream().map(d -> {
      final String label = StringUtil.isDefined(d.getTitle()) ? d.getTitle() : d.getFilename();
      return new AttachmentLink(URLUtil.getCurrentServerURL()+d.getUniversalURL(), label, d.getSize(), language);
    }).collect(Collectors.toList());
  }

  /**
   * Constructs a new link with the specified URL, labels and file size.
   * @param linkUrl the URL of the linked file.
   * @param linkLabel the label to render for that link.
   * @param linkSize the size in byte of the linked file.
   * @param language the language of the attachment.
   */
  public AttachmentLink(final String linkUrl, final String linkLabel, final long linkSize,
      final String language) {
    super(linkUrl, linkLabel, linkSize);
    this.language = language == null ? DisplayI18NHelper.getDefaultLanguage():language;
  }

  /**
   * Gets the size of the file referred by this link in text format. This method is used by
   * templates to indicate the size of the attached document targeted by this link.
   * @return the size of the linked file expressed into the more suitable size unit.
   */
  public String getFormattedLinkSize() {
    return formatFileSize(this.getLinkSize(), language);
  }

  private String formatFileSize(final long size, final String language) {
    final long divider;
    final String unit;
    if (size >= MemoryUnit.MB.getLimit().longValue()) {
      divider = MemoryUnit.MB.getLimit().longValue();
      unit = MemoryUnit.GB.getLabel(language);
    } else if (size >= MemoryUnit.KB.getLimit().longValue()) {
      divider = MemoryUnit.KB.getLimit().longValue();
      unit = MemoryUnit.MB.getLabel(language);
    } else if (size >= MemoryUnit.B.getLimit().longValue()) {
      divider = MemoryUnit.B.getLimit().longValue();
      unit = MemoryUnit.KB.getLabel(language);
    } else {
      divider = 1;
      unit = MemoryUnit.B.getLabel(language);
    }
    return (size / divider) + " " + unit;
  }
}
  