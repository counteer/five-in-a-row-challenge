package com.gomoku.game;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomoku.board.Board;
import com.gomoku.board.BoardFieldType;
import com.gomoku.player.Player;

/**
 * Gomoku game service, it is responsible for execute one match on the board by the actual state of the game and actual player.
 */
@Service
public class GameService {

    private static final Logger LOG = getLogger(GameService.class);

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    @Autowired
    public GameService(final ObjectMapper objectMapper, final RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public Optional<GameState> playOneRound(final GameState gameState, final BoardFieldType actualPlayer) {
        final Player player = gameState.getPlayers().get(actualPlayer);
        final Board board = gameState.getBoard();
        final URI playerURI = gameState.getPlayerUriBuilder().buildUri(player.getNetworkAddress(), board.toString(), actualPlayer);
        final HttpEntity<String> userResponse = executeURI(playerURI);
        final Map<String, Integer> readValue = getValueFromUserResponse(userResponse.getBody());
        if (!readValue.isEmpty()) {
            final Board changedBoard = board.mark(readValue.get("x"), readValue.get("y"), actualPlayer);
            return of(new GameState(gameState.getPlayers(), changedBoard));
        }
        return empty();
    }

    private ResponseEntity<String> executeURI(final URI playerURI) {
        return restTemplate.exchange(
                playerURI,
                GET,
                new HttpEntity<>(getHttpEntity()),
                String.class);
    }

    private Map<String, Integer> getValueFromUserResponse(final String response) {
        try {
            return objectMapper.readValue(response, new TypeReference<HashMap<String, Integer>>() {
            });
        } catch (final IOException e) {
            LOG.error("An error occured while try to parse user response.", e);
        }
        return Collections.<String, Integer> emptyMap();
    }

    private HttpHeaders getHttpEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", APPLICATION_JSON_VALUE);
        return headers;
    }

}
