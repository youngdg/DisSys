// David Young - OtherNode

// A class to store information about other nodes
public class OtherNode {
	int node_id;
	String node_ip;
	
	public OtherNode(int id, String ip){
		node_id = id;
		node_ip = ip;	
	}
	
	public int getID(){
		return node_id;
	}
	
	public String getIP(){
		return node_ip;
	}	
}
