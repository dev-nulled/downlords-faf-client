package com.faforever.client.reporting;

import com.faforever.client.api.dto.ModerationReportStatus;
import com.faforever.client.player.Player;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.time.LocalDateTime;
import java.util.Optional;

public class ModerationReport {
  private final StringProperty reportDescription;
  private final ObjectProperty<ModerationReportStatus> reportStatus;
  private final StringProperty gameIncidentTimeCode;
  private final StringProperty moderatorNotice;
  private final ObjectProperty<Player> lastModerator;
  private final ObjectProperty<Player> reporter;
  private final SetProperty<Player> reportedUsers;
  private final ObjectProperty<LocalDateTime> createTime;
  private final ObjectProperty<LocalDateTime> updateTime;
  private final StringProperty gameId;

  public ModerationReport() {
    reportDescription = new SimpleStringProperty();
    gameIncidentTimeCode = new SimpleStringProperty();
    moderatorNotice = new SimpleStringProperty();
    reportStatus = new SimpleObjectProperty<>();
    lastModerator = new SimpleObjectProperty<>();
    reporter = new SimpleObjectProperty<>();
    reportedUsers = new SimpleSetProperty<>(FXCollections.observableSet());
    createTime = new SimpleObjectProperty<>();
    updateTime = new SimpleObjectProperty<>();
    gameId = new SimpleStringProperty();
  }

  public static ModerationReport fromReportDto(com.faforever.client.api.dto.ModerationReport dto) {
    ModerationReport report = new ModerationReport();

    report.setReportStatus(dto.getReportStatus());
    report.setReportDescription(dto.getReportDescription());
    report.setGameIncidentTimeCode(dto.getGameIncidentTimecode());
    report.setModeratorNotice(dto.getModeratorNotice());
    report.setLastModerator(Player.fromDto(dto.getLastModerator()));
    report.setReporter(Player.fromDto(dto.getReporter()));
    Optional.ofNullable(dto.getCreateTime())
        .ifPresent(offsetDateTime -> report.setCreateTime(offsetDateTime.toLocalDateTime()));
    Optional.ofNullable(dto.getUpdateTime())
        .ifPresent(offsetDateTime -> report.setUpdateTime(offsetDateTime.toLocalDateTime()));
    Optional.ofNullable(dto.getGame())
        .ifPresent(game -> report.setGameId(game.getId()));
    dto.getReportedUsers().stream().map(Player::fromDto).forEach(player -> report.getReportedUsers().add(player));
    return report;
  }

  public ModerationReportStatus getReportStatus() {
    return reportStatus.get();
  }

  public void setReportStatus(ModerationReportStatus reportStatus) {
    this.reportStatus.set(reportStatus);
  }

  public ObjectProperty<ModerationReportStatus> reportStatusProperty() {
    return reportStatus;
  }

  public String getReportDescription() {
    return reportDescription.get();
  }

  public void setReportDescription(String reportDescription) {
    this.reportDescription.set(reportDescription);
  }

  public StringProperty reportDescriptionProperty() {
    return reportDescription;
  }

  public String getGameIncidentTimeCode() {
    return gameIncidentTimeCode.get();
  }

  public void setGameIncidentTimeCode(String gameIncidentTimeCode) {
    this.gameIncidentTimeCode.set(gameIncidentTimeCode);
  }

  public StringProperty gameIncidentTimeCodeProperty() {
    return gameIncidentTimeCode;
  }

  public String getModeratorNotice() {
    return moderatorNotice.get();
  }

  public void setModeratorNotice(String moderatorNotice) {
    this.moderatorNotice.set(moderatorNotice);
  }

  public StringProperty moderatorNoticeProperty() {
    return moderatorNotice;
  }

  public Player getLastModerator() {
    return lastModerator.get();
  }

  public void setLastModerator(Player lastModerator) {
    this.lastModerator.set(lastModerator);
  }

  public ObjectProperty<Player> lastModeratorProperty() {
    return lastModerator;
  }

  public Player getReporter() {
    return reporter.get();
  }

  public void setReporter(Player reporter) {
    this.reporter.set(reporter);
  }

  public ObjectProperty<Player> reporterProperty() {
    return reporter;
  }

  public ObservableSet<Player> getReportedUsers() {
    return reportedUsers.get();
  }

  public void setReportedUsers(ObservableSet<Player> reportedUsers) {
    this.reportedUsers.set(reportedUsers);
  }

  public SetProperty<Player> reportedUsersProperty() {
    return reportedUsers;
  }

  public LocalDateTime getCreateTime() {
    return createTime.get();
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime.set(createTime);
  }

  public ObjectProperty<LocalDateTime> createTimeProperty() {
    return createTime;
  }

  public LocalDateTime getUpdateTime() {
    return updateTime.get();
  }

  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime.set(updateTime);
  }

  public ObjectProperty<LocalDateTime> updateTimeProperty() {
    return updateTime;
  }

  public String getGameId() {
    return gameId.get();
  }

  public void setGameId(String gameId) {
    this.gameId.set(gameId);
  }

  public StringProperty gameIdProperty() {
    return gameId;
  }
}
