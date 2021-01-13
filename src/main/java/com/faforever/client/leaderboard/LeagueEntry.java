package com.faforever.client.leaderboard;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LeagueEntry {

  private final StringProperty username;
  private final IntegerProperty gamesPlayed;
  private final FloatProperty winLossRatio;
  private final ObjectProperty<League> league;
  private final IntegerProperty score;
  private final IntegerProperty majorDivisionIndex;
  private final IntegerProperty subDivisionIndex;

  public LeagueEntry() {
    username = new SimpleStringProperty();
    gamesPlayed = new SimpleIntegerProperty();
    winLossRatio = new SimpleFloatProperty();
    league = new SimpleObjectProperty<>();
    score = new SimpleIntegerProperty();
    majorDivisionIndex = new SimpleIntegerProperty();
    subDivisionIndex = new SimpleIntegerProperty();
  }

  public static LeagueEntry fromDto(com.faforever.client.api.dto.LeagueEntry entry) {
    LeagueEntry leagueEntry = new LeagueEntry();
    leagueEntry.setUsername(entry.getPlayer().getLogin());
    leagueEntry.setGamesPlayed(entry.getNumGames());
    leagueEntry.setWinLossRatio(entry.getWonGames() / (float) entry.getNumGames());
    leagueEntry.setLeague(League.fromDto(entry.getLeague()));
    leagueEntry.setScore(entry.getScore());
    leagueEntry.setMajorDivisionIndex(entry.getMajorDivisionIndex());
    leagueEntry.setSubDivisionIndex(entry.getSubDivisionIndex());
    return leagueEntry;
  }


  public String getUsername() {
    return username.get();
  }

  public void setUsername(String username) {
    this.username.set(username);
  }

  public StringProperty usernameProperty() {
    return username;
  }

  public League getLeague() {
    return league.get();
  }

  public void setLeague(League league) {
    this.league.set(league);
  }

  public ObjectProperty<League> leagueProperty() {
    return league;
  }

  public int getGamesPlayed() {
    return gamesPlayed.get();
  }

  public void setGamesPlayed(int gamesPlayed) {
    this.gamesPlayed.set(gamesPlayed);
  }

  public IntegerProperty gamesPlayedProperty() {
    return gamesPlayed;
  }

  public float getWinLossRatio() {
    return winLossRatio.get();
  }

  public void setWinLossRatio(float winLossRatio) {
    this.winLossRatio.set(winLossRatio);
  }

  public FloatProperty winLossRatioProperty() {
    return winLossRatio;
  }

  public int getScore() {
    return score.get();
  }

  public void setScore(int score) {
    this.score.set(score);
  }

  public IntegerProperty scoreProperty() {
    return score;
  }

  public int getMajorDivisionIndex() {
    return majorDivisionIndex.get();
  }

  public void setMajorDivisionIndex(int majorDivisionIndex) {
    this.majorDivisionIndex.set(majorDivisionIndex);
  }

  public IntegerProperty majorDivisionIndexProperty() {
    return majorDivisionIndex;
  }

  public int getSubDivisionIndex() {
    return subDivisionIndex.get();
  }

  public void setSubDivisionIndex(int subDivisionIndex) {
    this.subDivisionIndex.set(subDivisionIndex);
  }

  public IntegerProperty subDivisionIndexProperty() {
    return subDivisionIndex;
  }

  @Override
  public int hashCode() {
    return username.get() != null ? username.get().hashCode() : 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LeagueEntry that = (LeagueEntry) o;

    return !(username.get() != null ? !username.get().equalsIgnoreCase(that.username.get()) : that.username.get() != null);

  }

  @Override
  public String toString() {
    return "LeagueEntry{" +
        "username=" + username.get() +
        ",leaderboard=" + league.get().getTechnicalName() +
        '}';
  }
}
