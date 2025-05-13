/*******************************************************************************
 * Chris Iverson, Luci Crow, Emilee Stone
 * CS 123 - Spring

 * Battleship class:
 ** Battleship gameplay mechanics including
 **** Setting up the game
 **** Placing Ship
 **** Player vs. AI turns
 **** Winning logic
 **** Game Display

 /*****************************************************************************/
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class BattleShipGame
{
    private boolean userActivePlayer;
    private String userName;
    private final String oppoName;
    GameBoard playerBoard;
    GameBoard machineBoard;
    ArrayList<Ships> playerFleet;
    ArrayList<Ships> machineFleet;

    // Constructor
    public BattleShipGame()
    {
        userActivePlayer = false;
        userName = "";
        oppoName = "AI Battle Fleet";
        playerBoard = new GameBoard();
        machineBoard = new GameBoard();
    }

    //start getters / setters
    public boolean isUserActivePlayer()
    {
        return userActivePlayer;
    }
    public String getUserName()
    {
        return userName;
    }
    public String getOppoName()
    {
        return oppoName;
    }
    public void setUserActivePlayer(boolean set)
    {

        userActivePlayer = set;
    }
    public void setUserName(String nameInput)
    {
        userName = nameInput;
    }
    // end getters / setters

    /* Main game loop; Once called will start and run the battleship gameplay
     * manages setup, turns, player input, and ending the game
     */

    public void runBattleShip()
    {
        boolean runningBattleShip = true;
        Scanner input = new Scanner(System.in);
        setupBS();  // Setup ships and boards
        coinToss(); // Decides who goes first

        while (runningBattleShip)
        {
            // checking if it is the player's turn
            if (isUserActivePlayer())
            {
                // prompting user to input their shot coordinates
                System.out.print(getUserName() +
                        ", Call your Shot!\n(enter coordinates): ");
                String shot = "";
                // temp hold for user coordinate to be validated
                String temp = input.nextLine();
                // validating coordinates
                boolean checking = true;
                while (checking)
                {
                    if (isValidCoord(temp))
                    {
                        shot = temp;
                        checking = false;
                    }
                    else
                    {
                        System.out.print("Invalid coordinates. Try again: ");
                        temp = input.nextLine();
                    }
                } // end while(checking)

                /* pause for effect; checkHit() determines if the
                shot was hit/miss */
                wait(1000);
                checkHit( machineBoard, shot );
                wait(1000);

                /* if the AI's board shows all 17 ship parts have been hit,
                the user wins ending the loop  */
                if ( (checkWin(machineBoard)) )
                {
                    victoryScreen(getUserName());
                    runningBattleShip = false;
                }

                // displaying updated game board after shot
                displayBoards();
                setUserActivePlayer(false);
            } //end users turn

            // AI's Turn
            if (!isUserActivePlayer())
            {
                // pause for effect; a shot is generated randomly
                System.out.print("The AI is taking their turn. ");
                wait(1800);
                String shot = "";
                String temp = autoShot();
                boolean checking = true;
                // validating AI shot
                while (checking)
                {
                    if (isValidCoord(temp))
                    {
                        shot = temp;
                        checking = false;
                    }
                    else
                    {
                        temp = autoShot();
                    }
                } // end while(checking)
                // Updates board to reflect AI's chosen target
                System.out.println(getOppoName()+" fires at " + shot);
                wait(3000);
                checkHit(playerBoard, shot );

                // check if AI won
                setUserActivePlayer(true);
                if ( checkWin(playerBoard) )
                {
                    victoryScreen(getOppoName());
                    runningBattleShip = false;
                }
                displayBoards();
            } // end AI turn
        } //end while(runningBattleShip)
    } //end method

    /* Collects username; and allows user to manually set down their ships
     * places AI ships; displays boards; */

    // prompting user for their name; displaying the board
    public void setupBS()
    {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter your name: ");
        setUserName(input.nextLine());
        displayBoards();

        // Ship names and sizes
        String[] shipNames =
                {"Carrier", "Battleship", "Cruiser", "Submarine", "Frigate"};
        int[] shipSizes = {5, 4, 3, 3, 2};
        Ships[] playerShips = new Ships[5];
        playerFleet = new ArrayList<>();

        /* looping through each ship (e.g. carrier, battleship) and prompting
        the user to place it */
        for (int idx = 0; idx < shipNames.length; idx++)
        {
            boolean placed = false;
            while (!placed)
            {
                System.out.println("\nPlace your " + shipNames[idx] +
                        " (" + shipSizes[idx] + " spaces)");
                System.out.print("Enter rear coordinates (ex. A1): ");
                String rearCoord = input.nextLine().toLowerCase();
                System.out.print("Enter forward coordinates (ex. A5): ");
                String forwardCoord = input.nextLine().toLowerCase();

                // validating provided coordinates
                if (isValidCoord(rearCoord) && isValidCoord(forwardCoord))
                {
                    // converting string coordinates (e.g. A1) into Array indices
                    int[] startCoord = playerBoard.gamifyBoard(rearCoord);
                    int[] endCoord = playerBoard.gamifyBoard(forwardCoord);
                    boolean isVertical = startCoord[1] == endCoord[1];


                    /* validating placement (if ship fits on board, matches
                    the expected size, and doesn't overlap other ships) */
                    if (isValidPlacement(playerBoard, startCoord,
                            shipSizes[idx], isVertical))
                    {
                        /* creating new ship objects, places on board;
                        adds to players fleet */
                        Ships ship = new Ships(shipNames[idx], shipSizes[idx],
                                startCoord, isVertical);
                        placeShipOnBoard(ship, playerBoard, endCoord);
                        playerShips[idx] = ship;
                        placed = true;
                        playerFleet.add(playerShips[idx]);

                        // shows the updated board
                        displayBoards();
                    }
                    else
                    {
                        System.out.println("Invalid placement: That ship" +
                                " would go off the board or overlaps another ship. \nTry again.\n");
                    }
                } //end validation if
                else
                {
                    System.out.println("Invalid input. Coordinates must be " +
                            "A-J followed by 1-10 (ex. A1, C7). "
                            +"\nTry again.\n");
                }
            } //end while(!placing)
        } //end for()

        // randomly places AI fleet
        placeAIShips();
        System.out.print("The Machine is placing its ships");
        wait(4000);
        System.out.println();
    } //end setupBS

    /* places the ships on the board using the provided coordinates */
    public void placeShipOnBoard
    (Ships ship, GameBoard inputBoard, int[] endCoord)
    {
        // now uses real endCoord
        int[][] tiles = ship.getShipFullPosition(endCoord);

        for (int idx = 0; idx < tiles.length; idx++)
        {
            int row = tiles[idx][0];
            int col = tiles[idx][1];

            // Determine the part of the ship to display
            if (ship.getVertical()) {
                if (idx == 0)
                    inputBoard.setTile
                            (row, col, inputBoard.getVerticalSymbFront());
                else if (idx == tiles.length - 1)
                    inputBoard.setTile
                            (row, col, inputBoard.getVerticalSymbBack());
                else
                    inputBoard.setTile
                            (row, col, inputBoard.getVerticalSymbMiddle());
            }
            else
            {
                if (idx == 0)
                    inputBoard.setTile
                            (row, col, inputBoard.getHorizSymbBack());
                else if (idx == tiles.length - 1)
                    inputBoard.setTile
                            (row, col, inputBoard.getHorizSymbFront());
                else
                    inputBoard.setTile
                            (row, col, inputBoard.getHorizSymbMiddle());
            } // end else for Determine the part of the ship to display
        } //end for()
    } //end placeShips on board

    // random placement of the AI Ships
    public void placeAIShips()
    {
        String[] shipNames =
                {"Carrier", "Battleship", "Cruiser", "Submarine", "Frigate"};
        int[] shipSizes = {5, 4, 3, 3, 2};

        // creates and hold the AI's ships
        Ships[] machineShips = new Ships[5];
        machineFleet = new ArrayList<>();
        Random rand = new Random();

        // looping through each ship type
        for (int i = 0; i < shipNames.length; i++)
        {
            boolean placed = false;
            // ensures the ship is only placed in a valid spot
            while (!placed)
            {
                // generating a random position and orientation
                int row = rand.nextInt(10);
                int col = rand.nextInt(10);
                boolean isVertical = rand.nextBoolean();

                /* setting up the starting of the ship based on the
                random row/col and calculates the end */
                int[] startCoord = new int[2];
                startCoord[0] = row;
                startCoord[1] = col;
                int[] endCoord = new int[2];

                //validating placement
                if (isVertical)
                {
                    endCoord[0] = row + shipSizes[i] - 1;
                    endCoord[1] = col;
                }
                else
                {
                    endCoord[0] = row;
                    endCoord[1] = col + shipSizes[i] - 1;
                }

                if (isValidPlacement(machineBoard, startCoord,
                        shipSizes[i], isVertical))
                {
                    /* creating and placing the AI Ship; places on the machine
                     board; stores it into machineFleet list */
                    Ships aiShip = new Ships
                            (shipNames[i], shipSizes[i],
                                    startCoord, isVertical);
                    placeShipOnBoard(aiShip, machineBoard, endCoord);
                    machineShips[i] = aiShip;
                    machineFleet.add(machineShips[i]);
                    placed = true;
                }
            } //end while (!placed)
        } //end for ()
    } //end placeAIShips()

    // Validating the input coordinate string
    public boolean isValidCoord(String coord)
    {
        //Sets to lowercase
        coord = coord.toLowerCase();
        //Checks if the coord length is less than 2 or greater than 3
        if (coord.length() < 2 || coord.length() > 3)
        {
            return false;
        }
        //Pulls the letter
        char letter = coord.charAt(0);
        //Checks if letter is within parameters
        if (letter < 'a' || letter > 'j')
        {
            return false;
        }
        //Pulls the number
        String numPart = coord.substring(1);
        //checks if number is a digit. source: found on Google
        if (!numPart.matches("\\d+"))
        {
            return false;
        }
        //Changes it from a string to a digit
        int number = Integer.parseInt(numPart);
        //Checks if the number is within the range
        return number >= 1 && number <= 10;
    }

    public boolean isValidPlacement(GameBoard board, int[] startCoord,
                                     int size, boolean isVertical)
    {
        int rowStart = startCoord[0];
        int colStart = startCoord[1];
        for (int i = 0; i < size; i++)
        {
            int row;
            int col;
            if (isVertical) {
                row = rowStart + i;
                col = colStart;
            }
            else
            {
                row = rowStart;
                col = colStart + i;
            }
            // Check board bounds
            if (row < 0 || row >= 10 || col < 0 || col >= 10) {
                return false;
            }
            // Check if the tile is already occupied by another ship
            if (board.isShip(board.getTile(row, col))) {
                return false;
            }
        }
        return true;
    }

    public void displayBoards()
    {
        String[][] userStatus = playerBoard.getBoard();
        String[][] machineStatus = machineBoard.getBoard();
        for (int idx=0;idx<80;idx++)
            System.out.print("/"); //creates display top-border
        System.out.println();
        System.out.print("      " + getUserName() + " Battle Fleet");
        System.out.printf("%40s %n", getOppoName());
        System.out.print("  "); //starts column label spacing
        for (char colLabel = 'A'; colLabel <= 'J'; colLabel++)
        { //loop to establish values for player board column labels
            System.out.print(" " + colLabel + " ");
        }
        System.out.print("                ");//space between column labels
        for (char colLabel = 'A'; colLabel <= 'J'; colLabel++)
        { //loop to establish values for machine board column labels
            System.out.print(" " + colLabel + " ");
        }
        System.out.println();
        for (int row = 0; row <10; row++)
        {
            //player row labels taking up 2spaces
            System.out.printf("%2d", row+1);
            for (int col = 0; col < 10; col++)
            { //loop displays GameBoard values for player
                System.out.print(" " + userStatus[row][col] + " ");
            }
            System.out.print("              "); //space between grid rows
            //machine row labels taking up 2spaces
            System.out.printf("%2d", row+1);
            for (int col = 0; col < 10; col++)
            { //loop displays GameBoard values for machine
                if ( machineBoard.isShip(machineStatus[row][col]))
                    System.out.print(" " + machineBoard.getEmpty() + " ");
                else
                    System.out.print(" " + machineStatus[row][col] + " ");
            }
            System.out.println(); // starts next row on new line
        }
        for (int idx=0;idx<80;idx++)
            System.out.print("/"); // creates display bottom-border
        System.out.println();
    }

    public void checkHit( GameBoard inputBoard , String shot )
    {
        boolean changed = false;
        Scanner input = new Scanner(System.in);
        while(!changed)
        {
            int [] coord = inputBoard.gamifyBoard(shot);
            int row = coord[0];
            int col = coord[1];
            String target = inputBoard.getTile(row, col);


            if (inputBoard.isShip(target))
            {
                System.out.println("Hit!");
                inputBoard.setTile(row, col, inputBoard.getHit());
                changed = true;
            }
            else if (target.equals(inputBoard.getEmpty()))
            {
                inputBoard.setTile(row,col, inputBoard.getMiss());
                System.out.println("Miss!");
                changed = true;
            }
            else
            {
                System.out.print("Target invalid. Choose a new space: ");
                shot = input.nextLine();
            }
        } //end while (!changed)
    } //end checkHit()

    public boolean checkWin(GameBoard inputBoard)
    {
        String [][] boardPieces = inputBoard.getBoard();
        int counter = 0;

        for (int idx = 0; idx < 10; idx++)
        {
            for (int jdx = 0; jdx < 10; jdx++)
            {
                if(boardPieces[idx][jdx].equals(inputBoard.getHit()))
                {
                    counter++;
                }
                if(counter == 17)
                {
                    return true;
                }

            } //end loop to id hit ship
        } //end loop to id hit ship
        return false;
    } //end checkWin()

    private String autoShot()
    //generates argument for AI use of check hit
    {
        Random random = new Random();
        char col = (char) ('A' + random.nextInt(10));
        int row = random.nextInt(10) + 1;
        return "" + col + row;
    }

    public void coinToss()
    {
        Scanner userIn = new Scanner(System.in);
        String call_it;
        String result = flip();
        System.out.print("""
                            \n
                            Coin Toss for first turn.
                            Call Heads or Tails: \s""");
        String input = userIn.nextLine();

        // while-loop for input validation
        while (!(input.equalsIgnoreCase("heads")) &&
                !(input.equalsIgnoreCase("tails")))
        {
            System.out.print("Please enter heads or tails: ");
            input = userIn.nextLine();
        }
        call_it = input;
        if (result.equalsIgnoreCase(call_it))
        {
            wait(3000);
            /* delay added to make result "feel" more random to a user, the loop
             added to ensure the wait is seen as deliberate */
            System.out.println("The coin shows " + result +
                    ". You win the toss, so You take the first turn!");
            setUserActivePlayer(true);
        }
        else
        {
            wait(3000);
            /* delay added to make result "feel" more random to a user, the loop
             added to ensure the wait is seen as deliberate */
            System.out.println("The coin shows " + result +
                    ". You lose the toss, " + getOppoName()
                    + "\ntakes the first turn!\n");
            setUserActivePlayer(false);
        }
    } //end coinToss()

    private String flip()//helper method for coin toss
    {
        Random rand = new Random();
        int randInt = rand.nextInt(1000)+1;
        if (randInt % 2 == 0)
            return "heads";
        return "tails";
    }

    public static void wait(int ms)
/* puts the thread to sleep for the indicated number of milliseconds
Source:
https://stackoverflow.com/questions/24104313/how-do-i-make-a-delay-in-java
*/
    {
        try
        {
            for (int idx = 0; idx < 3; idx++)
            {
                Thread.sleep(ms/6);
                System.out.print(". ");
                Thread.sleep(ms/6);
            }
        }
        //if thread is interrupted while sleeping...
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt(); /* ...this restores the
            interrupted status so all other currently running code doesn't lose
            its state
            */
        }
    } //end wait()

    public void victoryScreen(String winningPlayer)
    {
        boolean entry = false;
        Scanner inputVicScr = new Scanner(System.in);
        System.out.println("------------------------------");
        System.out.println(winningPlayer + "Has defeated their opponent, \n" +
                winningPlayer + "has won Battle Ship!!!\n");
        while (!entry) {
            System.out.println("------------------------------");
            System.out.print("enter any key to continue: ");
            entry = inputVicScr.hasNext();
        }
    }
}