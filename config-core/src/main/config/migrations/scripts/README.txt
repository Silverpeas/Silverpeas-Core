In this directory are located the Groovy script files implied in the migration of the Silverpeas
datasources.
In order the migration tool to be able to find them, the scripts must be placed into the
subdirectory whose path matches the following pattern:

  scripts/<module>/[<version>|<fromVersion>/

where:
  <module> is the name of the module (see migration descriptor in the modules/ directory) they
belong to,
  <version> is the current version at which the module has to be installed,
  <fromVersion> is the upgrade version (prefixed by the word 'up'); it means the version from which
  a module will be upgraded to the next one.

In order to be executed by the migration tool, these scripts has to be referred through the XML tag
'script' (the 'type' attribute should be valued with 'groovy') in the corresponding migration
descriptor located in the modules/ directory.