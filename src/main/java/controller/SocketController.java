package controller;

import controller.game.GameLobby;
import response.SocketResponseSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketController extends Thread
{
    @Override
    public void run()
    {
        GameLobby gameLobby = new GameLobby();
        ServerSocket serverSocket = null;
        try
        {
            // TODO read address and port from file
            serverSocket = new ServerSocket(Constants.DEFAULT_PORT, 50,
                    InetAddress.getByName(Constants.DEFAULT_ADDRESS));
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
