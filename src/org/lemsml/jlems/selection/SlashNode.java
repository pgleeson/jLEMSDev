package org.lemsml.jlems.selection;

import java.util.ArrayList;

import org.lemsml.jlems.expression.Node;
import org.lemsml.jlems.logging.E;
import org.lemsml.jlems.run.ConnectionError;
import org.lemsml.jlems.run.RuntimeError;
import org.lemsml.jlems.run.StateInstance;
import org.lemsml.jlems.run.StateRunnable;
import org.lemsml.jlems.sim.ContentError;

public class SlashNode extends AbstractSelectionOperatorNode {

 
	 
	public SlashNode() {
		super("/");
	}

	public SlashNode copy() {
		return new SlashNode();
	}
	
 
	 
	public String getEvaluationProcessDescription() {
		 return "(Child selection in " + left + " select:  " + right + ")";
	}
 
	@Override
	public ArrayList<StateRunnable> getMatches(StateRunnable baseSI) throws ContentError, ConnectionError, RuntimeError {
		ArrayList<StateRunnable> ret = null;
		E.info("slash node get matches: left=" + left + " right=" + right);
		
		Node nl = getLeft();
		if (nl instanceof AbstractSelectionNode) {
			ArrayList<StateRunnable> wka = ((AbstractSelectionNode)nl).getMatches(baseSI);
			Node nr = getRight();
			if (nr instanceof  AbstractSelectionNode) {
				ret = new ArrayList<StateRunnable>();
				AbstractSelectionNode rsn = (AbstractSelectionNode)nr;
				for (StateRunnable si : wka) {
					ret.addAll(rsn.getMatches(si));
				}
				
			} else {
				throw new ContentError("cant subselect for: " + nr);
			}
			
		} else {
			throw new ContentError("cant get matches for: " + nl + " relative to " + baseSI);
		}
		
		return ret;
	}




	public double evaluateFloat(StateRunnable si) throws ContentError, ConnectionError, RuntimeError {
		double ret = 0;
		boolean ok = false;
		Node nl = getLeft();
		Node nr = getRight();
		
		if (nr instanceof SelectorNode) {
			if (nl instanceof AbstractSelectionNode) {
				ArrayList<StateRunnable> msia = ((AbstractSelectionNode)nr).getMatches(si);
				if (msia.size() == 1) {
					ret = ((SelectorNode)nr).getFloat(msia.get(0));
					
				} else {
					throw new ContentError("need exactly one match from : " + nl + " but got " + msia.size());
				}
			}
			
		}
		
		if (!ok) {
			throw new ContentError("cant get a float from : " + this + " on " + si);
		}
		return ret;
	}
}