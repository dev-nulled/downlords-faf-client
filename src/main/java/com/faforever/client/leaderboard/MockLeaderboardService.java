package com.faforever.client.leaderboard;

import com.faforever.client.FafClientApplication;
import com.faforever.client.i18n.I18n;
import com.faforever.client.task.CompletableTask;
import com.faforever.client.task.TaskService;
import com.faforever.client.util.Tuple;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.faforever.client.leaderboard.DivisionName.BRONZE;
import static com.faforever.client.leaderboard.DivisionName.COMMANDER;
import static com.faforever.client.leaderboard.DivisionName.DIAMOND;
import static com.faforever.client.leaderboard.DivisionName.GOLD;
import static com.faforever.client.leaderboard.DivisionName.I;
import static com.faforever.client.leaderboard.DivisionName.II;
import static com.faforever.client.leaderboard.DivisionName.III;
import static com.faforever.client.leaderboard.DivisionName.IV;
import static com.faforever.client.leaderboard.DivisionName.MASTER;
import static com.faforever.client.leaderboard.DivisionName.NONE;
import static com.faforever.client.leaderboard.DivisionName.SILVER;
import static com.faforever.client.leaderboard.DivisionName.V;
import static com.faforever.client.task.CompletableTask.Priority.HIGH;

@Lazy
@Service
@Profile(FafClientApplication.PROFILE_OFFLINE)
@RequiredArgsConstructor
public class MockLeaderboardService implements LeaderboardService {

  private final TaskService taskService;
  private final I18n i18n;

  public CompletableFuture<List<RatingStat>> getLeaderboardStats(String leaderboardTechnicalName) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<LeagueSeason> getLatestSeason(int leagueId) {
    LeagueSeason season = new LeagueSeason();
    season.setTechnicalName("1");
    return CompletableFuture.completedFuture(season);
  }

  @Override
  public CompletableFuture<List<Leaderboard>> getLeaderboards() {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<League>> getLeagues() {
    return CompletableFuture.completedFuture(List.of(
        League.fromDto(new com.faforever.client.api.dto.League("1", "ladder1v1", "mock", "mock", OffsetDateTime.now(), OffsetDateTime.now())),
        League.fromDto(new com.faforever.client.api.dto.League("2", "tmm2v2", "mock", "mock", OffsetDateTime.now(), OffsetDateTime.now())),
        League.fromDto(new com.faforever.client.api.dto.League("3", "team", "mock", "mock", OffsetDateTime.now(), OffsetDateTime.now()))
    ));
  }

  @Override
  public CompletableFuture<Integer> getAccumulatedRank(LeagueEntry entry) {
    AtomicInteger rank = new AtomicInteger();
    getDivisions(entry.getLeagueSeason().getId()).thenAccept(divisions -> {
      //discard lower divisions
      divisions.stream()
          .filter(division -> division.getMajorDivisionIndex() >= entry.getMajorDivisionIndex())
          .filter(division -> !(division.getMajorDivisionIndex() == entry.getMajorDivisionIndex() && division.getSubDivisionIndex() < entry.getSubDivisionIndex()))
          .forEach(division -> {
            if (division.getMajorDivisionIndex() == entry.getMajorDivisionIndex()
                && division.getSubDivisionIndex() == entry.getSubDivisionIndex()) {
              //add local rank of entry in own division
              rank.addAndGet(20);
            } else {
              getSizeOfDivision(division).thenApply(rank::addAndGet);
            }
          });
    });
    return CompletableFuture.completedFuture(rank.get());
  }

  @Override
  public CompletableFuture<List<Division>> getDivisions(int leagueSeasonId) {
    DivisionName[] subnames = {V, IV, III, II, I};
    DivisionName[] majornames = {BRONZE, SILVER, GOLD, DIAMOND, MASTER};
    List<Division> divisions = new LinkedList<>();
    for (int k=1; k<6; k++) {
      for (int i=1; i<6; i++) {
        Division div = new Division(1, k, i, majornames[k-1], subnames[i-1], 10);
        //if (k!=5 || i!=5)
        divisions.add(div);
      }
    }
    Division div2 = new Division(1, 6, 1, COMMANDER, NONE, 10);
    divisions.add(div2);
    return CompletableFuture.completedFuture(divisions);
  }

  @Override
  public CompletableFuture<Integer> getTotalPlayers(int leagueSeasonId) {
    AtomicInteger rank = new AtomicInteger();
    getDivisions(leagueSeasonId).thenAccept(divisions -> {
      divisions.forEach(division -> getSizeOfDivision(division).thenApply(rank::addAndGet));
    });
    return CompletableFuture.completedFuture(rank.get());
  }

  @Override
  public CompletableFuture<Integer> getSizeOfDivision(Division division) {
    //return getEntries(division).thenApply(List::size);
    return CompletableFuture.completedFuture((int) Math.abs(Math.random() * 100 + 200 - Math.pow(division.getMajorDivisionIndex() - 3.3, 2) * 30));
  }

  @Override
  public CompletableFuture<List<LeaderboardEntry>> getEntriesForPlayer(int playerId) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<LeaderboardEntry>> getEntries(Leaderboard leaderboard) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<Tuple<List<LeaderboardEntry>, Integer>> getPagedEntries(Leaderboard leaderboard, int count, int page) {
    return CompletableFuture.completedFuture(new Tuple<>(Collections.emptyList(), 1));
  }

  @Override
  public CompletableFuture<LeagueEntry> getLeagueEntryForPlayer(int playerId, int leagueSeasonId) {
    LeagueEntry entry = new LeagueEntry();
    LeagueSeason leagueSeason = LeagueSeason.fromDto(new com.faforever.client.api.dto.LeagueSeason("1", 1, 1, "mock", OffsetDateTime.now(), OffsetDateTime.now()));
    entry.setSubDivisionIndex(4);
    entry.setMajorDivisionIndex(2);
    entry.setScore(8);
    entry.setGamesPlayed(3);
    entry.setLeagueSeason(leagueSeason);

    Throwable noEntry = new Throwable();
    boolean testNoEntry = false;
    if (testNoEntry)
      return CompletableFuture.failedFuture(noEntry);
    else
      return CompletableFuture.completedFuture(entry);
  }

  @Override
  public CompletableFuture<List<LeagueEntry>> getEntries(Division division) {
    return taskService.submitTask(new CompletableTask<List<LeagueEntry>>(HIGH) {
      @Override
      protected List<LeagueEntry> call() throws Exception {
        updateTitle("Reading ladder");

        List<LeagueEntry> list = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
          String name = RandomStringUtils.random(10, true, false);
          int score = (int) (Math.random() * 25);
          int gamecount = (int) (Math.random() * 1000);
          float winloss = (float) (Math.random() * 100);

          list.add(createLeagueEntryBean(name, score, gamecount, winloss));

        }
        return list;
      }
    }).getFuture();
  }

  private LeagueEntry createLeagueEntryBean(String name, int score, int gamesPlayed, float winLossRatio) {
    LeagueEntry leagueEntry = new LeagueEntry();
    leagueEntry.setUsername(name);
    leagueEntry.setScore(score);
    leagueEntry.setGamesPlayed(gamesPlayed);
    leagueEntry.setWinLossRatio(winLossRatio);

    return leagueEntry;
  }
}
