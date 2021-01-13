package com.faforever.client.leaderboard;

import com.faforever.client.FafClientApplication;
import com.faforever.client.api.dto.DivisionName;
import com.faforever.client.leaderboard.LeaderboardController.League;
import com.faforever.client.remote.FafService;
import com.faforever.client.util.RatingUtil;
import com.faforever.client.util.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.faforever.client.api.dto.DivisionName.BRONZE;
import static com.faforever.client.api.dto.DivisionName.COMMANDER;
import static com.faforever.client.api.dto.DivisionName.DIAMOND;
import static com.faforever.client.api.dto.DivisionName.GOLD;
import static com.faforever.client.api.dto.DivisionName.I;
import static com.faforever.client.api.dto.DivisionName.II;
import static com.faforever.client.api.dto.DivisionName.III;
import static com.faforever.client.api.dto.DivisionName.IV;
import static com.faforever.client.api.dto.DivisionName.MASTER;
import static com.faforever.client.api.dto.DivisionName.NONE;
import static com.faforever.client.api.dto.DivisionName.SILVER;
import static com.faforever.client.api.dto.DivisionName.V;


@Lazy
@Service
@Profile("!" + FafClientApplication.PROFILE_OFFLINE)
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {
  private final FafService fafService;

  @Override
  public CompletableFuture<List<Leaderboard>> getLeaderboards() {
    return fafService.getLeaderboards();
  }

  public CompletableFuture<List<LeaderboardEntry>> getEntries(Leaderboard leaderboard) {
    return fafService.getAllLeaderboardEntries(leaderboard.getTechnicalName());
  }

  public CompletableFuture<Tuple<List<LeaderboardEntry>, Integer>> getPagedEntries(Leaderboard leaderboard, int count, int page) {
    return fafService.getLeaderboardEntriesWithPageCount(leaderboard.getTechnicalName(), count, page);
  }

  public CompletableFuture<List<RatingStat>> getLeaderboardStats(String leaderboardTechnicalName) {
    return fafService.getAllLeaderboardEntries(leaderboardTechnicalName).thenApply(this::toRatingStats);
  }

  @Override
  public CompletableFuture<List<DivisionStat>> getDivisionStats() {
//    return getDivisions().thenAccept(divisions -> {
//      divisions.stream().map(division ->
//          fafService.getDivisionLeaderboard(division).thenApply(this::toDivisionStats))
//          .collect(Collectors.toList());
//    });
    return null;
  }

  private List<RatingStat> toRatingStats(List<LeaderboardEntry> entries) {
    Map<Integer, Long> totalCount = countByRating(entries.stream());
    Map<Integer, Long> countWithoutFewGames = countByRating(entries.stream()
        .filter(entry -> entry.gamesPlayedProperty().get() >= MINIMUM_GAMES_PLAYED_TO_BE_SHOWN));

    return totalCount.entrySet().stream()
        .map(entry -> new RatingStat(
            entry.getKey(),
            entry.getValue().intValue(),
            countWithoutFewGames.getOrDefault(entry.getKey(), 0L).intValue()))
        .collect(Collectors.toList());
  }

  private DivisionStat toDivisionStats(List<LeaderboardEntry> entries) {
    return new DivisionStat(entries.size());
  }

  private Map<Integer, Long> countByRating(Stream<LeaderboardEntry> entries) {
    return entries.collect(Collectors.groupingBy(leaderboardEntry ->
        RatingUtil.roundRatingToNextLowest100(leaderboardEntry.getRating()), Collectors.counting()));
  }

  @Override
  public CompletableFuture<List<LeaderboardEntry>> getEntriesForPlayer(int playerId) {
    return fafService.getLeaderboardEntriesForPlayer(playerId);
  }

  @Override
  public CompletableFuture<LeaderboardEntry> getLeagueEntryForPlayer(int playerId, League leagueType) {
    LeaderboardEntry entry = new LeaderboardEntry();
    entry.setSubDivisionIndex(4);
    entry.setMajorDivisionIndex(2);
    entry.setScore(8);
    entry.setGamesPlayed(3);

    Throwable noEntry = new Throwable();
    boolean testNoEntry = false;
    if (testNoEntry)
      return CompletableFuture.failedFuture(noEntry);
    else
      return CompletableFuture.completedFuture(entry);
    //return fafService.getLeagueEntryForPlayer(playerId, leagueType.toString());
  }

  @Override
  public CompletableFuture<List<LeaderboardEntry>> getEntries(Division division) {
    return fafService.getDivisionLeaderboard(division);
  }

  @Override
  public CompletableFuture<List<Division>> getDivisions(League leagueType) {
    DivisionName[] subnames = {V, IV, III, II, I};
    DivisionName[] majornames = {BRONZE, SILVER, GOLD, DIAMOND, MASTER};
    List<Division> divisions = new LinkedList<Division>();
    for (int k=1; k<6; k++) {
      for (int i=1; i<6; i++) {
        Division div = new Division(1, k, i, majornames[k-1], subnames[i-1], 10, leagueType);
        //if (k!=5 || i!=5)
        divisions.add(div);
      }
    }
    Division div2 = new Division(1, 6, 1, COMMANDER, NONE, 10, leagueType);
    divisions.add(div2);
    return CompletableFuture.completedFuture(divisions);
    //return fafService.getDivisions(leagueType.toString());
  }

  @Override
  public CompletableFuture<List<LeaderboardEntry>> getDivisionEntries(Division division) {
    return fafService.getDivisionLeaderboard(division);
  }
}
