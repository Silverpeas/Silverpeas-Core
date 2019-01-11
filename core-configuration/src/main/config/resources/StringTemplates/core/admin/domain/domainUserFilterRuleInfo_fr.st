$domainUserFilterRuleInfo_common()$
<div class="domainUserFilterRuleHelp">
  Cette règle est définie via l'écriture d'une expression par
  <b><a class="prefixedNotationHelp" href="#"> Notation Préfixée</a></b> avec parenthèses.<br/>
  <p>
    $if(domainGoogle)$
      Elle permet d'appliquer sur les comptes utilisateurs Google une combinaison de règles.<br/>
      Chaque utilisateur est représenté par une structure JSON dans les échanges comme détaillé
      <b><a href="https://developers.google.com/admin-sdk/directory/v1/reference/users" target="_blank">ici</a></b>.
    $endif$
    </p>
    Les règles sont les suivantes :
    <div class="precision" style="margin-top:3px"><b>[attribut ressource]</b> représente le chemin complet de la structure JSON</div>
    <div class="precision"><b>[attribut ressource]</b> est sensible à la casse et <b>[valeur en chaîne de caractères]</b> ne l'est pas</div>
    <ul>
      <li><b>[attribut ressource] = <i>[valeur en chaîne de caractères]</i></b>
        cible les comptes dont la valeur de l'attribut est égale à la valeur indiquée
      </li>
      <li><b>[valeur en chaîne de caractères]</b> peut contenir une ou plusieurs occurrences du caractère spécial
        <b>%</b>. Il permet d'indiquer aucun caractère ou n'importe quelle suite de caractères
      </li>
    </ul>
    <p>
    Il peut être directement renseigné une <b>règle de base</b>, par exemple :
    <ul>
      $if(domainGoogle)$
      <li><span class="code">orgUnitPath = /SIEGE</span> pour cibler les comptes utilisateurs dont l'unité organisationnelle est exactement <span class="code">/SIEGE</span>.</li>
      <li><span class="code">orgUnitPath = /SIEGE%</span> pour cibler les comptes utilisateurs dont l'unité organisationnelle commence par <span class="code">/SIEGE</span>.</li>
      <li><span class="code">name.familyName = Cha%</span> pour cibler les comptes utilisateurs dont le nom commence par <span class="code">Cha</span>.</li>
      $endif$
    </ul>
  </p>
  <p>
    Pour <b>combiner plusieurs règles de base</b> entre elles, il faut utiliser les opérateurs
    disponibles :
    <ul>
      <li><b>&</b> : intersection entre les comptes ciblés de chacune des opérandes</li>
      <li><b>|</b> : union entre les comptes ciblés de chacune des opérandes</li>
      <li><b>!</b> : tous les comptes de la plate-forme sans ceux ciblés dans l'opérande. Il ne
        peut y avoir, pour cet opérateur, qu'une seule opérande
      </li>
    </ul>
  </p>
  <p>
    Lorsque l'attribut porte sur une <b>donnée sous la forme de liste</b>, il faut utiliser
    <span class="code"><b>[</b></span> et <span class="code"><b>]</b></span> pour la représenter
    et indiquer entre ces deux caractères la règle ou la combinaison de règles.<br/>
    Par exemple :
    <ul>
      $if(domainGoogle)$
      <li><span class="code">addresses[locality=GRENOBLE]</span> pour cibler les comptes utilisateurs
       dont la localité d'une des adresses est <span class="code">GRENOBLE</span>.</li>
      <li><span class="code">emails[|(address=%.com)(address=%.org)]</span> pour cibler les comptes
       utilisateurs dont l'un des e-mails se termine par <span class="code">.com</span> ou
       <span class="code">.org</span>.</li>
      $endif$
    </ul>
  </p>
  <p>
    Quelques exemples de combinaison de règles :
    <ul>
    $if(domainGoogle)$
      <li><span class="code">&(|(orgUnitPath=/SIEGE%)(orgUnitPath=/AGENCES%))(!(customSchemas.flags.technical=True))</span>
        : cible les comptes dont l'unité organisationnelle commence par <span class="code">/SIEGE</span>
         ou <span class="code">/AGENCES</span> et qui ne sont pas des comptes techniques
        <div class="precision"><span class="code">customSchemas.flags.technical</span> est un attribut
        non imposé par Google mais par les admistrateurs du compte Google G-Suite
        <a href="https://developers.google.com/admin-sdk/directory/v1/reference/users#customSchemas" target="_blank">(cf. doc)</a></div>
      </li>
      <li><span class="code">&(addresses[|(locality=GRENOBLE)(postalCode=38000)])(isAdmin=false)</span>
        : cible les comptes dont une des addresses postales contient <span class="code">GRENOBLE</span> pour la ville
         ou <span class="code">38000</span> pour le code postal et qui ne sont pas des comptes administrateurs
      </li>
    $endif$
      <li>
        etc.
      </li>
    </ul>
  </p>
</div>