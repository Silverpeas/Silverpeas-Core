<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<view:sp-page>
<view:sp-head-part>
  <view:includePlugin name="map"/>
  <style>
    html, body, #map-canvas {
      margin: 0;
      padding: 0;
      height: 100%;
    }
  </style>
</view:sp-head-part>
<view:sp-body-part>
  <div id="map-canvas" class="map"></div>
  <script type="text/javascript">
    (function() {

      const moiAddr = new MapAddress({
        street : ['196, rue Rocher Du Lorzier'],
        postalCode : '38430',
        city : 'Moirans'
      })

      const greAddr = new MapAddress({
        street : ['1, place Firmin Gautier'],
        postalCode : '38000',
        city : 'Grenoble'
      })

      const wrongAddr = new MapAddress({
        street : ['1, rue toto'],
        postalCode : '26000',
        city : 'Valence'
      })

      const perneLonLat = new MapLonLat(5.050281700000028, 43.9942173);

      const locations = [
        new MapLocation(undefined, moiAddr),
        new MapLocation(undefined, wrongAddr),
        new MapLocation(undefined, greAddr),
        new MapLocation(perneLonLat, undefined)
      ];

      whenSilverpeasReady().then(function() {
        new MapApi('#map-canvas').render().then(function(mapInstance) {
          const promises = locations.map(function(location) {
            return location.promiseAddress().then(function(address) {
              return address.promiseLonLat().then(function(lonLat) {
                mapInstance.addNewMarker({
                  color : '#000',
                  title : address.getStreet().join(','),
                  position : lonLat.asOlData(),
                  contentPromise : sp.promise.resolveDirectlyWith(address.format())
                });
              });
            });
          });
          sp.promise.whenAllResolvedOrRejected(promises).then(function() {
            mapInstance.autoFit();
          });
        });
      });
    })();
  </script>
</view:sp-body-part>
</view:sp-page>