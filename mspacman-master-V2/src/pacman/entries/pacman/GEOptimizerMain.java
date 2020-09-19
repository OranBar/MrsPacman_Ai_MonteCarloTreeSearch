package pacman.entries.pacman;

import java.util.Scanner;

import pacman.entries.genetic_algorithm.Gene;

public class GEOptimizerMain {

	public static void main(String[] args) {

		MyRunnable r = new MyRunnable();
		new Thread(r).start();

		System.out.println("Press Enter to stop thread");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		r.stop = true;
		scanner.close();

	}

	public static class MyRunnable implements Runnable {
	
		public volatile boolean stop = false;

		public void run() {
			System.out.println("Start of GE");
			while (!stop) {
				GeneticMCTSOptimizer ge = new GeneticMCTSOptimizer(200, 10);
				Gene result = ge.runGeneticAlgorithm(25);
				System.out.println("Result Avg Score: " + result.getFitness()
						+ "\nResult Parameters : \n" + result.getPhenotype());
			}
			System.out.println("End of GE");
		}
	}
}
