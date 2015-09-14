package com.oose2015.qliu24.hareandhounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoard {
	public static final Map<Integer, List<Integer>> allNabors;
	static {
		Map<Integer, List<Integer>> nabors= new HashMap<Integer, List<Integer>>();
		List<Integer> nabor0 = Arrays.asList(1,2,3);
		nabors.put(0, nabor0);
		List<Integer> nabor1 = Arrays.asList(0,2,4,5);
		nabors.put(1, nabor1);
		List<Integer> nabor2 = Arrays.asList(0,1,3,5);
		nabors.put(2, nabor2);
		List<Integer> nabor3 = Arrays.asList(0,2,5,6);
		nabors.put(3, nabor3);
		List<Integer> nabor4 = Arrays.asList(1,5,7);
		nabors.put(4, nabor4);
		List<Integer> nabor5 = Arrays.asList(1,2,3,4,6,7,8,9);
		nabors.put(5, nabor5);
		List<Integer> nabor6 = Arrays.asList(3,5,9);
		nabors.put(6, nabor6);
		List<Integer> nabor7 = Arrays.asList(4,5,8,10);
		nabors.put(7, nabor7);
		List<Integer> nabor8 = Arrays.asList(5,7,9,10);
		nabors.put(8, nabor8);
		List<Integer> nabor9 = Arrays.asList(5,6,8,10);
		nabors.put(9, nabor9);
		List<Integer> nabor10 = Arrays.asList(7,8,9);
		nabors.put(10, nabor10);
		allNabors = Collections.unmodifiableMap(nabors);
	}
}
