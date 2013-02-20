// CountManager.java: Servant Locator for count objects

import org.omg.PortableServer.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CountManagerActivator extends ServantActivatorPOA
{
	int k = 500;
	int activeobjectCount = 0;
	public String[] AOM = new String[k]; 
	CountPOAServant MyCount;
	Semaphore s1 = new Semaphore(1, true);
	Semaphore s2 = new Semaphore(1, true);
	LinkedList<State> queue = new LinkedList<State>();
	State s;

	public CountManagerActivator() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1 );
		 Runnable task = new Runnable() {
					public void run()
					{
						try
						{
							while(!queue.isEmpty()) {
								s = queue.pop();
								FileOutputStream f_Out = new FileOutputStream(s.getName());
								PrintWriter b_Out = new PrintWriter(new OutputStreamWriter(f_Out));
								b_Out.println(""+s.getSum());
								b_Out.close();
								f_Out.close();
								s2.release();

								System.out.println(".....sum saved as "+s.getSum());
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}

					}
				};
				
				scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.MILLISECONDS);
		
		/*ScheduledFuture<?> sf = scheduler.scheduleAtFixedRate(
				new Runnable() {
					public void run() {
						for(Iterator<String> it = queue.keySet().iterator(); it.hasNext();) {
							key = it.next();
							value = queue.get(key);
							queue.remove(key);
							try
							{
								System.out.println("FILE ::::::::"+key);
								System.out.println("Value::::::::"+value);
								FileOutputStream f_Out = new FileOutputStream(key);
								PrintWriter b_Out = new PrintWriter(new OutputStreamWriter(f_Out));
								b_Out.println(""+value);
								b_Out.close();
								f_Out.close();

								System.out.println(".....sum saved as "+value);
							}
							catch(Exception E)
							{
								System.out.println(".....exception encountered during state save.");
								E.printStackTrace();
							}
						}
					}
				},
				0,  1,TimeUnit.MICROSECONDS );*/		
	}

	public Servant incarnate(byte[] objectID, POA poa)
	{
		System.out.println(".....incarnate called for object ID "+new String(objectID));
		System.out.println(".....creating new servant");

		if(activeobjectCount<k){
			MyCount = new CountPOAServant();
			AOM[activeobjectCount] = new String(objectID);
			updateObjectMap(objectID);
		}
		else{
			try{
				s1.acquire();
				Random rand = new Random();
				s2.acquire();
				int aomObject = rand.nextInt(k);
				poa.deactivate_object(AOM[aomObject].getBytes());
				s2.acquire();
				MyCount = new CountPOAServant();
				AOM[aomObject] = new String(objectID);
			}catch (Exception e) {
				e.printStackTrace();
			}
			updateObjectMap(objectID);	
			s2.release();
		}

		s1.release();
		activeobjectCount++;
		return MyCount;
	}

	public void etherealize(byte[] objectID, POA poa, Servant servant, boolean cleanup_in_progress, boolean remaining_activations)
	{
		System.out.println(".....etherealize called for object ID "+new String(objectID));

		/*try
		{
			FileOutputStream f_Out = new FileOutputStream( new String(objectID)+".state");
			PrintWriter b_Out = new PrintWriter(new OutputStreamWriter(f_Out));
			int sum = ((CountPOAServant)(servant)).sum();
			b_Out.println(""+sum);
			b_Out.close();
			f_Out.close();
			s2.release();

			System.out.println(".....sum saved as "+sum);
		}
		catch(Exception E)
		{
			System.out.println(".....exception encountered during state save.");
			E.printStackTrace();
		}*/
		try
		{
			int sum = ((CountPOAServant)(servant)).sum();
			queue.push(new State(new String(objectID)+".state", sum));

			System.out.println(".....sum saved as "+sum);
		}
		catch(Exception E)
		{
			System.out.println(".....exception encountered during state save.");
			E.printStackTrace();
		}

	}
	
	public void updateObjectMap(byte[] objectID){

		
		if(new File(new String(objectID)+".state").exists()){
			System.out.println(".....reading state from file");
			try
			{
				FileInputStream f_In = new FileInputStream( new String(objectID)+".state");
				BufferedReader b_In = new BufferedReader(new InputStreamReader(f_In));
				int sum = Integer.parseInt(b_In.readLine());
				b_In.close();
				f_In.close();

				MyCount.sum(sum);
				System.out.println(".....sum set to "+sum);
			}
			catch(Exception E)
			{
				System.out.println(".....exception encountered during state restore.");
				E.printStackTrace();
			}
		}
		else{
			try {
				File f = new File(new String(objectID)+".state");
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		
	}
}