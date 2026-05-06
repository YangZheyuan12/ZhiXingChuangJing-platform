package com.zhixingchuangjing.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_packs")
public class MaterialPackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "pack_name", length = 128, nullable = false)
    private String packName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "asset_count")
    private Integer assetCount;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getPackName() { return packName; }
    public void setPackName(String packName) { this.packName = packName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getAssetCount() { return assetCount; }
    public void setAssetCount(Integer assetCount) { this.assetCount = assetCount; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
