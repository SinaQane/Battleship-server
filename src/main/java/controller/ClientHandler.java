package controller;

import controller.game.GameLobby;
import db.BoardDB;
import db.UserDB;
import event.EventVisitor;
import model.Board;
import model.Ship;
import model.User;
import model.game.Game;
import model.game.Side;
import response.Response;
import response.ResponseSender;
import response.responses.authentication.LoginResponse;
import response.responses.authentication.LogoutResponse;
import response.responses.authentication.SignupResponse;
import response.responses.gameplay.GameplayResponse;
import response.responses.menu.ChangeFrameResponse;
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
    private boolean running;
    private int viewingGameIndex;

    public ClientHandler(ResponseSender responseSender, GameLobby gameLobby)
    {
        this.responseSender = responseSender;
        this.gameLobby = gameLobby;
        running = true;
    }

    public void setGame(Game game)
    {
        this.game = game;
        synchronized (allGames)
        {
            if (!allGames.contains(game))
            {
                allGames.add(game);
            }
            else
            {
                allGames.set(allGames.indexOf(game), game);
            }
        }
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
        while (running)
        {
            try
            {
                responseSender.sendResponse(responseSender.getEvent().visit(this));
            }
            catch (Exception ignored) {}
        }
    }

    @SuppressWarnings("unused")
    public void kill()
    {
        running = false;
    }

    @Override
    public Response login(String username, String password)
    {
        if (!UserDB.getUserDB().exists(username))
        {
            return new LoginResponse(null, "wrong username", "");
        }
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
        return new LoginResponse(user, "", authToken);
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
        return new SignupResponse("");
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
        return new LogoutResponse("");
    }

    @Override
    public Response gameMove(String authToken, int x, int y)
    {
        if (!authToken.equals(this.authToken))
        {
            return new GameplayResponse(game);
        }
        else if (x == -1 && y == -1) // User didn't make a move
        {
            game.nextTurn(true);
            game.resetTimer();
            return new GameplayResponse(game);
        }
        else // User wants to drop bomb on a cell
        {
            if (0 <= x & x <= 9 & 0 <= y & y <= 9 &&
                    !game.getBoard(side.getRival()).getCell(x, y).isBombed())
            {
                boolean successful = game.dropBomb(side, x, y);
                if (successful)
                {
                    Board rivalBoard = game.getBoard(side.getRival());
                    for (Ship ship : rivalBoard.getShips())
                    {
                        if (game.isShipDestroyed(side.getRival(), ship))
                        {
                            for (Integer[] cell : ship.getShip())
                            {
                                int i = cell[0];
                                int j = cell[1];
                                game.explosion(side, i + 1, j + 1);
                                game.explosion(side, i , j + 1);
                                game.explosion(side, i + 1, j );
                                if (j > 0)
                                {
                                    game.explosion(side, i + 1, j - 1);
                                    game.explosion(side, i, j - 1);
                                }
                                if (i > 0)
                                {
                                    game.explosion(side, i - 1, j + 1);
                                    game.explosion(side, i - 1, j);
                                }
                                if (i > 0 && j > 0)
                                {
                                    game.explosion(side, i - 1, j - 1);
                                }
                            }
                        }
                    }
                }
                else
                {
                    game.nextTurn(true);
                }
            }
            game.resetTimer();
            return new GameplayResponse(game);
        }
    }

    @Override
    public Response updateTimer()
    {
        if (game != null)
        {
            game.updateTimer();
            return new GameplayResponse(game);
        }
        return new GameplayResponse(null);
    }

    @Override
    public Response getBoard(String authToken)
    {
        if (authToken.equals("0"))
        {
            Game viewingGame;
            synchronized (allGames)
            {
                viewingGame = allGames.get(viewingGameIndex);
            }
            return new GameplayResponse(viewingGame);
        }
        if (!authToken.equals(this.authToken))
        {
            return new GameplayResponse(null);
        }
        if (game != null)
        {
            if (game.getResult() != -1)
            {
                Side tempSide = game.getResult() == 0 ? Side.PLAYER_ONE : Side.PLAYER_TWO;
                game.setGameMessage("player " + game.getPlayer(tempSide).getUsername() + " won");
                if (game.isRunning()) game.endGame();
                UserDB.getUserDB().save(game.getPlayer(Side.PLAYER_ONE));
                UserDB.getUserDB().save(game.getPlayer(Side.PLAYER_TWO));
            }
        }
        return new GameplayResponse(game);
    }

    @Override
    public Response gamesList()
    {
        Game[] games;
        synchronized (allGames)
        {
            games = new Game[allGames.size()];
            for (int i = 0; i < allGames.size(); i++)
            {
                games[i] = allGames.get(i);
            }
        }
        return new GamesListResponse(games);
    }

    @Override
    public Response scoreboard()
    {
        List<User> users = UserDB.getUserDB().getALl();
        users.sort(Comparator.comparing(User::getScore));
        User[] usersArray = new User[users.size()];
        for (int i = 0; i < users.size(); i++)
        {
            usersArray[i] = users.get((users.size() - 1) - i);
        }
        return new ScoreboardResponse(usersArray);
    }

    @Override
    public Response viewGame(int index)
    {
        Game viewingGame;
        synchronized (allGames)
        {
            viewingGame = allGames.get(index - 1);
            viewingGameIndex = index - 1;
        }
        return new ViewGameResponse(viewingGame);
    }

    @Override
    public Response changeFrame(String frame)
    {
        synchronized (allGames)
        {
            allGames.remove(game);
        }
        game = null;
        return new ChangeFrameResponse(frame);
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
        if (authToken.equals(this.authToken))
        {
            gameLobby.startGameRequest(this, board);
            if (side.equals(Side.PLAYER_ONE))
            {
                return new StartGameResponse(game, 1);
            }
            return new StartGameResponse(game, 2);
        }
        return null;
    }

    @Override
    public Response resign(String authToken)
    {
        if (authToken.equals(this.authToken))
        {
            game.resign(side);
            UserDB.getUserDB().save(game.getPlayer(Side.PLAYER_ONE));
            UserDB.getUserDB().save(game.getPlayer(Side.PLAYER_TWO));
            synchronized (allGames)
            {
                allGames.remove(game);
            }
            game = null;
            return new ChangeFrameResponse("mainMenu");
        }
        return new GameplayResponse(game);
    }
}
