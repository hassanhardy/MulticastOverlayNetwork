/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multicastoverlaynetwork;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Hassan Khan
 */
public class MulticastOverlayNetwork implements Runnable{
    
    String FileName;
    String[][] nodeInfo;
    private static Set<PrintWriter> sWriters = new HashSet<>();
    
    String[] toSource = new String[3];
    int selfSocket;

    
    public MulticastOverlayNetwork(String FileName) {
        
        this.FileName = FileName;
    }
    
    //determine the node info, role and run corresponding class
    @Override
    public void run(){
        try{
            String[] selfInfo = new String[3];
            
            //determine path to the file
            String path = System.getProperty("user.dir");
            path = path + "\\config files" + "\\" + FileName;
            
            //Open the file and read the first line
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();
            
            //split the text file name to determine nodes number
            String[] fileNameSplit = FileName.split("\\.(?=[^\\.]+$)");
            String self = fileNameSplit[0];
            
            //determine network configuration
            ArrayList networkSetup = NetworkBuilder(reader);
            String[] routeUp = new String[3];
            
            //switch case to execute code corresponding to role.
            if(null == line)
                System.out.println("Node type not determined");
            else switch (line) {

                case "0":
                    
                    //deterime its own information
                    for(int i = 0; i < nodeInfo.length; i++){
                        if(nodeInfo[i][0].equals(self)){
                            selfInfo = nodeInfo[i];
                        }
                    }
                    System.out.println("Forwarder " + self + " started");
                    ExecutorService fPool = Executors.newFixedThreadPool(500);
                    
                    //determine the route it must take to the source
                    selfSocket = Integer.parseInt(selfInfo[2]);
                    for(int i = 0; i < networkSetup.size(); i++){
                        String[] temp = (String[]) networkSetup.get(i);
                        if(temp[1].equals(self)){
                            routeUp = temp;
                        }
                    }
                    
                    //get the socket # to the next node
                    for(int i = 0; i < nodeInfo.length; i++){
                        if(nodeInfo[i][0].equals(routeUp[0])){
                            toSource = nodeInfo[i];
                        }
                    }
                    //listen for connections to its own port and start the forward handler
                    int sS = Integer.parseInt(toSource[2]);
                    try(ServerSocket listener = new ServerSocket(selfSocket)){
                        while(true){
                            fPool.execute(new forwardHandler(listener.accept(),sS,self));
                        }
                    }

                case "1":
                    
                    //determine it's own information
                    for(int i = 0; i < nodeInfo.length; i++){
                        if(nodeInfo[i][0].equals(self)){
                            selfInfo = nodeInfo[i];
                        }
                    }
                    System.out.println("Source " + self + " started");
                    //create a pool and listen for connections
                    ExecutorService pool = Executors.newFixedThreadPool(500);
                    selfSocket = Integer.parseInt(selfInfo[2]);
                    try(ServerSocket listener = new ServerSocket(selfSocket)){
                        while(true){
                            pool.execute(new sourceHandler(listener.accept()));
                        }
                    }

                case "2":
                    //determine its own information
                    for(int i = 0; i < nodeInfo.length; i++){
                        if(nodeInfo[i][0].equals(self)){
                            selfInfo = nodeInfo[i];
                        }
                    }
                    System.out.println("Receiver " + self + " started");
                    //determine port for source-bound node
                    selfSocket = Integer.parseInt(selfInfo[2]);
                    for(int i = 0; i < networkSetup.size(); i++){
                        String[] temp = (String[]) networkSetup.get(i);
                        if(temp[1].equals(self)){
                            routeUp = temp;
                        }                
                    }
                    for(int i = 0; i < nodeInfo.length; i++){
                        if(nodeInfo[i][0].equals(routeUp[0])){
                            toSource = nodeInfo[i];
                        }
                    }

                    int fS = Integer.parseInt(toSource[2]);
                    
                    //create a new thread to the recieverHandler
                    recieverHandler client = new recieverHandler(fS, self);
                    client.run();

                    break;
                default:
                    System.out.println("2 data read");
                    break;
            }
        }
        catch(Exception e){
            System.out.println(e);
        }

    }
    

    
    private ArrayList NetworkBuilder(BufferedReader reader) throws IOException{
        //Read number of nodes in the network
        String nodeNum = reader.readLine();
        int numOfNodes = Integer.parseInt(nodeNum);
        String currentLine;
        
        ArrayList<String> listOfNodes = new ArrayList<String>();
        ArrayList<String> nodeLinks = new ArrayList<String>();
        
        //create and initialize a list to hold all the information about the nodes 
        for (int i = 0; i < numOfNodes; i++){
            listOfNodes.add(reader.readLine());
        }
        //the rest of the file is read and loaded into the a list containing all the adjacency matrices
        while((currentLine = reader.readLine()) != null){
            nodeLinks.add(currentLine);
        }
        
        String[][] arrOfNodes = new String[numOfNodes][3];
        
        //Split all the entries by the space between them
        for(int i = 0; i < listOfNodes.size(); i++){
            String Node = listOfNodes.get(i);
            arrOfNodes[i] = Node.split("\\s+",3);
        }
        
        String[][] arrOfLinks = new String[nodeLinks.size()][3];
        
        //Split all the entries by the space between them
        for(int i = 0; i < nodeLinks.size(); i++){
            String Link = nodeLinks.get(i);
            arrOfLinks[i] = Link.split("\\s+",3);
        }
        
        ArrayList network = new ArrayList();
        
        String[] sourceNode = new String[1];
        ArrayList forwardNodes = new ArrayList();
        ArrayList recieverNode = new ArrayList();
        int r = 0;
        int f = 0;
        
        //Split all the Nodes into forward, source and reciever nodes
        for(int i = 0; i < numOfNodes; i++){
            if (arrOfNodes[i][1].equals("0")){
                forwardNodes.add(arrOfNodes[i]);
                f++;
            }
            else if(arrOfNodes[i][1].equals("1")){
                sourceNode = arrOfNodes[i];
            }
            else if(arrOfNodes[i][1].equals("2")){
                recieverNode.add(arrOfNodes[i]);
                r++;
            }
        }
        
        //determine the shortest connections to the recievers
        ArrayList recieverConnections = new ArrayList();
        String[] minimum = null;
        //get all the connections to the nodes and find the minimum
        for(int i = 0; i < recieverNode.size(); i++){
            for(int j = 0; j < arrOfLinks.length; j++){
                String[] temp = (String[]) recieverNode.get(i);
                if(temp[0].equals(arrOfLinks[j][1])){
                    recieverConnections.add(arrOfLinks[j]);
                }
            }
            minimum = (String[]) recieverConnections.get(0);
            for(int j=1; j < recieverConnections.size(); j++){
                String[] test = (String[]) recieverConnections.get(j);
                if(Integer.parseInt(test[2]) < Integer.parseInt(minimum[2]))
                    minimum = (String[]) recieverConnections.get(j);
            }
            network.add(minimum);
            recieverConnections.clear();
        }
        
        //determne the shortest connections to the recievers
        ArrayList forwardConnections = new ArrayList();
        //get all the connections to the nodes and find the minimum
        for(int i = 0; i < forwardNodes.size(); i++){
            for(int j = 0; j < arrOfLinks.length; j++){
                String[] temp = (String[]) forwardNodes.get(i);
                if(temp[0].equals(arrOfLinks[j][1])){
                    forwardConnections.add(arrOfLinks[j]);
                }
            }
            minimum = (String[]) forwardConnections.get(0);
            for(int j = 1; j < forwardConnections.size(); j++){
                String[] test = (String[]) forwardConnections.get(j);
                if(Integer.parseInt(test[2]) < Integer.parseInt(minimum[2]))
                    minimum = (String[]) forwardConnections.get(j);
            }
            network.add(minimum);
            forwardConnections.clear();
        }
        //set the array of nodes to node info so that nodes can determine port numbers
        nodeInfo = arrOfNodes;
        return network;
    }
    
