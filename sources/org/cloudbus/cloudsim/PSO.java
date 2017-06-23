package org.cloudbus.cloudsim;

import net.sourceforge.jswarm_pso.Swarm;

public class PSO {
	private static SchedularParticle particles[];
	
	public static void main(String[] args) {
    	new PSO().run();
    }
	
	public PSO() {
		initParticles();
	}
	

    public void run() {
        Swarm swarm = new Swarm(Constants.POPULATION_SIZE, new SchedularParticle(), new SchedularFitnessFunction());
        
        swarm.setMinPosition(0);
        swarm.setMaxPosition(Constants.NO_OF_DATA_CENTERS - 1);
        swarm.setMaxMinVelocity(0.5);
        swarm.setParticles(particles);
        swarm.setParticleUpdate(new SchedularParticleUpdate(new SchedularParticle()));

        for (int i = 0; i < 500; i++) {
            swarm.evolve();
            if (i % 10 == 0) {
            	System.out.printf("Gloabl best at iteration (%d): %f\n", i, swarm.getBestFitness());
            }
        }

        System.out.println("\nThe best fitness value: " + swarm.getBestFitness());
        System.out.println("The best solution is: ");
        SchedularParticle bestParticle = (SchedularParticle) swarm.getBestParticle();
        System.out.println(bestParticle.toString());
    }

    private static void initParticles() {
        particles = new SchedularParticle[Constants.POPULATION_SIZE];
        for (int i = 0; i < Constants.POPULATION_SIZE; ++i)
            particles[i] = new SchedularParticle();
    }
}
