/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

const traitInputAttr = { placeholder: 'Text einfügen' };
const gI18n = {
  assetManager: {
    addButton: 'Bild hinzufügen',
    inputPlh: 'http://chemin/vers/image.jpg',
    modalTitle: 'Bild auswählen',
    uploadTitle: 'Ziehen Sie eine Datei hierher oder klicken Sie auf Upload'
  },
  // Here just as a reference, GrapesJS core doesn't contain any block,
  // so this should be omitted from other local files
  blockManager: {
    labels: {
      // 'block-id': 'Block Label',
    },
    categories: {
      // 'category-id': 'Kategorie Label',
    }
  },
  domComponents: {
    names: {
      '': 'Box',
      wrapper: 'Body',
      text: 'Text',
      comment: 'Kommentar',
      image: 'Bild',
      video: 'Video',
      label: 'Label',
      link: 'Link',
      map: 'Karte',
      tfoot: 'Tabellen Fußzeile',
      tbody: 'Tabellen Inhalt',
      thead: 'Tabellen Kopf',
      table: 'Tabelle',
      row: 'Zeile',
      cell: 'Zelle'
    }
  },
  deviceManager: {
    device: 'Gerät',
    devices: {
      desktop: 'Desktop',
      tablet: 'Tablet',
      mobileLandscape: 'Mobile Landscape',
      mobilePortrait: 'Mobile Portrait'
    }
  },
  panels: {
    buttons: {
      titles: {
        preview: 'Vorschau',
        fullscreen: 'Vollbild',
        'sw-visibility': 'Komponente anzeigen',
        'export-template': 'Endgültigen HTML-Code anzeigen',
        'open-sm': 'Stil Manager öffnen',
        'open-tm': 'Parameter',
        'open-layers': 'Layer Manager öffnen',
        'open-blocks': 'Block öffnen'
      }
    }
  },
  selectorManager: {
    label: 'Klassen',
    selected: 'Ausgewählt',
    emptyState: '- Status -',
    states: {
      hover: 'Hover',
      active: 'Klick',
      'nth-of-type(2n)': 'Gerade/Ungerade'
    }
  },
  styleManager: {
    empty:
      "Wählen Sie ein Element aus bevor Sie den Stil Manager nutzen",
    layer: 'Ebene',
    fileButton: 'Bilder',
    sectors: {
      general: 'Allgemein',
      layout: 'Layout',
      typography: 'Typographie',
      decorations: 'Dekorationen',
      extra: 'Extra',
      flex: 'Flex',
      dimension: 'Dimension'
    },
    // The core library generates the name by their `property` name
    properties: {
      float: 'Ausrichtung',
      display: 'Anzeige',
      position: 'Position',
      top: 'Oben',
      right: 'Rechts',
      left: 'Links',
      bottom: 'Unten',
      width: 'Breite',
      height: 'Höhe',
      'max-width': 'Breite max.',
      'max-height': 'Höhe max.',
      margin: 'Äußerer Abstand',
      'margin-top': 'Äußerer Abstand oben',
      'margin-right': 'Äußerer Abstand rechts',
      'margin-left': 'Äußerer Abstand links',
      'margin-bottom': 'Äußerer Abstand unten',
      padding: 'Innerer Abstand',
      'padding-top': 'Innerer Abstand oben',
      'padding-left': 'Innerer Abstand links',
      'padding-right': 'Innerer Abstand rechts',
      'padding-bottom': 'Innerer Abstand unten',
      'font-family': 'Schriftart',
      'font-size': 'Schriftgröße',
      'font-weight': 'Schriftstärke',
      'letter-spacing': 'Zeichenabstand',
      color: 'Schriftfarbe',
      'line-height': 'Zeilenhöhe',
      'text-align': 'Textausrichtung',
      'text-shadow': 'Textschatten',
      'text-shadow-h': 'Textschatten: horizontal',
      'text-shadow-v': 'Textschatten: vertikal',
      'text-shadow-blur': 'Textschatten: unschärfe',
      'text-shadow-color': 'Textschatten: Farbe',
      'border-top-left': 'Rand oben links',
      'border-top-right': 'Rand oben rechts',
      'border-bottom-left': 'Rand unten links',
      'border-bottom-right': 'Rand unten rechts',
      'border-radius-top-left': 'Rand Radius oben links',
      'border-radius-top-right': 'Rand Radius oben rechts',
      'border-radius-bottom-left': 'Rand Radius unten links',
      'border-radius-bottom-right': 'Rand Radius unten rechts',
      'border-radius': 'Rand Radius',
      border: 'Rand',
      'border-width': 'Randbreite',
      'border-style': 'Randstil',
      'border-color': 'Randfarbe',
      'box-shadow': 'Boxschatten',
      'box-shadow-h': 'Boxschatten: horizontal',
      'box-shadow-v': 'Boxschatten: vertikal',
      'box-shadow-blur': 'Boxschatten: Unschärfe',
      'box-shadow-spread': "Boxschatten: Streuung",
      'box-shadow-color': "Boxschatten: Farbe",
      'box-shadow-type': "Boxschatten: Typ",
      background: 'Hintergrund',
      'background-image': 'Hintergrundbild',
      'background-repeat': 'Hintergrund wiederholen',
      'background-position': 'Hintergrundposition',
      'background-attachment': 'Hintergrundanhang',
      'background-size': 'Hintergrundgröße',
      'background-color': 'Hintergrundfarbe',
      transition: 'Übergang',
      'transition-property': 'Übergang: Typ',
      'transition-duration': 'Übergang: Dauer',
      'transition-timing-function': 'Übergang: Zeitfunktion',
      perspective: 'Perspektive',
      transform: 'Transformation',
      'transform-rotate-x': 'Transformation: Rotation x',
      'transform-rotate-y': 'Transformation: Rotation y',
      'transform-rotate-z': 'Transformation: Rotation z',
      'transform-scale-x': 'Transformation: Skalierung x',
      'transform-scale-y': 'Transformation: Skalierung y',
      'transform-scale-z': 'Transformation: Skalierung z',
      'flex-direction': 'Flex Ausrichtung',
      'flex-wrap': 'Flex Wrap',
      'justify-content': 'Vertikale Ausrichtung',
      'align-items': 'Senkrechte Ausrichtung',
      'align-content': 'Achsenausrichtung',
      order: 'Reihenfolge',
      'flex-basis': 'Flex Basis',
      'flex-grow': 'Flex Wachsen',
      'flex-shrink': 'Flex Schrumpfen',
      'align-self': 'Eigene Ausrichtung'
    }
  },
  traitManager: {
    empty:
      "Wählen Sie ein Element aus bevor Sie den Komponenten Manager nutzen",
    label: 'Komponenteneinstellungen',
    traits: {
      // The core library generates the name by their `name` property
      labels: {
        id: 'ID',
        alt: 'Alternativtext',
        title: 'Titel',
        href: 'Link'
      },
      // In a simple trait, like text input, these are used on input attributes
      attributes: {
        id: traitInputAttr,
        alt: traitInputAttr,
        title: traitInputAttr,
        href: { placeholder: 'z.B. https://google.com' }
      },
      // In a trait like select, these are used to translate option names
      options: {
        target: {
          false: 'Dieses Fenster',
          _blank: 'Neues Fenster'
        }
      }
    }
  }
};
