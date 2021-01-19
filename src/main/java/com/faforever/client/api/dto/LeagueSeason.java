package com.faforever.client.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Type("leagueSeason")
@AllArgsConstructor
@NoArgsConstructor
public class LeagueSeason {
  @Id
  private String id;
  private Integer leagueId;
  private Integer leaderboardId;
  private String technicalName; // not provided yet
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
}
