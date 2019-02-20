package sabri;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SuperAgent extends Agent {

	private static final long serialVersionUID = 1L;
	
	protected boolean register(String type, String name){
		
		boolean ret = true;
		
		if (type == null){
			ret = false;
		}
		else{
			ServiceDescription sd = new ServiceDescription();
			if (name != null)
				sd.setName(name);
			sd.setType(type);
			
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			dfd.addServices(sd);
			
			try {
				DFService.register(this, dfd);
			} catch (FIPAException e) {
				e.printStackTrace();
				ret = false;
			}
		}
		
		return ret;
	}
	
	
	public static AID[] search(Agent agent, String type, String name){
		
		ServiceDescription sd = new ServiceDescription();
		if (name != null) sd.setName(name);
		if (type != null) sd.setType(type);
		
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(sd);
		
		DFAgentDescription[] aidDes = null;
		AID[] aids = null;
		
		try {
			aidDes = DFService.search(agent, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		if (aidDes != null && aidDes.length > 0){
			aids = new AID[aidDes.length];
			for (int i = 0; i < aidDes.length; i++){
				aids[i] = aidDes[i].getName(); 
			}
		}
		return aids;
	}
	
	
	public AID[] search(String type, String name){
		
		ServiceDescription sd = new ServiceDescription();
		if (name != null) sd.setName(name);
		if (type != null) sd.setType(type);
		
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(sd);
		
		DFAgentDescription[] aidDes = null;
		AID[] aids = null;
		
		try {
			aidDes = DFService.search(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		if (aidDes != null && aidDes.length > 0){
			aids = new AID[aidDes.length];
			for (int i = 0; i < aidDes.length; i++){
				aids[i] = aidDes[i].getName(); 
			}
		}
		return aids;

	}
	public static Properties getProps(String configFilePath) {

		Properties props = new Properties();
		try {
			FileInputStream inputFile = new FileInputStream(configFilePath);

			if (inputFile != null) {
				props.load(inputFile);
			}
			inputFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

}
