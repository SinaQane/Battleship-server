package controller;

import config.Config;
import constants.ServerConstants;
import controller.game.GameLobby;
import response.SocketResponseSender;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketController extends Thread
{
    private final Config config;

    public SocketController(Config config)
    {
        this.config = config;
    }

    @Override
    public void run()
    {
        GameLobby gameLobby = new GameLobby();
        ServerSocket serverSocket = null;
        try
        {
            int port = config.getProperty(Integer.class, "port").orElse(ServerConstants.DEFAULT_PORT);
            serverSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        while (true)
        {
            ClientHandler clientHandler;
            try
            {
                assert serverSocket != null;
                Socket socket = serverSocket.accept();
                clientHandler = new ClientHandler(new SocketResponseSender(socket),gameLobby);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                break;
            }
            clientHandler.start();
        }
    }
}
