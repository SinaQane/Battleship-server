import config.Config;
import constants.ServerConstants;
import controller.SocketController;

public class Main
{
    public static void main(String[] args)
    {
        SocketController socketController = new SocketController(new Config(ServerConstants.CONFIG_ADDRESS));
        socketController.start();
    }
}
