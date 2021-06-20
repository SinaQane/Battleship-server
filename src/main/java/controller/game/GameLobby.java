package controller.game;

import controller.ClientHandler;
import model.game.Game;

public class GameLobby
{
    private ClientHandler waiting;

    public synchronized void startGameRequest(ClientHandler clientHandler)
    {
        if (waiting == null)
        {
            waiting = clientHandler;
        }
        else
        {
            if (waiting != clientHandler)
            {
                Game game = new Game(waiting.getUser(), clientHandler.getUser());
                clientHandler.setGame(game);
                waiting.setGame(game);
                waiting = null;
            }
        }
    }
}