    private static class sourceHandler implements Runnable {
    
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        
        public sourceHandler (Socket socket){
            this.socket = socket;
        }
        //could put syncronized in run to help?
        public void run() {
        try{
            //initialize a scanner to read from the the system input and PrintWriter to forward that data
            in = new Scanner(System.in);
            out = new PrintWriter(socket.getOutputStream(), true);
            
            sWriters.add(out);
            
            while(in.hasNextLine()){
            //retriever the input from the system
                String input = in.nextLine();
                System.out.println("Server sending message: " + input);
                //if the message is /quit then close the source
                if(input.toLowerCase().startsWith("/quit")){
                    return;
                }
                //print the message to all the destinations
                for (PrintWriter writer: sWriters) {
                    String Message = "SOURCE//1110//" + input;
                    writer.println(Message);
                }
            }
            
        //Print any errors that may occur
        } catch (Exception e){
            System.out.println(e);
            //upon quiting, remove the writer from the list and close the socket
        } finally{
            if(out != null){
                sWriters.remove(out);
            }
            try {socket.close();} 
            catch (IOException e){}
        }
        
            
        }
    
    }
    
    private static class forwardHandler implements Runnable{
        
        private Socket socket;
        private int sS;
        private Socket serverSocket;
        private Scanner in;
        private PrintWriter out;
        private String self;
        private Set<PrintWriter> fWriters = new HashSet<>();
        
