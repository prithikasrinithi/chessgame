// ChessGame.java
public class ChessGame {
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}

// Game.java
import java.util.Scanner;

public class Game {
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Player currentPlayer;
    private Scanner scanner;

    public Game() {
        this.board = new Board();
        this.whitePlayer = new Player("White", true);
        this.blackPlayer = new Player("Black", false);
        this.currentPlayer = whitePlayer; // White moves first
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            board.displayBoard();
            System.out.println(currentPlayer.getName() + "'s turn");
            
            Move move = getPlayerMove();
            if (move == null) continue;
            
            if (isValidMove(move)) {
                makeMove(move);
                if (isCheckmate()) {
                    board.displayBoard();
                    System.out.println(currentPlayer.getName() + " wins!");
                    break;
                }
                switchPlayer();
            } else {
                System.out.println("Invalid move. Try again.");
            }
        }
        scanner.close();
    }

    private Move getPlayerMove() {
        System.out.print("Enter move (e.g., e2 e4): ");
        String input = scanner.nextLine();
        String[] parts = input.split(" ");
        if (parts.length != 2) {
            System.out.println("Invalid input format. Use 'sourceSquare targetSquare' (e.g., e2 e4)");
            return null;
        }
        return new Move(parts[0], parts[1]);
    }

    private boolean isValidMove(Move move) {
        Piece piece = board.getPiece(move.getFrom());
        
        // Basic validations
        if (piece == null) return false;
        if (piece.isWhite() != currentPlayer.isWhite()) return false;
        if (!piece.isValidMove(move, board)) return false;
        
        // Check if move puts or leaves own king in check
        Board tempBoard = board.clone();
        tempBoard.makeMove(move);
        if (tempBoard.isKingInCheck(currentPlayer.isWhite())) return false;
        
        return true;
    }

    private void makeMove(Move move) {
        board.makeMove(move);
    }

    private boolean isCheckmate() {
        boolean isWhite = !currentPlayer.isWhite(); // Check opposite player's king
        if (!board.isKingInCheck(isWhite)) return false;
        
        // Try all possible moves to see if check can be escaped
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board.board[i][j];
                if (piece != null && piece.isWhite() == isWhite) {
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            Move move = new Move(i, j, x, y);
                            if (isValidMove(move)) {
                                Board tempBoard = board.clone();
                                tempBoard.makeMove(move);
                                if (!tempBoard.isKingInCheck(isWhite)) return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
    }
}

// Board.java
public class Board {
    public Piece[][] board;

    public Board() {
        board = new Piece[8][8];
        initializeBoard();
    }

    private void initializeBoard() {
        // Place pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn(true);  // White pawns
            board[6][i] = new Pawn(false); // Black pawns
        }

        // Place other pieces
        placePiece(0, 0, new Rook(true));
        placePiece(0, 7, new Rook(true));
        placePiece(7, 0, new Rook(false));
        placePiece(7, 7, new Rook(false));

        placePiece(0, 1, new Knight(true));
        placePiece(0, 6, new Knight(true));
        placePiece(7, 1, new Knight(false));
        placePiece(7, 6, new Knight(false));

        placePiece(0, 2, new Bishop(true));
        placePiece(0, 5, new Bishop(true));
        placePiece(7, 2, new Bishop(false));
        placePiece(7, 5, new Bishop(false));

        placePiece(0, 3, new Queen(true));
        placePiece(7, 3, new Queen(false));

        placePiece(0, 4, new King(true));
        placePiece(7, 4, new King(false));
    }

    private void placePiece(int row, int col, Piece piece) {
        board[row][col] = piece;
    }

    public void displayBoard() {
        System.out.println("  a b c d e f g h");
        for (int i = 7; i >= 0; i--) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece == null) {
                    System.out.print(". ");
                } else {
                    System.out.print(piece.getSymbol() + " ");
                }
            }
            System.out.println(i + 1);
        }
        System.out.println("  a b c d e f g h");
    }

    public Piece getPiece(String position) {
        int[] coords = convertPosition(position);
        return board[coords[0]][coords[1]];
    }

    public void makeMove(Move move) {
        int[] from = convertPosition(move.getFrom());
        int[] to = convertPosition(move.getTo());
        board[to[0]][to[1]] = board[from[0]][from[1]];
        board[from[0]][from[1]] = null;
    }

    public boolean isKingInCheck(boolean isWhite) {
        // Find king's position
        int kingRow = -1, kingCol = -1;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece instanceof King && piece.isWhite() == isWhite) {
                    kingRow = i;
                    kingCol = j;
                    break;
                }
            }
            if (kingRow != -1) break;
        }

        // Check if any opponent's piece can capture the king
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.isWhite() != isWhite) {
                    Move move = new Move(i, j, kingRow, kingCol);
                    if (piece.isValidMove(move, this)) return true;
                }
            }
        }
        return false;
    }

    public static int[] convertPosition(String position) {
        int col = position.charAt(0) - 'a';
        int row = Character.getNumericValue(position.charAt(1)) - 1;
        return new int[]{row, col};
    }

    @Override
    public Board clone() {
        Board newBoard = new Board();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    newBoard.board[i][j] = board[i][j].clone();
                }
            }
        }
        return newBoard;
    }
}

