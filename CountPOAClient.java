// CountPOAClient.java  Static Client, VisiBroker for Java

import org.omg.CosNaming.*;

import java.io.File;
import java.util.*;

class CountPOAClient
{
	public static void main(String args[])
	{
		int k = 1000;
		try
		{
			for(int i=0;i<k;i++){
				if(new File("myCount"+i+".state").exists())
					new File("myCount"+i+".state").delete();
			}
			
			// Initialize the ORB
			System.out.println("Initializing the ORB");
			org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

			// Get a reference to the Naming service
			org.omg.CORBA.Object nameServiceObj = 
					orb.resolve_initial_references ("NameService");
			if (nameServiceObj == null) 
			{
				System.out.println("nameServiceObj = null");
				return;
			}

			org.omg.CosNaming.NamingContext nameService = 
					org.omg.CosNaming.NamingContextHelper.narrow (nameServiceObj);
			if (nameService == null) 
			{
				System.out.println("nameService = null");
				return;
			}

			CounterPOA.Count[] counter = new CounterPOA.Count[k+1];
			for(int i=0;i<k;i++){

				// resolve the Count object in the Naming service
				NameComponent[] myCount = {new NameComponent("myCount"+i, "")};
				counter[i] = CounterPOA.CountHelper.narrow(nameService.resolve(myCount));
			}


			// Set sum to initial value of 0
			for(int i=0;i<k;i++)
				counter[i].sum((int)0);

			int totalSum = 0;

			// Calculate Start time
			long startTime = System.currentTimeMillis();

			// Increment 1000 times
			System.out.println("Incrementing");
			for (int i = 0 ; i < 10000 ; i++ )
			{
				Random rand = new Random();
				System.out.println("\nCount ::::::::"+i);
				counter[rand.nextInt(k)].increment();
			}
			

			for (int i = 0 ; i < k ; i++ )
				totalSum+=counter[i].sum();

			// Calculate stop time; print out statistics
			long stopTime = System.currentTimeMillis();
			System.out.println("Avg Ping = "
					+ ((stopTime - startTime)/10000f) + " msecs");
			System.out.println("Avg. Sum = " + totalSum/k);
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Exception");
			System.err.println(e);
		}
	}
}