package sabri\src;

import java.sql.Date;
import java.util.ArrayList;

public class Product implements java.io.Serializable{

	public int id;
	public int price;
	public String time_end;
	public int nrBids=0;	
	public int bidStep;
	public String seller;
	public String buyerBid;
	public boolean sold=false;
	public ArrayList<String> views;
	public int nrViewrs=0;
	
	public int nrLastViews=0;
	public int nrLastBid=0;
	
	public boolean notCheck=false;
	
	public ArrayList<Double> probability_average=new ArrayList<Double>();
	
	
}