// Move.java
public class Move {
    private String from;
    private String to;

    public Move(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        char fromColChar = (char) ('a' + fromCol);
        char toColChar = (char) ('a' + toCol);
        this.from = fromColChar + String.valueOf(fromRow + 1);
        this.to = toColChar + String.valueOf(toRow + 1);
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
}

// Player.java
public class Player {
    private String name;
    private boolean isWhite;

    public Player(String name, boolean isWhite) {
        this.name = name;
        this.isWhite = isWhite;
    }

    public String getName() { return name; }
    public boolean isWhite() { return isWhite; }
}

// Abstract class for chess pieces
abstract class Piece {
    protected boolean isWhite;
    protected char symbol;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public boolean isWhite() { return isWhite; }
    public char getSymbol() { return symbol; }

    public abstract boolean isValidMove(Move move, Board board);
    public abstract Piece clone();

    protected boolean isPathClear(Move move, Board board) {
        int[] from = Board.convertPosition(move.getFrom());
        int[] to = Board.convertPosition(move.getTo());
        int rowStep = Integer.compare(to[0], from[0]);
        int colStep = Integer.compare(to[1], from[1]);
        
        int currentRow = from[0] + rowStep;
        int currentCol = from[1] + colStep;
        
        while (currentRow != to[0] || currentCol != to[1]) {
            if (board.board[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return true;
    }
}

// Concrete piece classes
class King extends Piece {
    public King(boolean isWhite) {
        super(isWhite);
        this.symbol = isWhite ? 'K' : 'k';
    }

    @Override
    public boolean isValidMove(Move move, Board board) {
        int[] from = Board.convertPosition(move.getFrom());
        int[] to = Board.convertPosition(move.getTo());
        
        int rowDiff = Math.abs(to[0] - from[0]);
        int colDiff = Math.abs(to[1] - from[1]);
        
        return rowDiff <= 1 && colDiff <= 1;
    }

    @Override
    public Piece clone() {
        return new King(isWhite);
    }
}

class Queen extends Piece {
    public Queen(boolean isWhite) {
        super(isWhite);
        this.symbol = isWhite ? 'Q' : 'q';
    }

    @Override
    public boolean isValidMove(Move move, Board board) {
        int[] from = Board.convertPosition(move.getFrom());
        int[] to = Board.convertPosition(move.getTo());
        
        int rowDiff = Math.abs(to[0] - from[0]);
        int colDiff = Math.abs(to[1] - from[1]);
        
        return (rowDiff == colDiff || rowDiff == 0 || colDiff == 0) && isPathClear(move, board);
    }

    @Override
    public Piece clone() {
        return new Queen(isWhite);
    }
}

class Rook extends Piece {
    public Rook(boolean isWhite) {
        super(isWhite);
        this.symbol = isWhite ? 'R' : 'r';
    }

    @Override
    public boolean isValidMove(Move move, Board board) {
        int[] from = Board.convertPosition(move.getFrom());
        int[] to = Board.convertPosition(move.getTo());
        
        return (from[0] == to[0] || from[1] == to[1]) && isPathClear(move, board);
    }

    @Override
    public Piece clone() {
        return new Rook(isWhite);
    }
}

class Bishop extends Piece {
    public Bishop(boolean isWhite) {
        super(isWhite);
        this.symbol = isWhite ? 'B' : 'b';
    }

    @Override
    public boolean isValidMove(Move move, Board board) {
        int[] from = Board.convertPosition(move.getFrom());
        int[] to = Board.convertPosition(move.getTo());
        
        int rowDiff = Math.abs(to[0] - from[0]);
        int colDiff = Math.abs(to[1] - from[1]);
        
        return rowDiff == colDiff && isPathClear(move, board);
    }

    @Override
    public Piece clone() {
        return new Bishop(isWhite);
    }
}

class Knight extends Piece {
    public Knight(boolean isWhite) {
        super(isWhite);
        this.symbol = isWhite ? 'N' : 'n';
    }

    @Override
    public boolean isValidMove(Move move, Board board) {
        int[] from = Board.convertPosition(move.getFrom());
        int[] to = Board.convertPosition(move.getTo());
        
        int rowDiff = Math.abs(to[0] - from[0]);
        int colDiff = Math.abs(to[1] - from[1]);
        
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    @Override
    public Piece clone() {
        return new Knight(isWhite);
    }
}

class Pawn extends Piece {
    public Pawn(boolean isWhite) {
        super(isWhite);
        this.symbol = isWhite ? 'P' : 'p';
    }

    @Override
    public boolean isValidMove(Move move, Board board) {
        int[] from = Board.convertPosition(move.getFrom());
        int[] to = Board.convertPosition(move.getTo());
        
        int direction = isWhite ? 1 : -1;
        int startRow = isWhite ? 1 : 6;
        
        // Normal move
        if (from[1] == to[1] && to[0] == from[0] + direction) {
            return board.board[to[0]][to[1]] == null;
        }
        
        // First move - can move two squares
        if (from[1] == to[1] && from[0] == startRow && to[0] == from[0] + 2 * direction) {
            return board.board[to[0]][to[1]] == null && 
                   board.board[from[0] + direction][from[1]] == null;
        }
        
        //
