$synchronizationRuleHelp_common()$
<div class="synchronizationRuleHelp">
  Cette règle est définie via l'écriture d'une expression par
  <b><a class="prefixedNotationHelp" href="#"> Notation Préfixée</a></b> avec parenthèses.<br/>
  Elle permet d'établir une combinaison entre les règles de base suivantes :
  <ul>
    <li><b>DS_AccessLevel = <i>[*, A, D, K, U ou G]</i></b>
      cible les comptes ayant le niveau d'accés indiqué :<br/>
      <b>*</b> = tous les comptes, <b>A</b> = les comptes administateur,<br/>
      <b>D</b> = les comptes gestionnaire de domaine, <b>K</b> = les comptes gestionnaire du Plan de
      Classement,<br/>
      <b>U</b> = les comptes utilisateurs, <b>G</b> = les comptes <i>invité</i>
    </li>
    <li><b>DS_Domains = <i>[identifiants de domaine séparés par une virgule]</i></b>
      cible les comptes enregistrés sur les domaines indiqués<br/>
      (fonctionne uniquement pour les groupes créés dans le <i>domaine mixte</i>)
    </li>
    <li><b>DC_<i>[propriété]</i> = <i>[valeur]</i></b>
      cible les comptes dont la propriété indiquée est égale à la valeur indiquée
    </li>
    <li><b>DR_Groups = <i>[identifiants de groupe séparés par une virgule]</i></b>
      cible les comptes directement liés à un des groupes indiqués (les comptes des
      sous-groupes sont ignorés)
    </li>
    <li><b>DR_GroupsWithSubGroups = <i>[identifiants de groupe séparés par une virgule]</i></b>
      cible les comptes liés à un des groupes indiqués ou à un des sous-groupes des groupes
      indiqués
    </li>
  </ul>
  Il peut être directement renseigné une règle de base, par exemple :<br/>
  <span class="example">DS_AccessLevel = A</span> pour cibler tous les comptes administrateur.
  <br/><br/>
  Pour combiner plusieurs règles de base entre elles, il faut utiliser les opérateurs
  disponibles :
  <ul>
    <li><b>&</b> : intersection entre les comptes ciblés de chacune des opérandes</li>
    <li><b>|</b> : union entre les comptes ciblés de chacune des opérandes</li>
    <li><b>!</b> : tous les comptes de la plate-forme sans ceux ciblés dans l'opérande. Il ne
      peut y avoir, pour cet opérateur, qu'une seule opérande
    </li>
  </ul>
  Quelques exemples :
  <ul>
    <li><span class="example">&(|(DS_AccessLevel=A)(DS_AccessLevel=U))(DC_company=Silverpeas)</span>
      : cible les comptes administrateur et les comptes utilisateur dont la valeur associée à la
      propriété 'company' est égale à 'Silverpeas'
    </li>
    <li>
      <span class="example">!(&(|(DS_AccessLevel=A)(DS_AccessLevel=U))(DC_company=Silverpeas))</span>
      : la négation de la règle précédente. Cela cible donc tous les comptes de la plate-forme
      qui ne sont pas ciblés par la règle précédente
    </li>
    <li>
      <span class="example">&(|(DS_AccessLevel=A)(DS_AccessLevel=U))(!(DC_company=Silverpeas))</span>
      : cible les comptes administrateurs et les comptes utilisateurs dont la valeur associée à la
      propriété 'company' n'est pas égale à 'Silverpeas'
    </li>
    <li>
      etc.
    </li>
  </ul>
</div>