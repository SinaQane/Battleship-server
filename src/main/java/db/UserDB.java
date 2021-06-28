package db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import constants.ServerConstants;
import model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class UserDB
{
    private final GsonBuilder gsonBuilder = new GsonBuilder();
    private final Gson gson = gsonBuilder.setPrettyPrinting().create();

    static UserDB userDB;

    private UserDB() {}

    public static UserDB getUserDB()
    {
        if (userDB == null)
        {
            userDB = new UserDB();
        }
        return userDB;
    }

    public boolean exists(String username)
    {
        return get(username) != null;
    }

    public User get(String username)
    {
        try
        {
            String path = ServerConstants.USERS_ADDRESS + "/" + username;
            return gson.fromJson(Files.readString(Paths.get(path)), User.class);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public List<User> getALl()
    {
        List<User> result = new LinkedList<>();
        File usersDirectory = new File(ServerConstants.USERS_ADDRESS);
        for (String username : Objects.requireNonNull(usersDirectory.list()))
        {
            try
            {
                String jsonString = Files.readString(Paths.get(ServerConstants.USERS_ADDRESS + "/" + username));
                User tempUser = gson.fromJson(jsonString, User.class);
                result.add(tempUser);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void save(User user)
    {
        String path = ServerConstants.USERS_ADDRESS + "/" + user.getUsername();
        File file = new File(path);
        if(file.getParentFile().mkdirs())
        {
            System.out.println("users directory was created.");
        }
        if (!file.exists())
        {
            try
            {
                if (file.createNewFile())
                {
                    System.out.println(user.getUsername() + "th user's file was created.");
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        FileOutputStream fileOutputStream = null;
        try
        {
            fileOutputStream = new FileOutputStream(path, false);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        assert fileOutputStream != null;
        PrintStream printStream = new PrintStream(fileOutputStream);
        printStream.println(gson.toJson(user));
        printStream.flush();
        printStream.close();
        try
        {
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
