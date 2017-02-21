package new_ex;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import new_ex.Seller;

public class SellerTick extends TickerBehaviour {

	private static final long serialVersionUID = 3487495895819000L;
	private Seller MeAgent;
	
	public Properties props;
	String product_id;
	int check_every;

	public SellerTick(Seller agent, long time){

		super (agent, time);

		this.MeAgent = agent;

	}

	@Override
	protected void onTick() {
		
		ACLMessage ansMsg;
		ACLMessage msg = MeAgent.receive();
		String answer;
			
		// in case of any message
		if (msg != null && msg.getContent()!=null){
			
			System.out.println("[~]"+ MeAgent.getLocalName()+": "+" is replying to buyer "+ msg.getSender().getLocalName() +" about product "+msg.getContent().split(";")[0] +" to "+ msg.getSender().getLocalName());
			ansMsg = msg.createReply();
			// type of message
			int perform = msg.getPerformative();
			// content of message
			String content = msg.getContent();
			
			// if a buyer is asking for any product
			if (perform == ACLMessage.INFORM_IF){
				
				// check if product's time ended
				MeAgent.timeEnded(Integer.parseInt( content));
				
				Product prod= searchProduct(MeAgent.myProducts,Integer.parseInt( content));

				Serializable result=(Serializable) prod;
				// if product if found inform buyer
				if(result!=null){
					if(prod.sold==true){
						answer="sold";
						ansMsg.setPerformative(ACLMessage.INFORM);
					}
					else{
						MeAgent.insertViewer(Integer.parseInt( content) , msg.getSender().getLocalName());
						answer="yes";
						ansMsg.setPerformative(ACLMessage.INFORM);
					}
				}
				// if product not found
				else{
					ansMsg.setPerformative(ACLMessage.INFORM);
					answer="no";
				}
				
				try {
					ansMsg.setContentObject(result);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				MeAgent.send(ansMsg);
				
			}
			else if(perform == ACLMessage.PROPOSE){
				// structure of message is
				// product_id;bid_money(step);curr_nrBids;
				String[] bid=msg.getContent().split(";");
				
				System.out.println("[~]"+ MeAgent.getLocalName()+": "+msg.getSender().getLocalName()+" has requested a bid for product "+bid[0]+" to "+ MeAgent.getLocalName());
				int nrCurrBids=((Seller) MeAgent).getProduct(2).nrBids;  //Integer.parseInt(bid[0])
				int nrLastBids=Integer.parseInt(bid[2]);
				int money=Integer.parseInt(bid[1]);
				int pid=Integer.parseInt(bid[0]);
				String buyer=msg.getSender().getLocalName();
				//if no had placed a bid during my last request for info and now
				if (!(nrCurrBids>nrLastBids)){
					MeAgent.bidToProduct(pid,money,buyer);
					Product product_now=MeAgent.getProduct(pid);
					System.out.println("[~]"+ MeAgent.getLocalName()+": "+" sais: After bid => product's price is: "+product_now.price+" Nr. of bids: "+product_now.nrBids+" from buyer: "+msg.getSender().getLocalName() );
					 
				}
			}	
		}	
		
		MeAgent.check_end();
	}
	
	
	// this method checks product list for product that time ended
	public void timeEnded(ArrayList<Product> list){
		Product prod=new Product();
		
	
		for (Product product : list) {
			Date now=new Date();
			//int time=(int)now.getTime()/1000;
			int endTime=(int)MeAgent.calculateRemaindTime(product.time_end.toString());
			if(endTime<0){
				prod=product;
				System.out.println("[~]"+ MeAgent.getLocalName()+": "+" Bid time for product with ID="+product.id+" has ended and buyer: "+product.buyerBid+" won the product");
				int idx=MeAgent.getProductIndex(product.id);
				prod.sold=true;
				MeAgent.myProducts.set(idx,prod);
				ACLMessage msgToBuyer= new ACLMessage(ACLMessage.CONFIRM);
				AID[] listBuyer = MeAgent.search("buyer", product.buyerBid.toString());
				if (listBuyer.length > 0) {
					msgToBuyer.setSender(listBuyer[0]);
					msgToBuyer.setContent("You won this product");
					MeAgent.send(msgToBuyer);
					System.out.println("[~]"+ MeAgent.getLocalName()+": "+" buyer: "+product.buyerBid+" won product with ID="+product.id+" and buyer is notified");
					
				}
				else System.out.println("[~]"+ MeAgent.getLocalName()+": "+" buyer: "+product.buyerBid+" won product with ID="+product.id+" but buyer could not be found");
				
			}		
		}
		
	}
	
	
	// search a product in the given list and returns it
	public Product searchProduct(CopyOnWriteArrayList<Product> myProducts,int pid){
		Product prod=new Product();
		boolean found=false;
	
		for (Product product : myProducts) {
			if(product.id==pid)
			{
				prod=product;
				found=true;
				break;
			}	
		}
		if(found){
			return prod;
		}
		else return null;
	}
}
