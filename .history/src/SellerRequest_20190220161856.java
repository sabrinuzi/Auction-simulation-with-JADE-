package sabri\src;

import java.util.Date;

public class SellerRequest {
	public String seller;
	
	// store last time that a buyer requested information to a seller about a product
	public Date lastRequest;
	
	// this tells how many minutes later the buyer is going to bid in seconds
	public long bidTime;
}
