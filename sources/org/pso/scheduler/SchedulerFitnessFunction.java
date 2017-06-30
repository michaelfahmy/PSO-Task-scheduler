package org.pso.scheduler;

import net.sourceforge.jswarm_pso.FitnessFunction;

public class SchedulerFitnessFunction extends FitnessFunction {
    private static double[][] execTimeMatrix, communTimeMatrix;

    SchedulerFitnessFunction() {
        super(false);
        initMatrices();
    }

    @Override
    public double evaluate(double[] position) {
//        double alpha = 0.3;
//        return alpha * calcTotalTime(position) + (1 - alpha) * calcMakespan(position);
        return calcMakespan(position);
    }

    private double calcTotalTime(double[] position) {
        double totalCost = 0;
        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            int dcId = (int) position[i];
            totalCost += execTimeMatrix[i][dcId] + communTimeMatrix[i][dcId];
        }
        return totalCost;
    }

    public double calcMakespan(double[] position) {
        double makespan = 0;
        double[] dcWorkingTime = new double[Constants.NO_OF_DATA_CENTERS];

        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            int dcId = (int) position[i];
            if(dcWorkingTime[dcId] != 0) --dcWorkingTime[dcId];
            dcWorkingTime[dcId] += execTimeMatrix[i][dcId] + communTimeMatrix[i][dcId];
            makespan = Math.max(makespan, dcWorkingTime[dcId]);
        }
        return makespan;
    }
    public double[][] getExecTimeMatrix()  { return execTimeMatrix; }
    public double[][] getCoumnTimeMatrix() { return communTimeMatrix; }
    
    private void initMatrices() {
        System.out.println("Initializing input matrices (e.g. exec time & communication time matrices");
        execTimeMatrix = new double[Constants.NO_OF_TASKS][Constants.NO_OF_DATA_CENTERS];
        communTimeMatrix = new double[Constants.NO_OF_TASKS][Constants.NO_OF_DATA_CENTERS];

        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            for (int j = 0; j < Constants.NO_OF_DATA_CENTERS; j++) {
                execTimeMatrix[i][j] = Math.random() * 500;
                communTimeMatrix[i][j] = Math.random() * 500 + 20;
            }
        }
    }
}
