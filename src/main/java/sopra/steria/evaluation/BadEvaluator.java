package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BPiece;

public class BadEvaluator implements Evaluator {
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;

    // Piece-Square Tables (from white's perspective, index 0 = a1, index 63 = h8)
    private static final int[] PAWN_PST = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10,-20,-20, 10, 10,  5,
         5, -5,-10,  0,  0,-10, -5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5,  5, 10, 25, 25, 10,  5,  5,
        10, 10, 20, 30, 30, 20, 10, 10,
        50, 50, 50, 50, 50, 50, 50, 50,
         0,  0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] KNIGHT_PST = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };

    private static final int[] BISHOP_PST = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] ROOK_PST = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         0,  0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] QUEEN_PST = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] KING_PST = {
         20, 30, 10,  0,  0, 10, 30, 20,
         20, 20,  0,  0,  0,  0, 20, 20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30
    };

    @Override
    public int evaluate(BBoard board) {
        int whiteMaterial = countMaterial(board, BPiece.white);
        int blackMaterial = countMaterial(board, BPiece.black);

        int whitePst = countPST(board, BPiece.white);
        int blackPst = countPST(board, BPiece.black);

        int score = (whiteMaterial + whitePst) - (blackMaterial + blackPst);

        return board.isWhiteToMove() ? score : -score;
    }

    private int countMaterial(BBoard board, int color) {
        int material = 0;
        material += PAWN_VALUE * Long.bitCount(board.getBitboard(BPiece.pawn | color));
        material += KNIGHT_VALUE * Long.bitCount(board.getBitboard(BPiece.knight | color));
        material += BISHOP_VALUE * Long.bitCount(board.getBitboard(BPiece.bishop | color));
        material += ROOK_VALUE * Long.bitCount(board.getBitboard(BPiece.rook | color));
        material += QUEEN_VALUE * Long.bitCount(board.getBitboard(BPiece.queen | color));
        return material;
    }

    private int countPST(BBoard board, int color) {
        int score = 0;
        boolean isWhite = color == BPiece.white;
        score += evaluatePiecePST(board.getBitboard(BPiece.pawn | color), PAWN_PST, isWhite);
        score += evaluatePiecePST(board.getBitboard(BPiece.knight | color), KNIGHT_PST, isWhite);
        score += evaluatePiecePST(board.getBitboard(BPiece.bishop | color), BISHOP_PST, isWhite);
        score += evaluatePiecePST(board.getBitboard(BPiece.rook | color), ROOK_PST, isWhite);
        score += evaluatePiecePST(board.getBitboard(BPiece.queen | color), QUEEN_PST, isWhite);
        score += evaluatePiecePST(board.getBitboard(BPiece.king | color), KING_PST, isWhite);
        return score;
    }

    private int evaluatePiecePST(long bitboard, int[] table, boolean isWhite) {
        int score = 0;
        while (bitboard != 0) {
            int square = Long.numberOfTrailingZeros(bitboard);
            score += table[isWhite ? square : square ^ 56];
            bitboard &= bitboard - 1;
        }
        return score;
    }
}
