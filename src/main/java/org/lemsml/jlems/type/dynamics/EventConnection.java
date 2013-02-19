package org.lemsml.jlems.type.dynamics;

import org.lemsml.jlems.expression.ParseError;
import org.lemsml.jlems.expression.ParseTree;
import org.lemsml.jlems.logging.E;
import org.lemsml.jlems.run.BuilderElement;
import org.lemsml.jlems.run.EventConnectionBuilder;
import org.lemsml.jlems.sim.ContentError;
import org.lemsml.jlems.type.Component;
import org.lemsml.jlems.type.ComponentType;
import org.lemsml.jlems.type.Dimension;
import org.lemsml.jlems.type.Lems;
import org.lemsml.jlems.type.LemsCollection;
import org.lemsml.jlems.type.ParamValue;
import org.lemsml.jlems.type.structure.Assign;
import org.lemsml.jlems.type.structure.BuildElement;

public class EventConnection extends BuildElement {

	public String from;
	public String to;

    public String delay;
	
	public String receiver;
	public String receiverContainer;
	public String sourcePort;
	public String targetPort;

	public LemsCollection<Assign> assigns = new LemsCollection<Assign>();


	@Override
	public void resolveLocal(Lems lems, ComponentType ct) throws ContentError, ParseError {
		for (Assign ass : assigns) {
			ParseTree pt = lems.getParser().parseExpression(ass.getExpression());
			ass.setDoubleEvaluator(pt.makeFloatEvaluator());
		}
	}
	
	@Override
	public BuilderElement makeBuilder(Component cpt) throws ContentError, ParseError {

        //E.info("makeBuilder on "+cpt+" from: "+from+" ("+sourcePort+"), to: "+to+" ("+targetPort+") -> "+ receiver +", assigns: "+assigns);
		EventConnectionBuilder ret = new EventConnectionBuilder(from, to);
	
		if (sourcePort != null && cpt.hasAttribute(sourcePort))  {
			ret.setSourcePortID(cpt.getStringValue(sourcePort));
		}
		if (targetPort != null && cpt.hasAttribute(targetPort)) {
			ret.setTargetPortID(cpt.getStringValue(targetPort));
		}

		if (delay != null && cpt.hasAttribute(delay)) {
			ParamValue pv = cpt.getParamValue(delay);
			ret.setDelay(pv.getDoubleValue(Dimension.getTimeDimension()));
		}
		//System.out.println("cpt: "+cpt+", atr: "+cpt.attributes);
              
		if (receiver != null && cpt.getRelativeComponent(receiver)!=null) {
			Component receiverComponent = cpt.getRelativeComponent(receiver);
            //E.info("EventConnection, receiver: ["+receiver+"] resolved to: ["+receiverComponent+"]");
			ret.setReceiverStateType(receiverComponent.getStateType());

            for (Assign ass : assigns) {
                String ea = ass.getExposeAs();
                if (ea != null) {
                    E.warning("Expose as in EventConnection is not used");
                 }
                ret.addAssignment(ass.getProperty(), ass.getDoubleEvaluator());
            }
		}

		if (receiverContainer != null && cpt.hasAttribute(receiverContainer)) {
			String sv = cpt.getStringValue(receiverContainer);
			ret.setReceiverContainer(sv);
		}
	
		return ret;
	}
	
}