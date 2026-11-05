# Application instance limits

An application may declare either or both optional fields at the end of its `WAComponent` descriptor,
after `parameters`:

```xml
<maxInstancesPerSpace>1</maxInstancesPerSpace>
<maxInstancesPerPlatform>5</maxInstancesPerPlatform>
```

Absence or zero means unlimited. Positive values are inclusive maximum active instance counts.
Negative values are invalid. A space limit applies to the direct parent only, independently of
subspaces. Platform limits apply across all spaces. Both constraints are enforced if declared.
There is no application-specific name in the Core policy.

Discussion declares one instance per space. Delegated news declares one per platform.
The descriptor additions live in Silverpeas-Components and require this Core build first.
Space permission inheritance remains the existing descriptor default.

## Enforcement

`ComponentInstManager` checks before creation (including copies), cross-space moves and restoration.
Restoring via a status update is checked too. Reordering within a space does not consume capacity.
Moving an active global instance does not consume another global slot. Basket entries do not count.
Existing installations with excess instances keep them; further additions/restorations are refused.
The catalogue is advisory and includes an existing-instance link for a singleton. The server always
rechecks under the transaction lock so stale pages cannot bypass the rule.

The mandatory JTA transaction retains the no-op UPDATE row lock until the membership write commits
or rolls back. The direct-space row is the space mutex. The lowest existing space row is the platform
mutex; platform locks are acquired first when both limits apply. No additional SQL schema is needed.
The SQL uses ordinary UPDATE and SELECT statements for the supported databases. Concurrent H2 tests
verify that a second connection waits for commit and sees the updated count. Application servers must
use their normal JTA-enlisted Silverpeas datasource. Direct manual SQL bypasses the service policy.

## Descriptor schema

`core-library/src/main/resources/org/silverpeas/core/admin/component/component.xsd` is a bundled copy
of the official https://www.silverpeas.org/xsd/component.xsd, extended with the two optional fields.
A test validates both fields and rejects negatives. This updated schema should also be published at
the public schema URL by its maintainer. Registry loading remains JAXB-compatible with existing
descriptors and does not require a network request to validate them at startup.

## Validation

Run the focused administration/descriptor tests and the catalogue browser regression. Build Core
before the component configurations and rebuild/reinstall the Silverpeas assembly afterwards.
No existing instance is deleted, migrated, renamed or given different permissions automatically.
