import org.json.simple.*;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.*;
import java.util.*;
import java.io.*;


public class Node implements PeerSearch {
	int nodeidentifier;
	InetSocketAddress ip;
	ArrayList<OtherNode> routing_table = new ArrayList<OtherNode>();
	Hashtable<String, Integer> wordIndex = new Hashtable<String, Integer>();
	protected DatagramSocket socket; 
	ArrayList<OtherNode> node_addresses = new ArrayList<OtherNode>();

	public Node(String name) {
		nodeidentifier = hashCode(name);
	}
	
	public Node(String name, InetSocketAddress joinip){
		nodeidentifier = hashCode(name);
		ip = joinip;
	}

	@Override
	// A new node is initialised
	public void init(DatagramSocket udp_socket)  throws Exception {
		this.socket = udp_socket;
	}

	@Override
	// Method to join node to bootstrap node.  JSON message is created, which is sent to the bootstrap
	public long joinNetwork(InetSocketAddress bootstrap_node, String identifier, String target_identifier) throws Exception {
		int target = hashCode(target_identifier);
		byte[] sendData = new byte[1024];
		Encoder encode = new Encoder();
		sendData = encode.join(nodeidentifier, target, ip);
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, ip.getAddress(), 8888);

		sendData = new byte[sendData.length];
		socket.send(sendPacket);
		return 0;
	}

	@Override
	// Method to leave the network, JSON messages send to other nodes informing them of this
	public boolean leaveNetwork(long network_id) throws Exception {
		for(int i = 0; i<routing_table.size(); i++){

			byte[] sendData = new byte[1024];
			Encoder encode = new Encoder();							
			sendData = encode.leave(nodeidentifier);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip.getAddress(), 8888);
			socket.send(sendPacket);	
		}
		return false;
	}

	@Override
	// A method to index a url and a number of words
	public void indexPage(String url, String[] unique_words) throws Exception {
		for(int i=0; i<unique_words.length; i++){
			int word = hashCode(unique_words[i]);
			for(int j = 0; j< routing_table.size(); j++){
				if(routing_table.get(j).getID() == nearestNode(word)){
					DatagramSocket clientSocket = socket;
					byte[] sendData = new byte[1024];
					Encoder encode = new Encoder();
					sendData = encode.createIndex(routing_table.get(j).getID(), nodeidentifier, unique_words[i], url);
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip.getAddress(), 8888);
					clientSocket.send(sendPacket);
				}
			}
		}	
	}

	@Override
	// A method to search for the url index of given words
	public SearchResult[] search(String[] words) throws IOException {
		for(int i = 0; i< words.length; i++){
			byte[] sendData = new byte[1024];
			Encoder encode = new Encoder();							
			sendData = encode.search(words[i], hashCode(words[i]), nodeidentifier);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip.getAddress(), 8888);
			socket.send(sendPacket);
		}
		return null;
	}

	// A method which deals with incoming messages
	public void waitAndProcessMessages() throws Exception {
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(receivePacket);
			receiveData = new byte[receiveData.length];
			String sentence = new String(receivePacket.getData());
			
			// Null characters are removed
			int counter = 0;
			while (sentence.charAt(counter) != '\0') {
				counter++;
			}
			sentence = sentence.substring(0, counter);

			// All of the messages are parsed
			JSONParser parser = new JSONParser();
			ContainerFactory containerFactory = new ContainerFactory() {
				public List creatArrayContainer() {
					return new LinkedList();
				}

				public Map createObjectContainer() {
					return new LinkedHashMap();
				}
			};
			String type = null, ip_address = null, links = null, keyword = null, word = null;
			Integer gateway_id = 0;
			Integer node_id = 0, target_id =0, sender_id = 0;
			int messport = 0;
			try {
				Map json = (Map) parser.parse(sentence, containerFactory);
				Iterator iter = json.entrySet().iterator();
				System.out.println("==iterate result==");
				while(iter.hasNext()){
					Map.Entry entry = (Map.Entry)iter.next();
					System.out.println(entry.getKey() + "=>" + entry.getValue());
					if(entry.getKey().equals("type")){
						type = (String) entry.getValue();
					}
					if(entry.getKey().equals("node_id")){
						node_id =  Integer.parseInt(entry.getValue().toString());
					}
					if(entry.getKey().equals("ip_address")){
						ip_address = (String) entry.getValue();
					}
					if(entry.getKey().equals("target_id")){
						target_id = Integer.parseInt(entry.getValue().toString());
					}
					if(entry.getKey().equals("port")){
						messport = Integer.parseInt(entry.getValue().toString());
					}
					if(entry.getKey().equals("gateway_id")){
						gateway_id = Integer.parseInt(entry.getValue().toString());
					}			      
					if(entry.getKey().equals("links")){
						links = (String) entry.getValue();
					}
					if(entry.getKey().equals("keyword")){
						keyword = (String) entry.getValue();
					}
					if(entry.getKey().equals("word")){
						word = (String) entry.getValue();
					}
					if(entry.getKey().equals("sender_id") ||entry.getKey().equals("send_id")){
						sender_id = Integer.parseInt(entry.getValue().toString());
					}			      
				}
			} catch (ParseException pe) {
				System.out.println(pe);
			}

			// A message is received indicating a node wants to join, added to routing table if wants to join this node
			if(type.equals("JOINING_NETWORK_SIMPLIFIED")){
				if(target_id == nodeidentifier){
					Encoder encode = new Encoder();
					sendData = encode.routinginfo(nodeidentifier, node_id,
							ip_address, routing_table);
					//int p = 0;

					InetAddress IPAddress = receivePacket.getAddress();
					DatagramPacket sendPacket = new DatagramPacket(
							sendData, sendData.length, IPAddress,
							messport);
					sendData = new byte[sendData.length];
					socket.send(sendPacket);
					addToAddress(node_id, ip_address);
				}else{
					// Joining network relay message is sent to next node
					for(int i =0; i<routing_table.size(); i++ ){
						if(routing_table.get(i).getID() == nearestNode(target_id)){
							Encoder encode = new Encoder();							
							sendData = encode.joinnetrelay(node_id, target_id, nodeidentifier);
							InetAddress IPAddress = receivePacket.getAddress();
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 8888);
							sendData = new byte[sendData.length];
							socket.send(sendPacket);
						}
					}
				}				
			}

			// Relay message is received
			if(type.equals("JOINING_NETWORK_RELAY_SIMPLIFIED")){
				if(target_id == nodeidentifier){

					Encoder encode = new Encoder();
					sendData = encode.routinginfo(nodeidentifier, node_id,
							ip_address, routing_table);
					InetAddress IPAddress = receivePacket.getAddress();
					DatagramPacket sendPacket = new DatagramPacket(
							sendData, sendData.length, IPAddress,
							8888);
					sendData = new byte[sendData.length];
					socket.send(sendPacket);
					addToAddress(node_id, ip_address);
				}else{
					for(int i =0; i<routing_table.size(); i++ ){
						if(routing_table.get(i).getID() == nearestNode(target_id)){
							Encoder encode = new Encoder();							
							sendData = encode.joinnetrelay(node_id, target_id, nodeidentifier);
							InetAddress IPAddress = receivePacket.getAddress();
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 8888);
							sendData = new byte[sendData.length];
							socket.send(sendPacket);
						}
					}
				}	
			}

			// Routing info is received and parsed
			List table = new JSONArray();
			if(type.equals("ROUTING_INFO")){
				if(target_id == nodeidentifier){
					addToAddress(node_id, ip_address);
					try {
						Map json = (Map) parser.parse(sentence, containerFactory);
						Iterator iter = json.entrySet().iterator(); 
						while (iter.hasNext()) {
							Map.Entry entry = (Map.Entry) iter.next();
							if (entry.getKey().equals("route_table")) {
								table = (List) entry.getValue();
							}
						}
					} catch (ParseException pe) {
						System.out.println(pe);
					}
					for(int i = 0; i<table.size();i++){
						System.out.println("Table: "+table.get(i));
						ArrayList<String> words = new ArrayList<String>();
						words = listdecode(table.get(i).toString());
						addToAddress(Integer.parseInt(words.get(0)), words.get(1));
					}

				}
			}

			// A leave message is received, this nodes info is removed from routing table
			if(type.equals("LEAVE_NETWORK")){
				removeAddress(node_id);
			}

			// An index message is received to index a word and url
			if(type.equals("INDEX")){
				updateWordIndex(links);
				for(int i =0; i<routing_table.size(); i++ ){
					if(routing_table.get(i).getID() == nearestNode(sender_id)){
						Encoder encode = new Encoder();							
						sendData = encode.ackIndex(sender_id, keyword);
						InetAddress IPAddress = receivePacket.getAddress();
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 8888);
						sendData = new byte[sendData.length];
						socket.send(sendPacket);
					}
				}	
			}

			// A search message is received, search results are sent back
			if(type.equals("SEARCH")){
				if(node_id == nodeidentifier){
					Encoder encode = new Encoder();							
					sendData = encode.searchResults(word, sender_id, node_id, wordIndex);
					InetAddress IPAddress = receivePacket.getAddress();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 8888);
					sendData = new byte[sendData.length];
					socket.send(sendPacket);
				}
			}

			// An ack index message is received indicating that the index was successful
			if(type.equals("ACK_INDEX")){
				if(node_id == nodeidentifier){
					System.out.println("ACK Index Received");
				}else{
					for(int i =0; i<routing_table.size(); i++ ){
						if(routing_table.get(i).getID() == nearestNode(node_id)){
							Encoder encode = new Encoder();							
							sendData = encode.ackIndex(node_id, keyword);
							InetAddress IPAddress = receivePacket.getAddress();
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 8888);
							sendData = new byte[sendData.length];
							socket.send(sendPacket);
						}
					}
				}
			}

		}
	}

	// Finds the nearest neighbour of a node that is in routing table
	public int nearestNode(int node_id){
		int closest = 0;
		int val = Integer.MAX_VALUE;
		for(int i=0; i<routing_table.size(); i++){
			int diff = Math.abs(node_id - routing_table.get(i).getID());
			if(diff < val){
				val = diff;
				closest = routing_table.get(i).getID();
			}
		}
		return closest;
	}

	// Information about node is added to routing table
	protected void addToAddress(int node_id, String node_ip) {
		boolean duplicate = false;
		OtherNode onode = new OtherNode(node_id, node_ip);
		if (routing_table.size() >= 10) {
			routing_table.remove(0);
		}
		for (int i = 0; i < routing_table.size(); i++) {
			if (routing_table.get(i).getID() == node_id) {
				duplicate = true;
			}
		}
		if (!duplicate) {
			routing_table.add(onode);
		}
	}

	// Information about a node is removed from a routing table
	protected void removeAddress(int node_id) {

		for (int i = 0; i < routing_table.size(); i++) {
			if(routing_table.get(i).getID() == node_id){
				routing_table.remove(i);
			}
		}

	}

	// Method to split the strings that are returned from a routing message
	ArrayList<String> listdecode(String word){
		ArrayList<String> words = new ArrayList<String>();
		int i = 0;

		int start = 0;
		while(word.charAt(i)!='}'){
			if(word.charAt(i) == '='){
				start = i+1;
			}
			if(word.charAt(i)==','){
				words.add(word.substring(start, i));
			}
			i++;
		}
		words.add(word.substring(start, i));
		return words;
	}

	// The index table is updated
	void updateWordIndex(String link){
		if(!wordIndex.containsKey(link)){
			wordIndex.put(link, 1);
		}else{	
			Integer n = wordIndex.get(link);
			System.out.println("VALUE!!!!"+n);
			wordIndex.put(link, n+1);
		}
		System.out.println(wordIndex);
	}

	public int hashCode(String str) {
		int hash = 0;
		for (int i = 0; i < str.length(); i++) {
			hash = hash * 31 + str.charAt(i);
		}
		return Math.abs(hash);
	}
}
