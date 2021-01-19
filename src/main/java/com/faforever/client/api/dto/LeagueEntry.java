package com.faforever.client.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Type("leagueLeaderboard")
public class LeagueEntry {
  @Id
  private String id;
  private int rank; // Will we even need/have this?
  private Integer score;
  private Integer numGames;
  private Integer wonGames;
  private Boolean isActive;
  private Integer majorDivisionIndex;
  private Integer subDivisionIndex;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;

  @Relationship("player")
  private Player player;

  @Relationship("league")
  private LeagueSeason leagueSeason;
}
