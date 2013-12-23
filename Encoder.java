import java.io.IOException;
import java.io.StringWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.*;

// A class to create JSON messages
public class Encoder {

	public Encoder(){
		
	}
	// JSON message is created to join a network
	byte[] join(int nodeidentifier, int target, InetSocketAddress ip){
		byte[] sendData = new byte[1024];
		JSONObject obj=new JSONObject();
		obj.put("type", "JOINING_NETWORK_SIMPLIFIED");
		obj.put("node_id", nodeidentifier);
		obj.put("target_id", target);
		obj.put("ip_address", ip.getAddress().toString());	
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		sendData = jsonText.getBytes();
		return sendData;
	}
	
	// A JSON message is created for join network relay
	byte[] joinnetrelay(int nodeidentifier, int target, int gateid){
		byte[] sendData = new byte[1024];
		JSONObject obj=new JSONObject();
		obj.put("type", "JOINING_NETWORK_RELAY_SIMPLIFIED");
		obj.put("node_id", nodeidentifier);
		obj.put("target_id", target);
		obj.put("gateway_id", gateid);		
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		sendData = jsonText.getBytes();
		return sendData;
	}
	
	// A JSON message is created for leaving the network
	byte[] leave(int nodeidentifier){
		byte[] sendData = new byte[1024];
		JSONObject obj=new JSONObject();
		obj.put("type", "LEAVING_NETWORK");
		obj.put("node_id", nodeidentifier);	
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		sendData = jsonText.getBytes();
		return sendData;
	}
	
	// Routing info is parsed and then included in a JSON message
	byte[] routinginfo(int node_id, int target, String ip, ArrayList<OtherNode> oth){
		byte[] sendData = new byte[1024];	
		JSONObject obj=new JSONObject();
		List l1 = new LinkedList();
		obj.put("type", "ROUTING_INFO");
		obj.put("node_id", node_id);
		obj.put("target_id", target);
		obj.put("ip", ip);
		obj.put("route_table", l1);
		for(int i = 0; i<oth.size(); i++){
			Map m1 = new LinkedHashMap();
			m1.put("node_id", oth.get(i).getID());
			m1.put("ip_address", oth.get(i).getIP());
			l1.add(m1);
		}
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		sendData = jsonText.getBytes();
		return sendData;
	}
	
	// A JSON message is created to index a work
	byte[] createIndex(int target, int sender, String word, String url){
		byte[] sendData = new byte[1024];	
		JSONObject obj=new JSONObject();
		obj.put("type", "INDEX");
		obj.put("target_id", target);
		obj.put("sender_id", sender);
		obj.put("keyword", word);
		obj.put("links", url);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		sendData = jsonText.getBytes();
		return sendData;
	}
	
	// A JSON message is created to do a search on words
	byte[] search(String word, int node_id, int send_id){
		byte[] sendData = new byte[1024];	
		JSONObject obj=new JSONObject();
		obj.put("type", "SEARCH");
		obj.put("word", word);
		obj.put("node_id", node_id);
		obj.put("sender_id", send_id);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		sendData = jsonText.getBytes();
		return sendData;
	}
	
	byte[] searchResults(String word, int node_id, int send_id, Hashtable<String, Integer> wordIndex){
		byte[] sendData = new byte[1024];	
		JSONObject obj=new JSONObject();
		List l1 = new LinkedList();
		obj.put("type", "SEARCH_RESPONSE");
		obj.put("word", word);
		obj.put("node_id", node_id);
		obj.put("sender_id", send_id);
		obj.put("response", l1);
		Iterator<Entry<String, Integer>> it = wordIndex.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, Integer> entry = it.next();
			Map m1 = new LinkedHashMap();
			m1.put("url", entry.getKey());
			m1.put("ip_address", entry.getValue());
			l1.add(m1);
		}
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		sendData = jsonText.getBytes();
		return sendData;
	}
	
	byte[] ackIndex(int node, String word){
		byte[] sendData = new byte[1024];	
		JSONObject obj=new JSONObject();
		obj.put("type", "ACK_INDEX");
		obj.put("node_id", node);
		obj.put("keyword", word);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		sendData = jsonText.getBytes();
		return sendData;
	}
	
	byte[] ping(int target, int sender, int ip){
		byte[] sendData = new byte[1024];	
		JSONObject obj=new JSONObject();
		obj.put("type", "PING");
		obj.put("target_id", target);
		obj.put("sender_id", sender);
		obj.put("sender_id", ip);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		sendData = jsonText.getBytes();
		return sendData;
	}
	
	byte[] ack(int node_id, int ip){
		byte[] sendData = new byte[1024];	
		JSONObject obj=new JSONObject();
		obj.put("type", "ACK");
		obj.put("target_id", node_id);
		obj.put("sender_id", ip);
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();
		sendData = jsonText.getBytes();
		return sendData;
	}
}
