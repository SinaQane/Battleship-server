package controller;

import controller.game.GameLobby;
import event.EventVisitor;
import model.Board;
import model.game.Game;
import model.game.Side;
import response.Response;
import response.ResponseSender;

public class ClientHandler extends Thread implements EventVisitor
{
    private final ResponseSender sender;
    private final GameLobby gameLobby;
    private Game game;
    private Side side;

    public ClientHandler(ResponseSender sender, GameLobby gameLobby)
    {
        this.sender = sender;
        this.gameLobby = gameLobby;
    }

    public void setSide(Side side)
    {
        this.side = side;
    }

    public void setGame(Game game)
    {
        this.game = game;
    }

    @Override
    public Response login(String username, String password)
    {
        return null;
    }

    @Override
    public Response signup(String username, String password)
    {
        return null;
    }

    @Override
    public Response logout(String authToken) {
        return null;
    }

    @Override
    public Response gameMove(String authToken, int x, int y)
    {
        return null;
    }

    @Override
    public Response getBoard(String authToken)
    {
        return null;
    }

    @Override
    public Response gamesList()
    {
        return null;
    }

    @Override
    public Response scoreboard()
    {
        return null;
    }

    @Override
    public Response viewGame(Game game)
    {
        return null;
    }

    @Override
    public Response pickBoard(String authToken)
    {
        return null;
    }

    @Override
    public Response startGame(String authToken, Board board)
    {
        return null;
    }
}
