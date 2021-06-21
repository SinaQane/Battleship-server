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
                Game game = new Game(waiting.getUser(), clientHandler.getUser());
                clientHandler.setSide(Side.PLAYER_TWO);
                clientHandler.setGame(game);
                waiting.setGame(game);
                waiting = null;
            }
        }
    }
}
