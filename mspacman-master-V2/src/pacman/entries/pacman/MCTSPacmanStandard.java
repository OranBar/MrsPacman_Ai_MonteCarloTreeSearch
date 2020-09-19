package pacman.entries.pacman;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.RandomNonRevPacMan;
import pacman.controllers.examples.RandomPacMan;
import pacman.entries.monteCarloTree.MCTree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MCTSPacmanStandard extends MCTree<Game, MOVE> {

	private Random random = new Random();
	
	private int mcTreeMaxIterations = 100;
	private int maxDPolicyIters = 22;
	private double noGhostsEatenAfterPowerPillReward = -100;
	private double eatenGhostReward = 20;
	
	/* Best parameters
	 * delay = 10
	 * RandomNonRevPacman
	 * LegacyGhosts
	 * private int mcTreeMaxIterations = 100;
	 * private int maxDPolicyIters = 22;
	 * Level 4
	 * Score 13900
	 * T 2047
	 */
	
	// Reward for eating a power pill when ghosts the closest to pacman
	// private double maxRewardPowerPillEat = 120; //Just put a mult. Do the math in the normalization
	
	// private double scoreRewardMult = 1;
	
	@Override
	protected int getMaxIterations(Game state) {
		return mcTreeMaxIterations;
	}
	
	@Override
	protected double defaultPolicy(Game initialState) {
		Game currentState = getCopy(initialState);
		Controller<MOVE> pacman = new RandomNonRevPacMan();
		Legacy2TheReckoning ghosts = new Legacy2TheReckoning();
		double reward = 0.0;
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
			
			double multiplier = 1.0;
			for(GHOST ghost : GHOST.values()){
				if(currentState.wasGhostEaten(ghost)){
					reward = reward + (eatenGhostReward * multiplier);
					multiplier += 0.5;
				}
			}
			
			if(currentState.wasPacManEaten()){
				return -100;
			}
			i++;
		}
		
		if(powerPillEaten && ghostsEaten == 0){
			reward += noGhostsEatenAfterPowerPillReward ;
		}
		reward += currentState.getScore() - initialState.getScore();
		return reward;
	}
	
	protected double defaultPolicyOld(Game initialState) {
		Game currentState = getCopy(initialState);
		Controller<MOVE> pacman = new RandomPacMan();
		Legacy2TheReckoning ghosts = new Legacy2TheReckoning();
		
		int i=0;
		while(isTerminalState(currentState) || i < maxDPolicyIters){
			MOVE pacmanMove = pacman.getMove(currentState, -1);
			EnumMap<GHOST, MOVE> ghostMoves = ghosts.getMove(currentState, -1);
			currentState.advanceGame(pacmanMove, ghostMoves);
			i++;
			if(currentState.wasPacManEaten()){
				return -100;
			}
		}
		return currentState.getScore() - initialState.getScore();
		
	}

	@Override
	protected boolean isTerminalState(Game state) {
		return state.wasPacManEaten() || state.getNumberOfActivePills() == 0;
	}

	@Override
	protected List<MOVE> getValidAcitons(Game state) {
		List<MOVE> result = new LinkedList<MOVE>();
		for(MOVE validMove : state.getPossibleMoves(state.getPacmanCurrentNodeIndex(), state.getPacmanLastMoveMade()) ){
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

	@Override
	protected Game getCopy(Game state) {
		return state.copy();
	}


}