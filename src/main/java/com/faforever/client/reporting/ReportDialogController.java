package com.faforever.client.reporting;

import ch.micheljung.fxwindow.FxStage;
import com.faforever.client.api.dto.ModerationReportStatus;
import com.faforever.client.fx.Controller;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.fx.StringCell;
import com.faforever.client.fx.WrappingStringCell;
import com.faforever.client.i18n.I18n;
import com.faforever.client.notification.Action;
import com.faforever.client.notification.ImmediateNotification;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.notification.Severity;
import com.faforever.client.player.Player;
import com.faforever.client.player.PlayerService;
import com.faforever.client.replay.Replay;
import com.faforever.client.theme.UiService;
import com.faforever.client.util.Assert;
import com.faforever.client.util.TimeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Slf4j
public class ReportDialogController implements Controller<Node> {

  private final ModerationService moderationService;
  private final NotificationService notificationService;
  private final PlayerService playerService;
  private final I18n i18n;
  private final UiService uiService;
  private final TimeService timeService;

  public VBox reportDialogRoot;
  public Label reportLabel;
  public TextField offender;
  public TextArea reportDescription;
  public TextField gameID;
  public TextField gameTime;
  public TableView<ModerationReport> reportTable;
  public TableColumn<ModerationReport, Number> idColumn;
  public TableColumn<ModerationReport, LocalDateTime> createTimeColumn;
  public TableColumn<ModerationReport, ObservableSet<Player>> offenderColumn;
  public TableColumn<ModerationReport, String> gameColumn;
  public TableColumn<ModerationReport, String> descriptionColumn;
  public TableColumn<ModerationReport, Player> moderatorColumn;
  public TableColumn<ModerationReport, String> noticeColumn;
  public TableColumn<ModerationReport, ModerationReportStatus> statusColumn;
  private Window ownerWindow;

  public void initialize() {
    reportTable.setPlaceholder(new Label(i18n.get("report.noReports")));

    idColumn.setCellValueFactory(param -> param.getValue().reportIdProperty());
    idColumn.setCellFactory(param -> new StringCell<>(Number::toString));
    createTimeColumn.setCellValueFactory(param -> param.getValue().createTimeProperty());
    createTimeColumn.setCellFactory(param -> new StringCell<>(timeService::asDateTime));
    offenderColumn.setCellValueFactory(param -> param.getValue().reportedUsersProperty());
    offenderColumn.setCellFactory(param -> new WrappingStringCell<>((players ->
        players.stream().map(Player::getUsername).collect(Collectors.joining(", ")))));
    gameColumn.setCellValueFactory(param -> param.getValue().gameIdProperty());
    gameColumn.setCellFactory(param -> new StringCell<>(String::toString));
    descriptionColumn.setCellValueFactory(param -> param.getValue().reportDescriptionProperty());
    descriptionColumn.setCellFactory(param -> new WrappingStringCell<>(String::toString));
    moderatorColumn.setCellValueFactory(param -> param.getValue().lastModeratorProperty());
    moderatorColumn.setCellFactory(param -> new StringCell<>(Player::getUsername));
    noticeColumn.setCellValueFactory(param -> param.getValue().moderatorNoticeProperty());
    noticeColumn.setCellFactory(param -> new WrappingStringCell<>(String::toString));
    statusColumn.setCellValueFactory(param -> param.getValue().reportStatusProperty());
    statusColumn.setCellFactory(param -> new StringCell<>(status -> i18n.get(status.getI18nKey())));

    updateReportTable();
  }

  public void onReportButtonClicked() {
    if (!gameID.getText().isBlank() && gameTime.getText().isBlank()) {
      notificationService.addNotification(new ImmediateNotification(i18n.get("report.warning.title"),
          i18n.get("report.warning.noGameTime"),
          Severity.WARN,
          List.of(new Action(i18n.get("yes"), ev -> submitReport()),
              new Action(i18n.get("no"), ev -> {
              }))));
    } else if (offender.getText().isBlank()) {
      notificationService.addNotification(new ImmediateNotification(i18n.get("report.warning.title"),
          i18n.get("report.warning.noOffender"),
          Severity.WARN,
          List.of(new Action(i18n.get("dismiss"), ev -> {
          }))));
    } else {
      submitReport();
    }
  }

  public void submitReport() {
    Player currentPlayer = playerService.getCurrentPlayer().orElseThrow(() -> new IllegalStateException("Player must be set"));

    ModerationReport report = new ModerationReport();
    report.setReporter(currentPlayer);
    if (!gameID.getText().isBlank()) {
      report.setGameId(gameID.getText());
    }
    report.setReportDescription(reportDescription.getText());
    report.setGameIncidentTimeCode(gameTime.getText());

    playerService.getPlayerByName(offender.getText()).thenAccept(player -> player.ifPresentOrElse(user -> {
          report.getReportedUsers().add(user);
          moderationService.postModerationReport(report);
          updateReportTable();
          clearReport();
        },
        () -> {
          log.info(String.format("No player named %s", offender.getText()));
          notificationService.addNotification(new ImmediateNotification(i18n.get("report.warning.title"),
              i18n.get("report.warning.noPlayer"),
              Severity.WARN,
              List.of(new Action(i18n.get("dismiss"), ev -> {
              }))));
        }))
        .exceptionally(throwable -> {
          notificationService.addImmediateErrorNotification(throwable, "report.error");
          return null;
        });
  }

  public void setOffender(Player player) {
    offender.setText(player.getUsername());
  }

  public void setOffender(String username) {
    offender.setText(username);
  }

  public void setGame(Replay game) {
    TextFields.bindAutoCompletion(offender, game.getTeams().entrySet().stream().flatMap(entry -> entry.getValue().stream())
        .collect(Collectors.toList()));
    gameID.setText(String.valueOf(game.getId()));
  }

  public void setAutoCompleteWithOnlinePlayers() {
    TextFields.bindAutoCompletion(offender, playerService.getPlayerNames());
  }

  private void clearReport() {
    offender.setText("");
    reportDescription.setText("");
    gameID.setText("");
    gameTime.setText("");
  }

  private void updateReportTable() {
    moderationService.getModerationReports().thenAccept(reports ->
        JavaFxUtil.runLater(() -> reportTable.setItems(FXCollections.observableList(reports.stream()
            .filter(report -> report.getCreateTime().isAfter(LocalDateTime.now().minusYears(1)))
            .sorted(Comparator.comparing(ModerationReport::getCreateTime).reversed())
            .collect(Collectors.toList())))));
  }

  public Pane getRoot() {
    return reportDialogRoot;
  }

  public void show() {
    Assert.checkNullIllegalState(ownerWindow, "ownerWindow must be set");

    FxStage fxStage = FxStage.create(reportDialogRoot)
        .initOwner(ownerWindow)
        .initModality(Modality.WINDOW_MODAL)
        .withSceneFactory(uiService::createScene)
        .allowMinimize(false)
        .apply();

    Stage stage = fxStage.getStage();
    stage.showingProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) {
        reportDialogRoot.getChildren().clear();
      }
    });
    stage.show();
  }

  public void setOwnerWindow(Window ownerWindow) {
    this.ownerWindow = ownerWindow;
  }

}
