/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException 
    {  
        // Static getInstance method is called with hashing SHA  
        MessageDigest md = MessageDigest.getInstance("SHA-256");  
  
        // digest() method called  
        // to calculate message digest of an input  
        // and return array of byte 
        return md.digest(input.getBytes(StandardCharsets.UTF_8));  
    } 
    
    public static String toHexString(byte[] hash) 
    { 
        // Convert byte array into signum representation  
        BigInteger number = new BigInteger(1, hash);  
  
        // Convert message digest into hex value  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
  
        // Pad with leading zeros 
        while (hexString.length() < 32)  
        {  
            hexString.insert(0, '0');  
        }  
  
        return hexString.toString();  
    } 
	// DONE
	public static void AddUser(Ticketmaster esql){//1
		System.out.println("Plase enter the necessary information\n");

		String firstName, lastName, email, pw, pw2;
		int phoneNum;
		// first name, last name, email, phone
		
		// ***get first name from user
		do {
			System.out.print("\t	Enter first name: ");
			try {
				firstName = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***get last name from user
		do {
			System.out.print("\t	Enter last name: ");
			try {
				lastName = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***get email from user
		do {
			System.out.print("\t	Enter email: ");
			try {
				email = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***get phone number from user
		do {
			System.out.print("\t	Enter phone number: ");
			try {
				phoneNum = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***get password from user
		do {
			System.out.print("\t	Enter password: ");
			try {
				pw = in.readLine();
				pw2 = toHexString(getSHA(pw));
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		
		String SQL = "INSERT INTO Users VALUES(\'" + email + "\', \'" + lastName + "\', \'" + firstName + "\', \'" + String.valueOf(phoneNum) + "\', \'" + pw2 + "\');";
		try {
			esql.executeUpdate(SQL);
		}catch(SQLException e) {
			System.err.println(e.getMessage());
		}

	}
	// DONE
	public static void AddBooking(Ticketmaster esql){//2
		System.out.println("Please input the necessary information: ");
		int bid, seat, sid=0, sid_query ;
		String seats, email="", status;  
		// prompt user for status, dateTime, seats, show id
		// existing and valid user, show, movie,seating, theater, and cinema

		// ***Check valid user
		int counter = 0;
		while(counter <= 0) {
			String user_query = "SELECT * FROM Users WHERE email = '";
			do{
				try{
					System.out.print("\tEnter an existing email to add a booking: ");
					email = in.readLine();
					System.out.println(email);
					break;
				}catch (Exception e) {
					System.out.println("Invalid input");
					continue;
				}
			}while(true);
			// email += "';";
			user_query += (email + "';");
			try{
				counter = esql.executeQueryAndPrintResult(user_query);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
			if(counter <= 0) {
				System.out.println("Your email does not exist");
			}
		}
		// ***Check valid Show + ShowID
		counter = 0;
		while(counter <= 0) {
			String show = "SELECT * FROM Shows WHERE sid = ";
			do{
				try{
					System.out.print("\tEnter an existing show id to add a booking: ");
					sid = Integer.parseInt(in.readLine());
					show += String.valueOf(sid);
					break;
				} catch(Exception e) {
					System.out.println("Invalid input: ");
					continue;
				}
			}while(true);
			try{
				counter = esql.executeQuery(show);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
			if(counter <= 0) {
				System.out.println("Your show id does not exist");
			}
		}
		// ***Check valid Movie 
		counter = 0;
		while(counter <= 0) {
			String movie = "SELECT * FROM Movies WHERE mvid = ";
			int mvid; 
			do {
				try{
					System.out.print("\tEnter an existing movie id to add a booking: ");
					mvid = Integer.parseInt(in.readLine());
					movie += String.valueOf(mvid);
					break; 
				} catch(Exception e) {
					System.out.println("Invalid input: ");
					continue;
				}
			}while(true);
			try{
				counter = esql.executeQuery(movie);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
			if(counter <= 0) {
				System.out.println("Invalid input: ");
				continue;
			}
		}
		// ***Check valid Seating (Cinema + Show)
		counter = 0;
		while(counter <= 0) {
			String cinemaSeat = "SELECT * FROM CinemaSeats WHERE csid = ";
			int csid; 
			do {
				try{
					System.out.print("\tEnter an existing Cinema seat to add a booking: ");
					csid = Integer.parseInt(in.readLine());
					cinemaSeat += String.valueOf(csid);
					break;
				} catch(Exception e) {
					System.out.println("Invalid input");
					continue;
				}
			}while(true);
			try{
				counter = esql.executeQuery(cinemaSeat);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
			if(counter <= 0) {
				System.out.println("Invalid input: ");
				continue; 
			}
		}
		counter = 0;
		while(counter <= 0) {
			String showSeat = "SELECT * FROM ShowSeats WHERE ssid = ";
			int ssid; 
			do {
				try{
					System.out.print("\tEnter an existing Show seat to add a booking: ");
					ssid = Integer.parseInt(in.readLine());
					showSeat += String.valueOf(ssid);
					break;
				} catch(Exception e) {
					System.out.println("Invalid input");
					continue; 
				}
			}while(true);
			try{
				counter = esql.executeQuery(showSeat);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
			if(counter <= 0) {
				System.out.println("Invalid input: ");
				continue; 
			}
		}
		// ***Check valid Theater
		counter = 0;
		while(counter <= 0) {
			String theater = "SELECT * FROM Theaters WHERE tid = ";
			int tid;
			do {
				try{
					System.out.print("\tEnter an existing Theater to add a booking: ");
					tid = Integer.parseInt(in.readLine());
					theater += String.valueOf(tid);
					break;
				} catch(Exception e) {
					System.out.println("Invalid input");
					continue;
				}
			}while(true);
			try{
				counter = esql.executeQuery(theater);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
			if(counter <= 0) {
				System.out.println("Invalid input");
				continue;
			}
		}
		// ***Check valid Cinema
		counter = 0;
		while(counter <= 0) {
			String cinema = "SELECT * FROM Cinemas WHERE cid = ";
			int cid; 
			do {
				try{
					System.out.print("\tEnter an existing Cinema to add a booking: ");
					cid = Integer.parseInt(in.readLine());
					cinema += String.valueOf(cid);
					break;
				} catch(Exception e) {
					System.out.println("Invalid input");
					continue;
				}
			}while(true);
			try{
				counter = esql.executeQuery(cinema);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
			if(counter <= 0) {
				System.out.println("Invalid input");
				continue;
			}
		}
		
		do {
			System.out.print("\t	Enter booking id: ");
			try {
				bid = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get status
		do {
			System.out.print("\t	Enter status: ");
			try {
				status = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get dateTime
		String dateTime;
		do {
			System.out.print("\t	Enter dateTime: ");
			try {
				dateTime = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get seats
		int getSeat;
		do {
			System.out.print("\t	Enter how many seats: ");
			try {
				getSeat = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		String query = "INSERT INTO Bookings VALUES(" + bid + ", \'" + status + "\', \'" + dateTime + "\', " + getSeat + ", " + sid + ", \'" + email + "\');";
        
		try {
			esql.executeUpdate(query);
		}catch(SQLException e) {
			System.err.println(e.getMessage());
		}
		
	}
	// DONE
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		int mvid, duration, sid;
		String title, country, description, lang, genre, rdate, sdate, sttime, edtime;
		// ***Get mvid
		do {
			System.out.print("\t	Enter movie id: ");
			try {
				mvid = Integer.parseInt(in.readLine());
				break;
			}catch(Exception e){
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get tile
		do {
			System.out.print("\t	Enter title: ");
			try {
				title = in.readLine();
				break;
			}catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get rdate
		do {
			System.out.print("\t	Enter release date: ");
			try {
				rdate = in.readLine();
				break;
			}catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get country
		do {
			System.out.print("\t	Enter country of origin: ");
			try {
				country = in.readLine();
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get description
		do {
			System.out.print("\t	Enter description: ");
			try {
				description = in.readLine();
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get duration
		do {
			System.out.print("\t	Enter duration (in seconds): ");
			try {
				duration = Integer.parseInt(in.readLine());
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get lang
		do {
			System.out.print("\t	Enter language of movie: ");
			try {
				lang = in.readLine();
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get genre
		do {
			System.out.print("\t	Enter genre: ");
			try {
				genre = in.readLine();
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);

		// ***Get sid
		do {
			System.out.print("\t	Enter show id: ");
			try {
				sid = Integer.parseInt(in.readLine());
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get sdate
		do {
			System.out.print("\t	Enter show date: ");
			try {
				sdate = in.readLine();
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get sttime
		do {
			System.out.print("\t	Enter start time: ");
			try {
				sttime = in.readLine();
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		// ***Get edtime
		do {
			System.out.print("\t	Enter end time: ");
			try {
				edtime = in.readLine();
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		int tid;
		do {
			System.out.print("\t	Enter theater id: ");
			try {
				tid = Integer.parseInt(in.readLine());
				break;
			}
			catch(Exception e) {
			System.out.println("Invalid input");
			continue;
			}
		}while(true);

		// ***Verify
		String movieQuery = "INSERT INTO MOVIES VALUES(" + mvid + ", \'" + title + "\' ,\'" + rdate + "\', \'" + country + "\', \'" + description + "\', " + duration + ", \'" + lang + "\', \'" + genre + "\')";
		try {
			esql.executeUpdate(movieQuery);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
		String showQuery = "INSERT INTO Shows VALUES(" + sid + ", " + mvid + ", \'" + sdate + "\', \'" + sttime + "\', \'" + edtime + "\')";
		try {
			esql.executeUpdate(showQuery);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
		String playQuery = "INSERT INTO Plays VALUES(" + sid + ", " + tid + ")";
		try {
			esql.executeUpdate(playQuery);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
		

	}
	// DONE
	public static void CancelPendingBookings(Ticketmaster esql){//4
		String cancelPendingBookingsQuery = "UPDATE Bookings SET status = 'Cancelled' WHERE status = 'Pending'";
		try{
			esql.executeUpdate(cancelPendingBookingsQuery);
		} catch(SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	// TODO
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		
		System.out.println("Please enter the following information: ");
		int bid;
		do {
			System.out.print("\t	Enter booking id: ");
			try {
				bid = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		
		String query1 = "SELECT A.ssid, A.price \nFROM ShowSeats A, Bookings B\nWHERE A.bid = B.bid;";
		List<List<String>> str = new ArrayList<List<String>>();
		try{
			str = esql.executeQueryAndReturnResult(query1);
		}catch(SQLException e) {
			System.err.println(e.getMessage());
		}
		System.out.println("List of show seat ids and prices that you currently booked: \n" + str);
		
		String query2 = "SELECT A.ssid, A.price \nFROM Bookings B, Shows S, ShowSeats A\nWHERE B.bid = " + bid + " AND B.sid = S.sid AND A.sid = S.sid AND A.bid IS NULL;";
		List<List<String>> str2 = new ArrayList<>();
		try{
			str2 = esql.executeQueryAndReturnResult(query2);
		}catch(SQLException e) {
			System.err.println(e.getMessage());
		}
		System.out.println("List of show seat ids and prices that are available: \n" + str2);
		
		int curr_ssid, new_ssid; 
		do {
			System.out.print("\t	Enter the original seat (ssid) that you want to change: ");
			try {
				curr_ssid = Integer.parseInt(in.readLine());
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		do {
			System.out.print("\t	Enter new seat (ssid)to change to: ");
			try {
				new_ssid = Integer.parseInt(in.readLine());
				break;
			} catch(Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		
		String curr_seat_price = "";
		for(List<String> k : str) {
			if(k.contains(String.valueOf(curr_ssid))) {
				curr_seat_price = k.get(1);
			}
		}
		// System.out.println(curr_seat_price);
		
		String new_seat_price = "";
		for(List<String> l : str2) {
			if(l.contains(String.valueOf(new_ssid))) {
				new_seat_price = l.get(1);
			}
		}
		
		
		String query3, query4;
		if(curr_seat_price.equals(new_seat_price)) {
			query3 = "UPDATE ShowSeats SET bid = NULL WHERE ssid = " + curr_ssid + ";";
			try {
				esql.executeUpdate(query3);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
			
			query4 = "UPDATE ShowSeats SET bid = " + bid + " WHERE ssid = " + new_ssid + ";";
			try {
				esql.executeUpdate(query4);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
			
		} else {
			System.out.println("ERROR: Prices don't match");
		}
		
		

	}
	// DONE
	public static void RemovePayment(Ticketmaster esql){//6
		 int bid; 
		do {
			System.out.print("\t	Enter booking id: ");
			try {
				bid = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		String removePaymentQuery = "UPDATE Bookings SET status = 'Cancelled' WHERE bid = " + bid + ";";
		try{
			esql.executeUpdate(removePaymentQuery);
		} catch(SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	// DONE
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		String query = "SELECT bid FROM Bookings WHERE status = 'Cancelled';";
		List<List<String>> arr = new ArrayList<>();
		try {
			arr = esql.executeQueryAndReturnResult(query);
		} catch(SQLException e) {
			System.err.println(e.getMessage());
		}
		for(List<String> i : arr) {
			String str = "DELETE FROM Payments WHERE bid = " + i.get(0) + ";";
			try{
				esql.executeUpdate(str);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
		}
		
		String query2 = "SELECT bid FROM Bookings WHERE status = 'Cancelled';";
		List<List<String>> arr1 = new ArrayList<>();
		try {
			arr1 = esql.executeQueryAndReturnResult(query2);
		} catch(SQLException e) {
			System.err.println(e.getMessage());
		}
		for(List<String> j : arr1) {
			String qry = "DELETE FROM ShowSeats WHERE bid = " + j.get(0) + ";";
			try {
				esql.executeUpdate(qry);
			} catch(SQLException e) {
				System.err.println(e.getMessage());
			}
		}
		
		String deleteUserQuery = "DELETE FROM Bookings WHERE status = 'Cancelled';";
		try{
			esql.executeUpdate(deleteUserQuery);
		} catch(SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
	    // Remove all Shows on a given date at a speciﬁc Cinema.
	    //If there are any bookings on this day, 
	    //they can be cancelled using the “Remove a Payment” method
	    //implemented above. 
	    //Use case: the cinema is closed for a holiday
	    //or some unforeseen circumstance.
	    
	    //the date + Cinema cid is what determines what show(s) 
	    //get deleted. 
	       
	    //helper sub-function: Enter cinema cid - Will output all
	    //shows that are showing at this cinema, output displayed as 
	    //Show's sid, and date. 
	    
	    //Helper function:
	    int userCidInput = 0;
	    String showDate = "";
	    
	    //System.out.print("Enter the cinema ID: ");
	    do{
	        System.out.print("Enter the cinema ID as int: ");
		try { // read the integer, parse it and break.
		        userCidInput = Integer.parseInt(in.readLine());
			    break;
		    }catch (Exception e) {
			    System.out.println("Your input is invalid!");
			    continue;
		    }//end try
	    }while (true);
	    
	    String queryStatement = "SELECT S.sid, S.sdate\nFROM Shows S, Cinemas C, Theaters T, Plays P\nWHERE C.cid = " + Integer.toString(userCidInput) + " AND T.cid = C.cid AND T.tid = P.tid AND P.sid = S.sid;";
	    
	    //Change 6/14 4am: Changing to list so as to check size of rows. 
	    //If size == 0, not possible to remove shows, as there are none
	    //to begin with.
	    List<List<String>> listofPossible = new ArrayList<>();
	    try{
	    	
	        //esql.executeQueryAndPrintResult(queryStatement);
	        listofPossible = esql.executeQueryAndReturnResult(queryStatement);
		
	    }catch(SQLException e){
	    	System.out.println("SQL Error: Getting listofPossible");
	    	System.err.println(e.getMessage());
	    }
	    if(listofPossible.size() == 0){
				System.out.println("This cinema currently has no shows. Returning to main menu...");
				return;
		}
		
		System.out.println("Shows:\nsid\tsdate");
		for(List<String> possible: listofPossible){
				System.out.println(possible.get(0) + "   " + possible.get(1));
				
		}
		//System.out.print("\n");
	    
	    //now have cid. Select date.
	    boolean nonvalidDate = true;
	    do{
			System.out.print("Enter show start date (Format year-month-day xxxx-xx-xx)[Enter Q to quit and return to menu]: ");
			try{
				showDate = in.readLine();
			}catch(Exception e){
				System.out.println("Wrong input: ");
				continue;
			}
		    if(showDate.matches("\\d{4}-\\d{2}-\\d{2}")){
		        //if statement to signify that the pattern matches
		        //now check if valid date
		        for(List<String> i : listofPossible){
					if(i.get(1).matches(showDate)){
						nonvalidDate = false;
					}
				}
				System.out.println("Entered date does not match the available options.");
				continue;
	    	    
		        //break;
		    }
		    else if(showDate.equals("Q")){
					return;
			}
		    else{
		        System.out.println("Patten does not match! Pattern is xxxx-xx-xx (year-month-day)");
		        continue;
		    }
		    //break;

	    }while(nonvalidDate);
	    
	    //now have Shows sdate and Cinemas cid.
	    //use to delete all shows on particular date
	    
	    //Steps to make
	    //For each To-be-deleted Show:
	    //1. For each Bookings pointing to a to-be-deleted Show, set to Cancelled.
	    //2. Delete Payment that point to a Soon to be deleted Booking (These Bookings point to a soon
	    //to be deleted Show.). 
	    //3. Delete the Booking using functionality of func. 6 & 7 above.
	    //4. Delete Show Seatings that are pointing to soon to be deleted
	    //Show.
	    // May need to delete entry in Plays that referencees the particular Show
	    //5. Delete the Show.
	    
	    List<List<String>> listofShowsSid = new ArrayList<>();
	    
	    queryStatement = "SELECT DISTINCT S.sid\nFROM Shows S, Cinemas C, Theaters T, Plays P\nWHERE C.cid = " + Integer.toString(userCidInput) + " AND T.cid = C.cid AND T.tid = P.tid AND P.sid = S.sid AND S.sdate = '" + showDate + "';";
	    
	    try{
	    	
	        listofShowsSid = esql.executeQueryAndReturnResult(queryStatement);
		
	    }catch(SQLException e){
	    	System.out.println("SQL Error: ");
	    	System.err.println(e.getMessage());
	    }
	    
	    //now have a list of (distinct) shows sids that need to be deleted.
	    //Loop through list to delete each show.
	    List<List<String>> listofBookingsBid = new ArrayList<>();
	    String bookingQuery = "";
	    
	    if(listofShowsSid.size() > 0){
			//DEBUG statement
			System.out.println("DEBUG: Number of shows on this date at this cinema: " + Integer.toString(listofShowsSid.size()));
			for(List<String> show : listofShowsSid){
			    //debug statement
			    System.out.println("DEBUG: Sid of soon to be deleted Show: " + show.get(0));
			    
			    //Grab ALL Bookings pointing to this particular Show
			    queryStatement = "SELECT B.bid\nFROM Bookings B, Shows S\nWHERE B.sid = S.sid AND S.sid = " +  show.get(0) + ";";
			    
			    try{
	    	
					listofBookingsBid = esql.executeQueryAndReturnResult(queryStatement);
		
				}catch(SQLException e){
					System.out.println("SQL ListofBookings Error: ");
					System.err.println(e.getMessage());
				}
				//now have list of all bookings that point to to-be-deleted show
				if(listofBookingsBid.size() > 0){
					
					queryStatement = "UPDATE Bookings SET status = 'Cancelled' WHERE sid = " + show.get(0) + ";";
					try{
						esql.executeUpdate(queryStatement);
						
					} catch(SQLException e) {
						System.out.println("SQL UPDATE Bookings Error:");
						System.err.println(e.getMessage());
					}
					
					//End Attempt 2
					
					//Error occurs because multiple rows returned with (SELECT B.bid FROM Bookings B...)
					//SOLN Attempt 1, Fit in for loop, 
					//List <- Booking bids where status = 'CANCELLED' AND sid = show.get(0)
					for(List<String> booking : listofBookingsBid){
							bookingQuery = "DELETE FROM Payments WHERE bid = " + booking.get(0);
							try{
								esql.executeUpdate(bookingQuery);
						
							} catch(SQLException e) {
								System.out.println("SQL DELETE Payments Error:");
								System.err.println(e.getMessage());
							}
					}
					
					/*
					//now delete these Bookings' Payments
					bookingQuery = "DELETE FROM Payments WHERE bid = (SELECT B.bid FROM Bookings B, Payments P WHERE B.status = 'Cancelled' AND B.bid = P.bid);";
					try{
						esql.executeUpdate(bookingQuery);
						
					} catch(SQLException e) {
						System.out.println("SQL DELETE Payments Error:");
						System.err.println(e.getMessage());
					}
					*/
					//now delete the bookings that have status cancelled.
					bookingQuery = "DELETE FROM Bookings WHERE status = 'Cancelled' AND sid = " + show.get(0) + ";";
					try{
						esql.executeUpdate(bookingQuery);
					} catch(SQLException e) {
						System.err.println(e.getMessage());
						System.out.println("SQL DELETE Bookings Error:");
					}
					
				}
				//done deleting Bookings, now delete Show Seatings
				
			    queryStatement = "DELETE FROM ShowSeats\nWHERE sid = (SELECT sid\nFROM Shows\nWHERE sid = " + show.get(0) + ");";
			    try{
						esql.executeUpdate(queryStatement);
					} catch(SQLException e) {
						System.err.println(e.getMessage());
						System.out.println("SQL DELETE ShowSeats Error:");
				}
				//must delete Plays entry as well
				
				queryStatement = "DELETE FROM Plays\nWHERE sid = " + show.get(0) + ";";
				try{
					esql.executeUpdate(queryStatement);
				} catch(SQLException e) {
					System.err.println(e.getMessage());
					System.out.println("SQL DELETE Plays Error:");
				}
				
				//now can delete Show
				queryStatement = "DELETE FROM Shows\n WHERE sid = " + show.get(0) + ";";
				try{
						esql.executeUpdate(queryStatement);
					} catch(SQLException e) {
						System.err.println(e.getMessage());
						System.out.println("SQL DELETE Shows Error:");
				}
			    
			}
			
		}
		
	           
	           
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		
	    String queryStatement = "";
	    int cinId = 0;
	    int showId = 0;
	    do{
	        System.out.print("Enter cinema ID: ");
			try { // read the integer, parse it and break.
		        cinId = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
		    }//end try
	    }while (true);
		
	    //use cinId to get cid in Theaters that matches with a cid in Cinemas
	    do{
	        System.out.print("Enter show ID: ");
			try { // read the integer, parse it and break.
				showId = Integer.parseInt(in.readLine());
				break;
		    }catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
		    }//end try
	    }while (true);
	    
	    try{
	    	queryStatement = "select C.tid, C.tname, C.cid\nfrom Shows A, Plays B, Theaters C\nwhere C.tid = B.tid AND B.sid = A.sid AND A.sid = " + showId + " AND C.cid = ( select D.cid\nfrom Cinemas D\nwhere D.cid = " + cinId + " );";
	        esql.executeQueryAndPrintResult(queryStatement);
		
	    }catch(SQLException e){
	    	System.out.println("SQL Error");
	    }

	

	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    String timeStr = "";
	    String dateStr = "";
	    String queryStatement = "";
	    do{
			System.out.print("Enter time: ");
			try{
				timeStr = br.readLine();
				if(timeStr.matches("\\d{2}:\\d{2}:\\d{2}")){
		        //if statement to signify that the pattern matches
	    	        
			
		        //System.out.println(queryStatement);
		        //esql.executeQueryAndPrintResult(queryStatement);
					break;

				}
				else{
		        System.out.println("Patten does not match! Pattern is 00:00:00 (hr:min:sec)");
					continue;
				}
		    //break;
			}catch(Exception e){
				System.out.println("Wrong input: ");
				continue;
			}

	    }while(true);

	    do{
		System.out.print("Enter date: ");
			try{
				dateStr = br.readLine();
				if(dateStr.matches("\\d{4}-\\d{2}-\\d{2}")){
					//if statement to signify that the pattern matches
	    	        
			
					//System.out.println(queryStatement);
					//esql.executeQueryAndPrintResult(queryStatement);
					break;

				}
				else{
					System.out.println("Patten does not match! Pattern is xxxx-xx-xx (year-month-day)");
					continue;
				}
				//break;
			}catch(Exception e){
				System.out.println("Wrong input: ");
				continue;
			}

		}while(true);

	    //now have values timeStr and dateStr
	    try{
	    	queryStatement = "select *\n from Shows A\nwhere A.sttime = '" +  timeStr + "' AND A.sdate = '" + dateStr + "';";
	        esql.executeQueryAndPrintResult(queryStatement);
		
	    }catch(SQLException e){
	    	System.out.println("SQL Error");
	    }

		
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		//
		String queryStatement = "";
		try{
		    queryStatement = "SELECT title\nFROM Movies\n WHERE title ILIKE '%LOVE%' AND rdate > '2010-12-31';";
		    esql.executeQueryAndPrintResult(queryStatement);

		}catch(SQLException e){
		    System.out.println("SQL Error");
		}
		
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//
		String queryStatement = "";
		try{
		    queryStatement = "SELECT A.fname, A.lname, A.email\nFROM Users A, Bookings B\nWHERE B.status = 'Pending' AND B.email = A.email;";
		    esql.executeQueryAndPrintResult(queryStatement);

		}catch(SQLException e){
		    System.out.println("SQL Error");
		}
		

	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		//User gives cid. This is queried to check if this is a valid ID.
		//User input will be checked against list to see if cid eneterd is a valid
		//cid. Afterward, ask user for start + end dates. Make sure Start Date <= End
		//Date.
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	     
	    String queryStatement = "";
	    String userSelection = "";
	    int cinemaIdInt = 0;
	    String cinemaIdStr = "";
	    String startDate = "";
	    String endDate = "";
	   
        List<List<String>> listofCinemas;
        queryStatement = "SELECT cid, cname\nFROM Cinemas;";
	    try{
	        
	        
	        //contains the list of valid cid's from all available cinemas.
			listofCinemas = esql.executeQueryAndReturnResult(queryStatement);
	        
                //boolean validCidSelection = false;

	        //while(!validCidSelection){
	    }catch(SQLException e){
	        System.out.println("Exception: SQL Error");
		return;
	    }
	    System.out.println("Cinemas:\ncid\tcname\n-------");
	    
	    int j = 0;
	    if(listofCinemas.size() > 10){
			j = 10;
		}
		else{
				j = listofCinemas.size();
		}
		/*
	    for(List<String> element : listofCinemas){
			System.out.println(element.get(0) + "   " + element.get(1));
	    }
	    */
	    for(int k = 0; k < j; ++k){
				System.out.println(listofCinemas.get(k).get(0) + "   " + listofCinemas.get(k).get(1));
		} 

	    boolean invalidCidSelection = true;
	    do{
	        System.out.print("Enter cinema ID: ");
		try { // read the integer, parse it and break.
			cinemaIdInt = Integer.parseInt(in.readLine());
                        //after this statement, we are confident that the
                        //input is in integer format, so we can covert
                        //back to string for comparison.
                        
			cinemaIdStr = Integer.toString(cinemaIdInt);
 	            
            for(List<String> element : listofCinemas){
	                    //System.out.println("New Entry: ");
				for(String val : element){
	                        //System.out.println(val);
					if(val.contains(cinemaIdStr)){
						invalidCidSelection = false;
		            }
	            }
	        }
	                
		        
			//break;
		    }catch (Exception e) {
				System.out.println("Your input is invalid! Must enter as integer");
				continue;
		    }//end try
	    }while (invalidCidSelection);
	    
	    
	    //System.out.println("Sample of shows, movie titles that are in ")

	    //now have a valid cid.
	    
            //now get start and end dates

	    do{
			System.out.print("Enter show start date (Format year-month-day xxxx-xx-xx): ");
		try{
		    startDate = br.readLine();
		    if(startDate.matches("\\d{4}-\\d{2}-\\d{2}")){
		        //if statement to signify that the pattern matches
	    	        
			
		        //System.out.println(queryStatement);
		        //esql.executeQueryAndPrintResult(queryStatement);
				break;

		    }
		    else{
		        System.out.println("Patten does not match! Pattern is xxxx-xx-xx (year-month-day)");
		        continue;
		    }
		    //break;
		}catch(Exception e){
		    System.out.println("Wrong input: ");
		    continue;
		}

	    }while(true);


	    do{
			System.out.print("Enter show end date (Format year-month-day xxxx-xx-xx): ");
			try{
				endDate = br.readLine();
				if(endDate.matches("\\d{4}-\\d{2}-\\d{2}")){
		        //if statement to signify that the pattern matches
	    	        
			
		        //check if this is a date later than startDate
					if((Integer.parseInt(endDate.substring(0, 4)) > Integer.parseInt(startDate.substring(0, 4))) || (Integer.parseInt(endDate.substring(5, 7)) > Integer.parseInt(startDate.substring(5, 7))) || (Integer.parseInt(endDate.substring(8, 10)) >= Integer.parseInt(startDate.substring(8, 10)))){
			    
						break;
					}
					else{
						System.out.println("Error: Given end date is a date earlier than the start date.");
					}
		        //break;

				}	
				else{
					System.out.println("Patten does not match! Pattern is xxxx-xx-xx (year-month-day)");
					continue;
				}
		    //break;
			}catch(Exception e){
				System.out.println("Wrong input: ");
				continue;
			}

	    }while(true);


            //now have valid cid, start, and end dates.
            //need to get  movie title
	    do{

	        try{
		    //In this iteration of function, going to make assumption that user will enter movie title in exact format.
		    System.out.print("Enter movie title: ");
		    userSelection = br.readLine();  
		    break;

	        }catch(Exception e){
		    System.out.println("Wrong Input: ");
		    continue;
	        }
	    }while(true);

	    //make query
	    try{
			queryStatement = "SELECT A.title, A.duration, B.sdate, B.sttime, B.edtime\nFROM Movies A, Shows B, Theaters T, Plays P\nWHERE '" + cinemaIdStr + "' = T.cid AND T.tid = P.tid AND P.sid = B.sid AND B.sdate >= '" + startDate + "' AND B.sdate <= '" + endDate + "' AND B.mvid = A.mvid AND A.title = '" + userSelection + "';";

			esql.executeQueryAndPrintResult(queryStatement);
	    }
        catch(SQLException e){
			System.out.println("SQL error.");
		}
		
		
	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    String userEmail = "";
	    String query = "";
	
	    do{

	        try{
		    //In this iteration of function, going to make assumption that user will enter movie in exact format.
				System.out.print("Enter user email: ");
				userEmail = br.readLine();
				if(userEmail.matches("[\\w]+[@][\\w]+\\.[\\w]+")){
	                break;
				} 
		    //break;
				System.out.println("Not in expected format. Enter in format: [letters or numbers]@[domain]");

	        }catch(Exception e){
				System.out.println("Wrong Input: ");
				continue;
	        }
	    }while(true);
                
	   //now have email
	    query = "SELECT A.title, B.sdate, B.sttime, C.tname, S.sno\nFROM Movies A, Shows B, Theaters C, Plays P, CinemaSeats S, Bookings T, Users U\nWHERE U.email = '" + userEmail + "' AND U.email = T.email AND T.sid = B.sid AND A.mvid = B.mvid AND B.sid = P.sid AND P.tid = C.tid AND C.tid = S.tid;";
            
	    try{
	        esql.executeQueryAndPrintResult(query);
	    }
	    catch(SQLException e){
	        System.out.println("SQL error.");
	    }
        		
	}
	
}

