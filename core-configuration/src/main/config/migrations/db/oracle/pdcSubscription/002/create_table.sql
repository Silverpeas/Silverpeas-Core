CREATE TABLE SB_PDC_Subscription
(
	id			int		NOT NULL ,
	name			varchar (255)	NOT NULL ,
	ownerId			int		NOT NULL
)
;

CREATE TABLE SB_PDC_Subscription_Axis
(
	id			int		NOT NULL ,
	pdcSubscriptionId	int		NOT NULL ,
	axisId			int		NOT NULL ,
	value			varchar (100)
)
;