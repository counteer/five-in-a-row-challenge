package com.gomoku.board;

import static com.gomoku.board.BoardFieldType.NONE;
import static com.gomoku.board.BoardFieldType.PLAYER_O;
import static com.gomoku.board.BoardFieldType.PLAYER_X;
import static java.util.Arrays.asList;
import static java.util.Arrays.fill;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

/**
 * The board of gomoku game.
 *
 * @author zeldan
 *
 */
public class Board {

    private static final int LIMIT_TO_WIN = 5;

    private final BoardFieldType[][] table;

    public Board(final int width, final int height) {
        this.table = new BoardFieldType[width][height];
        for (final BoardFieldType[] row : table) {
            fill(row, NONE);
        }
    }

    public void mark(final int x, final int y, final BoardFieldType boardFieldType) {
        table[x - 1][y - 1] = boardFieldType;
    }

    public boolean isFull() {
        for (final BoardFieldType[] row : table) {
            for (final BoardFieldType element : row) {
                if (NONE.equals(element)) {
                    return false;
                }
            }
        }
        return true;
    }

    public Optional<BoardFieldType> getWinner() {
        Optional<BoardFieldType> winner = empty();
        for (final BoardFieldType boardFieldType : asList(PLAYER_O, PLAYER_X)) {
            if (hasHorizontalWin(boardFieldType) || hasVerticalWin(boardFieldType)) {
                winner = of(boardFieldType);
                break;
            }
        }
        return winner;
    }

    private boolean hasHorizontalWin(final BoardFieldType winnerType) {
        int actualCount = 0;
        for (final BoardFieldType[] row : table) {
            for (final BoardFieldType column : row) {
                actualCount = increaseOrResetCount(column, winnerType, actualCount);
                if (actualCount == LIMIT_TO_WIN) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasVerticalWin(final BoardFieldType winnerType) {
        int actualCount = 0;
        for (int row = 0; row < table.length; row++) {
            for (int column = 0; column < table[row].length; column++) {
                actualCount = increaseOrResetCount(table[column][row], winnerType, actualCount);
                if (actualCount == LIMIT_TO_WIN) {
                    return true;
                }
            }
        }
        return false;
    }

    private int increaseOrResetCount(final BoardFieldType actualField, final BoardFieldType winnerType, final int count) {
        return actualField == winnerType ? count + 1 : 0;
    }
}
