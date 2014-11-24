package com.silverpeas.sharing.repository;

import com.silverpeas.sharing.model.DownloadDetail;
import org.silverpeas.persistence.model.identifier.UniqueLongIdentifier;
import org.silverpeas.persistence.repository.jpa.JpaBasicEntityManager;

/**
 * @author: ebonnet
 */
public class DownloadDetailJpaManager
    extends JpaBasicEntityManager<DownloadDetail, UniqueLongIdentifier>
    implements DownloadDetailRepository {
}
