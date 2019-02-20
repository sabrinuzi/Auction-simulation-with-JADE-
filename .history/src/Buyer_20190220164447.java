package sabri;

import jade.core.AID;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.introspection.AddedBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class Buyer extends SuperAgent {

	public Properties props;
	private static final long serialVersionUID = 1L;
	private static final int SECOND_MILLIS = 1000;
	private static final int TICK_EVERY = 2000;
	public final static long MINUTE_MILLIS = SECOND_MILLIS * 60;
	/**
	 * The main method
	 */
	// my data
	public String id;
	public int pid;
	public int budget;

	public int tick_every;

	public ArrayList<Product> myProducts;
	/*
	* A List to keep all requests made to a seller.
	*/
	public ArrayList<SellerRequest> myRequests;
	public Date lastRequest;
	
	/*
	* this specified the way how to calculate the time of the next bid in buyerTick
	* 1- time remained devided by 2
	* 2- Bid per time remained
	*/
	public int bidMethod;
	
	protected void setup() {

		String myName = getLocalName();
		this.lastRequest=new Date();
		myProducts = new ArrayList<Product>();
		this.myRequests = new ArrayList<SellerRequest>();
		String[] agentType = { "buyer", "seller" };
		register(agentType[0], myName);
		tick_every = 2000;

		System.out.println("Launched buyer named: " + myName);
		
		/*
		* The file syntax is "resources/<agentName>.conf"
		 * Get properties from file <agent name>.props The file contains all the
		 * information to be load by a specific agent.
		 */
		String fileName = "resources" + File.separator + myName + ".conf";
		
		props = (Properties) getProps(fileName);
		this.id = props.getProperty("ID");
		this.pid = Integer.parseInt(props.getProperty("pid"));
		this.budget = Integer.parseInt(props.getProperty("myBudget"));
		this.bidMethod = Integer.parseInt(props.getProperty("myBidMethod"));
		System.out.println("My ID: " + id+" my budget is: "+budget);
		
		SequentialBehaviour sb = new SequentialBehaviour();
		
		/*
		* run after 2 seconds
		* when agents wakes up send to all seller product id he is interested and waits for info
		*/
		WakerBehaviour wb = new WakerBehaviour(this, 2000) {
		
			protected void onWake() { 
				AID[] list = search("seller", null);
				if (list.length > 0) {

					// INFORM_IF means if the seller has this product for sale
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM_IF);
					
					msg.setContent(pid + "");
					
					SellerRequest request;
					for (int i = 0; i < list.length; i++) {
						if (!list[i].equals(getAID())){
							msg.addReceiver(list[i]);
							System.out.println("[~] "+myName+": is asking seller "+list[i].getLocalName()+" about product  with id=" + pid);

						}
						request=new SellerRequest();
						request.seller=list[i].getLocalName();
						request.lastRequest=new Date();
					
						myRequests.add(request);
							
					}
					//update last request to current date time;
					lastRequest=new Date();
					
					send(msg);
				}
			}

		};

		TickerBehaviour tb = new BuyerTick(this, tick_every);
		sb.addSubBehaviour(wb);
		sb.addSubBehaviour(tb);
		addBehaviour(sb);
	}
	
	// removes from the list the product that its time is ended
	public void timeEnded(int pid,String seller){	
		
		if(ProductExists(pid,seller)){
			int idx = getProductIndex(pid,seller);
			Product currProd = getProduct(pid,seller);
			
			Date now = new Date();
			int time = (int)now.getTime()/1000;
			int endTime = (int)calculateRemaindTime(currProd.timeEnd.toString());
			if(endTime < 0){
				System.out.println("[~]"+ getLocalName()+": "+" is removing from his list product "+pid);
				myProducts.remove(idx);
				
			}	
		}
	}
	
	// this method takes and date string and calculates the time remained till the product time ends
	// method return time in minutes
	public long calculateRemaindTime(String d) {

		SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
		Date d1 = null;
		Date now = new Date();
		Date d2 = null;
		try {
			d1 = format.parse(d); //"18/09/15 10:00:00"
		} catch (ParseException e) {
			e.printStackTrace();
		}

		long diff = d1.getTime() - now.getTime();
		long diffMinutes = (diff / (1000));
		return diffMinutes;
	}
	
	// checks if product exists in buyer's list of products
	public boolean ProductExists(int pid,String seller) {
		boolean found = false;
		for (Product product : myProducts) {
			if (product.id == pid && product.seller.equals(seller)) {
				found = true;
				break;
			} 
		}
		return found;
	}

	
	// checks product list and returns the prduct for the given product ID, 
	public Product getProduct(int pid,String seller) {
		Product p = new Product();
		for (Product product : myProducts ) {
			if (product.id == pid  && product.seller.equals(seller)) {
				p = product;
				break;
			}
		}
		return p;
	}
	

	// returns the product index at buyer's list of products for the given product ID
	public int getProductIndex(int pid,String seller) {
		int idx = 0;
		 outerloop:
		for (Product product : myProducts) {
			if (product.id == pid && product.seller.equals(seller)) {
				return idx;

			} 
			idx++;
		}
		return 0;
	}

	// returns the last request of buyer, made to the given seller, 
	public Date getLastReuestForSeller(String seller) {
		Date s = null;
		for (SellerRequest request : myRequests) {
			if (request.seller.equals(seller)) {
				s = request.lastRequest;
				break;
			}
		}
		return s;
	}

	// return the index of the time request for the given seller,
	public int getRequestIndex(String seller) {
		int idx = 0;
		for (SellerRequest request : myRequests) {
			if (request.seller.equals(seller)) {
				return idx;	
			}
			else
				idx++;
		}
		return idx;
	}
	
	// return and random int, within the range of min max given in parameters
	public static int randInt(int min, int max) {

	    Random rand = new Random();

	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}

}
