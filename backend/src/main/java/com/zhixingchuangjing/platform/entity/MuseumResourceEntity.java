package com.zhixingchuangjing.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "museum_resources")
public class MuseumResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "external_id", length = 128, nullable = false)
    private String externalId;

    @Column(name = "resource_type", length = 20, nullable = false)
    private String resourceType;

    @Column(name = "category", length = 64, nullable = false)
    private String category;

    @Column(name = "dynasty", length = 64)
    private String dynasty;

    @Column(name = "material", length = 64)
    private String material;

    @Column(name = "region", length = 64)
    private String region;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "subtitle", length = 255)
    private String subtitle;

    @Column(name = "museum_name", length = 128)
    private String museumName;

    @Column(name = "cover_url", length = 255)
    private String coverUrl;

    @Column(name = "detail_url", length = 255)
    private String detailUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tags_json", columnDefinition = "JSON")
    private String tagsJson;

    @Column(name = "metadata_json", columnDefinition = "JSON")
    private String metadataJson;

    @Column(name = "raw_payload", columnDefinition = "JSON")
    private String rawPayload;

    @Column(name = "cache_status", length = 20, nullable = false)
    private String cacheStatus;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Column(name = "cache_expire_at")
    private LocalDateTime cacheExpireAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDynasty() { return dynasty; }
    public void setDynasty(String dynasty) { this.dynasty = dynasty; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getMuseumName() { return museumName; }
    public void setMuseumName(String museumName) { this.museumName = museumName; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDetailUrl() { return detailUrl; }
    public void setDetailUrl(String detailUrl) { this.detailUrl = detailUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTagsJson() { return tagsJson; }
    public void setTagsJson(String tagsJson) { this.tagsJson = tagsJson; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
    public String getCacheStatus() { return cacheStatus; }
    public void setCacheStatus(String cacheStatus) { this.cacheStatus = cacheStatus; }
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
    public LocalDateTime getCacheExpireAt() { return cacheExpireAt; }
    public void setCacheExpireAt(LocalDateTime cacheExpireAt) { this.cacheExpireAt = cacheExpireAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
