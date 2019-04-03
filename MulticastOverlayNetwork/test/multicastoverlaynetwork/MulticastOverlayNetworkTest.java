/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multicastoverlaynetwork;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.junit.Test;
import org.junit.*;

/**
 *
 * @author Hassan Khan
 */
public class MulticastOverlayNetworkTest {
    
    
    public MulticastOverlayNetworkTest() {
    }

    /**
     * Test of run method, of class MulticastOverlayNetwork.
     */
    @Test
    public void testRun() throws InterruptedException {
        System.out.println("run");
        String str = "";
        ExecutorService mPool = Executors.newFixedThreadPool(500);
        
        PrintStream stdOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        
        System.setOut(new PrintStream(outContent));
        
        //complex network
        mPool.execute(new MulticastOverlayNetwork("1.txt"));
        mPool.execute(new MulticastOverlayNetwork("2.txt"));
        mPool.execute(new MulticastOverlayNetwork("3.txt"));
        mPool.execute(new MulticastOverlayNetwork("4.txt"));
        mPool.execute(new MulticastOverlayNetwork("5.txt"));
        mPool.execute(new MulticastOverlayNetwork("6.txt"));
        mPool.execute(new MulticastOverlayNetwork("7.txt"));
        //complex network ends here
        
        //simple network
//        mPool.execute(new MulticastOverlayNetwork("1.txt"));
//        mPool.execute(new MulticastOverlayNetwork("2.txt"));
//        mPool.execute(new MulticastOverlayNetwork("3.txt"));
//        mPool.execute(new MulticastOverlayNetwork("4.txt"));
//        mPool.execute(new MulticastOverlayNetwork("5.txt"));
        //simple network ends here
        
        String input = "String to track\n no\n nope\n";
        
        
        InputStream in = new ByteArrayInputStream(input.getBytes()); 
        System.setIn(in);
        
        
        Thread.sleep(10000);
        try {
            str = outContent.toString("UTF-8");
            
            //complex network assertions
            assertTrue(str.toLowerCase().contains("source 1 started"));

            assertTrue(str.toLowerCase().contains("forwarder 2 started"));

            assertTrue(str.toLowerCase().contains("forwarder 3 started"));

            assertTrue(str.toLowerCase().contains("receiver 4 started"));

            assertTrue(str.toLowerCase().contains("forwarder 5 started"));

            assertTrue(str.toLowerCase().contains("receiver 6 started"));

            assertTrue(str.toLowerCase().contains("receiver 7 started"));
            
            assertTrue(str.toLowerCase().contains("forward node 2 connected to 15001"));
            
            assertTrue(str.toLowerCase().contains("forward node 3 connected to 15001"));
            
            assertTrue(str.toLowerCase().contains("client node 4 connected to 15002"));
            
            assertTrue(str.toLowerCase().contains("forward node 5 connected to 15003"));
            
            assertTrue(str.toLowerCase().contains("client node 6 connected to 15003"));
            
            assertTrue(str.toLowerCase().contains("client node 7 connected to 15005"));
            //Complex network assertions end here
            
            //Simple network assertions
//            assertTrue(str.toLowerCase().contains("source 1 started"));
//
//            assertTrue(str.toLowerCase().contains("forwarder 2 started"));
//
//            assertTrue(str.toLowerCase().contains("forwarder 3 started"));
//
//            assertTrue(str.toLowerCase().contains("receiver 4 started"));
//
//            assertTrue(str.toLowerCase().contains("receiver 5 started"));
//            
//            assertTrue(str.toLowerCase().contains("forward node 2 connected to 15001"));
//            
//            assertTrue(str.toLowerCase().contains("forward node 3 connected to 15001"));
//            
//            assertTrue(str.toLowerCase().contains("client node 4 connected to 15002"));
//            
//            assertTrue(str.toLowerCase().contains("client node 5 connected to 15003"));
            //cSimple network assertions end here
            
            System.setOut(stdOut);
            
            System.out.print(str);

            // TODO review the generated test code and remove the default call to fail.
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
    }
    
}
