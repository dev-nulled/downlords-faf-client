package com.faforever.client.game;

import com.faforever.commons.lobby.GameVisibility;

import java.util.Set;

public record NewGameInfo(
    String title,
    String password,
    String featuredModName,
    String map,
    Set<String> simMods,
    GameVisibility gameVisibility,
    Integer ratingMin,
    Integer ratingMax,
    Boolean enforceRatingRange
) {
  public NewGameInfo(String title, String password, String featuredModName, String map, Set<String> simMods) {
    this(title, password, featuredModName, map, simMods, GameVisibility.PUBLIC, null, null, false);
  }

  public NewGameInfo {
    if (ratingMin != null && ratingMax != null && ratingMin > ratingMax) {
      throw new IllegalArgumentException(
          "Min cannot be greater than min: min %d max %d".formatted(ratingMin, ratingMax));
    }
  }
}
