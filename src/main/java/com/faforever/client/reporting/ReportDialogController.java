package com.faforever.client.reporting;

import ch.micheljung.fxwindow.FxStage;
import com.faforever.client.fx.Controller;
import com.faforever.client.i18n.I18n;
import com.faforever.client.player.Player;
import com.faforever.client.player.PlayerService;
import com.faforever.client.theme.UiService;
import com.faforever.client.util.Assert;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Slf4j
public class ReportDialogController implements Controller<Node> {

  private final ModerationService moderationService;
  private final PlayerService playerService;
  private final I18n i18n;
  private final UiService uiService;

  public VBox reportDialogRoot;
  public TextField reportedUser;
  public TextField reportDescription;
  public TextField gameID;
  public TextField gameTime;
  private Window ownerWindow;

  public void onReportButtonClicked() {
    Player currentPlayer = playerService.getCurrentPlayer().orElseThrow(() -> new IllegalStateException("Player must be set"));

    ModerationReport report = new ModerationReport();
    report.setReporter(currentPlayer);
    if (!gameID.getText().isEmpty()) {
      report.setGameId(gameID.getText());
    }
    report.setReportDescription(reportDescription.getText());
    report.setGameIncidentTimeCode(gameTime.getText());
    playerService.getPlayersByName(reportedUser.getText()).thenApply(players -> report.getReportedUsers().add(players.get(0)))
        .thenApply(aVoid -> moderationService.postModerationReport(report));


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
