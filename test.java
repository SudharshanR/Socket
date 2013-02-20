// CountManager.java: Servant Locator for count objects

import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.io.*;
import java.util.*;



public class test extends ServantActivatorPOA
{   
	private final int max_Actv_Objs = 500;

	private CountPOAServant[] servant = new CountPOAServant[max_Actv_Objs];
	Hashtable<Integer,String> active_Object_Table = new Hashtable<Integer,String>();

	Map<String,Integer> que_mp = Collections.synchronizedMap(new HashMap<String,Integer>());

	private int r1=-1;
	public boolean processed=true;		
	public test()
	{
		super();
		for(int i =0; i < max_Actv_Objs; i++)
		{
			servant[i] = new CountPOAServant();
			System.out.println("Servant "+ i+" is created");
		}
		//worker thread
		new Thread(
				new Runnable()
				{
					public void run()
					{      

						try{
							Thread.sleep(15000); 
						}
						catch(Exception exp)
						{
							exp.printStackTrace();
						}

						synchronized(que_mp){
							Iterator<String> it = que_mp.keySet().iterator();
							System.out.println("Thread is in Progress.......");
							while(it.hasNext())
							{
								String filename  = it.next();
								try{
									System.out.println("Filename = "+filename);
									FileOutputStream f_Out = new FileOutputStream(filename+".state");
									PrintWriter b_Out = new PrintWriter(new OutputStreamWriter(f_Out));
									int sum = (Integer)que_mp.get(filename);
									System.out.println(sum);
									b_Out.println(""+ sum);
									System.out.println("Sum saved as" + sum);
									b_Out.close();
									f_Out.close();
									it.remove();
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
							}
						}
					}

				}
				).start();

	}

	public synchronized void numGenerator()
	{
		r1++;
		r1=r1%max_Actv_Objs;
	}

	public Servant incarnate(byte[] objectID, POA poa)
	{
		numGenerator();

		synchronized (servant[r1]) {
			String strCountObjID =(String) active_Object_Table.get(r1);
			if(strCountObjID!=null)
			{
				byte[] objIDByteStream = strCountObjID.getBytes();
				try{
					poa.deactivate_object(objIDByteStream);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
				active_Object_Table.remove(r1);
				try{
					servant[r1].wait();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				} 	
			}
			active_Object_Table.put(r1, new String(objectID));	
		}
		System.out.println(".....incarnate called for object ID "+new String(objectID));

		System.out.println(".....reading state from file");
		try
		{
			servant[r1].sum(0);
			if(que_mp.containsKey(new String(objectID)))
			{
				servant[r1].sum((java.lang.Integer)(que_mp.get(new String(objectID))));
				que_mp.remove(new String(objectID));
			}
			else
			{
				FileInputStream f_In = new FileInputStream( new String(objectID)+".state");
				BufferedReader b_In = new BufferedReader(new InputStreamReader(f_In));
				int sum = Integer.parseInt(b_In.readLine());
				b_In.close();
				f_In.close();
				servant[r1].sum(sum);
				System.out.println(".....sum set to "+sum);
			}
		}
		catch(Exception E)
		{
			System.out.println(".....exception encountered during state restore.");
		}
		return servant[r1];
	}

	public void etherealize(byte[] objectID, POA poa, Servant servant, boolean cleanup_in_progress, boolean remaining_activations)
	{
		System.out.println(".....etherealize called for object ID "+new String(objectID));

		try
		{

			synchronized(servant)
			{
				que_mp.put(new String(objectID),(((CountPOAServant)(servant)).sum()));
				servant.notify();
			}
		}
		catch(Exception E)
		{
			System.out.println(".....exception encountered during state save.");
		}

	}
}
