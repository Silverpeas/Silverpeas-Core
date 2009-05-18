package com.stratelia.silverpeas.silverstatistics.control;

import java.util.Collection;

public interface ComponentStatisticsInterface
{
    public Collection getVolume(String spaceId, String componentId) throws Exception;
}
