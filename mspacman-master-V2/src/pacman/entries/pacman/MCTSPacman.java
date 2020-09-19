package pacman.entries.pacman;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import pacman.controllers.Controller;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.StarterPacMan;
import pacman.entries.monteCarloTree.MCTree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MCTSPacman extends MCTree<Game, MOVE> {

	public static Scanner scanner = new Scanner(System.in);
	
	private Random random = new Random();
	
	private Controller<MOVE> pacman = new StarterPacMan();
	private Legacy2TheReckoning ghosts = new Legacy2TheReckoning();
	
	
	private int mcTreeMaxIterations = 100;
	private int maxDPolicyIters = 17;
	private double pacmanWasEatenReward = 0;
	private double pacmanIsAlive = 1;
	// private double distanceFromGhostsRewardMult = 0;
	private double eatenPillReward = 1;
	private double pillsRewardMult = 1;
	private double eatenGhostReward = 1;
	private double ghostsDistanceFromPowePillRewardMult = 3;
	private double noGhostsEatenAfterPowerPillReward = 0;
	
	private double minDistance = 0.1;
	private double maxDistance = 150; //Not real max distance, used for max reward when normalizing
	
	private boolean debugMode = false;

	// Reward for eating a power pill when ghosts the closest to pacman
	// private double maxRewardPowerPillEat = 120; //Just put a mult. Do the math in the normalization

	// private double scoreRewardMult = 1;
	
	@Override
	protected int getMaxIterations(Game state) {
		return mcTreeMaxIterations;
	}
	
	@Override
	protected void preRunHook(Game state) {
		debugMode = false;
		int inputNumber = 0;
		//int inputNumber = scanner.nextInt();
		if(inputNumber == 1){
			debugMode = true;
		}
	}
	
	
	@Override
	protected double defaultPolicy(Game initialState) {
		Game currentState = getCopy(initialState);
		double reward = 0;
		boolean powerPillEaten = false;
		int ghostsEaten = 0;
	
		int i=0;
		while(isTerminalState(currentState) || i < maxDPolicyIters){
			MOVE pacmanMove = pacman.getMove(currentState, -1);
			EnumMap<GHOST, MOVE> ghostMoves = ghosts.getMove(currentState, -1);
			currentState.advanceGame(pacmanMove, ghostMoves);
			
			if(currentState.wasPowerPillEaten()){
				powerPillEaten = true;
			}
			for(GHOST ghost : GHOST.values()){
				if(currentState.wasGhostEaten(ghost)){
					ghostsEaten++;
				}
			}
			reward += rewardGameState(currentState);
			i++;
		}
		
		if(powerPillEaten && ghostsEaten == 0){
			reward += noGhostsEatenAfterPowerPillReward;
		}
		
		 reward += rewardFinalState(currentState, initialState);
		// reward += currentState.getScore() - initialState.getScore();
		return reward;
	}
	
	private double rewardGameState(Game currentState) {
		double reward = 0.0;
		
		// Reward eating ghosts
		double multiplier = 1;
		for(GHOST ghost : GHOST.values()){
			if(currentState.wasGhostEaten(ghost)){
				if(debugMode){ System.out.println("Ghost eaten: +"+(eatenGhostReward * multiplier)); }
				reward = reward + (eatenGhostReward * multiplier);
				multiplier += 0.5;
			}
		}
		
		/*
		// Reward good power pill grabs
		if(currentState.wasPowerPillEaten()){
			double distanceFromGhosts = getPacmanDistanceFromTwoClosestGhosts(currentState);
			if(debugMode){ System.out.println("Powerpill eaten: +"+(normalizeDistance(distanceFromGhosts, 2) * ghostsDistanceFromPowePillRewardMult)); }
			if(debugMode){ System.out.println("Distance from 2 closest ghosts was "+distanceFromGhosts); }
			reward += (normalizeDistance(distanceFromGhosts, 2) * ghostsDistanceFromPowePillRewardMult);
		}
		*/
		
		return reward;
	}

	private double normalizeDistance(double distanceFromGhosts, int mult) {
		double normalizedValue = 0.0;
		normalizedValue = (distanceFromGhosts - minDistance*mult) / (maxDistance*mult - minDistance*mult);
		return normalizedValue;
	}
	
	private double normalizeDistance(double distanceFromGhosts) {
		return normalizeDistance(distanceFromGhosts, 1);
	}

	private double rewardFinalState(Game finalGameState, Game previousGameState ){
		double reward = 0.0;
		
		if(finalGameState.wasPacManEaten()){
			if(debugMode){ System.out.println("Pacman was eaten "+pacmanWasEatenReward); }
			reward += pacmanWasEatenReward; 
		} else {
			reward += pacmanIsAlive ;
		}
		
		/*
		// Distance from Ghosts reward
		int finalDistanceFromGhosts = getPacmanDistanceFromTwoClosestGhosts(finalGameState);
		int previousDistanceFromGhosts = getPacmanDistanceFromTwoClosestGhosts(previousGameState);
		int distanceGained = (finalDistanceFromGhosts - previousDistanceFromGhosts);
		
		if(debugMode){ System.out.println("Reward for clostest 2 ghosts distance "+(normalizeDistance(distanceGained, 2) * distanceFromGhostsRewardMult)); }
		reward += normalizeDistance(distanceGained, 2) * distanceFromGhostsRewardMult;
		*/
		
		// Pills eaten reward
		int prevNoOfPills = previousGameState.getNumberOfActivePills();
		int finalNoOfPills = finalGameState.getNumberOfActivePills();
		int noOfPillsEaten = prevNoOfPills - finalNoOfPills;  
		
		if(debugMode){ System.out.println("Reward for pills eaten "+(normalizePillsEaten(noOfPillsEaten) * pillsRewardMult)); }
		// reward += normalizePillsEaten(noOfPillsEaten) * pillsRewardMult;
		reward += (noOfPillsEaten * eatenPillReward);
		
		if(debugMode){ System.out.println("REWARD IS "+reward); }
		return reward;
	}
	
	private double normalizePillsEaten(int noOfPillsEaten) {
		double normalizedNoOfPilsEaten = 0.0;
		double minPillsEaten = 0, maxPillsEaten = maxDPolicyIters;
		normalizedNoOfPilsEaten = (noOfPillsEaten - minPillsEaten) / (maxPillsEaten - minPillsEaten); 
		return normalizedNoOfPilsEaten;
	}

	private int getPacmanDistanceFromAllGhosts(Game state){
		int distance = 0;
		int pacmanNodeIndex = state.getPacmanCurrentNodeIndex();
		for(GHOST currentGhost : GHOST.values()){
			int ghostIndex = state.getGhostCurrentNodeIndex(currentGhost);
			MOVE ghostLastMove = state.getGhostLastMoveMade(currentGhost);
			distance += state.getShortestPathDistance(ghostIndex, pacmanNodeIndex, ghostLastMove);
		}
		return distance;
	}
	
	private int getPacmanDistanceFromTwoClosestGhosts(Game state){
		int[] distances = new int[4];
		int pacmanNodeIndex = state.getPacmanCurrentNodeIndex();
		int i = 0;
		for(GHOST currentGhost : GHOST.values()){
			int ghostIndex = state.getGhostCurrentNodeIndex(currentGhost);
			distances[i] = state.getShortestPathDistance(ghostIndex, pacmanNodeIndex);
			i++;
		}
		Arrays.sort(distances);
		if(debugMode) { System.out.println("Distance from 2 closest ghosts is "+(distances[0] + distances[1])); }
		return distances[0] + distances[1];
	}

	@Override
	protected boolean isTerminalState(Game state) {
		return state.wasPacManEaten() || state.getNumberOfActivePills() == 0;
	}

	@Override
	protected List<MOVE> getValidAcitons(Game state) {
		List<MOVE> result = new LinkedList<MOVE>();
		for(MOVE validMove : state.getPossibleMoves(state.getPacmanCurrentNodeIndex()) ){
			result.add(validMove);
		}
		return result;
	}

	@Override
	protected Game computeStateAfterAction(Game stateInfo, MOVE action) {
		Game stateClone = getCopy(stateInfo);
		
		// Add in ANN
		EnumMap<GHOST,MOVE> ghostMoves = new EnumMap<GHOST, MOVE>(GHOST.class);
		ghostMoves.put(GHOST.BLINKY, getRandomGhostMove(stateClone, GHOST.BLINKY));
		ghostMoves.put(GHOST.INKY, getRandomGhostMove(stateClone, GHOST.INKY));
		ghostMoves.put(GHOST.PINKY, getRandomGhostMove(stateClone, GHOST.PINKY));
		ghostMoves.put(GHOST.SUE, getRandomGhostMove(stateClone, GHOST.SUE));
		
		stateClone.advanceGame(action, ghostMoves);
		
		return stateClone;
	}

	private MOVE getRandomGhostMove(Game state, GHOST ghost) {
		if(state.getGhostLairTime(ghost) > 0){
			return MOVE.NEUTRAL;
		}
		int nodeIndex = state.getGhostCurrentNodeIndex(ghost);
		MOVE lastMove = state.getGhostLastMoveMade(ghost);
		MOVE[] movesAvailableToGhost = state.getPossibleMoves(nodeIndex, lastMove);
		int rndIndex = random.nextInt(movesAvailableToGhost.length);
		return movesAvailableToGhost[rndIndex];
	}
	
	private MOVE getLegacy2GhostMove(Game state, GHOST ghost){
		EnumMap<GHOST, MOVE> ghostMoves = ghosts.getMove(state, -1);
		return ghostMoves.get(ghost);
	}

	@Override
	protected Game getCopy(Game state) {
		return state.copy();
	}


}