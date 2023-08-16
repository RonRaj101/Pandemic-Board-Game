package chatBotAgent;
/*This is just a simple pandemic file.  It allows the user to type in a few commands.
  It reads in the cities from a file.
  The user can quit, print their current location, and print the actions they can perform,
  move, print all cities, print all connections, and print connections from the current location.
  You probably need to change the path for the cityMapFileName below.
  Chris Huyck wrote this.  I've not asked the pandemic folks to use their game, so don't 
  distribute it beyond CST 3170.
 */

//Note: you need to import scanner to use it to read from the screen.
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.lang.reflect.Array;
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

//There's only one class in this program.  All functions are here, and it runs from
//the main function.
public class pandemic {
	//class variables on top
	private static Scanner shellInput;    //These two are for the shell scanner.
	private static boolean shellOpen = false;
	//Note use a seed (1) for debugging.  
	private static Random randomGenerator = new Random();
	private static int numberCities = -1;	
	private static int numberConnections = -1;	
	private static String[] cities; //Cities
	private static int[] diseaseCubes; //Number of disease cubes in the associated city.
	private static  int[][] connections; //The connections via offset in the cities array.
	private static int[] userLocation = {0,0};  //These are the users' location that can change.
	private static int currentUser = 0;
	private static String[] cityColors = {"Blue", "Yellow", "Black", "Red"};
	private static boolean[] colorCured = {false, false, false, false};

	//##Change this to your path.##
	private static final String cityMapFileName= "../CST_3170_CW3/src/chatBotAgent/fullMap.txt";
	private static final int NUMBER_USERS = 2;
	private static final String[] userNames = {"Ronit","Chris"};
	
	private static final String playerDeckFileName= "../CST_3170_CW3/src/chatBotAgent/playerDeck.txt";
	public static ArrayList<String[]> playerDeck = new ArrayList<String[]>();
//	1. [1, 4]
//	2. [15,3]
	
	public static ArrayList<String[]> playerHands = new ArrayList<String[]>();
	//1. [0, 1 ,4]
	public static ArrayList<String[]> researchStations = new ArrayList<String[]>();
	//Similar to playerHand Cards, discarded there to be pushed here.
	
	//keeps track of the number of actions for each player
	public static int actionsLeft = 4; 
	
	//keeps track of available moves for any user
	public static ArrayList<Integer> availableMoves = new ArrayList<Integer>();
	//	1. [1, 4]
	
	//The constants for the commands.
	private static final int QUIT = 0;
	private static final int PRINT_LOCATION = 1;
	private static final int MOVE = 2;
	private static final int PRINT_ACTIONS = 3;
	private static final int PRINT_CARDS = 4;
	private static final int PRINT_CITIES = 5;
	private static final int PRINT_CONNECTIONS = 6;
	private static final int PRINT_ADJACENT_CITIES = 7;
	private static final int PRINT_DISEASES = 8;
	private static final int REMOVE = 9;
	private static final int BUILD_RESEARCH_ST = 10;
	private static final int VIEW_RESEARCH_ST = 11;
	private static final int CURE_DISEASE = 12;
	private static final int DRAW_CARDS = 13;
	private static final int CURR_STATUS = 14;
	private static final int SHARE_CARD = 15;
	
	private static String[] actionList = {"move", "remove", "build", "share", "cure", "status"};
	private static String[] altCommandList = {"location", "cards", "infections", "stations", "cities", "connections", "actions", "help", "quit"};
		
