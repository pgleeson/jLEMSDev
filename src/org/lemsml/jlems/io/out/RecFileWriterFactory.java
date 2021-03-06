package org.lemsml.jlems.io.out;

 
import org.lemsml.jlems.core.lite.simulation.RecWriter;
import org.lemsml.jlems.core.out.RecWriterFactory;

public final class RecFileWriterFactory extends RecWriterFactory {

	static RecFileWriterFactory instance;
	
	// inject this into the jLEMS DataViewerFactory:
	public static void initialize() {
		if (instance == null) {
			instance = new RecFileWriterFactory();
		}
	 
	} 
	
	private RecFileWriterFactory() {
		super();
		RecWriterFactory.getFactory().setDelegate(this);
	}
	
	
	@Override
	public RecWriter newRecWriter(String fid, String fnm, String fmt) {
		RecWriter ret = new RecFileWriter(fid, fnm, fmt);	
		return ret;
	}

 
}
