package pacman.entries.pacman;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import pacman.controllers.Controller;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.StarterPacMan;
import pacman.entries.monteCarloTree.MCTree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class LateGameMCTS extends MCTree<Game, MOVE> {

	private Legacy2TheReckoning ghosts = new Legacy2TheReckoning();
	private Controller<MOVE> pacman = new StarterPacMan();
	
	private int maxIterations = 100;
	private int maxDPolicyIters = 22;
	
	private double pillEatenReward = 1;
	private double pillsRewardMult = 1;
	private double pacmanWasEatenReward = -20;
	private double levelCompleteReward = 60;
	
	@Override
	protected Game getCopy(Game state) {
		return state.copy();
	}

	@Override
	protected int getMaxIterations(Game state) {
		return maxIterations;
	}
	
	@Override
	protected double defaultPolicy(Game initialState) {
		Game currentState = getCopy(initialState);
		
		double reward = 0.0;
		
		int i=0;
		while(isTerminalState(currentState) || i < maxDPolicyIters){
			MOVE pacmanMove = pacman.getMove(currentState, -1);
			EnumMap<GHOST, MOVE> ghostMoves = ghosts.getMove(currentState, -1);
			currentState.advanceGame(pacmanMove, ghostMoves);
			
			if(currentState.wasPacManEaten()){
				return pacmanWasEatenReward;
			}
			if(currentState.getNumberOfActivePills() == 0){
				return levelCompleteReward;
			}
		}
		reward += getReward(initialState, currentState);
		
		return reward;
	}
	
	private double getReward(Game prevState, Game finalState){
		double reward = 0.0; 
		

		// Reward pills eaten
		int pillsEaten = prevState.getNumberOfActivePills() - finalState.getNumberOfActivePills();
		reward += (pillsEaten * pillEatenReward) * pillsRewardMult;
		
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
}
