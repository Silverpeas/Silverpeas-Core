package com.stratelia.silverpeas.contentManager;

import java.util.List;

/**
 * The interface for all the content (filebox+, ..) Every container have to
 * implement this interface and declare it in the containerDescriptor (xml)
 */
public interface ContentInterface {
  /** Find all the SilverContents with the given SilverContentIds */
  public List getSilverContentById(List alSilverContentId, String sComponentId,
      String sUserId, List alContentUserRoles);

}
