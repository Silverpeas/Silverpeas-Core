<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<WAComponent xmlns="http://silverpeas.org/xml/ns/component"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://silverpeas.org/xml/ns/component http://www.silverpeas.org/xsd/component.xsd">
  <name>$WAComponent.name$</name>
  <behaviors>
    <behavior>workflow</behavior>
  </behaviors>
  <label>
    $WAComponent.label.keys:{k| <message lang="$k$">$WAComponent.label.(k)$</message>};separator="\n"$
  </label>
  <description>
    $WAComponent.description.keys:{k| <message lang="$k$">$WAComponent.description.(k)$</message>};separator="\n"$
  </description>
  <suite>
    <message lang="fr">05 Workflow</message>
  </suite>
  <visible>true</visible>
  <visibleInPersonalSpace>false</visibleInPersonalSpace>
  <portlet>false</portlet>
  <router>RprocessManager</router>
  <profiles>
    $WAComponent.profiles:{<profile name="$it.name$">
  <label>
    $it.label.keys:{k| <message lang="$k$">$it.label.(k)$</message>};separator="\n"$
  </label>
  <help>
    $it.help.keys:{k| <message lang="$k$">$it.help.(k)$</message>};separator="\n"$
  </help>
</profile>};separator="\n"$
  </profiles>
  <parameters>
    <parameter>
      <name>XMLFileName</name>
      <label>
        <message lang="fr">Modèle du processus</message>
        <message lang="en">Process model</message>
      </label>
      <order>0</order>
      <mandatory>true</mandatory>
      <value>$processModelFileName$</value>
      <type>text</type>
      <updatable>always</updatable>
      <help>
        <message lang="fr">Permet de spécifier le fichier XML qui décrit le processus</message>
        <message lang="en">Allows you to specify the XML file that describes the process</message>
      </help>
      <warning/>
    </parameter>
    <parameter>
      <name>exportCSV</name>
      <label>
        <message lang="fr">Export CSV</message>
        <message lang="en">CSV export</message>
      </label>
      <order>1</order>
      <mandatory>true</mandatory>
      <value>no</value>
      <type>checkbox</type>
      <updatable>always</updatable>
      <help>
        <message lang="fr">Export des données dans un fichier CSV</message>
        <message lang="en">Exporting all data in a CSV file</message>
      </help>
      <warning/>
    </parameter>
    <parameter>
      <name>fieldsToExport</name>
      <label>
        <message lang="fr">Champs à exporter</message>
        <message lang="en">Fields to export</message>
      </label>
      <order>2</order>
      <mandatory>false</mandatory>
      <value></value>
      <type>text</type>
      <updatable>always</updatable>
      <help>
        <message lang="fr">Les champs du workflow à exporter</message>
        <message lang="en">Fields to be exported</message>
      </help>
      <warning/>
    </parameter>
    <parameter>
      <name>historyTabEnable</name>
      <label>
        <message lang="fr">Historique</message>
        <message lang="en">History</message>
      </label>
      <order>3</order>
      <mandatory>false</mandatory>
      <value>yes</value>
      <type>checkbox</type>
      <updatable>always</updatable>
      <help>
        <message lang="fr">Permet de consulter l'historique détaillé de chaque demande</message>
        <message lang="en">Allows you to consult the detailed history of each request</message>
      </help>
      <warning/>
    </parameter>
    <parameter>
      <name>filterHistory</name>
      <label>
        <message lang="fr">Historique filtré</message>
        <message lang="en">Filtered history</message>
      </label>
      <order>4</order>
      <mandatory>false</mandatory>
      <value>no</value>
      <type>checkbox</type>
      <updatable>always</updatable>
      <help>
        <message lang="fr">Les formulaires associés à chaque état sont visibles uniquement si l'utilisateur courant avait accès à l'état</message>
        <message lang="en">The forms associated with each state are visible only if the current user had access to the state</message>
      </help>
      <warning/>
    </parameter>
    <parameter>
      <name>hideReturn</name>
      <label>
        <message lang="fr">Retour à...</message>
        <message lang="en">Return to...</message>
      </label>
      <order>5</order>
      <mandatory>false</mandatory>
      <value>no</value>
      <type>checkbox</type>
      <updatable>always</updatable>
      <help>
        <message lang="fr">Permet à l'utilisateur de retourner la demande à l'état précédent</message>
        <message lang="en">Allows the user to return the request to the previous state</message>
      </help>
      <warning/>
    </parameter>
    <parameter>
      <name>printButtonEnabled</name>
      <label>
        <message lang="fr">Impression</message>
        <message lang="en">Print</message>
      </label>
      <order>6</order>
      <mandatory>false</mandatory>
      <value>yes</value>
      <type>checkbox</type>
      <updatable>always</updatable>
      <help>
        <message lang="fr">Permet d'imprimer la demande</message>
        <message lang="en">Allows user to print the request</message>
      </help>
      <warning/>
    </parameter>
    <parameter>
      <name>saveButtonEnabled</name>
      <label>
        <message lang="fr">Brouillon</message>
        <message lang="en">Draft</message>
      </label>
      <order>7</order>
      <mandatory>false</mandatory>
      <value>no</value>
      <type>checkbox</type>
      <updatable>always</updatable>
      <help>
        <message lang="fr">Possibilité de sauvegarder en brouillon</message>
        <message lang="en">Possibility to save in draft</message>
      </help>
      <warning/>
    </parameter>
    <parameter>
      <name>processIdVisible</name>
      <label>
        <message lang="fr">Identifiant</message>
        <message lang="en">Display id</message>
      </label>
      <value>no</value>
      <mandatory>false</mandatory>
      <type>checkbox</type>
      <updatable>always</updatable>
      <order>8</order>
      <help>
        <message lang="fr">Permet d'afficher l'identifiant de la demande</message>
        <message lang="en">Allow to display the request identifier</message>
      </help>
      <warning/>
    </parameter>
    <parameter>
      <name>attachmentTabEnable</name>
      <label>
        <message lang="fr">Onglet "Fichiers"</message>
        <message lang="en">"Files" tab</message>
      </label>
      <value>no</value>
      <mandatory>false</mandatory>
      <type>checkbox</type>
      <updatable>always</updatable>
      <order>9</order>
      <help>
        <message lang="fr">Permet de joindre des fichiers annexes (en plus de ceux définis dans le dossier)</message>
        <message lang="en">Allows attachment of additional files (in addition to those defined in the request)</message>
      </help>
      <warning/>
    </parameter>
  </parameters>
</WAComponent>