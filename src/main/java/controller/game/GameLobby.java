package controller.game;

import controller.ClientHandler;
import model.Board;
import model.game.Game;
import model.game.Side;

public class GameLobby
{
    private ClientHandler waiting;
    private Board waitingBoard;

    public synchronized void startGameRequest(ClientHandler clientHandler, Board board)
    {
        if (waiting == null)
        {
            waiting = clientHandler;
            waitingBoard = board;
            clientHandler.setSide(Side.PLAYER_ONE);
        }
        else
        {
            if (waiting != clientHandler)
            {
                Game game = new Game(waiting.getUser(), clientHandler.getUser());
                game.setBoard(0, waitingBoard);
                game.setBoard(1, board);
                clientHandler.setSide(Side.PLAYER_TWO);
                waiting.setGame(game);
                clientHandler.setGame(game);
                waitingBoard = null;
                waiting = null;
            }
        }
    }
}
