package com.faforever.client.leaderboard;

import lombok.Value;

@Value
public class Division {
  int leagueSeasonId;
  int majorDivisionIndex;
  int subDivisionIndex;
  DivisionName majorDivisionName;
  DivisionName subDivisionName;
  int highestScore;

  @Override
  public String toString() {
    return majorDivisionName.getImageKey() + subDivisionName.getImageKey();
  }
}
