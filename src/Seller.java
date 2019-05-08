package sabri;
import java.io.File;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import sabri.Product;

public class Seller extends SuperAgent {

	private static final long serialVersionUID = 1L;
	private static final int TICK_EVERY = 2000;

	public Properties props;
	
	public CopyOnWriteArrayList<Product> myProducts;
	public Seller agent;
	public Product prod;
	
	// my data
	public String id;
	public String lastChecked;
	
	protected void setup() {
		
		String myName = getLocalName();
		this.lastChecked = "";
		this.myProducts = new CopyOnWriteArrayList< Product >();
		
		register("seller", getLocalName());
	
		System.out.println("[~]" + getLocalName() + ": "+"Launched seller named:  " + myName);
		
		// The file syntax is "resources/<agentName>.conf"
		String fileName = "resources" + File.separator + myName + ".conf";
		
		/**
        * Get properties from file <agent name>.props The file contains all the
        * information to be load by a specific agent.
        */
		props = (Properties) getProps(fileName);
		this.id = props.getProperty("ID");
		String[] products = props.getProperty("pid").split(";");
		String[] prices = props.getProperty("prices").split(";");
		String[] timesEnd = props.getProperty("end_time").split(";");
		String[] steps = props.getProperty("step").split(";");
		
		// Get the product from the reaources file
		for (int i = 0; i < products.length; i++) {
			prod = new Product();
			prod.id = Integer.parseInt(products[i]);
            prod.price = Integer.parseInt(prices[i]);
            
			// For test purpose I use tthis date time  "15/12/08 23:12:20"; instead of timesEnd[i]; 
			prod.timeEnd = timesEnd[i]; 
			prod.bidStep = Integer.parseInt(steps[i]);
			prod.seller = this.getLocalName();
			prod.nrBids = 0;
			prod.sold = false;
			prod.views = new ArrayList<String>();
			this.myProducts.add(prod);
		}
		
		addBehaviour(new SellerTick(this, TICK_EVERY));
	}
	
	public void checkEnd() {	
		Iterator<Product> ite = myProducts.iterator();
		while (ite.hasNext()) {
			Product prod = ite.next();
			this.timeEnded(prod.id);
		}
    }
    
	// Check if product time ended
	public void timeEnded(int pid) {	
		
		int idx = getProductIndex(pid);
		Product currProd = getProduct(pid);
		
        Date now = new Date();
        int time = (int)now.getTime() / 1000;
        int endTime = (int)calculateRemaindTime(currProd.timeEnd.toString());
        if (endTime < 0) {
            if (!currProd.notCheck) {
                currProd.sold = true;
                currProd.notCheck = true;
                myProducts.set(idx, currProd);
                if (currProd.buyerBid!= null) {
                    System.out.println("\n[~]" + getLocalName() + " Bid time for product with ID=" + pid + " has ended and buyer: " + currProd.buyerBid + " won the product \n");
                    ACLMessage msgToBuyer = new ACLMessage(ACLMessage.CONFIRM); 
                    AID[] listBuyer = search("buyer", currProd.buyerBid.toString());
                    
                    if (listBuyer.length > 0) {
                        msgToBuyer.setSender(listBuyer[0]);
                        msgToBuyer.setContent("You won this product");
                        send(msgToBuyer);
                        System.out.println("[~]" + getLocalName()+": " + " buyer: " + currProd.buyerBid + " won product with ID=" + currProd.id + " and buyer is notified");
                    } else {
                        System.out.println("[~]" + getLocalName() + ": " + " buyer: " + currProd.buyerBid + " won product with ID=" + currProd.id + " but buyer could not be found");
                    }
                } else {
                    System.out.println("\n[~]" + getLocalName() + " Bid time for product with ID=" + pid + " has ended and no one won the product \n");
                }
            }	
        }	
	}
    
    /*
	* Searches if a product is found for this agent
    */
    public Product searchProduct(ArrayList<Product> list, int pid) {
		Product prod = new Product();
		boolean found = false;
		
		for (Product product : list) {
			if (product.id == pid) {
				System.out.print(" Price  " + prod.id);
                found = true;
                prod = product;
				break;
			}		
		}
		if (found) {
		    return prod;
        }
        
		return null;
	}
	
    /*
    * @return String  generates answer string to buyer
    */
    public String buyerAsking(int pid) {
		return null;
	}
    
	public long calculateRemaindTime(String d) {
		SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

		Date d1 = null;
		Date now = new Date();

        try {
			d1 = format.parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		long diff = d1.getTime() - now.getTime();
		long diffMinutes = (diff / (1000));

		return diffMinutes;
	}
	
	/**
	* This method generates answer string and action to buyer when he bids
	*/
	public void bidToProduct(int pid,int money,String buyer) {
		
		int idx = this.getProductIndex(pid);
		Product currProd = this.getProduct(pid);
		currProd.nrBids = currProd.nrBids+1;
		currProd.price = currProd.price+money;
		currProd.buyerBid = buyer;
		myProducts.set(idx, currProd);
	}
    
    /**
     * inesrts a new viewer in product
     * 
     * @param pid
     * @param buyer
    */
	public void insertViewer(int pid,String buyer) {
        boolean insert=true;
        Product prod = getProduct(pid);	
        
        if (prod.views.size() > 0) {
            for(String b:prod.views) {
                if (b.equals(buyer)) {
                    insert = false;
                    break;
                }
            }
        }

        if (insert) {
            prod.views.add(buyer);
            int idx=this.getProductIndex(pid);
            myProducts.set(idx, prod);
        } 	
    }
		
	/** 
	* This method generates answer string and action to buyer when he bids 
	*/
	public String buyProduct(int pid) {
		return null;
	}

	/**
     * Get the product Seller is searching for
     * 
     * @param pid
     * @return Product
     */
	public Product getProduct(int pid) {
		Product product = new Product();
		for (Product p : myProducts) {
			if (p.id == pid) {
				prod = p;
				break;
			}	
        }
        
		return proproductd;
	}
	
	/**
	* @return index for given product
	*/
	public int getProductIndex(int pid) {
		int idx = 0;
		for (Product product : myProducts) {
			if (product.id == pid) {
				break;
			} 
			idx++;
        }
        
		return idx;
	}
}
