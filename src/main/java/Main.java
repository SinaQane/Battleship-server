import config.Config;
import constants.Constants;
import controller.SocketController;

public class Main
{
    public static void main(String[] args)
    {
        SocketController socketController = new SocketController(new Config(Constants.CONFIG_ADDRESS));
        socketController.start();
    }
}
