package com.naturewalls.app.data.local

import com.naturewalls.app.data.model.Wallpaper

/** Entity ⇄ domain mapping. Categories are stored pipe-separated. */

fun WallpaperEntity.toWallpaper(): Wallpaper = Wallpaper(
    id = id,
    title = title,
    thumbUrl = thumbUrl,
    previewUrl = previewUrl,
    fullUrl = fullUrl,
    width = width,
    height = height,
    creator = creator,
    credit = credit,
    licenseName = licenseName,
    licenseUrl = licenseUrl,
    description = description,
    modificationNote = modificationNote,
    sourcePageUrl = sourcePageUrl,
    locationLabel = locationLabel,
    categories = categories.split("|").filter { it.isNotBlank() },
    uploadDate = uploadDate,
    groupId = groupId,
    subCategoryId = subCategoryId
)

fun Wallpaper.toEntity(cachedAt: Long = System.currentTimeMillis()): WallpaperEntity = WallpaperEntity(
    id = id,
    title = title,
    thumbUrl = thumbUrl,
    previewUrl = previewUrl,
    fullUrl = fullUrl,
    width = width,
    height = height,
    creator = creator,
    credit = credit,
    licenseName = licenseName,
    licenseUrl = licenseUrl,
    description = description,
    modificationNote = modificationNote,
    sourcePageUrl = sourcePageUrl,
    locationLabel = locationLabel,
    categories = categories.joinToString("|"),
    uploadDate = uploadDate,
    groupId = groupId,
    subCategoryId = subCategoryId,
    cachedAt = cachedAt
)
