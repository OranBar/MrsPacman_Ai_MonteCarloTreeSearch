package pacman.entries.pacman;

import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class StandardMCTSController extends Controller<MOVE>{
	
	private MCTSPacmanStandard mct = new MCTSPacmanStandard();
	private MOVE lastMove = MOVE.RIGHT;
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		int closestGhostNodeIndex = game.getGhostCurrentNodeIndex(getClosestGhostToPac(game));
		int secondClosestGhostNodeIndex = game.getGhostCurrentNodeIndex(getSecondClosestGhost(game));
		int pacmanIndex = game.getPacmanCurrentNodeIndex();
		boolean pacmanIsInJunction = game.isJunction(pacmanIndex);
		boolean closestGhostIsInJunction = game.isJunction(closestGhostNodeIndex);
		boolean secondClosestGhostIsInJunction = game.isJunction(secondClosestGhostNodeIndex);
		
		MOVE[] pacmanPossibleMoves = game.getPossibleMoves(pacmanIndex, game.getPacmanLastMoveMade());
		
		if(pacmanPossibleMoves.length == 1 ){
			lastMove = pacmanPossibleMoves[0];
		} else if(pacmanIsInJunction || closestGhostIsInJunction /*|| secondClosestGhostIsInJunction*/){
			try {
				lastMove = mct.runMCTSearch(game);
			} catch (InterruptedException e) {
				System.err.println("MCT was Interrupted");
			}
		}
		
		return lastMove;
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
	
	private GHOST getSecondClosestGhost(Game game){
		int pacmanNodeIndex = game.getPacmanCurrentNodeIndex();
		GHOST secondClosestGhost = null;
		int minDistance = Integer.MAX_VALUE;
		for(GHOST ghost : GHOST.values()){
			if(ghost == getClosestGhostToPac(game)){
				continue;
			}
			int currentGhostNodeIndex = game.getGhostCurrentNodeIndex(ghost);
			int distanceFromGhost = game.getShortestPathDistance(currentGhostNodeIndex, pacmanNodeIndex);
			if(distanceFromGhost < minDistance){
				secondClosestGhost = ghost;
				minDistance = distanceFromGhost;
			}
		}
		return secondClosestGhost;
	}

}
