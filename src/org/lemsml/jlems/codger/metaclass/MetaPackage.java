package org.lemsml.jlems.codger.metaclass;

import java.util.ArrayList;

import org.lemsml.jlems.logging.E;

public class MetaPackage {

	
	String pckg;
	
	MetaPackage parent;
	
	public MetaPackage(String p) {
		pckg = p;
	}

	 
	public MetaPackage(String p, MetaPackage gp) {
		this(p);
		parent = gp;
	}

	
	public boolean isRoot() {
		boolean ret = true;
		if (parent != null) {
			ret = false;
		}
		return ret;
	}
	

	String getPackageName() {
		String ret = "";
		if (parent != null) {
			ret = parent.getPackageName();
		}
		if (ret.length() > 0) {
			ret += ".";
		}
		ret += pckg;
		return ret;
	}
	

	public String getFQClassName(String cid) {
		 String ret = getPackageName() + "." + cid;
		 return ret;
	}


	public ArrayList<String> getSubPackages() {
		ArrayList<String> ret = new ArrayList<String>();
		MetaPackage wk = this;
		while (true) {
			if (wk.isRoot()) {
				break;
			} else {
				ret.add(0, wk.pckg);
				wk = wk.parent;
			}
		}
		return ret;
	}

}
