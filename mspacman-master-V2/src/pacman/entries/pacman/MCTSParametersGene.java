package pacman.entries.pacman;

import java.util.Random;

import com.sun.xml.internal.ws.policy.spi.PolicyAssertionValidator.Fitness;

import pacman.entries.genetic_algorithm.Gene;

public class MCTSParametersGene extends Gene {

	private static final long serialVersionUID = 1L;
	
	private Random random = new Random();
	
	public MCTSParametersGene(int chromosomeSize) {
		super(chromosomeSize);
	}

	@Override
	public void randomizeChromosome() {
		//TODO: if something doesn't work, check HERE
		//maxIterations
		mChromosome[0] = random.nextDouble() * 400;
		//maxDPolicyIters
		mChromosome[1] = random.nextDouble() * 30;
		
		for(int i=2; i<mChromosome.length; i++){
			mChromosome[i] = random.nextDouble() * 100;
		}
		//pacmanWasEatenReward
		mChromosome[4] = (-1) * mChromosome[4]; 
		//noGhostsEatenAfterPowerPillReward
		mChromosome[5] = (-1) * mChromosome[5]; 
		//distaRectionRewardMult
	}

	@Override
	public String getPhenotype() {
		String result = "Gene Fitness is "+getFitness()+"\n";
		result = result + "maxIterations = " + (int)mChromosome[0]+"\n";
		result = result + "maxDPolicyIters = "+ (int) mChromosome[1]+"\n";
		result = result + "pillEatenReward = "+ mChromosome[2]+"\n";
		result = result + "eatenGhostReward = "+ mChromosome[3]+"\n";
		result = result + "pacmanWasEatenReward = "+ mChromosome[4]+"\n";
		result = result + "noGhostsEatenAfterPowerPillReward = "+ mChromosome[5]+"\n";
		result = result + "distaRectionRewardMult = "+ mChromosome[6]+"\n";
		result = result + "distaRectionTrigger = "+ mChromosome[7]+"\n";
		result = result + "ghostDistanceAfterPPThreshold = " + mChromosome[8]+"\n";
		result = result + "ghostDistanceAfterPPReward = " + mChromosome[9]+"\n";
		return result;
	}
	
	public String toString(){
		return getPhenotype();
	}

	
}
