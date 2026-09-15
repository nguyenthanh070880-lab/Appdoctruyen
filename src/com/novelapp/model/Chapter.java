package com.novelapp.model;

import java.time.LocalDateTime;

public class Chapter {
    private int chapterId;
    private int storyId;
    private double chapterNumber;
    private String title;
    private String content;
    private boolean isFree;
    private int priceCoin;
    private int wordCount;
    private long viewCount;
    private String moderationStatus;   // DRAFT, PENDING, APPROVED, REJECTED
    private boolean isDeleted;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters & Setters
    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public double getChapterNumber() { return chapterNumber; }
    public void setChapterNumber(double chapterNumber) { this.chapterNumber = chapterNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }

    public int getPriceCoin() { return priceCoin; }
    public void setPriceCoin(int priceCoin) { this.priceCoin = priceCoin; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }

    public String getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(String moderationStatus) { this.moderationStatus = moderationStatus; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}