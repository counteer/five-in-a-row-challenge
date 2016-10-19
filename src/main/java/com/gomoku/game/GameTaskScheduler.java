package com.gomoku.game;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gomoku.board.BoardFieldType;
import com.gomoku.player.Player;
import com.gomoku.repository.PlayerRepository;

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

    private final PlayerRepository playerRepository;

    private final ScheduledExecutorService scheduler;

    private final int lengthOfOneRoundInMinutes;

    private final int lengthOfTheGameInMinutes;

    public GameTaskScheduler(
            @Autowired final GameTask gameTask,
            @Autowired final PlayerRepository playerRepository,
            @Autowired final ScheduledExecutorService scheduler,
            @Value("${game.lengthOfOneRoundInMinutes}") final int lengthOfOneRoundInMinutes,
            @Value("${game.lengthOfTheGameInMinutes}") final int lengthOfTheGameInMinutes) {
        this.gameTask = gameTask;
        this.playerRepository = playerRepository;
        this.scheduler = scheduler;
        this.lengthOfOneRoundInMinutes = lengthOfOneRoundInMinutes;
        this.lengthOfTheGameInMinutes = lengthOfTheGameInMinutes;
    }

    public void startAndScheduleGames() {
        final List<Player> players = playerRepository.findAll();
        final ScheduledFuture<?> countdown = scheduler.schedule(() -> LOG.info("Out of time!"), lengthOfTheGameInMinutes, MINUTES);
        int round = 1;
        while (!countdown.isDone()) {
            try {
                startRound(round++, players);
                sleep(lengthOfOneRoundInMinutes * ONE_MINUTE_IN_MILLISEC);
            } catch (final InterruptedException e) {
                LOG.warn("The game is interrupted.");
            }
        }
        scheduler.shutdown();
    }

    private void startRound(final int round, final List<Player> players) {
        LOG.info("The round '{}' is started.", round);
        players.forEach(playerOne -> {
            players.forEach(playerTwo -> {
                if (!playerOne.equals(playerTwo)) {
                    LOG.info("--- Player '{}' versus Player '{}'", playerOne.getUserName(), playerTwo.getUserName());
                    final GameTaskResult gameTaskResult = gameTask.matchAgainstEachOther(playerOne, playerTwo);
                    final Optional<BoardFieldType> winner = gameTaskResult.getWinner();
                    if (winner.isPresent()) {
                        LOG.info("------ The winner is: " + winner.get());
                    } else {
                        LOG.info("------ The game is draw.");
                    }
                }
            });
        });
    }

}
