package org.lemsml.jlemsio.examples;

import java.io.File;
 
import org.lemsml.jlemsio.examples.RunFileExample;
 

public class Example4 {
	
	public static void main(String[] argv) {
	
 		
		File fdir = new File("../jLEMS");
		
		RunFileExample fe = new RunFileExample(fdir, "example4.xml");
		
		fe.run();
	
	}
	 
    
    
}