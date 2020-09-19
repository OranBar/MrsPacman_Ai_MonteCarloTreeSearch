package pacman.entries.genetic_algorithm;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;     // arrayLists are more versatile than arrays
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;        // for generating random numbers


/**0
 * Genetic Algorithm sample class <br/>
 * <b>The goal of this GA sample is to maximize the number of capital letters in a String</b> <br/>
 * compile using "javac GeneticAlgorithm.java" <br/>
 * test using "java GeneticAlgorithm" <br/>
 *
 * @author A.Liapis
 */

public abstract class GeneticAlgorithm {
    private Random random = new Random();

    protected int chromosomeSize = 10;
    protected int populationSize = 500;
    protected ArrayList<Gene> mPopulation;
    private float mutationProbability = 30/100;
    
    /**
     * Creates the starting population of Gene classes, whose chromosome contents are random
     * @param size: The size of the popultion is passed as an argument from the main class
     */
    public GeneticAlgorithm(int popuationSize, int chromosomeSize){
        // initialize the arraylist and each gene's initial weights HERE
    	this.populationSize = popuationSize;
    	this.chromosomeSize = chromosomeSize;
        mPopulation = new ArrayList<Gene>();
        for(int i = 0; i < popuationSize; i++){
            Gene entry = createNewGene(chromosomeSize);
            entry.randomizeChromosome();
            mPopulation.add(entry);
        }
        evaluateGeneration();
    }
    
    public GeneticAlgorithm(int popuationSize, int chromosomeSize, List<Gene> seed){
        // initialize the arraylist and each gene's initial weights HERE
    	this.populationSize = popuationSize;
    	this.chromosomeSize = chromosomeSize;
        mPopulation = new ArrayList<Gene>(seed);
        while(mPopulation.size() < populationSize){
            Gene entry = createNewGene(chromosomeSize);
            entry.randomizeChromosome();
            mPopulation.add(entry);
        }
        evaluateGeneration();
    }
    
    protected abstract Gene createNewGene(int chromosomeSize);
    
    /**
     * For all members of the population, runs a heuristic that evaluates their fitness
     * based on their phenotype. The evaluation of this problem's phenotype is fairly simple,
     * and can be done in a straightforward manner. In other cases, such as agent
     * behavior, the phenotype may need to be used in a full simulation before getting
     * evaluated (e.g based on its performance)
     */
    public abstract void evaluateGeneration();
 
 // One point Crossover Reproduction
  	public Gene[] onePointCrossoverReproduction(Gene first, Gene other) {
  		Gene firstChild = createNewGene(chromosomeSize);
  		Gene secondChild = createNewGene(chromosomeSize);
  		Gene[] result = {firstChild, secondChild};
  		
  		int crossoverIndex = new Random().nextInt(first.getChromosomeSize() - 1);
  		// First half
  		for (int i = 0; i < crossoverIndex; i++) {
  			result[0].mChromosome[i] = first.getChromosomeElement(i);
  			result[1].mChromosome[i] = other.getChromosomeElement(i);
  		}
  		// Second half
  		for (int i = crossoverIndex; i < first.getChromosomeSize(); i++) {
  			result[0].mChromosome[i] = other.getChromosomeElement(i);
  			result[1].mChromosome[i] = first.getChromosomeElement(i);
  		}
  		return result;
  	}
  	
  	public Gene[] uniformCrossover(Gene first, Gene other){
  		Gene firstChild = createNewGene(chromosomeSize);
  		Gene secondChild = createNewGene(chromosomeSize);
  		Gene[] result = {firstChild, secondChild};
  		
  		for(int i=0; i<chromosomeSize; i++){
  			if(Math.random() > 0.5){
  				firstChild.mChromosome[i] = first.mChromosome[i];
  				secondChild.mChromosome[i] = other.mChromosome[i]; 
  			} else {
  				firstChild.mChromosome[i] = other.mChromosome[i];
  				secondChild.mChromosome[i] = first.mChromosome[i]; 
  			}
  		}
  		return result;
  	}
    
    /**
     * With each gene's fitness as a guide, chooses which genes should mate and produce offspring.
     * The offspring are added to the population, replacing the previous generation's Genes either
     * partially or completely. The population size, however, should always remain the same.
     * If you want to use mutation, this function is where any mutation chances are rolled and mutation takes place.
     */
    public void produceNextGenerationbyTournament(){
        // Tournament technique
    	ArrayList<Gene> newGeneration = new ArrayList<Gene>();
    	
    	while(newGeneration.size() < populationSize){
    		ArrayList<Gene> ranking = new ArrayList<Gene>();
    		
    		for(int i=0; i<getTournamentSampleSize(); i++){
    			int randomIndex = random.nextInt(mPopulation.size());
    			ranking.add(mPopulation.get(randomIndex));
    		}
    		Collections.sort(ranking);
    		Collections.reverse(ranking);
    		
    		//Reproduce
    		Gene[] children = uniformCrossover(ranking.get(0), ranking.get(1));
    		//Mutations
    		for(int i=0; i<2; i++){
    			if(Math.random() < mutationProbability){
    				children[i].mutate();
    			}
    		}
    		
    		newGeneration.add(children[0]);
    		newGeneration.add(children[1]);
    	}
    	
    	mPopulation = newGeneration;
    }
    
