package pacman.entries.pacman;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.entries.genetic_algorithm.Gene;
import pacman.entries.genetic_algorithm.GeneticAlgorithm;
import pacman.entries.monteCarloTree.MCTree;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class GeneticMCTSOptimizer extends GeneticAlgorithm{

	private int numTrials = 20;
	
	public GeneticMCTSOptimizer(int popuationSize, int chromosomeSize) {
		super(popuationSize, chromosomeSize);
		numTrials = 20;
	}
	
	public GeneticMCTSOptimizer(int popuationSize, int chromosomeSize, List<Gene> seed) {
		super(popuationSize, chromosomeSize, seed);
		numTrials = 20;
	}
	@Override
	protected Gene createNewGene(int chromosomeSize) {
		return new MCTSParametersGene(chromosomeSize);
	}

	@Override
	public void evaluateGeneration() {
		double geneFitness = 0.0f;
		MCTree<Game, MOVE> mcTree = null;
		for(Gene gene : mPopulation){
			mcTree = new TheRealMCTSPacman(gene.getChromosome());
			
			// Do not evaluate the fitness if it has already been done. 
			// Usually when using a seed
			if(gene.getFitness() > 0){
				continue;
			}
			
			Executor exec=new Executor();
			Controller<MOVE> pacmanController = new TheRealPacmanController(mcTree);
			
			// 0 iterations on MCT or Default Policy
			try{
				geneFitness = exec.runExperiment(pacmanController, new Legacy2TheReckoning(), 20);
			} catch(NullPointerException e){
				gene.setFitness(0);
				return;
			}
			gene.setFitness(geneFitness);
		
		}
	}

	@Override
	protected int getTournamentSampleSize() {
		return 5;
	}

	@Override
	public boolean stopAlgorithm() {
		return false;
	}
	
	@Override
	public void printResults(int generationCount) {
		System.out.println("generation number "+generationCount);
		
		Gene bestGene = null;
		double bestGeneFitness = -1;
		double currentGeneFitness = -1 ;
        for(Gene gene : mPopulation){
        	currentGeneFitness = gene.getFitness();
        	if(gene.getFitness() > bestGeneFitness){
        		bestGeneFitness = currentGeneFitness;
        		bestGene = gene;
        	}
        }
        
        System.out.println("Best's Avg Score: " + bestGene.getFitness()
				+ "\nBest's Parameters : \n" + bestGene.getPhenotype());
	}
	
	private List<Gene> deserializeGeneList(String serFileName){
		List<Gene> result = null;
		try {
			FileInputStream fis = new FileInputStream(serFileName);
			ObjectInputStream  ois = new ObjectInputStream(fis);
			result = (List<Gene>) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	//////////////////////////////////////////////////////////////////////////////
    // Run GE
    
	public static void main(String[] args) {
		List<Gene> seed = null;
		try {
			FileInputStream fis = new FileInputStream("Generation_1_V1.ser");
			ObjectInputStream  ois = new ObjectInputStream(fis);
			seed = (List<Gene>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		GeneticMCTSOptimizer ge = new GeneticMCTSOptimizer(20, 10, seed.subList(0, 5) );
		Gene result = ge.runGeneticAlgorithm(20);
		System.out.println("---- GE IS DONE -----");
		System.out.println("Best's Avg Score: " + result.getFitness()
				+ "\nBest's Parameters : \n" + result.getPhenotype());
				
	}
    
	/*
    // Retrieve serialized data
    public static void main(String[] args) {
		
	}
	*/
	
	
}