	/***Functions for user commands***/
	//Get the users input and translate it to the constants.  Could do lots more 
	//error handling here.
	private static int processUserInput(String inputString) {
		if (inputString.compareTo("quit") == 0)
			return QUIT;
		else if (inputString.compareTo("location") == 0)
			return PRINT_LOCATION;
		else if (inputString.compareTo("cities") == 0)
			return PRINT_CITIES;
		else if(inputString.compareTo("cards") == 0)
			return PRINT_CARDS;
		else if (inputString.compareTo("connections") == 0)
			return PRINT_CONNECTIONS;
		else if (inputString.compareTo("adjacent") == 0)
			return PRINT_ADJACENT_CITIES;
		else if (inputString.compareTo("infections") == 0)
			return PRINT_DISEASES;
		else if (inputString.compareTo("move") == 0)
			return MOVE;
		else if (inputString.compareTo("remove") == 0)
			return REMOVE;
		else if (inputString.compareTo("build") == 0)
			return BUILD_RESEARCH_ST;
		else if (inputString.compareTo("stations") == 0)
			return VIEW_RESEARCH_ST;
		else if (inputString.compareTo("cure") == 0)
			return CURE_DISEASE;
		else if (inputString.compareTo("draw") == 0)
			return DRAW_CARDS;
		else if (inputString.compareTo("status") == 0)
			return CURR_STATUS;
		else if (inputString.compareTo("share") == 0)
			return SHARE_CARD;
		else if ((inputString.compareTo("actions") == 0) ||
				 (inputString.compareTo("help") == 0))
			return PRINT_ACTIONS;
		else 
			return -1;
	}
	
	//Make sure the scanner is open, then get the user input and make sure it's reasonable.
	//Return the integer command.
	private static int getUserInput() {
		boolean gotReasonableInput = false;
		int processedUserInput = -1;

		//Open up the scanner if it's not already open.
		if (!shellOpen) {
			shellInput = new Scanner(System. in);
			shellOpen = true;
			//todo, add error checking.
		}
		//loop until the user types in a command that is named.  It may not be a valid move.
		while (!gotReasonableInput) {
			String userInput = shellInput.nextLine();
			System.out.println("The user typed:"+ userInput);
			//Translate the user's input to an integer.
			processedUserInput = processUserInput(userInput); 						
			if (processedUserInput >= 0)
				gotReasonableInput = true;
			else
				System.out.println(userInput + "is not a good command. Try 'actions'.");				
		}		
		return processedUserInput;
	}

	//print out the integer associated with what the user typed.
	private static void echoUserInput(int userInput) {
		System.out.println("The user chose:"+ userInput);
	}
		
