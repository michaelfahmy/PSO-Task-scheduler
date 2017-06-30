package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.pso.scheduler.*;

public class TaskScheduler {
	static Datacenter[] datacenter;
	private static List<Vm> vmlist;
	private static List<Cloudlet> cloudletList;
	private static PSO PSOSchedularInstance = new PSO();;
	public static double mapping[] = PSOSchedularInstance.run();
	
	public double[] getPsoMapping() { return mapping; }
	
	public static void main(String[] args) {
		double execTimeMatrix[][] = PSOSchedularInstance.getExecTimeMatrix();
		double communTimeMatrix[][] = PSOSchedularInstance.getCommunTimeMatrix();
		Log.printLine("Starting Task Schedular Simulation...");
		try {
			int num_user = 1;
        	Calendar calendar = Calendar.getInstance();
        	boolean trace_flag = false;
        	CloudSim.init(num_user, calendar, trace_flag);

    		datacenter = new Datacenter[Constants.NO_OF_DATA_CENTERS];
        	for(int i = 0; i < Constants.NO_OF_DATA_CENTERS; i++) {
        		datacenter[i] = createDatacenter("Datacenter_" + i);
        	}

        	DatacenterBroker broker = createBroker();
        	int brokerId = broker.getId();

        	vmlist = new ArrayList<Vm>();        	
        	int mips = 1000;
        	long size = 10000;
        	int ram = 512;
        	long bw = 1000;
        	int pesNumber = 1;
        	String vmm = "Xen";
        	
        	for(int i = 0; i < Constants.NO_OF_DATA_CENTERS; i++) {
        		vmlist.add(new Vm(datacenter[i].getId(), brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared()));
        	}
        	broker.submitVmList(vmlist);
        	
        	cloudletList = new ArrayList<Cloudlet>();
        	pesNumber = 1;
        	long fileSize = 1000;
        	long outputSize = 300;
        	UtilizationModel utilizationModel = new UtilizationModelFull();
        	
        	for(int i = 0; i < Constants.NO_OF_TASKS; i++) {
        		Cloudlet cloudlet1 = new Cloudlet(i, (int) (1e3 * (execTimeMatrix[i][(int) mapping[i]] + communTimeMatrix[i][(int) mapping[i]])), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            	cloudlet1.setUserId(brokerId);
            	cloudletList.add(cloudlet1);
        	}
        	broker.submitMapping(mapping);
        	broker.submitCloudletList(cloudletList);
        	
        	CloudSim.startSimulation();
        	
        	List<Cloudlet> newList = broker.getCloudletReceivedList();
        	CloudSim.stopSimulation();

        	printCloudletList(newList);
        	Log.printLine("simulating PSO finished!");
		}catch(Exception e) {
			System.out.println("An error has been occurred!\n" + e.getMessage());
		}
	}

	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
        Cloudlet cloudlet;
        
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");
    	
        double mxFinishTime = 0;
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
                Log.print("SUCCESS");

            	Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                     indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                         indent + indent + dft.format(cloudlet.getFinishTime()));
            	
            	mxFinishTime = Math.max(mxFinishTime, cloudlet.getFinishTime());
            }
        }
        System.out.println(mxFinishTime);
	}

	private static Datacenter createDatacenter(String name){

		List<Host> hostList = new ArrayList<Host>();

		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		//4. Create Host with its id and list of PEs and add them to the list of machines
		int hostId = 0;
		int ram = 2048; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000;


		//in this example, the VMAllocatonPolicy in use is SpaceShared. It means that only one VM
		//is allowed to run on each Pe. As each Host has only one Pe, only one VM can run on each Host.
		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList,
    				new VmSchedulerSpaceShared(peList)
    			)
    		); // This is our first machine

		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.0;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

	       DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
	                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return broker;
	}

	
}
