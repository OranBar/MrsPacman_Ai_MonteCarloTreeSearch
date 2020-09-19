package pacman.entries.pacman;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.NearestPillPacMan;
import pacman.controllers.examples.RandomNonRevPacMan;
import pacman.controllers.examples.RandomPacMan;
import pacman.controllers.examples.StarterPacMan;
import pacman.entries.monteCarloTree.MCTree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class TheRealMCTSPacmanV1 extends MCTree<Game, MOVE>{

	private Random random = new Random();
	
	private Legacy2TheReckoning ghosts = new Legacy2TheReckoning();
	private Controller<MOVE> defaultPacmanController = new RandomNonRevPacMan();
	
	private int maxIterations = 363;
	private int maxDPolicyIters = 10;
	
	private double levelCompleteReward = 143.5638326595746;
	private double pillEatenReward = 1.3;
	private double eatenGhostReward = 100.7126;
	private double pacmanWasEatenReward = -134.9627;
	private double noGhostsEatenAfterPowerPillReward = -33.38293540761698;
	private double distaRectionRewardMult = 0.6178065183842563;
	// DistaRection will only be taken into account when the ghosts get closer to the Trigger
	private double distaRectionTrigger = 71.98519934499646;

	private double lateGameTimeTrigger = 2000;
	private int lateGameActivePillsNoTrigger = 6;
	
	public TheRealMCTSPacmanV1() {
	
	}
	
	public TheRealMCTSPacmanV1(double[] params) {
		this.maxIterations = (int) params[0];
		this.maxDPolicyIters = (int) params[1];
		this.levelCompleteReward = 300;
		this.pillEatenReward = params[2];
		this.eatenGhostReward = params[3];
		this.pacmanWasEatenReward = params[4];
		this.noGhostsEatenAfterPowerPillReward = params[5];
		this.distaRectionRewardMult = params[6];
		this.distaRectionTrigger = params[7];
	}
	
	public TheRealMCTSPacmanV1(int maxIterations, int maxDPolicyIters,
			double levelCompleteReward, double pillEatenReward,
			double pillsRewardMult, double eatenGhostReward,
			double pacmanWasEatenReward,
			double noGhostsEatenAfterPowerPillReward,
			double distaRectionRewardMult, double distaRectionTrigger) {
		this.maxIterations = maxIterations;
		this.maxDPolicyIters = maxDPolicyIters;
		this.levelCompleteReward = levelCompleteReward;
		this.pillEatenReward = pillEatenReward;
		this.eatenGhostReward = eatenGhostReward;
		this.pacmanWasEatenReward = pacmanWasEatenReward;
		this.noGhostsEatenAfterPowerPillReward = noGhostsEatenAfterPowerPillReward;
		this.distaRectionRewardMult = distaRectionRewardMult;
		this.distaRectionTrigger = distaRectionTrigger;
	}

	@Override
	protected Game getCopy(Game state) {
		return state.copy();
	}

	@Override
	protected int getMaxIterations(Game state) {
		return maxIterations;
	}
	
	private int getDefaultPolicyIterations(Game state){
		return maxDPolicyIters;
	}
	
	@Override
	protected double defaultPolicy(Game initialState) {
		Game currentState = getCopy(initialState);
		Controller<MOVE> pacman = defaultPacmanController;
		
		double reward = 0.0;
		boolean powerPillEaten = false;
		int ghostsEaten = 0;		
		
		int i=0;
		while(isTerminalState(currentState) || i < getDefaultPolicyIterations(currentState)){
			if(ghostsAreEdible(currentState)){
				pacman = new StarterPacMan();
			}
			
			if(currentState.wasPowerPillEaten()){
				powerPillEaten = true;
				pacman = new StarterPacMan();
			}
			//If a few pills are left, just try to get there ASAP
			//Doesn't really work
			if(isLateGame(currentState)){
				pacman = new NearestPillPacMan();
			}
			
			// Reward for eating ghosts
			double multiplier = 1.0;
			for(GHOST ghost : GHOST.values()){
				if(currentState.wasGhostEaten(ghost)){
					reward = reward + (eatenGhostReward  * multiplier);
					multiplier += 0.5;
					ghostsEaten++;
				}
			}
			
			if(currentState.wasPacManEaten()){
				return pacmanWasEatenReward;
			}
			if(currentState.getNumberOfActivePills() == 0){
				return levelCompleteReward;
			}
			
			
			MOVE pacmanMove = pacman.getMove(currentState, -1);
			EnumMap<GHOST, MOVE> ghostMoves = ghosts.getMove(currentState, -1);
			currentState.advanceGame(pacmanMove, ghostMoves);
			i++;
		}
		
		if(powerPillEaten && ghostsEaten == 0){
			reward += noGhostsEatenAfterPowerPillReward ;
		}
		
		reward += getReward(initialState, currentState);
		
		return reward;
	}
	
	private boolean ghostsAreEdible(Game state) {
		for(GHOST ghost : GHOST.values()){
			if(state.getGhostEdibleTime(ghost)>0){
				return true;
			}
		}
		return false;
	}

	private double getReward(Game prevState, Game finalState){
		double reward = 0.0; 
		// Level complete reward
		if(finalState.getNumberOfActivePills() == 0){
			reward += levelCompleteReward;
		}
		
		// Reward pills eaten
		int pillsEaten = prevState.getNumberOfActivePills() - finalState.getNumberOfActivePills();
		reward += (pillsEaten * pillEatenReward);
		
		
		// Reward distance from ghosts based on direction they are coming from:
		// Ghosts coming from multiple directions is bad, same direction is good
		// Let's call it DistaRection
		double prevDistaRection = getDistaRection(prevState);
		double finalDistaRection = getDistaRection(finalState);
		double distaRectionDelta = finalDistaRection - prevDistaRection;
		if(prevDistaRection < distaRectionTrigger){
			
			//System.out.println("DistaRection Triggered: "+prevDistaRection);
			//System.out.println("  default policy gained: "+distaRectionDelta);
			
			if(areGhostsEdible(finalState)==false){
				reward += distaRectionDelta * distaRectionRewardMult;
			}
		}
		
		return reward;
	}
	
	private boolean areGhostsEdible(Game state) {
		for(GHOST ghost : GHOST.values()){
			if(state.isGhostEdible(ghost)){
				return true;
			}
		}
		return false;
	}

	/* Does not work because nodes are numerated casually, not related to their cartesian 
	 * position in the game.
	 */
	private double getDistaRection(Game state) {
		int resultantVectorX = 0, resultantVectorY = 0;
		
		int pacmanNodeIndex = state.getPacmanCurrentNodeIndex();
		int pacmanX = state.getNodeXCood(pacmanNodeIndex);
		int pacmanY = state.getNodeYCood(pacmanNodeIndex);
		
		for(GHOST ghost : GHOST.values()){
			int ghostIndex = state.getGhostCurrentNodeIndex(ghost);
			int ghostX = state.getNodeXCood(ghostIndex);
			int ghostY = state.getNodeYCood(ghostIndex);
			
			resultantVectorX += (ghostX - pacmanX);
			resultantVectorY += (ghostY - pacmanY);
		}
		
		return Math.sqrt(resultantVectorX*resultantVectorX + resultantVectorY*resultantVectorY);
	}
	
	private boolean isLateGame(Game state){
		/*
		if(state.getCurrentLevelTime() > lateGameTimeTrigger){
			return true;
		}
		if(state.getNumberOfActivePills() < lateGameActivePillsNoTrigger){
			return true;
		}
		*/
		
		
		return false;
	}

	@Override
	protected boolean isTerminalState(Game state) {
		return state.wasPacManEaten() || state.getNumberOfActivePills() == 0;
	}
/*
	@Override
	protected List<MOVE> getValidAcitons(Game state) {
		List<MOVE> result = new LinkedList<MOVE>();
		for(MOVE validMove : state.getPossibleMoves(state.getPacmanCurrentNodeIndex(), state.getPacmanLastMoveMade()) ){
			result.add(validMove);
		}
		return result;
	}
*/
	protected List<MOVE> getValidAcitons(Game state) {
		List<MOVE> result = new LinkedList<MOVE>();
		boolean ghostWasEaten = wasGhostEaten(state);
		boolean powerPillEaten = state.wasPowerPillEaten();
		MOVE[] validMoves = null;
		
		if(ghostWasEaten || powerPillEaten){
			validMoves = state.getPossibleMoves(state.getPacmanCurrentNodeIndex());
		} else {
			validMoves = state.getPossibleMoves(state.getPacmanCurrentNodeIndex(), state.getPacmanLastMoveMade());
		}
		
		for(MOVE move : validMoves ){
			result.add(move);
		}
		return result;
	}
	
	private GHOST getClosestGhostToPac(Game game){
		int pacmanNodeIndex = game.getPacmanCurrentNodeIndex();
		GHOST closestGhost = null;
		int minDistance = Integer.MAX_VALUE;
		for(GHOST ghost : GHOST.values()){
			int currentGhostNodeIndex = game.getGhostCurrentNodeIndex(ghost);
			int distanceFromGhost = game.getShortestPathDistance(currentGhostNodeIndex, pacmanNodeIndex);
			if(distanceFromGhost < minDistance){
				closestGhost = ghost;
				minDistance = distanceFromGhost;
			}
		}
		return closestGhost;
	}
	
	private boolean wasGhostEaten(Game game) {
		for(GHOST ghost : GHOST.values()){
			if(game.wasGhostEaten(ghost)){
				return true;
			}
		}
		return false;
	}

	@Override
	protected Game computeStateAfterAction(Game stateInfo, MOVE action) {
		Game stateClone = getCopy(stateInfo);
		
		//TODO: Add in ANN
		EnumMap<GHOST,MOVE> ghostMoves = new EnumMap<GHOST, MOVE>(GHOST.class);
		ghostMoves.put(GHOST.BLINKY, getLegacy2GhostMove(stateClone, GHOST.BLINKY));
		ghostMoves.put(GHOST.INKY, getLegacy2GhostMove(stateClone, GHOST.INKY));
		ghostMoves.put(GHOST.PINKY, getLegacy2GhostMove(stateClone, GHOST.PINKY));
		ghostMoves.put(GHOST.SUE, getLegacy2GhostMove(stateClone, GHOST.SUE));
		
		stateClone.advanceGame(action, ghostMoves);
		
		return stateClone;
	}
	
	private MOVE getLegacy2GhostMove(Game state, GHOST ghost){
		EnumMap<GHOST, MOVE> ghostMoves = ghosts.getMove(state, -1);
		return ghostMoves.get(ghost);
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

}
