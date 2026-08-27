-- The subscriptions on the PdC are now taken in charge by the subscription API of Silverpeas.
-- SB_PDC_Subscription only defines from now the sets of positions on the PdC that can be aimed by
-- a subscription. The existing subscriptions, that could only be created by the subscribers
-- themselves, are transferred into the subscriptions data source.
INSERT INTO subscribe (subscriberId, subscriberType, subscriptionMethod, resourceId, resourceType,
                       space, instanceId, creatorId, creationDate)
SELECT CAST(ownerId AS varchar2(100)),
       'USER',
       'SELF_CREATION',
       CAST(id AS varchar2(100)),
       'PDC',
       '-',
       '-',
       CAST(ownerId AS varchar2(100)),
       CURRENT_TIMESTAMP
FROM SB_PDC_Subscription;

ALTER TABLE SB_PDC_Subscription DROP COLUMN ownerId;
