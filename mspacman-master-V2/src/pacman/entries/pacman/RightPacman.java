package pacman.entries.pacman;

import java.util.EnumMap;

import com.sun.org.apache.bcel.internal.generic.MONITORENTER;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.internal.Maze;
import pacman.game.internal.Node;

public class RightPacman extends Controller<MOVE>{
/*
	@SuppressWarnings("unused")
	private void importantMethods(Game game){
		int[] pills = game.getPillIndices();
		Maze maze = game.getCurrentMaze();
		Node[] nodes = maze.graph;
		EnumMap<MOVE, int[]> map = nodes[0].allNeighbouringNodes;
		double distance = game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.BLINKY), DM.PATH);
		//Get shortest path to escape
		int fromNodeIndex=0, toNodeIndex=0;
		game.getNextMoveAwayFromTarget(fromNodeIndex, toNodeIndex, DM.PATH);
		game.getNextMoveTowardsTarget(fromNodeIndex, toNodeIndex, DM.PATH);
		game.getClosestNodeIndexFromNodeIndex(fromNodeIndex, targetNodeIndices, distanceMeasure);
		
		game.getGhostCurrentNodeIndex(GHOST.BLINKY)
	}
*/	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		Maze maze = game.getCurrentMaze();
		double distance = game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.BLINKY), DM.PATH);
		if(distance<10){
			return game.getPacmanLastMoveMade().opposite();
		} else {
			return game.getPacmanLastMoveMade();
		}
	}
	
}
