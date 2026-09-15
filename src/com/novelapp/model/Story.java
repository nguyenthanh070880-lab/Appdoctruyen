package com.novelapp.model;

import java.time.LocalDateTime;
import java.util.List;

public class Story {
    private int storyId;
    private int authorId;
    private String authorName;          // lấy thêm từ join
    private String title;
    private String slug;
    private String description;
    private String coverUrl;
    private String status;              // ONGOING, COMPLETED, DROPPED
    private String moderationStatus;    // DRAFT, PENDING, APPROVED, REJECTED
    private boolean isPaid;
    private long viewCount;
    private int favoriteCount;
    private int followCount;
    private double ratingAvg;
    private int ratingCount;
    private boolean isDeleted;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<String> genres;        // danh sách thể loại

    // Constructors
    public Story() {}

    // Getters & Setters
    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(String moderationStatus) { this.moderationStatus = moderationStatus; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }

    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }

    public int getFollowCount() { return followCount; }
    public void setFollowCount(int followCount) { this.followCount = followCount; }

    public double getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(double ratingAvg) { this.ratingAvg = ratingAvg; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}