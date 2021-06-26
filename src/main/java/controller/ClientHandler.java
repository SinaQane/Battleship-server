package controller;

import controller.game.GameLobby;
import db.BoardDB;
import db.UserDB;
import event.EventVisitor;
import model.Board;
import model.User;
import model.game.Game;
import model.game.Side;
import response.Response;
import response.ResponseSender;
import response.responses.authentication.LoginResponse;
import response.responses.authentication.LogoutResponse;
import response.responses.authentication.SignupResponse;
import response.responses.gameplay.GameplayResponse;
import response.responses.menu.GamesListResponse;
import response.responses.menu.ScoreboardResponse;
import response.responses.menu.ViewGameResponse;
import response.responses.startgame.PickBoardResponse;
import response.responses.startgame.StartGameResponse;
import util.TokenGenerator;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ClientHandler extends Thread implements EventVisitor
{
    public static final List<Game> allGames = new LinkedList<>();

    private final TokenGenerator tokenGenerator = new TokenGenerator();
    private final ResponseSender responseSender;
    private final GameLobby gameLobby;
    private String authToken;
    private User user;
    private Game game;
    private Side side;

    public ClientHandler(ResponseSender responseSender, GameLobby gameLobby)
    {
        this.responseSender = responseSender;
        this.gameLobby = gameLobby;
    }

    public void setGame(Game game)
    {
        this.game = game;
        allGames.add(game);
    }

    public User getUser()
    {
        return user;
    }

    public void setSide(Side side)
    {
        this.side = side;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                responseSender.sendResponse(responseSender.getEvent().visit(this));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                break;
            }
        }
    }

    @Override
    public Response timeout(String kind)
    {
        if (kind.equals("game move"))
        {
            game.nextTurn();
            return new GameplayResponse(game);
        }
        // TODO else if (kind.equals("board selection")) ? (can be handled in client tho)
        return null;
    }

    @Override
    public Response login(String username, String password)
    {
        User requestedUser = UserDB.getUserDB().get(username);
        if (!requestedUser.getPassword().equals(password))
        {
            return new LoginResponse(null, "wrong password", "");
        }
        if (requestedUser.isOnline())
        {
            return new LoginResponse(null, "already online", "");
        }
        authToken = tokenGenerator.newToken();
        user = requestedUser;
        user.login();
        UserDB.getUserDB().save(user);
        return new LoginResponse(user, "login successful", authToken);
    }

    @Override
    public Response signup(String username, String password)
    {
        if (UserDB.getUserDB().exists(username))
        {
            return new SignupResponse("already taken");
        }
        user = new User(username, password);
        UserDB.getUserDB().save(user);
        return new SignupResponse("signup successful");
    }

    @Override
    public Response logout(String authToken)
    {
        if (!authToken.equals(this.authToken))
        {
            return new LogoutResponse("invalid token");
        }
        user.logout();
        UserDB.getUserDB().save(user);
        user = null;
        return new LogoutResponse("logout successful");
    }

    @Override
    public Response gameMove(String authToken, int x, int y)
    {
        if (!authToken.equals(this.authToken))
        {
            return new GameplayResponse(null);
        }
        else if (x == -1 && y == -1) // User didn't make a move
        {
            game.nextTurn();
            return new GameplayResponse(game);
        }
        else // User wants to drop bomb on a cell
        {
            if (0 <= x & x <= 9 & 0 <= y & y <= 9 &&
                    !game.getBoard(side.getRival()).getCell(x, y).getShip().isDestroyed())
            {
                game.dropBomb(side, x, y);
            }
            return new GameplayResponse(game);
        }
    }

    @Override
    public Response getBoard(String authToken)
    {
        if (!authToken.equals(this.authToken))
        {
            return new GameplayResponse(null);
        }
        if (game.getResult() != -1)
        {
            Side tempSide = game.getResult() == 0 ? Side.PLAYER_ONE : Side.PLAYER_TWO;
            game.setGameMessage("player " + game.getPlayer(tempSide).getUsername() + " won");
            allGames.remove(game);
            game.endGame();
        }
        return new GameplayResponse(game);
    }

    @Override
    public Response gamesList()
    {
        return new GamesListResponse((Game[]) allGames.toArray());
    }

    @Override
    public Response scoreboard()
    {
        List<User> users = UserDB.getUserDB().getALl();
        users.sort(Comparator.comparing(User::getScore));
        return new ScoreboardResponse((User[]) users.toArray());
    }

    @Override
    public Response viewGame(int index)
    {
        return new ViewGameResponse(allGames.get(index));
    }

    @Override
    public Response pickBoard(String authToken)
    {
        if (!authToken.equals(this.authToken))
        {
            return new PickBoardResponse(null);
        }
        return new PickBoardResponse(BoardDB.getBoardDB().getBoards());
    }

    @Override
    public Response startGame(String authToken, Board board)
    {
        if (!authToken.equals(this.authToken))
        {
            return new LogoutResponse("invalid token");
        }
        gameLobby.startGameRequest(this);
        return new StartGameResponse(game);
    }
}