    public void produceNextGenerationbyMixedSelection(){
    	ArrayList<Gene> newGeneration = new ArrayList<Gene>();
        
    	//RankedPopulation.
    	List<Gene> rankedPopulation = (List<Gene>) mPopulation.clone();
    	Collections.sort(rankedPopulation);
    	Collections.reverse(rankedPopulation);
    	System.out.println("Sorted Population");
    	for(int i=0; i<rankedPopulation.size(); i++){
    		System.out.println(i+"th element's fitness is "+rankedPopulation.get(i).getFitness());
    	}
    	
    	//Keep the 15% best
    	int index = (int)(populationSize/100.0)*15;
    	newGeneration.addAll( rankedPopulation.subList(0, index) );
    	
    	//Randomize 10%
    	for(int i=0; i<populationSize/10; i++){
    		Gene newGene = createNewGene(chromosomeSize);
    		newGene.randomizeChromosome();
    		newGeneration.add(newGene);
    		
    	}
    	
    	// Tournament technique
    	while(newGeneration.size() < populationSize){
    		ArrayList<Gene> ranking = new ArrayList<Gene>();
    		
    		for(int i=0; i<getTournamentSampleSize(); i++){
    			int randomIndex = random.nextInt(mPopulation.size());
    			ranking.add(mPopulation.get(randomIndex));
    		}
    		Collections.sort(ranking);
    		Collections.reverse(ranking);
    		
    		//Reproduce
    		Gene[] children = uniformCrossover(ranking.get(0), ranking.get(1));
    		//Mutations
    		for(int i=0; i<2; i++){
    			if(Math.random() < mutationProbability){
    				children[i].mutate();
    			}
    		}
    		
    		newGeneration.add(children[0]);
    		newGeneration.add(children[1]);
    	}
    	
    	mPopulation = newGeneration;
    }

    protected abstract int getTournamentSampleSize();

	// accessors
    /**
     * @return the size of the population
     */
    public int size(){ return mPopulation.size(); }
    /**
     * Returns the Gene at position <b>index</b> of the mPopulation arrayList
     * @param index: the position in the population of the Gene we want to retrieve
     * @return the Gene at position <b>index</b> of the mPopulation arrayList
     */
    public Gene getGene(int index){ return mPopulation.get(index); }
    
    public Gene getBestGeneInPopulation(){
    	Gene bestGene = mPopulation.get(0);
    	for(Gene gene : mPopulation){
    		if(gene.compareTo(bestGene) == 1 ){
    			bestGene = gene;
    		}
    	}
    	return bestGene;
    }
    
    public void printResults(int generationCount) {
    	 
	}
    
    public abstract boolean stopAlgorithm();
    
    public Gene runGeneticAlgorithm(int maxIterations){
        // Initializing the population (we chose 500 genes for the population,
        // but you can play with the population size to try different approaches)
        int generationCount = 0;
        // For the sake of this sample, evolution goes on forever.
        // If you wish the evolution to halt (for instance, after a number of
        //   generations is reached or the maximum fitness has been achieved),
        //   this is the place to make any such checks
        int iter = 0;
        List<Gene> bestGenes = new LinkedList<Gene>();
        while(stopAlgorithm() || iter++<maxIterations){
        	printResults(generationCount);
        	produceNextGenerationbyMixedSelection();
        	evaluateGeneration();
            
            generationCount++;
            bestGenes.add(getBestGeneInPopulation());
            serializeList(bestGenes, "BestGenes");
            serializeList(mPopulation, ("Generation_"+iter) );
        }
        serializeList(bestGenes, "bestGenes");
        printResults(generationCount);
        Gene result = getBestGeneInPopulation();
		System.out.println("Best's Avg Score: " + result.getFitness()
				+ "\nBest's Parameters : \n" + result.getPhenotype());
        return result;
    }

	private void serializeList(List<Gene> list, String fileName) {
		try{
		    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName+".ser"));
		    oos.writeObject(list);
		    oos.close();
	    }catch(IOException ioe){
	    	ioe.printStackTrace();
	    }
	}
    
    
 
    
}
