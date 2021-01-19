package com.faforever.client.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Type("leagueLeaderboard")
public class LeagueEntry {
  @Id
  private String id;
  private Integer rank; // Not supported at the moment
  private Integer score; // (may be null)
  private Integer numGames;
  private Integer wonGames; // Not supported at the moment
  //server only stores subDivisionId instead of explicitly these two (may be null)
  private Integer majorDivisionIndex;
  private Integer subDivisionIndex;

  @Relationship("player")
  private Player player;

  @Relationship("league")
  private LeagueSeason leagueSeason;
}
