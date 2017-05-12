package org.silverpeas.core.test.rule;

import org.silverpeas.core.cache.service.CacheServiceProvider;

/**
 * @author Yohann Chastagnier
 */
public class LibCoreCommonAPI4Test extends CommonAPI4Test {

  @Override
  protected void beforeEvaluate() {
    super.beforeEvaluate();
    clearCacheData();
  }

  private void clearCacheData() {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
  }
}
