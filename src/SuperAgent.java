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
		} else {
			ServiceDescription serviceDescription = new ServiceDescription();
			if (name != null) {
                serviceDescription.setName(name);
            }
			serviceDescription.setType(type);
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			dfd.addServices(serviceDescription);
			
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
		
		ServiceDescription serviceDescription = new ServiceDescription();
		if (name != null) {
            serviceDescription.setName(name);
        }
		if (type != null){
            serviceDescription.setType(type);
        }
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(serviceDescription);
		
		DFAgentDescription[] aidDes = null;
		AID[] aids = null;
		
		try {
			aidDes = DFService.search(agent, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		if (aidDes != null && aidDes.length > 0) {
			aids = new AID[aidDes.length];
			for (int i = 0; i < aidDes.length; i++) {
				aids[i] = aidDes[i].getName(); 
			}
		}
		return aids;
	}
	
	
	public AID[] search(String type, String name){
		
		ServiceDescription serviceDescription = new ServiceDescription();
		if (name != null) serviceDescription.setName(name);
		if (type != null) serviceDescription.setType(type);
		
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(serviceDescription);
		
		DFAgentDescription[] aidDes = null;
		AID[] aids = null;
		
		try {
			aidDes = DFService.search(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		if (aidDes != null && aidDes.length > 0) {
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
