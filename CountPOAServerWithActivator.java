// CountPOAServerWithActivator.java: The Count Server main program

import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import java.io.*;

class CountPOAServerWithActivator
{
	static public void main(String[] args)
	{
		int k = 1000;
		try
		{
			// Initialize the ORB
			org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

			// get a reference to the root POA
			POA rootPOA =
					POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

			// Create policies for our persistent POA
			org.omg.CORBA.Policy[] policies = {
					rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT),
					rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
					rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN),
					rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER)
			};

			// Create myPOA with the right policies
			final POA myPOA = rootPOA.create_POA( "count_poa", rootPOA.the_POAManager(), policies );

			org.omg.CORBA.Object countObj[] = new org.omg.CORBA.Object[k+1];
			for(int i=0;i<k;i++){
				// Decide on the ID for the servant
				final byte[] countId = ("myCount"+i).getBytes();

				// Generate an Object Reference, but _not_ a servant (yet).
				countObj[i]=myPOA.create_reference_with_id(countId, CounterPOA.CountHelper.type().id() );
			}

			// Activate the POA manager
			rootPOA.the_POAManager().activate();
			
			// Assign the servant manager
			CountManagerActivator mgr = new CountManagerActivator();
			myPOA.set_servant_manager( mgr._this(orb) );

			// get a reference to the Naming Service root context
			org.omg.CORBA.Object nameServiceObj =
					orb.resolve_initial_references("NameService");
			if (nameServiceObj == null)
			{
				System.out.println("nameServiceObj = null");
				return;
			}

			NamingContextExt nameService =
					NamingContextExtHelper.narrow(nameServiceObj);
			if (nameService == null)
			{
				System.out.println("nameService = null");
				return;
			}
			
			for(int i=0;i<k;i++){
				// bind the Count object in the Naming service
				NameComponent[] countName = {new NameComponent("myCount"+i, "")};
				nameService.rebind(countName, countObj[i]);
				System.out.println(countObj[i] + " is ready.");
			}

			// Wait for incoming requests
			orb.run();

		} catch(Exception e)
		{ e.printStackTrace();
		}
	}
}