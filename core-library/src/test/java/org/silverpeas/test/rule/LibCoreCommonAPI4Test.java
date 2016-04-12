package org.silverpeas.core.test.rule;

import org.silverpeas.core.cache.service.CacheServiceProvider;

/**
 * @author Yohann Chastagnier
 */
public class LibCoreCommonAPI4Test extends CommonAPI4Test {

  @Override
  protected void beforeEvaluate(final TestContext context) {
    super.beforeEvaluate(context);
    clearCacheData();
  }

  private void clearCacheData() {
    CacheServiceProvider.getRequestCacheService().clear();
    CacheServiceProvider.getThreadCacheService().clear();
  }
}
