package com.gomoku.player;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;

import com.gomoku.board.Board;
import com.gomoku.board.BoardFieldType;

/**
 * URI Builder to build player URI.
 *
 * @author zeldan
 *
 */
public final class PlayerUriBuilder {

    private PlayerUriBuilder() {

    }

    public static URI buildUri(final String baseUrl, final Board board, final BoardFieldType boardFieldType) {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("width", board.getWidth())
                .queryParam("height", board.getHeight())
                .queryParam("table", board.toString())
                .queryParam("player", boardFieldType);
        return builder.build().encode().toUri();
    }
}
