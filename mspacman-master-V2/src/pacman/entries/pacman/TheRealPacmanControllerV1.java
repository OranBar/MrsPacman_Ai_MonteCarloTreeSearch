package pacman.entries.pacman;

import pacman.controllers.Controller;
import pacman.entries.monteCarloTree.MCTree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class TheRealPacmanControllerV1 extends Controller<MOVE> {
	
	private MCTree<Game, MOVE> mct = new TheRealMCTSPacmanV1();
	private MCTree<Game, MOVE> lateGameMct = new LateGameMCTS();
	
	private MOVE lastMove = MOVE.RIGHT;
	
	private int lateGameTimeTrigger = 2000;
	private int lateGameActivePillsNoTrigger = 20;
	
	private boolean debugVar = false;
	
	public TheRealPacmanControllerV1() {
	
	}
	
	public TheRealPacmanControllerV1(MCTree<Game,MOVE> mcTree) {
		this.mct = mcTree;
	}
	
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		int closestGhostNodeIndex = game.getGhostCurrentNodeIndex(getClosestGhostToPac(game));
		int pacmanIndex = game.getPacmanCurrentNodeIndex();
		boolean pacmanIsInJunction = game.isJunction(pacmanIndex);
		boolean closestGhostIsInJunction = game.isJunction(closestGhostNodeIndex);
		boolean powerPillEaten = game.wasPowerPillEaten();
		boolean ghostWasEaten = wasGhostEaten(game);
		
		MOVE[] pacmanPossibleMoves = game.getPossibleMoves(pacmanIndex, game.getPacmanLastMoveMade());
		
		if(pacmanPossibleMoves.length == 1 ){
			lastMove = pacmanPossibleMoves[0];
		} else if(pacmanIsInJunction || closestGhostIsInJunction || powerPillEaten || ghostWasEaten){
			try {
				if(isLateGame(game)){
					if(debugVar == false){
						System.out.println("Late Game has begun");
						debugVar = true;
					}
					lastMove = lateGameMct.runMCTSearch(game);
				} else {
					lastMove = mct.runMCTSearch(game);
				}
			} catch (InterruptedException e) {
				System.err.println("MCT was Interrupted");
			}
		}
		
		return lastMove;
	}
	
	private boolean wasGhostEaten(Game game) {
		for(GHOST ghost : GHOST.values()){
			if(game.wasGhostEaten(ghost)){
				return true;
			}
		}
		return false;
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
}
