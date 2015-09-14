package com.oose2015.qliu24.hareandhounds;

public class PosInfo {
	private String pieceType;
	private Integer x;
	private Integer y;

	public PosInfo (String pieceType, int[] pos) {
		this.pieceType = pieceType;
		this.x = pos[0];
		this.y = pos[1];
	}
	
	public String getPieceType() {
		return this.pieceType;
	}
	
	public Integer getX() {
		return this.x;
	}
	
	public Integer getY() {
		return this.y;
	}
}
