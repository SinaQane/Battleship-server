package controller.game;

import controller.ClientHandler;
import model.game.Game;
import model.game.Side;

public class GameLobby
{
    private ClientHandler waiting;

    public synchronized void startGameRequest(ClientHandler clientHandler)
    {
        if (waiting == null)
        {
            waiting = clientHandler;
            clientHandler.setSide(Side.PLAYER_ONE);
        }
        else
        {
            if (waiting != clientHandler)
            {
                Game game = null; // TODO = new Game(playerOne, playerTwo);
                clientHandler.setSide(Side.PLAYER_TWO);
                waiting.setGame(game);
                clientHandler.setGame(game);
                waiting = null;
            }
        }
    }
}
