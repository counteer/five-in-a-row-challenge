package com.gomoku.game.task;

import static com.gomoku.game.GameStatus.FINISHED;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gomoku.game.Game;
import com.gomoku.game.repository.GameRepository;
import com.gomoku.history.History;
import com.gomoku.history.repository.HistoryRepository;
import com.gomoku.player.Player;
import com.gomoku.score.Score;
import com.gomoku.score.ScoreType;
import com.gomoku.score.repository.ScoreRepository;

/**
 * Scheduler service to start and schedule games between every players in the given time period.
 *
 * @author zeldan
 *
 */
@Service
public class GameTaskScheduler {

    private static final int ONE_MINUTE_IN_MILLISEC = 60000;

    private static final Logger LOG = getLogger(GameTaskScheduler.class);

    private final GameTask gameTask;

    private final HistoryRepository historyRepository;

    private final ScheduledExecutorService scheduler;

    private final GameRepository gameRepository;

    private final ScoreRepository scoreRepository;

    private final int lengthOfOneRoundInMinutes;

    private final int lengthOfTheGameInMinutes;

    public GameTaskScheduler(
            @Autowired final GameTask gameTask,
            @Autowired final HistoryRepository historyRepository,
            @Autowired final ScheduledExecutorService scheduler,
            @Autowired final GameRepository gameRepository,
            @Autowired final ScoreRepository scoreRepository,
            @Value("${game.lengthOfOneRoundInMinutes}") final int lengthOfOneRoundInMinutes,
            @Value("${game.lengthOfTheGameInMinutes}") final int lengthOfTheGameInMinutes) {
        this.gameTask = gameTask;
        this.historyRepository = historyRepository;
        this.scheduler = scheduler;
        this.gameRepository = gameRepository;
        this.scoreRepository = scoreRepository;
        this.lengthOfOneRoundInMinutes = lengthOfOneRoundInMinutes;
        this.lengthOfTheGameInMinutes = lengthOfTheGameInMinutes;
    }

    public void startAndScheduleGames(final String gameId, final List<Player> players) {

        final ScheduledFuture<?> countdown = scheduler.schedule(() -> LOG.info("Out of time!"), lengthOfTheGameInMinutes, MINUTES);
        int round = 1;
        while (!countdown.isDone()) {
            try {
                startRound(gameId, round++, players);
                sleep(lengthOfOneRoundInMinutes * ONE_MINUTE_IN_MILLISEC);
            } catch (final InterruptedException e) {
                LOG.warn("The game is interrupted.");
            }
        }
        final Game game = gameRepository.findOne(gameId);
        game.setGameStatus(FINISHED);
        gameRepository.save(game);
    }

    private void startRound(final String gameId, final int round, final List<Player> players) {
        LOG.info("The round '{}' is started.", round);
        final AtomicInteger gameNr = new AtomicInteger(1);
        players.forEach(playerOne -> {
            players.forEach(playerTwo -> {
                if (!playerOne.equals(playerTwo)) {
                    LOG.info("--- Player '{}' versus Player '{}'", playerOne.getUserName(), playerTwo.getUserName());
                    final GameTaskResult gameTaskResult = gameTask.matchAgainstEachOther(playerOne, playerTwo);
                    final Optional<Player> winner = gameTaskResult.getWinner();
                    final History history = new History(gameId, round, gameNr.getAndIncrement(), playerOne, playerTwo, winner, gameTaskResult.getSteps());
                    historyRepository.save(history);
                    if (winner.isPresent()) {
                        scoreRepository.save(new Score(gameId, round, gameNr.get(), winner.get().getUserName(), ScoreType.VICTORY.getScore()));
                        LOG.info("------ The winner is: " + winner.get().getUserName());
                    } else {
                        final int scoreOfDraw = ScoreType.DRAW.getScore();
                        scoreRepository.save(new Score(gameId, round, gameNr.get(), playerOne.getUserName(), scoreOfDraw));
                        scoreRepository.save(new Score(gameId, round, gameNr.get(), playerTwo.getUserName(), scoreOfDraw));
                        LOG.info("------ The game is draw.");
                    }
                    LOG.info("------ The id of history is: " + history.getId());
                }
            });
        });
    }

}
