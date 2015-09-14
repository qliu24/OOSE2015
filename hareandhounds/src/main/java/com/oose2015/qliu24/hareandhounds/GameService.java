//-------------------------------------------------------------------------------------------------------------//
// Code based on a tutorial by Shekhar Gulati of SparkJava at
// https://blog.openshift.com/developing-single-page-web-applications-using-java-8-spark-mongodb-and-angularjs/
//-------------------------------------------------------------------------------------------------------------//

package com.oose2015.qliu24.hareandhounds;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameService {

    private Sql2o db;

    private final Logger logger = LoggerFactory.getLogger(GameService.class);

    /**
     * Construct the model with a pre-defined datasource. The current implementation
     * also ensures that the DB schema is created if necessary.
     *
     * @param dataSource
     */
    public GameService(DataSource dataSource) throws GameServiceException {
        db = new Sql2o(dataSource);

        //Create the schema for the database if necessary. This allows this
        //program to mostly self-contained. But this is not always what you want;
        //sometimes you want to create the schema externally via a script.
        try (Connection conn = db.open()) {
            String sql = "CREATE TABLE IF NOT EXISTS games (game_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "                                 hare_id INTEGER, hounds_id INTEGER, board TEXT, " +
            		     "                                 state TEXT)";
            String sql2 = "CREATE TABLE IF NOT EXISTS boardHistory (game_id INTEGER, board TEXT)";
            conn.createQuery(sql).executeUpdate();
            conn.createQuery(sql2).executeUpdate();
        } catch(Sql2oException ex) {
            logger.error("Failed to create schema at startup", ex);
            throw new GameServiceException("Failed to create schema at startup", ex);
        }
    }

    /**
     * Create a new Game entry to the "games" table with corresponding piece type joined the game
     * @param body The Json object with "pieceType: <type>"
     * @return A map of <gameId: <gameId>, playerId: <playerId>, pieceType: <type>> of the created game
     */
    public Map<String, String> createNewGame(String body) throws GameServiceException {
    	JsonObject o = new JsonParser().parse(body).getAsJsonObject();
    	JsonElement ele = o.get("pieceType");
    	if (ele.isJsonNull()) {
    		throw new GameServiceException("Bad Request", 400);
    	} else {
	    	String pieceType = ele.getAsString();
	    	Game game = new Game(pieceType);
	
	        String sql = "INSERT INTO games (hare_id, hounds_id, board, state) " +
	                     "             VALUES (:hareId, :houndsId, :board, :state)" ;
	
	        try (Connection conn = db.open()) {
	            int gameId = (int)conn.createQuery(sql, true)
	                .bind(game)
	                .executeUpdate()
	                .getKey();
	            Map<String, String> rst = new LinkedHashMap<String, String>();
	            rst.put("gameId",Integer.toString(gameId));
	            rst.put("playerId", "1");
	            rst.put("pieceType", pieceType);
	            return rst;
	        } catch(Sql2oException ex) {
	            logger.error("TodoService.createNewTodo: Failed to create new entry", ex);
	            throw new GameServiceException("TodoService.createNewTodo: Failed to create new entry", ex);
	        }
    	}
    }

    /**
     * Find a game state given an gameId.
     *
     * @param id The id for the game entry
     * @return A map of <state: current state> corresponding to the id if one is found, otherwise null
     */
    public Map<String, String> findState(String id) throws GameServiceException { 
    	Game game = this.findGame(id);
        if (game == null) {
        	throw new GameServiceException("Invalid game id", 404);
        } else {
        	Map<String, String> rst = new LinkedHashMap<String,String>();
            rst.put("state", game.getState());
            return rst;
        }
    }
    
    /**
     * Find a game board given an gameId.
     *
     * @param id The id for the game entry
     * @return A List of map of <pieceType: <type>, x: <x>, y: <y> > corresponding to the id if one is found,
     * otherwise null
     */
    public List<PosInfo> findBoard(String id) throws GameServiceException {
        Game game = this.findGame(id);
        if (game == null) {
        	throw new GameServiceException("Invalid game id", 404);
        } else {
        	List<PosInfo> rst = new ArrayList<PosInfo>();
            PosInfo rst1 = new PosInfo("HARE", game.getPosHare());
            int[] houndsPos = game.getPosHounds();
            PosInfo rst2 = new PosInfo("HOUND", Arrays.copyOfRange(houndsPos, 0, 2));
            PosInfo rst3 = new PosInfo("HOUND", Arrays.copyOfRange(houndsPos, 2, 4));
            PosInfo rst4 = new PosInfo("HOUND", Arrays.copyOfRange(houndsPos, 4, 6));
            rst.add(rst1);
            rst.add(rst2);
            rst.add(rst3);
            rst.add(rst4);
            return rst;
        }
    }

	/**
     * Update the specified game entry with newly joined player info
     * Update the specified game entry to "TURN_HOUND" state
     * Start to track the game board history
     * @param id The id for the game entry
     * @return A map of <gameId: <id>, playerId: <id>, pieceType: <type> > corresponding to the id if found
     */
    public Map<String, String> joinGame(String id) throws GameServiceException {
    	Game game = this.findGame(id);
        if (game == null) {
    		throw new GameServiceException("Invalid game id", 404);
    	} else if (!game.getState().equals("WAITING_FOR_SECOND_PLAYER")) {
    		throw new GameServiceException("Second player already joined", 410);
    	} else {
    		Map<String, String> rst = new LinkedHashMap<String,String>();
            rst.put("gameId", id);
            rst.put("playerId", "2");
            
    		if (game.getHareId().equals("1")) {
    			game.setHoundsId("2");
    			rst.put("pieceType", "HOUND");
    		} else {
    			game.setHareId("2");
    			rst.put("pieceType", "HARE");
    		}
    		
    		game.setState("TURN_HOUND");
    		this.updateGame(id, game);
    		this.addHistoryBoard(id, game.getBoard());
            return rst;
    	}
    }
    
    /**
     * Update the specified game entry with player's move, if legal.
     * Check the game board history for STALLING
     * If no win, add the game board history
     * @param id The id for the game entry
     * @param body The Json object "<playerId: <id>, fromX: <x>, fromY: <y>, toX: <x>, toY: <y>>"
     * @return A map of <playerId: <id>> corresponding to the player just played
     */
    public Map<String, String> playGame(String id, String body) throws GameServiceException {
    	JsonObject o = new JsonParser().parse(body).getAsJsonObject();
    	JsonElement ele1 = o.get("playerId");
    	JsonElement ele2 = o.get("fromX");
    	JsonElement ele3 = o.get("fromY");
    	JsonElement ele4 = o.get("toX");
    	JsonElement ele5 = o.get("toY");
    	if (ele1.isJsonNull() || ele2.isJsonNull() || ele3.isJsonNull() || ele4.isJsonNull() || ele5.isJsonNull()) {
    		throw new GameServiceException("Bad Request", 400);
    	} else {
	    	String playerId = ele1.getAsString();
	    	String fromX = ele2.getAsString();
	    	String fromY = ele3.getAsString();
	    	String toX = ele4.getAsString();
	    	String toY = ele5.getAsString();

	        Game game = this.findGame(id);
	        if (game == null) {
	        	throw new GameServiceException("INVALID_GAME_ID", 404);
	        } else if (!(playerId.equals(game.getHareId()) || playerId.equals(game.getHoundsId()))) {
	        	throw new GameServiceException("INVALID_PLAYER_ID", 404);
	        } else if (!game.isRightTurn(playerId)) {
	        	throw new GameServiceException("INCORRECT_TURN", 422);
	        } else {
	        	Game newGame = game.move(fromX, fromY, toX, toY);
	        	if (newGame == null) {
	        		throw new GameServiceException("ILLEGAL_MOVE", 422);
	        	} else {
	        		if (this.checkHistoryBoard(id, newGame.getBoard()) == 2) {
	        			newGame.setState("WIN_HARE_BY_STALLING");
	        		} else {
	        			this.addHistoryBoard(id, newGame.getBoard());
	        		}
	        		this.updateGame(id, newGame);
	        	}
	        }
	
	        Map<String, String> rst = new LinkedHashMap<String,String>();
	        rst.put("playerId", playerId);
	        return rst;
    	}
    }

    //-----------------------------------------------------------------------------//
    // Helper Classes and Methods
    //-----------------------------------------------------------------------------//

	public static class GameServiceException extends Exception {
        private int code = 500;
    	public GameServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    	public GameServiceException(String message, int code) {
            super(message);
            this.code = code;
        }
    	public int getCode() {
    		return this.code;
    	}
    }
    
    /**
     * Find game given game id
     * @param id Game id
     * @return The game corresponding to the id.
     */
	private Game findGame(String id) throws GameServiceException {
		String sql = "SELECT * FROM games WHERE game_id = :gameId ";
		try (Connection conn = db.open()) {
			int idInt = Integer.parseInt(id);
	        Game game = conn.createQuery(sql)
	            .addParameter("gameId", idInt)
	            .addColumnMapping("game_id", "gameId")
	            .addColumnMapping("hare_id", "hareId")
	            .addColumnMapping("hounds_id", "houndsId")
	            .executeAndFetchFirst(Game.class);
	        if (game == null) {
	    		throw new GameServiceException("Invalid game id", 404);
	    	} else {
	    		return game;
	    	}

		} catch(Sql2oException ex) {
	        logger.error("Fail to find game", ex);
	        throw new GameServiceException("Fail to find game", ex);
	    }
	}
	
	/**
     * Update the game given game id with new content in "game"
     * @param id Game id
     * @param game New game with updated content
     */
	private void updateGame(String id, Game game) throws GameServiceException {
		String sql = "UPDATE games SET hare_id = :hareId, hounds_id = :houndsId, board = :board, state = :state WHERE game_id = :gameId ";
		try (Connection conn = db.open()) {
			conn.createQuery(sql)
                .bind(game)  // one-liner to map all Game object fields to query parameters :hareId etc
                .addParameter("gameId", Integer.parseInt(id))
                .executeUpdate();
		} catch(Sql2oException ex) {
	        logger.error("Fail to update game", ex);
	        throw new GameServiceException("Fail to update game", ex);
		}
	}
	
	/**
     * Add entity to the "boardHistory" table to keep track of specific game's board history
     * @param id Game id
     * @param board String that represents the board
     */
	private void addHistoryBoard(String id, String board) throws GameServiceException {
		String sql = "INSERT INTO boardHistory (game_id, board) " +
                "             VALUES (:gameId, :board)" ;

	    try (Connection conn = db.open()) {
	        conn.createQuery(sql)
	            .addParameter("gameId", Integer.parseInt(id))
                .addParameter("board", board)
	            .executeUpdate();
	    } catch(Sql2oException ex) {
	        logger.error("Failed to add new board history", ex);
	        throw new GameServiceException("Failed to add new board history", ex);
	    }	
	}

	/**
     * Check how many entities in the "boardHistory" table with exact the same game id and game board
     * @param id Game id
     * @param board String that represents the board
     * @return Counts of entities with exact the same game id and game board
     */
	private int checkHistoryBoard(String id, String board) throws GameServiceException {
		String sql = "SELECT count(*) FROM boardHistory WHERE game_id = :gameId AND board = :board";
		try (Connection conn = db.open()) {
			return conn.createQuery(sql)
                .addParameter("gameId", Integer.parseInt(id))
                .addParameter("board", board)
                .executeScalar(Integer.class);
		} catch(Sql2oException ex) {
	        logger.error("Fail to check game board history", ex);
	        throw new GameServiceException("Fail to check game board history", ex);
		}
	}
}