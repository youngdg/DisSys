import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

class SearchResult{
   String[] words; // strings matched for this url
   String url;   // url matching search query 
   long frequency; //number of hits for page
}

public interface PeerSearch {
    void init(DatagramSocket udp_socket) throws Exception; // initialise with a udp socket
    long joinNetwork(InetSocketAddress bootstrap_node, String identifier, String target_identifier) throws Exception; //returns network_id, a locally 
                                       // generated number to identify peer network
    boolean leaveNetwork(long network_id) throws IOException, Exception; // parameter is previously returned peer network identifier
    void indexPage(String url, String[] unique_words) throws Exception;
    SearchResult[] search(String[] words) throws IOException;
	
}