	//Print out the cities adjacent to the userLocation
	private static void printAdjacentCities () {
		ArrayList<String> citiesAdjacent = new ArrayList<String>(); 
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			if (citiesAdjacent(userLocation[currentUser],cityNumber)) {
				citiesAdjacent.add(cities[cityNumber]);
			}
		}
		System.out.println(Arrays.deepToString(citiesAdjacent.toArray()));
	}
	
	//Print out all possible user actions.
	private static void printActions() {
		System.out.println ("\nType in on the terminal with the following followed by no spaces finish with return.");
		System.out.println(Arrays.deepToString(actionList));
		System.out.println(Arrays.deepToString(altCommandList));
		
	}

	//Print out all the users' locations.
	private static void printUserLocations() {
		System.out.println("The current user is " + userNames[currentUser]);
		for (int userNumber = 0; userNumber<NUMBER_USERS;userNumber++) {
			int printUserLocation = userLocation[userNumber];
			
			System.out.println (userNames[userNumber] + " is in " + cities[printUserLocation]);
		}
	}
	
	//Handle the user's commands.
	private static boolean processUserCommand(int userInput) {
		echoUserInput(userInput);
		
		if (userInput == QUIT) 
			return true;
		else if (userInput == PRINT_LOCATION)
			printUserLocations();
		else if (userInput == PRINT_CITIES)
			printCities();
		else if (userInput == PRINT_CARDS)
			printCards();
		else if (userInput == PRINT_CONNECTIONS)
			printConnections();
		else if (userInput == PRINT_ADJACENT_CITIES)
			printAdjacentCities();
		else if (userInput == PRINT_DISEASES)
			printInfectedCities();
		else if (userInput == PRINT_ACTIONS)
			printActions();
		else if (userInput == MOVE) {
			moveUser();
			actionDone();
		}
		else if (userInput == BUILD_RESEARCH_ST) {
			buildResearchStation();
			actionDone();
		}
		else if (userInput == VIEW_RESEARCH_ST) {
			viewResearchStations();
		}
		else if (userInput == CURE_DISEASE) {
			//if disease can be cured, cure disease and remove 1 action
			if(cureDisease())
				actionDone();
		}
		else if (userInput == CURR_STATUS) {
			currentStatus();
		}
		else if (userInput == SHARE_CARD) {
			shareCard();
		}
		else if (userInput == REMOVE) {
			//if cube/s can be removed, cure disease and remove 1 action
			if (removeCube()) actionDone();
		}
		else if (userInput == DRAW_CARDS) {
			//testing purposes
			drawCard();
		}
		return false;
	}
	
	private static void moveToCity(int cityToMoveTo) {
		System.out.println(userNames[currentUser] +" has moved from " +
				cities[userLocation[currentUser]] + " to " + 
				cities[cityToMoveTo] + ".");
		userLocation[currentUser] = cityToMoveTo;
	}
	
	private static void removeCurrentCityCard() {
		//remove card of current city
		for (String[] card : playerHands) {
			if(card[0].equals(String.valueOf(currentUser)) && card[1].equals(String.valueOf(userLocation[currentUser]))) {
				if(playerHands.remove(card)) {
					break;
				}
			}
		}
	}
	
	private static void viewDirectCities(ArrayList<Integer> avl_cities) {
		//prints out the cities user has cards of/can move directly to
		ArrayList<String> cityList = new ArrayList<String>();
		
		for (Integer city : avl_cities) {
			cityList.add(cities[city]);
		}
		
		System.out.println(Arrays.deepToString(cityList.toArray()));
	}
	
	private static boolean checkIfShuttle(int from, int to) {
		//checks if both are stations and a shuttle flight is possible
		boolean fromStation = false, toStation = false;
		
		for (String[] cityInfo : researchStations) {
			//check from
			if(cityInfo[0].equals(Integer.toString(from))) {
				fromStation = true;
			}
			//check to
			if(cityInfo[0].equals(Integer.toString(to))) {
				toStation = true;
			}
		}
		
		if(fromStation && toStation){
			return true;
		}
		return false;
	}
	
	/*** Action Functions ***/
	//Ask the user where to move, get the city, and if valid, move the user's location.
	private static void moveUser() {
		boolean moved = false;
		boolean free_move = false;
		while (!moved) {
			ArrayList<Integer> avl_cities = new ArrayList<Integer>();
			
			//check playerhand for any cards that could be used for movement, add them to avl_cities arraylist
			for(int city = 0; city < playerHands.size(); city++) {
				
				if(Integer.valueOf(playerHands.get(city)[0]) == currentUser) {
					
					//if card of current city not available then print all other carded cities.
					if(Integer.valueOf(playerHands.get(city)[1]) != userLocation[currentUser]) {	
						avl_cities.add(Integer.valueOf(playerHands.get(city)[1]));
					}
					
					else {
						//if free move is true, clear and add all cities to the list of available/accessible cities
						free_move = true;
						avl_cities.clear();
						for (int city1 = 0; city1 < cities.length; city1++) {
							avl_cities.add(city1 + 1);
						}
					}
					
				}
				
			}
			
			System.out.println("Your Movement options are: ");
			System.out.println("\n-> You can move to any adjacent city");
			printAdjacentCities();
			
			if(free_move) {
				System.out.println("\n-> Move anywhere on the map using current city card");
			}
			else {
				System.out.println("\n-> Use a player card to move directly to a city");
				viewDirectCities(avl_cities);
			}
			
			if(researchStations.size() > 1) {
				//allow user to take the shuttle flight if more than 1 research stations
				System.out.println("\n-> Move to another city with a research station");
				viewResearchStations();
			}
			
			System.out.println("\nType your city of choice");
			String userInput = shellInput.nextLine();
			int cityToMoveTo = getCityOffset(userInput);
			
		
			if (cityToMoveTo == -1) {
				System.out.println(userInput + " is not a valid city.");
			}
			
			//move to adjacent city, if user has card and city is adjacent, no card would be used
			else if(citiesAdjacent(userLocation[currentUser],cityToMoveTo)) {
					moveToCity(cityToMoveTo);
					moved = true;
			}
			
			//If avl_cities has current location/free_move = true
			else if (avl_cities.contains(cityToMoveTo)) {
				//free move is to determine whether to remove the current city card or destination city card
				if(free_move) {			
					//remove current city card and then move to another city on the map	
					removeCurrentCityCard();
					moveToCity(cityToMoveTo);
					
					moved = true;
				}
				else {
					//move to destination city and then remove that city card.
					moveToCity(cityToMoveTo);
					removeCurrentCityCard();	
						
					moved = true;
				}
			}
			
			//check if shuttle flight is possible, then move user to the location
			else if(checkIfShuttle(userLocation[currentUser], cityToMoveTo)) {
				moveToCity(cityToMoveTo);
				moved = true;
			}
			
			else {
				System.out.println ("You can't move to " + userInput + ".  Try one of these.");
			}
		}
		
	}
	
	private static int getCityColor(int cityId) {
		//get city color based on its id [0-12];blue [13-24];yellow [25-36];black [37-48];red
		if(cityId <= 12) {
			return 0;
		}
		else if(cityId <= 24) {
			return 1;
		}
		else if(cityId <= 36) {
			return 2;
		}
		else if(cityId <= 48) {
			return 3;
		}
		return -1;
	}
	
	//Remove a cube from the current location.  If there's not, return false for an error.
	private static boolean removeCube() {
		int currentUserLocation = userLocation[currentUser];
		
		if (diseaseCubes[currentUserLocation] > 0) {
			//check if disease cured, then allow user to remove all cubes
			if(colorCured[getCityColor(currentUserLocation)]) {
				diseaseCubes[currentUserLocation] = 0;
				System.out.println("There are " + diseaseCubes[currentUserLocation] + " left");
			}
			
			else {
				//otherwise just remove one cube per action
				diseaseCubes[currentUserLocation]--;
				System.out.println("There are " + diseaseCubes[currentUserLocation] + " left");
			}
			return true;
		}
		else {
			System.out.println("The space you're on has no disease cubes.");
			return false;
		}
	}
	
	private static void actionDone() {
		if(actionsLeft <= 1) {
			//give user 2 cards but should be within limit
			drawCard();
			checkCardsLimit();
			
			currentUser++;
			currentUser%=NUMBER_USERS;
			//reset actions for new user
			actionsLeft = 4;
			
			infectCityRandom();
			System.out.println(userNames[currentUser] + ", Your Current Location is: " + cities[userLocation[currentUser]] + " with "+ actionsLeft+" Action/s Remaining");
		}
		else {
			actionsLeft--;
			System.out.println(actionsLeft + " Action/s Remaining");
			printActions();
		}
	}
	
	
	/***Code for the city graph ***/
	//Read the specified number of cities.  If it crashes, it should throw to the calling catch.
	private static void readCities(int numCities, Scanner scanner) {
		//A simple loop reading cities in.  It assumes the file is text with the last character 
		//of the line being the last letter of the city name.
		for (int cityNumber = 0; cityNumber < numCities; cityNumber++) {
			String cityName = scanner.nextLine();
			cities[cityNumber] = cityName;
			
		}
	}
		
	//Print out the list of all the cities.
	private static void printCities() {
		System.out.println(numberCities + " Cities.");
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			System.out.println(cities[cityNumber]);
		}
	}
	
	//Loop through the city array, and return the offset of the cityName parameter in that
	//array.  Return -1 if the cityName is not in the array.
	private static int getCityOffset(String cityName) {
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			if (cityName.compareTo(cities[cityNumber]) == 0) 
				return cityNumber;
		}
		return -1;
	}

	//Look through the connections and see if the city numbers are in them.  If
	//Return whether they are in the list.
	private static boolean citiesAdjacent(int city1,int city2) {
		for (int compareConnection = 0; compareConnection < numberConnections; compareConnection ++) {
			if ((connections[0][compareConnection] == city1) &&
				(connections[1][compareConnection] == city2))
				return true;
			//Need to check both ways A to B and B to A as only one connection is stored.
			else if ((connections[0][compareConnection] == city2) &&
					(connections[1][compareConnection] == city1))
					return true;		
		}
		return false;
	}

	//Read the specified number of connections.  If it crashes, it should throw to the calling catch.
	private static void readConnections(int numConnections, Scanner scanner) {
		//A simple loop reading connections in.  It assumes the file is text with the last 
		//character of the line being the last letter of the city name.  The two 
		//cities are separated by a ; with no spaces
		for (int connectionNumber = 0; connectionNumber < numConnections; connectionNumber++) {
			String connectionName = scanner.nextLine();
			String cityName[] = connectionName.split(";");
			int firstCityOffset = getCityOffset(cityName[0]);
			int secondCityOffset = getCityOffset(cityName[1]);
			connections[0][connectionNumber] = firstCityOffset;
			connections[1][connectionNumber] = secondCityOffset;
		}
	}		
	
	//Print out the full list of connections.
	private static void printConnections( ) {
		System.out.println(numberConnections + " Connections.");
		for (int connectionNumber = 0; connectionNumber < numberConnections; connectionNumber++) {
			String firstCity = cities[connections[0][connectionNumber]];
			String secondCity = cities[connections[1][connectionNumber]];
			System.out.println(firstCity + " " + secondCity);
		}
	}
			
	//Open the city file, allocate the space for the cities, and connections, then read the 
	//cities, and then read the connections.  It uses those class variables.
	private static void readCityGraph() {

		//Open the file and read it.  
		try {
		      File fileHandle = new File(cityMapFileName);
		      Scanner mapFileReader = new Scanner(fileHandle);

		      //read the number of cities and allocate variables.
		      numberCities = mapFileReader.nextInt();
		      String data = mapFileReader.nextLine();  //read the rest of the line after the int
		      cities = new String[numberCities]; //allocate the cities array
		      diseaseCubes = new int[numberCities];
		      
		      //tead the number of connections and allocate variables.
		      numberConnections = mapFileReader.nextInt();
		      data = mapFileReader.nextLine();  //read the rest of the line after the int
		      connections = new int[2][numberConnections];

		      //read cities
		      readCities(numberCities,mapFileReader);
		      //readConnections 
		      readConnections(numberConnections,mapFileReader);
		      
		      mapFileReader.close();
		    } 
		 
		 catch (FileNotFoundException e) {
		      System.out.println("An error occurred reading the city graph.");
		      e.printStackTrace();
		    }
	}
	
	private static void readPlayerDeck() {

		//Open the file and read it.  
		try {
		      File fileHandle = new File(playerDeckFileName);
		      Scanner deckFileReader = new Scanner(fileHandle);

		      //readConnections 
		      for (int deckCityNumber = 0; deckCityNumber < cities.length; deckCityNumber++) {
					String connectionName = deckFileReader.nextLine();
					String cityInfo[] = connectionName.split(";");
					String CityColor = cityInfo[1];
					//for each row in the playerdeck, get its city and associated color to store it inside the player deck array list
					String[] info  = {Integer.toString(deckCityNumber), CityColor};
					playerDeck.add(info);
				}
		      
		      deckFileReader.close();
		      
		      //shuffle playerDeck Randomly
		      Collections.shuffle(playerDeck);
		      
		      int initCardShuffleCount = 4;
		      //give each player 4 cards
		      for(int player = 0; player < NUMBER_USERS; player++) {
		    	  for(int cardCount = 0; cardCount < initCardShuffleCount; cardCount++) {
		    		 String[] cardInfo = {Integer.toString(player), playerDeck.get(0)[0], playerDeck.get(0)[1]};
		    		 playerHands.add(cardInfo);
		    		 //remove from player Deck after giving to user
		    		 playerDeck.remove(0);
		    	  } 
		      }
		    } 
		 
		 catch (FileNotFoundException e) {
		      System.out.println("An error occurred reading the Player Deck File.");
		      e.printStackTrace();
		  }
	}
	
	
	
	private static void initInfectCities() {
		ArrayList<Integer> infectedCities = new ArrayList<Integer>();
		//choose any 9 cities from 48
		for(int cityNum = 0; cityNum < 9; cityNum++) {
			int randomCity = randomGenerator.nextInt(48);
			//if not already in the infected cities, add it.
			if(!infectedCities.contains(randomCity)) {
				infectedCities.add(randomCity);
				
				//last 3, 1 cube each
				if(cityNum < 9) {
					diseaseCubes[randomCity] = 1;
				}
				//second 3, 2 cubes each
				if(cityNum < 6) {
					diseaseCubes[randomCity] = 2;
				}
				//first 3, 3 cubes each
				if(cityNum < 3) {
					diseaseCubes[randomCity] = 3;
				}
			}
			else {
				//otherwise start again
				cityNum--;
			}
			
		}
	}
	
	private static void infectCityRandom() {
		//choose random city from the player deck, add one more cube to it
		//since there is no implementation of epidemics, infection rate remains at 1.
		int randomCity = randomGenerator.nextInt(playerDeck.size());
		
		String[] infectedCity = playerDeck.get(randomCity);
		int cityId = Integer.parseInt(infectedCity[0]);
		
		diseaseCubes[cityId]++;
	}
	
	private static void buildResearchStation() {
		boolean canBuild = false;
		//check if can build (has current city card)
		for (int cards = 0; cards < playerHands.size(); cards++){
			if(Integer.parseInt(playerHands.get(cards)[0]) == currentUser && Integer.parseInt(playerHands.get(cards)[1]) == userLocation[currentUser]) { 
				String cityid = playerHands.get(cards)[1]; 
				String colorid = Integer.toString(Integer.parseInt(playerHands.get(cards)[2]) - 1); 
				//get card data, and remove from the player deck
				canBuild = true;
				playerHands.remove(cards);
				
				//append to research stations arraylist
				String[] stationInfo = {cityid, colorid};
				researchStations.add(stationInfo);
			}
		}
		
		if(canBuild) {
			System.out.println("Research Station Built at " + cities[userLocation[currentUser]]+"...");
		}
		else {
			System.out.println("Cannot Build Research Station here!! You need to have a card for this city.");
			//reimburse action if cannot build
			actionsLeft++;
		}
	}
	
	private static void viewResearchStations() {
		ArrayList<String> stations = new ArrayList<String>();
		for (String[] card : researchStations) {
			stations.add(cities[Integer.parseInt(card[0])]);
		}
		System.out.println(Arrays.deepToString(stations.toArray()));
		
	}
	
	private static ArrayList<String[]> checkCureCards() {
		
		int countBlue = 0, countYellow = 0, countBlack = 0, countRed = 0;
		
		ArrayList<String[]> blueCities = new ArrayList<String[]>();
		ArrayList<String[]> yellowCities = new ArrayList<String[]>();
		ArrayList<String[]> blackCities = new ArrayList<String[]>();
		ArrayList<String[]> redCities = new ArrayList<String[]>();
		
		for (String[] card : playerHands) {
			//keep track of the number of cities of each color.
			if(card[0].equals(Integer.toString(currentUser))) {
				System.out.println(Arrays.deepToString(card));
				//1. Blue 2.Yellow 3.Black 4.Red
				if(card[2].equals("1")) {
					countBlue++;
					blueCities.add(card);
				}
				else if(card[2].equals("2")) {
					countYellow++;
					yellowCities.add(card);
				}
				else if(card[2].equals("3")) {
					countBlack++;
					blackCities.add(card);
				}
				else if(card[2].equals("4")) {
					countRed++;
					redCities.add(card);
				}
			
			}
		}

		
		if(countBlue >= 5) {
			
			return blueCities;
		}
		if(countYellow >= 5) {
			
			return yellowCities;
		}
		if(countBlack >= 5) {
		
			return blackCities;
		}
		if(countRed >= 5) {
			
			return redCities;
		}
		
		ArrayList<String[]> empty = new ArrayList<String[]>();
		String[] emptyRes = {"-1", "-1", "-1"};
		empty.add(emptyRes);
		
		return empty;
		
	}
	
	private static boolean isStation() {
		//check if current location is a station
		for (String[] station : researchStations) {
			if(station[0].equals(Integer.toString(userLocation[currentUser]))) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean cureDisease() {
		//check if this is a STATION
		if(isStation()) {
			ArrayList<String[]> citiesToRemove = new ArrayList<String[]>();
			citiesToRemove = checkCureCards();
			//get color of disease which needs to be cured
			int cureColorCode = Integer.parseInt(citiesToRemove.get(0)[2]) - 1;
			
			//check if eligible to cure, and not already cured
			if(!citiesToRemove.get(0)[0].equals("-1") ) {
				if(!colorCured[cureColorCode]) {
				
					for (String[] cityCard : citiesToRemove) {
							int index = playerHands.indexOf(cityCard);
							String[] removedCard = playerHands.remove(index);
							System.out.println("Removed " + cities[Integer.parseInt(removedCard[1])]);
					}
					
					//if cured, make colorCured[color-id] = true
					colorCured[cureColorCode] = true;
					
					System.out.println("The " + cityColors[cureColorCode] + " Disease has been cured!");
					
					return true;
				}
				else {
					System.out.println("This color disease has already been cured!");
					return false;
				}
			}
			else {
				System.out.println("Not Enough Cards! Need 5 of one color");
				return false;
			}
			
		}
		else {
			System.out.println("Cannot Cure Disease, not at research station");
			return false;
		}
		
	}
	
	private static void drawCard() {
		//draw 2 cards every turn, after an action has been completed.
		
		String[] cardInfo1 = new String[3];
		String[] cardInfo2 = new String[3];
		
		//copy previous array into new one, 1st index to be the player id, do for both
		System.arraycopy(playerDeck.remove(0), 0, cardInfo1, 1, playerDeck.remove(0).length);
		cardInfo1[0] = String.valueOf(currentUser);
		playerHands.add(cardInfo1);
		
		System.arraycopy(playerDeck.remove(0), 0, cardInfo2, 1, playerDeck.remove(0).length);
		cardInfo2[0] = String.valueOf(currentUser);
		playerHands.add(cardInfo2);		
	
	}
	
	private static void checkCardsLimit() {
		int cardCount = 0;
		//limits user to 7 cards, prompts to discard if in excess
		ArrayList<String[]> currentUserCards = new ArrayList<String[]>();
		
		//count and store current user cards
		for (int cards = 0; cards < playerHands.size(); cards++){
			if(Integer.parseInt(playerHands.get(cards)[0]) == currentUser) { 
				cardCount++;
				currentUserCards.add(playerHands.get(cards));
			}
		}
		
		//limit player hand to 7 cards
		if(cardCount > 7) {
			System.out.println("You need to discard " + (cardCount - 7) + " cards from your pile");
			
			//number of cards to remove to make it 7
			int cardsToRemove = cardCount - 7;
			
			//prompt user to discard if excess number of cards
			for (int card = 0; card < cardsToRemove; card++) {
				printCards();
				System.out.println("Remove by typing its name: ");
				String userInput = shellInput.nextLine();
				int cityToRemove = getCityOffset(userInput);
				
				if(cityToRemove != -1) {
					//if city is eligible, remove from player hand
					for (String[] cards : currentUserCards) {
						if(cities[Integer.parseInt(cards[1])].equals(cities[cityToRemove])) {
							playerHands.remove(cards);
							System.out.println(cities[cityToRemove]+" Removed!");
						}
					}
				}
				else{
					System.out.println("Incorrect Input! Try Again!");
					card--;
				}
			}
		}
		
	}
	
	private static void shareCard() {
		//if both users at the same location, allow sharing of cards if one has it.
		if(userLocation[0] == userLocation[1]) {
			String[] card = null;
			
			for (String[] handCard : playerHands) {
				if(handCard[1].equals(Integer.toString(userLocation[0]))) {
					card = handCard;
				}
			}
			
			//switch player id to other player/swap if any one user has current city card
			if(card != null) {
				if(card[0].equals("0")) {				
					card[0] = "1";
					System.out.println(cities[Integer.parseInt(card[1])] +  " card transferred from " + userNames[Integer.parseInt(card[0])]);
				}
				else if(card[1].equals("1")) {
					card[0] = "0";
					System.out.println(cities[Integer.parseInt(card[1])] +  " card transferred from " + userNames[Integer.parseInt(card[0])]);
				}
			}
			else {
				System.out.println("None of the players have the current city card!");
			}
		}
		else {
			System.out.println("Both Players have to be at the same location to share a card!");
		}
	}
	
	private static void printCards() {
		//give each player 4 cards from the bottom(top, if inverted)
		
			for (int cards = 0; cards < playerHands.size(); cards++){
				if(Integer.parseInt(playerHands.get(cards)[0]) == currentUser) { 
					String cityname = cities[Integer.parseInt(playerHands.get(cards)[1])]; 
					String color = cityColors[Integer.parseInt(playerHands.get(cards)[2]) - 1]; 
					System.out.println("You have " + cityname + " which is " + color);
				}
			}
		
		System.out.println("\n");
		
	}
	
	//check hand & add reseach stations
	private static void printInfectedCities() {
		for (int cityNumber = 0;  cityNumber < numberCities; cityNumber ++) {
			if (diseaseCubes[cityNumber] > 0) {
				System.out.println(cities[cityNumber] + " has " + diseaseCubes[cityNumber] + " cubes.");
			}
		}
	}
	
	private static void currentStatus() {
		System.out.println("\n(" + playerDeck.size() + ") Cards Remain in the Player Deck");
		
		System.out.println("-----");
		for (int color = 0; color < colorCured.length; color++) {
			if(colorCured[color]) {
				System.out.println(cityColors[color] + " disease is cured");
			}
			else {
				System.out.println(cityColors[color] + " disease is not cured");
			}
		}
		
		System.out.println("-----");
		System.out.println("Research Stations At: ");
		viewResearchStations();
		System.out.println("-----");
	}
	
	private static boolean gameState() {
		
		
		//calculate total cubes on the map
		int totalCubes = 0;
		for (int i = 0; i < diseaseCubes.length; i++) {
			totalCubes += diseaseCubes[i];
		}
		
		//if total is zero & all diseases cured, hence pandemic over
		if(colorCured[0] && colorCured[1] && colorCured[2] && colorCured[3] && totalCubes == 0) {
			System.out.println("All Diseases Cured & Cubes Removed!! YOU WON!!");
			return true;
		}
		
		//otherwise if there are not enough player cards to distribute then end game. You LOST!!
		if(playerDeck.size() == 0 && playerHands.size() == 0) {
			System.out.println("Not Enough Player Cards!! You LOST!!");
			return true;
		}
		
		return false;
		
	}
	
	private static void initResearchStations() {
		//initialising research stations, adding at Atlanta (0,0)
		String[] atlanta_research_station = {"0","0"};
		researchStations.add(atlanta_research_station);
	}
	
	private static void initGame() {
		readCityGraph();
		readPlayerDeck();
		
		initInfectCities();
		initResearchStations();
		System.out.println("\n"+userNames[currentUser] + ", Your Current Location is: " + cities[userLocation[currentUser]]);
		printActions();
	}
	
	
	//The main function of the program.  Enter and exit from here.
	//It is a simple getInput processInput loop until the game is over.  
	public static void main(String[] args) {
		boolean gameDone = false;
		initGame();
		while (!gameDone && !gameState()) {
			int userInput = getUserInput();	
			gameDone = processUserCommand(userInput);
		}
		
		System.out.println("Game Over!! ");
	}

	
}