        public forwardHandler(Socket socket, int sS, String self){
            this.socket = socket;
            this.sS = sS;
            this.self = self;
        }
        
        public void run(){
            try{
                //connect to node leading to the source
                serverSocket = new Socket("127.0.0.1", sS);
                
                //create a scanner that reads the input from the source-direction node
                //and a print writer that writes to the reciever-bound node
                in = new Scanner(serverSocket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                System.out.println("Forward Node " + self + " connected to " + sS);
                
                fWriters.add(out);
                
                while(in.hasNextLine()){
                    String input = in.nextLine();
                    System.out.println("Forwarder Node " + self + " Received Message: " + input);
                    for(PrintWriter writer : fWriters){
                        String Message = "FORWARD" + self + "//" + input;
                        writer.println(Message);
                    }
                
                }
            }
            catch(Exception e){
                System.out.println(e);
            }
            finally{
                if (out != null){
                    fWriters.remove(out);
                }
                try{socket.close();} catch(IOException e){}
            }
        }
    }
        
    private static class recieverHandler{
    
        Scanner in;
        private Socket serverSocket;
        private int fS;
        private String self;
        
        public recieverHandler(int fS, String self){
            this.fS = fS;
            this.self = self;
        }
        
        public void run() throws IOException{
            try{
                //connects to the socket
                serverSocket = new Socket("127.0.0.1", fS);
                //creates a scanner for that connection
                in = new Scanner(serverSocket.getInputStream());
                System.out.println("Client Node " + self + " connected to " + fS);
                
                //print the read input to the system out
                while(in.hasNextLine()){
                    String line = in.nextLine();
                    System.out.println("Client Node " + self + " Received Message: " + line);
                }


            }
            catch(Exception e){
                System.out.println("Issue Occured : reciever");
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        
        ExecutorService mPool = Executors.newFixedThreadPool(500);
        //initiate all the nodes in seperate threads of the pool
        //for complex network
        mPool.execute(new MulticastOverlayNetwork("1.txt"));
        mPool.execute(new MulticastOverlayNetwork("2.txt"));
        mPool.execute(new MulticastOverlayNetwork("3.txt"));
        mPool.execute(new MulticastOverlayNetwork("4.txt"));
        mPool.execute(new MulticastOverlayNetwork("5.txt"));
        mPool.execute(new MulticastOverlayNetwork("6.txt"));
        mPool.execute(new MulticastOverlayNetwork("7.txt"));
        
        //for simple network
//        mPool.execute(new MulticastOverlayNetwork("1.txt"));
//        mPool.execute(new MulticastOverlayNetwork("2.txt"));
//        mPool.execute(new MulticastOverlayNetwork("3.txt"));
//        mPool.execute(new MulticastOverlayNetwork("4.txt"));
//        mPool.execute(new MulticastOverlayNetwork("5.txt"));
    }
    
}