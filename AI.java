/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;


import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.*;

/** A Player that computes its own moves.
 *  @author Suliman Alharbi
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 3;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Board b = getBoard();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        PieceColor currentPlayer = myColor();
        if (sense == 1) {
            currentPlayer = RED;
        } else if (sense == -1) {
            currentPlayer = BLUE;
        }
        Move best;
        best = null;
        int bestScore;
        if (currentPlayer == RED) {
            bestScore = -INFTY;
            ArrayList<Move> listOfMoves = getListOfMoves(board, RED);
            for (int i = 0; i < listOfMoves.size(); i += 1) {
                Board copyBoard = new Board(board);
                Move currentMove = listOfMoves.get(i);
                copyBoard.makeMove(currentMove);
                int currentScore = minMax(copyBoard,
                        depth - 1, false, -1, alpha, beta);
                alpha = max(currentScore, alpha);
                if (bestScore <= currentScore || best == null) {
                    bestScore = currentScore;
                    best = currentMove;
                }
                if (beta <= alpha) {
                    break;
                }
            }

        } else {
            bestScore = INFTY;
            ArrayList<Move> listOfMoves = getListOfMoves(board, BLUE);
            for (int i = 0; i < listOfMoves.size(); i += 1) {
                Board copyBoard = new Board(board);
                Move currentMove = listOfMoves.get(i);
                copyBoard.makeMove(currentMove);
                int currentScore = minMax(copyBoard,
                        depth - 1, false,
                        1, alpha, beta);
                beta = min(currentScore, beta);
                if (bestScore >= currentScore || best == null) {
                    bestScore = currentScore;
                    best = currentMove;
                }
                if (beta <= alpha) {
                    break;
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestScore;
    }

    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }

        return board.redPieces() - board.bluePieces();
    }

    private ArrayList<Move> getListOfMoves(Board board,
                                           PieceColor currentPlayer) {
        ArrayList<Move> listOfMoves = new ArrayList<Move>();
        for (char rows : ROWS) {
            for (char cols : COLS) {
                if (board.get(cols, rows) == currentPlayer) {
                    outerloop:
                    for (int r = -2; r <= 2; r += 1) {
                        for (int c = -2; c <= 2; c += 1) {
                            char currentRow = (char) (rows + r);
                            char currentCol = (char) (cols + c);
                            if (board.get(currentCol, currentRow) == EMPTY) {
                                Board copyBoard = new Board(board);
                                Move currentMove = Move.move(cols,
                                        rows,
                                        currentCol, currentRow);
                                listOfMoves.add(currentMove);

                            }
                        }
                    }
                }
            }
        }
        return listOfMoves;
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();
    /** list of Cols. */
    private static final char[] COLS = {'a', 'b', 'c', 'd', 'e', 'f', 'g'};
    /** list of Rows. */
    private static final char[] ROWS = {'1', '2', '3', '4', '5', '6', '7'};
}
