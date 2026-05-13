package com.faforever.client.game;

import com.faforever.client.domain.server.GameInfo;
import com.faforever.client.domain.server.PlayerInfo;
import com.faforever.client.fx.FxApplicationThreadExecutor;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.mapstruct.GameMapper;
import com.faforever.client.net.ConnectionState;
import com.faforever.client.player.PlayerService;
import com.faforever.client.remote.FafServerAccessor;
import com.faforever.commons.lobby.GameStatus;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Downloads necessary maps, mods and updates before starting
 */
@Lazy
@Service
@Slf4j
@RequiredArgsConstructor
public class GameService implements InitializingBean {

  private final FafServerAccessor fafServerAccessor;
  private final PlayerService playerService;
  private final GameMapper gameMapper;
  private final FxApplicationThreadExecutor fxApplicationThreadExecutor;

  private final ObservableMap<Integer, GameInfo> gameIdToGame = FXCollections.synchronizedObservableMap(
      FXCollections.observableHashMap());
  @Getter
  private final ObservableList<GameInfo> games = JavaFxUtil.attachListToMap(
      FXCollections.synchronizedObservableList(FXCollections.observableArrayList(game -> new Observable[]{
          game.statusProperty(), game.teamsProperty(), game.titleProperty(), game.mapFolderNameProperty(),
          game.simModsProperty(), game.passwordProtectedProperty()
      })), gameIdToGame);

  @Override
  public void afterPropertiesSet() {
    fafServerAccessor.getEvents(com.faforever.commons.lobby.GameInfo.class)
                     .flatMap(gameInfo -> gameInfo.getGames() == null ? Flux.just(gameInfo) : Flux.fromIterable(
                         gameInfo.getGames()))
                     .flatMap(gameInfo -> Mono.zip(Mono.just(gameInfo), Mono.justOrEmpty(getByUid(gameInfo.getUid()))
                                                                            .switchIfEmpty(
                                                                                initializeGameBean(gameInfo))))
                     .publishOn(fxApplicationThreadExecutor.asScheduler())
                     .map(TupleUtils.function(gameMapper::update))
                     .doOnError(throwable -> log.error("Error processing game", throwable))
                     .filter(game -> game.getStatus() == GameStatus.CLOSED)
                     .doOnNext(GameInfo::removeListeners)
                     .map(GameInfo::getId)
                     .publishOn(fxApplicationThreadExecutor.asScheduler())
                     .doOnNext(gameIdToGame::remove)
                     .doOnError(throwable -> log.error("Error closing game", throwable))
                     .retry()
                     .subscribe();

    fafServerAccessor.connectionStateProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == ConnectionState.DISCONNECTED) {
        fxApplicationThreadExecutor.execute(gameIdToGame::clear);
      }
    });
  }

  private Mono<GameInfo> initializeGameBean(com.faforever.commons.lobby.GameInfo gameInfo) {
    return Mono.fromCallable(() -> {
                 GameInfo newGame = new GameInfo();
                 newGame.setId(gameInfo.getUid());
                 newGame.addPlayerChangeListener(generatePlayerChangeListener(newGame));
                 return newGame;
               }).doOnNext(game -> gameMapper.update(gameInfo, game))
               .publishOn(fxApplicationThreadExecutor.asScheduler())
               .doOnNext(game -> gameIdToGame.put(game.getId(), game));
  }

  private ChangeListener<Set<Integer>> generatePlayerChangeListener(GameInfo newGame) {
    return (observable, oldValue, newValue) -> {
      List<PlayerInfo> playersWhoLeftGame = oldValue.stream()
                                                    .filter(player -> !newValue.contains(player))
                                                    .map(playerService::getPlayerByIdIfOnline)
                                                    .flatMap(Optional::stream)
                                                    .filter(player -> newGame.equals(player.getGame()))
                                                    .toList();
      List<PlayerInfo> playersWhoJoinedGame = newValue.stream()
                                                      .filter(player -> !oldValue.contains(player))
                                                      .map(playerService::getPlayerByIdIfOnline)
                                                      .flatMap(Optional::stream)
                                                      .toList();
      fxApplicationThreadExecutor.execute(() -> {
        playersWhoLeftGame.forEach(player -> player.setGame(null));
        playersWhoJoinedGame.forEach(player -> player.setGame(newGame));
      });
    };
  }

  public Optional<GameInfo> getByUid(Integer uid) {
    return Optional.ofNullable(gameIdToGame.get(uid));
  }

  public ObservableValue<GameInfo> observeByUid(Integer uid) {
    return Bindings.valueAt(gameIdToGame, uid);
  }
}
