package db;

import constants.Constants;
import model.Board;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class BoardDB
{
    static BoardDB boardDB;

    private BoardDB() {}

    public static BoardDB getBoardDB()
    {
        if (boardDB == null)
        {
            boardDB = new BoardDB();
        }
        return boardDB;
    }

    public Board[] getBoards()
    {
        Random randomGenerator = new Random();
        List<Board> boards = new LinkedList<>();
        List<Integer> randomNumbers = new LinkedList<>();
        while (randomNumbers.size() < 3)
        {
            int random = randomGenerator.nextInt(10);
            if (!randomNumbers.contains(random))
            {
                randomNumbers.add(random);
            }
        }
        for (Integer i : randomNumbers)
        {
            Board board = new Board();
            try
            {
                File boardFile = new File(Constants.BOARDS_ADDRESS + "/" + i + ".board");
                Scanner scanner = new Scanner(boardFile);
                for (int j = 0; j < 10; j++)
                {
                    String[] shipString = scanner.nextLine().split(" ");
                    List<Integer[]> coordinates = new LinkedList<>();
                    coordinates.add(new Integer[]{Integer.parseInt(shipString[1]), Integer.parseInt(shipString[2])});
                    for (int k = 1; k < Integer.parseInt(shipString[0]); k++)
                    {
                        if (shipString[3].equals("V"))
                        {
                            coordinates.add(new Integer[]{Integer.parseInt(shipString[1]), Integer.parseInt(shipString[2]) + k});
                        }
                        else
                        {
                            coordinates.add(new Integer[]{Integer.parseInt(shipString[1]) + k, Integer.parseInt(shipString[2])});
                        }
                    }
                    board.setShip(coordinates);
                    boards.add(board);
                }
                scanner.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        return (Board[]) boards.toArray();
    }
}
