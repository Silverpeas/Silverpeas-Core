/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.media.streaming;

import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.media.streaming.StreamingProvider;
import org.silverpeas.core.media.streaming.StreamingProvidersRegistry;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.rs.WebEntity;
import org.silverpeas.core.webapi.media.MediaDefinitionEntity;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.webapi.media.streaming.StreamingRequester.getJsonOembedAsString;

/**
 * This entity ensures that all streaming data are formatted in a single way whatever the
 * streaming provider.
 * @author silveryocha
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StreamingProviderDataEntity implements WebEntity {
  private static final long serialVersionUID = 4017230238128160967L;

  public static final String NAME = "default";

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlTransient
  private String originalUrl;

  @XmlElement
  private String provider;

  @XmlElement
  private String title;

  @XmlElement
  private String author;

  @XmlElement
  private String formattedDurationHMS;

  @XmlElement
  private MediaDefinitionEntity definition =
      MediaDefinitionEntity.createFrom(Definition.fromZero());

  @XmlElement
  private String embedHtml;

  @XmlElement
  private URI thumbnailUrl;

  @XmlElement
  private MediaDefinitionEntity thumbnailDefinition;

  @XmlElement
  private List<URI> thumbnailPreviewUrls = new ArrayList<>();

  /**
   * Creates a streaming provider data entity from specified homepage url.
   * @param homepageUrl the streaming home page url.
   * @return the streaming provider data entity representing the specified streaming.
   */
  public static Optional<StreamingProviderDataEntity> from(final String homepageUrl) {
    return StreamingProvidersRegistry.get().getFromUrl(homepageUrl)
        .map(p -> {
          final OembedDataEntity oembedData =
              JSONCodec.decode(getJsonOembedAsString(homepageUrl), OembedDataEntity.class);
          final StreamingProviderDataEntity entity = StreamingProviderDataEntityFactory.get()
              .createWith(p, oembedData);
          entity.originalUrl = homepageUrl;
          return entity;
        })
        .map(e -> {
          if (URLUtil.getCurrentServerURL().startsWith("https")) {
            e.forceSecureEmbedUrl();
          }
          return e;
        });
  }

  public StreamingProviderDataEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Constructor from OembedDataEntity ({@literal http://oembed.com}).
   * @param streamingProvider the Silverpeas provider identifier.
   * @param oembedData the oembed data as JSON format.
   */
  protected StreamingProviderDataEntity(StreamingProvider streamingProvider,
      final OembedDataEntity oembedData) {
    this.provider = streamingProvider.getName();
    this.title = oembedData.getTitle();
    this.author = oembedData.getAuthor();
    this.embedHtml = oembedData.getHtml();

    String givenThumbnailUrl = oembedData.getThumbnailUrl();
    if (isDefined(givenThumbnailUrl)) {
      this.thumbnailUrl = URI.create(givenThumbnailUrl);
      String thumbnailWidth = oembedData.getThumbnailWidth();
      String thumbnailHeight = oembedData.getThumbnailHeight();
      if (isDefined(thumbnailWidth) && isDefined(thumbnailHeight)) {
        this.thumbnailDefinition = MediaDefinitionEntity.createFrom(Definition
            .of(Integer.parseInt(thumbnailWidth.replaceAll("[^0-9].", "")),
                Integer.parseInt(thumbnailHeight.replaceAll("[^0-9].", ""))));
      }
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  protected StreamingProviderDataEntity() {
  }

  @Override
  public URI getURI() {
    return uri;
  }

  public void setUri(final URI uri) {
    this.uri = uri;
  }

  public StreamingProvider getProvider() {
    return StreamingProvidersRegistry.get().getByName(provider).orElse(null);
  }

  public void setProvider(final StreamingProvider provider) {
    this.provider = provider.getName();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(final String author) {
    this.author = author;
  }

  public String getFormattedDurationHMS() {
    return formattedDurationHMS;
  }

  public void setFormattedDurationHMS(final String formattedDurationHMS) {
    this.formattedDurationHMS = formattedDurationHMS;
  }

  public MediaDefinitionEntity getDefinition() {
    return definition;
  }

  public void setDefinition(final MediaDefinitionEntity definition) {
    this.definition = definition;
  }

  public String getSilverpeasEmbedUrl() {
    return UriBuilder.fromUri(URLUtil.getApplicationURL())
        .path("services/media/streaming/player")
        .queryParam("url", originalUrl)
        .build()
        .toString();
  }

  public String getEmbedHtml() {
    return embedHtml;
  }

  public void setEmbedHtml(final String embedHtml) {
    this.embedHtml = embedHtml;
  }

  public void forceSecureEmbedUrl() {
    setEmbedHtml(getEmbedHtml().replace("http://", "https://"));
  }

  public URI getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(final URI thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public MediaDefinitionEntity getThumbnailDefinition() {
    return thumbnailDefinition;
  }

  public void setThumbnailDefinition(final MediaDefinitionEntity thumbnailDefinition) {
    this.thumbnailDefinition = thumbnailDefinition;
  }

  public List<URI> getThumbnailPreviewUrls() {
    return thumbnailPreviewUrls;
  }

  public void setThumbnailPreviewUrls(final List<URI> thumbnailPreviewUrls) {
    this.thumbnailPreviewUrls = thumbnailPreviewUrls;
  }
}
