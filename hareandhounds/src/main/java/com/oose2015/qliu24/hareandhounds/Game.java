//-------------------------------------------------------------------------------------------------------------//
// Code based on a tutorial by Shekhar Gulati of SparkJava at
// https://blog.openshift.com/developing-single-page-web-applications-using-java-8-spark-mongodb-and-angularjs/
//-------------------------------------------------------------------------------------------------------------//

package com.oose2015.qliu24.hareandhounds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    private String gameId;
    private String hareId;
    private String houndsId;
    private String board;
    private String state = "WAITING_FOR_SECOND_PLAYER";

    public Game(String id, String hareId, String houndsId, String board, String state) {
        this.gameId = id;
        this.hareId = hareId;
        this.houndsId = houndsId;
        this.board = board;
        this.state = state;
    }
    
    public Game(String pieceType) {
    	this.board = new String("hhohoooooor");
        this.state = new String("WAITING_FOR_SECOND_PLAYER");
        if (pieceType.equals("HARE")) {
            this.hareId = new String("1");
            this.houndsId = new String("0");
        } else {
        	this.hareId = new String("0");
            this.houndsId = new String("1");
        }
    }
    
    public String getGameId() {
        return this.gameId;
    }

    public String getHareId() {
        return this.hareId;
    }
    
    public String getHoundsId() {
        return this.houndsId;
    }
    
    public String getBoard() {
        return this.board;
    }
    
    public String getState() {
        return this.state;
    }
    
    public void setHareId(String id) {
    	this.hareId = id;
    }
    
    public void setHoundsId(String id) {
    	this.houndsId = id;
    }
    
    public void setBoard(String board) {
    	this.board = board;
    }

    public void setState(String state) {
    	this.state = state;
    }
    
    public int[] getPosHare() {
    	int hareIdx = this.board.indexOf('r');
        int[] harePos = idxToPos(hareIdx);
        return harePos;
    }
    
    public int[] getPosHounds() {
    	int houndsIdx1 = this.board.indexOf('h');
        int houndsIdx2 = this.board.indexOf('h',houndsIdx1+1);
        int houndsIdx3 = this.board.indexOf('h',houndsIdx2+1);
        int[] houndsPos1 = idxToPos(houndsIdx1);
        int[] houndsPos2 = idxToPos(houndsIdx2);
        int[] houndsPos3 = idxToPos(houndsIdx3);
        int[] rst = new int[6];
        System.arraycopy(houndsPos1, 0, rst, 0, 2);
        System.arraycopy(houndsPos2, 0, rst, 2, 2);
        System.arraycopy(houndsPos3, 0, rst, 4, 2);
        return rst;
    }
    
    public boolean isRightTurn(String playerId) {
    	if ((state.equals("TURN_HOUND") && houndsId.equals(playerId)) || (state.equals("TURN_HARE") && hareId.equals(playerId))) {
    		return true;
    	}
    	return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Game game = (Game) o;

        if (gameId != null ? !gameId.equals(game.gameId) : game.gameId != null) return false;
        if (hareId != null ? !hareId.equals(game.hareId) : game.hareId != null) return false;
        if (houndsId != null ? !houndsId.equals(game.houndsId) : game.houndsId != null) return false;
        if (board != null ? !board.equals(game.board) : game.board != null) return false;
        return !(state != null ? !state.equals(game.state) : game.state != null);

    }

    @Override
    public int hashCode() {
        int result = gameId != null ? gameId.hashCode() : 0;
        result = 31 * result + (hareId != null ? hareId.hashCode() : 0);
        result = 31 * result + (houndsId != null ? houndsId.hashCode() : 0);
        result = 31 * result + (board != null ? board.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id='" + gameId + '\'' +
                ", hareId='" + hareId + '\'' +
                ", houndsId='" + houndsId + '\'' +
                ", board='" + board + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
    
    /**
     * This method converts idx to X and Y position info
     */
    private int[] idxToPos(int idx) {
    	int[] pos = new int[2];
    	switch (idx) {
	    	case 0: pos[0] = 0;
	    	        pos[1] = 1;
	    	        break;
	    	case 1: pos[0] = 1;
	                pos[1] = 0;
	                break;
	    	case 2: pos[0] = 1;
	                pos[1] = 1;
	                break;
	    	case 3: pos[0] = 1;
	                pos[1] = 2;
	                break;
	    	case 4: pos[0] = 2;
	                pos[1] = 0;
	                break;
	    	case 5: pos[0] = 2;
	                pos[1] = 1;
	                break;
	    	case 6: pos[0] = 2;
	                pos[1] = 2;
	                break;
	    	case 7: pos[0] = 3;
	                pos[1] = 0;
	                break;
	    	case 8: pos[0] = 3;
	                pos[1] = 1;
	                break;
	    	case 9: pos[0] = 3;
	                pos[1] = 2;
	                break;
	    	case 10: pos[0] = 4;
	                 pos[1] = 1;
	                 break;      
    	}
		return pos;
	}

    private int posToIdx(int x, int y) {
    	if (x==0) {
    		return 0;
    	} else if (x==4) {
    		return 10;
    	} else {
    		return 3*x+y-2;
    	}
    }
	public Game move(String fromX, String fromY, String toX, String toY) {
		if (this.getState().equals("TURN_HOUND")) {
			return this.moveHounds(fromX, fromY, toX, toY);
		} else if (this.getState().equals("TURN_HARE")) {
			return this.moveHare(fromX, fromY, toX, toY);
		} else {
			return null;
		}
	}

	private Game moveHounds(String fromX, String fromY, String toX, String toY) {
		int fromIdx = this.posToIdx(Integer.parseInt(fromX), Integer.parseInt(fromY));
		int toIdx = this.posToIdx(Integer.parseInt(toX), Integer.parseInt(toY));
		
		if (this.board.charAt(fromIdx) != 'h') {
			return null;
		} else if (this.board.charAt(toIdx) != 'o') {
			return null;
		} else if (Integer.parseInt(fromX) > Integer.parseInt(toX)) {
			return null;
		} else if (!isLegal(fromIdx, toIdx)){
			return null;
		} else {
			StringBuilder boardNew = new StringBuilder(this.board);
			boardNew.setCharAt(fromIdx, 'o');
			boardNew.setCharAt(toIdx, 'h');
			String boardNewStr = boardNew.toString();
			String stateNew = new String("TURN_HARE");
			if ( this.isHoundsWin(boardNewStr)) {
				stateNew = new String("WIN_HOUND");
			}
			return new Game(this.gameId, this.hareId, this.houndsId, boardNewStr, stateNew);
		}
	}

	private Game moveHare(String fromX, String fromY, String toX, String toY) {
		int fromIdx = this.posToIdx(Integer.parseInt(fromX), Integer.parseInt(fromY));
		int toIdx = this.posToIdx(Integer.parseInt(toX), Integer.parseInt(toY));
		
		if (this.board.charAt(fromIdx) != 'r' ) {
			return null;
		} else if (this.board.charAt(toIdx) != 'o') {
			return null;
		} else if (!isLegal(fromIdx, toIdx)){
			return null;
		} else {
			StringBuilder boardNew = new StringBuilder(this.board);
			boardNew.setCharAt(fromIdx, 'o');
			boardNew.setCharAt(toIdx, 'r');
			String boardNewStr = boardNew.toString();
			String stateNew = new String("TURN_HOUND");
			int[] houndsPos = this.getPosHounds();
			int toXInt = Integer.parseInt(toX);
			if (toXInt <= houndsPos[0]) {
				stateNew = new String("WIN_HARE_BY_ESCAPE");
			}
			return new Game(this.gameId, this.hareId, this.houndsId, boardNewStr, stateNew);
		}
	}
	
	private boolean isLegal(int fromIdx, int toIdx) {
		List<Integer> nabors = GameBoard.allNabors.get(fromIdx);
		return nabors.contains(toIdx);
	}
	
	private boolean isHoundsWin(String boardNew) {
		int hareIdx = boardNew.indexOf('r');
		int houndsIdx1 = boardNew.indexOf('h');
        int houndsIdx2 = boardNew.indexOf('h',houndsIdx1+1);
        int houndsIdx3 = boardNew.indexOf('h',houndsIdx2+1);
		if (hareIdx == 4 || hareIdx == 6 || hareIdx == 10) {
			List<Integer> hareNabors = GameBoard.allNabors.get(hareIdx);
			if (houndsIdx1==hareNabors.get(0) && houndsIdx2==hareNabors.get(1) && houndsIdx3==hareNabors.get(2)) {
				return true;
			}
		}
		return false;
	}
}
