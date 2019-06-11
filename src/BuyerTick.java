package sabri;

import java.util.ArrayList;
import java.util.Date;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Properties;

public class BuyerTick extends TickerBehaviour {
    
    public Properties props;
    private Buyer MeAgent;
    private static final long serialVersionUID = 1L;

    public BuyerTick(Buyer agent, long time) {
        super (agent, time);
        this.MeAgent = agent;
    }
    
    protected void onTick() {
        ACLMessage msg ;
        // Checks for message
        msg = MeAgent.receive();
        
        // If there is any read the content
        if (msg != null) {
            Object obj = new Object();
            System.out.println(MeAgent.getLocalName() + "  there is a message- msg performative=" + msg.getPerformative() + "  my perf should be=" + ACLMessage.CONFIRM);
            // in case of any information messages from sellers
            if (msg.getPerformative()==ACLMessage.INFORM) {
                try {
                    obj = msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                
                if (obj == null || ((Product) obj).sold) {
                    System.out.println("[~]" + MeAgent.getLocalName() + " : " + msg.getSender().getLocalName() + "  sais he does not have such a product or may be sold ");
                    if (obj!=null) {
                        Product product = (Product)obj;
                        MeAgent.timeEnded(product.id,product.seller);
                    }

                } else {
                    Product product = (Product) obj;
                    System.out.println("[~]" + MeAgent.getLocalName() + " : " + msg.getSender().getLocalName() + "  sais he has such a product with price " + product.price + "  and will end in " + MeAgent.calculateRemaindTime(product.timeEnd) + "  seconds");
                    
                    //update product list
                    if (MeAgent.productExists(product.id,msg.getSender().getLocalName())) {
                        //if I have money to continue the bid marathon
                        if (MeAgent.budget - product.bidStep >= product.price ) {
                            //if I am the last one who bid do not bid again
                            if (MeAgent.getLocalName().equals(product.buyerBid)) {
                                
                                System.out.println("[~]" + MeAgent.getLocalName() + " : sais: I am not biding again to " + msg.getSender().getLocalName() + " , I am the last one who did on this product ");                                    
                                // updates the requests list for this seller
                                SellerRequest sr = new SellerRequest();
                                sr.lastRequest = new Date();
                                sr.seller = product.seller;
                                
                                int moneyLeft = MeAgent.budget-product.price;
                                int spIdx = MeAgent.getRequestIndex(product.seller);
                                int bidStep = product.bidStep;
                                long timeLeft = MeAgent.calculateRemaindTime(product.timeEnd);
                                long  bidsLeft = moneyLeft/bidStep;
                                long bidPerSeconds = timeLeft/bidsLeft;
                                // here the agent decide to bid acording to the method he is programmed to think from the creation.
                                switch (MeAgent.bidMethod) {
                                    case 1:
                                        sr.bidTime = timeLeft / 2;
                                        break;
                                    case 2:
                                        sr.bidTime = bidPerSeconds;
                                        break;
                                    case 3:
                                        Date now = new Date();
                                    default:
                                        break;
                                }
                                
                                // update last requests to current seller
                                MeAgent.myRequests.set(spIdx,sr);
                            
                            // Think to bid or not to bid :)
                            } else {
                                Product currProduct = MeAgent.getProduct(product.id, product.seller);
                                int bidDifferenc = product.nrBids - currProduct.nrBids;
                                int priceDiff = product.price - currProduct.price;
                                
                                int idx = MeAgent.getProductIndex(product.id, product.seller);
                                MeAgent.myProducts.set(idx, product);
                                
                                int moneyLeft = MeAgent.budget-product.price;
                                int bidStep = product.bidStep;
                                long timeLeft = MeAgent.calculateRemaindTime(product.timeEnd);
                                long bidsLeft = moneyLeft / bidStep;
                                long bidPerSeconds = timeLeft / bidsLeft;
                                
                                // Updates the requests list for this seller
                                SellerRequest sr = new SellerRequest();
                                sr.lastRequest = new Date();
                                sr.seller = product.seller;
                                int spIdx = MeAgent.getRequestIndex(product.seller);
                                
                                // Here the agent decide to bid acording to the method he is programmed to think from the creation.
                                switch (MeAgent.bidMethod) {
                                case 1:
                                    sr.bidTime = timeLeft / 2;
                                    break;
                                case 2:
                                    sr.bidTime = bidPerSeconds;
                                    break;
                                case 3:
                                    Date now = new Date();
                                default:
                                    break;
                                }
                                
                                // Update last requests to current seller
                                MeAgent.myRequests.set(spIdx, sr); 
                                // Get a rando number, probability
                                double  propability = Math.random();
                                int bids = currProduct.nrBids - currProduct.nrLastBid;
                                int views = currProduct.nrViewrs - currProduct.nrLastViews;
                                double bViews;
                                
                                // Calculate bid per viewers ration
                                if (views != 0) {
                                    bViews = bids / views;
                                } else {
                                    bViews = 0;
                                }
                                
                                double toBid = bViews * propability;
                                currProduct.probabilityAverage.add(toBid);
                                
                                double sum = 0;
                                for (double v:currProduct.probabilityAverage) {
                                    sum += v;
                                }
                                double ave = sum / currProduct.probabilityAverage.size();
                                double threshold = ave * 0.5;
                                if (toBid >= threshold) {

                                }
                                
                                // update product with current values
                                currProduct.nrLastBid = currProduct.nrBids;
                                currProduct.nrLastViews = currProduct.nrViewrs;
                                int indx = MeAgent.getProductIndex(currProduct.id, currProduct.seller);
                                MeAgent.myProducts.set(indx,currProduct);
                                
                                ACLMessage ans = msg.createReply();
                                ans.setPerformative(ACLMessage.PROPOSE);
                                
                                /*
                                * structure of message is
                                * product_id;bid_money(step);curr_nrBids;
                                */
                                ans.setContent(product.id  + " ;" + product.bidStep  + " ;" + product.nrBids);
                                MeAgent.send(ans);    
                            }

                        } else {
                            // if buyer do not have enough money, remove the product.
                            System.out.println("[~]" + MeAgent.getLocalName() + " : " + " is Removing product ");
                            
                            int idx = MeAgent.getProductIndex(product.id,product.seller);
                            MeAgent.myProducts.remove(idx);
                            System.out.println("[~]" + MeAgent.getLocalName() + " : " + " is removing from my list product " + product.id + "  ; his budget:" + MeAgent.budget + "  but product price is: " + product.price);    
                        }
                    } else {
                        //add for the first  time product to my list
                        MeAgent.myProducts.add(product);
                    }
                }
            } else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                System.out.println("[~]" + MeAgent.getLocalName()  + " : " + " content=" + msg.getContent()  + "  I won the product from seller " + msg.getSender().getLocalName());    
                this.stop();
            }
        }
        /*
        * System.out.println("MeAgent.myProducts.length=" + MeAgent.myProducts.size());
        * collect information from sellers 
        */
        for (Product product : MeAgent.myProducts) {
            // check if it's time to ask seller for product information
            System.out.println("MeAgent.myProduct=" + product.buyerBid);
            Date now = new Date();
            
            long diff = Math.abs( MeAgent.getLastReuestForSeller(product.seller).getTime() - now.getTime()) / 1000;
            int pid = product.id;
            long timeToRequest = MeAgent.myRequests.get(MeAgent.getRequestIndex(product.seller)).bidTime;
        
            System.out.println("[~]" + MeAgent.getLocalName() + " : next seller request is after " + timeToRequest + "  seconds");
            if (diff > timeToRequest) {
                AID[] list = MeAgent.search("seller", product.seller);
                if (list.length > 0) {
                    // INFORM_IF means if the seller has this product for sale
                    ACLMessage msgToSeller = new ACLMessage(ACLMessage.INFORM_IF);
                    msgToSeller.setContent(pid + "");
                    
                    for (int i = 0; i < list.length; i++) {
                        if (!list[i].equals(MeAgent.getAID())) {
                            msgToSeller.addReceiver(list[i]);
                        }
                    }
                    
                    //update last request to current date time;
                    SellerRequest sellerRequest = new SellerRequest();
                    sellerRequest.lastRequest = new Date();
                    sellerRequest.seller = product.seller;
                    int idx = MeAgent.getRequestIndex(product.seller);
                    MeAgent.myRequests.set(idx, sellerRequest);
                    System.out.println("[~]" + MeAgent.getLocalName()  + " : " + " i am asking again " + product.seller  + "  for PRODUCT: " + product.id);
                    MeAgent.send(msgToSeller);
                }
            }
        }
    }  
}
