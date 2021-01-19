package com.faforever.client.leaderboard;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class LeagueSeason {
  private final IntegerProperty id;
  private final IntegerProperty leagueId;
  private final IntegerProperty leaderboardId;
  private final StringProperty technicalName; // not provided yet
  private final ObjectProperty<OffsetDateTime> startDate;
  private final ObjectProperty<OffsetDateTime> endDate;

  public LeagueSeason() {
    id = new SimpleIntegerProperty();
    leagueId = new SimpleIntegerProperty();
    leaderboardId = new SimpleIntegerProperty();
    technicalName = new SimpleStringProperty();
    startDate = new SimpleObjectProperty<>();
    endDate = new SimpleObjectProperty<>();
  }

  public static LeagueSeason fromDto(com.faforever.client.api.dto.LeagueSeason dto) {
    LeagueSeason leagueSeason = new LeagueSeason();
    leagueSeason.setId(Integer.parseInt(dto.getId()));
    leagueSeason.setLeagueId(dto.getLeagueId());
    leagueSeason.setLeaderboardId(dto.getLeaderboardId());
    leagueSeason.setTechnicalName(dto.getTechnicalName());
    leagueSeason.setStartDate(dto.getStartDate());
    leagueSeason.setEndDate(dto.getEndDate());
    return leagueSeason;
  }

  public Integer getId() {
    return id.get();
  }

  public void setId(int id) {
    this.id.set(id);
  }

  public IntegerProperty idProperty() {
    return id;
  }

  public Integer getLeagueId() {
    return leagueId.get();
  }

  public void setLeagueId(int leagueId) {
    this.leagueId.set(leagueId);
  }

  public IntegerProperty leagueIdProperty() {
    return leagueId;
  }

  public Integer getLeaderboardId() {
    return leaderboardId.get();
  }

  public void setLeaderboardId(int leaderboardId) {
    this.leaderboardId.set(leaderboardId);
  }

  public IntegerProperty leaderboardIdProperty() {
    return leaderboardId;
  }

  public String getTechnicalName() {
    return technicalName.get();
  }

  public void setTechnicalName(String technicalName) {
    this.technicalName.set(technicalName);
  }

  public StringProperty technicalNameProperty() {
    return technicalName;
  }

  public OffsetDateTime getStartDate() {
    return startDate.get();
  }

  public void setStartDate(OffsetDateTime startDate) {
    this.startDate.set(startDate);
  }

  public ObjectProperty<OffsetDateTime> startDateProperty() {
    return startDate;
  }

  public OffsetDateTime getEndDate() {
    return endDate.get();
  }

  public void setEndDate(OffsetDateTime endDate) {
    this.endDate.set(endDate);
  }

  public ObjectProperty<OffsetDateTime> endDateProperty() {
    return endDate;
  }
}
