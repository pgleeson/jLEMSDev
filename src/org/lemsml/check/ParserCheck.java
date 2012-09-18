
package org.lemsml.check;
 
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import org.lemsml.expression.AndNode;
import org.lemsml.expression.BooleanEvaluable;
import org.lemsml.expression.DoubleEvaluable;
import org.lemsml.expression.OrNode;
import org.lemsml.expression.ParseError;
import org.lemsml.expression.Parser;
import org.lemsml.util.E;

 
public class ParserCheck {
 
	static String[] expressions = {"(v/VOLT)",
            "rate * exp((v - midpoint)/scale)",
            "(exp(73 * X))",
            "( 0.76 ) / TIME_SCALE",
            "sqrt(4)",
				"exp (x * (-0.059))"};
	

	boolean ok = false;
	
	
	public void checkExamples() {
		ok = true;
		parseCheck();
		evalCheck1();
		evalCheck2();
		conditionChecks();
	}
	
	
	
	public void parseCheck() {   
         Parser p = new Parser();
        	 for (String expr: expressions) {
        		
        		 try {
        			 DoubleEvaluable res = p.parseExpression(expr);

        			 E.info("Parsed " + expr + " to: " + res.toString() + ", (" + res.getClass() + ")");
        		 } catch (Exception ex) {
        			 ok = false;
        			 E.error("Failed to parse " + expr);
        			 ex.printStackTrace();
        		 }
        	 }
         }
   
	
	
	
    public void evalCheck1() {
    	try {
        Parser p = new Parser();
        String src = "3 + (1.3e-4 + 5) + (3 *4)+ 4/(4.*45) + 34.2E-2 + sin(a + b) / cos(b + c)";
        DoubleEvaluable root = p.parseExpression(src);

        E.info("parsing " + src);
  
        HashMap<String, Double> valHM = new HashMap<String, Double>();
        double a = 2;
        double b = 3;
        double c = 4;
        double d = 5;

        valHM.put("a", a);
        valHM.put("b", b);
        valHM.put("c", c);
        valHM.put("d", d);
        double eval = root.evalD(valHM);
        double res = 3 + (1.3e-4 + 5) + (3 *4)+ 4/(4.*45) + 34.2E-2 + Math.sin(a + b) / Math.cos(b + c);
        
        if (Math.abs(res - eval) > 1.e-6) {
        	ok = false;
        	E.error("Evaluation failed, got " + eval + " but should be " + res + " for " + src);
        } else {
        	E.info("Eval OK for " + src + " " + res + ", " + eval);
        }
    	} catch (Exception ex) {
    		ok = false;
    		ex.printStackTrace();
    	}
    }
    
    
    public void evalCheck2() {
    	try {
    	Parser p = new Parser();

        String src= "ln(c)";
        DoubleEvaluable dev = p.parseExpression(src);
         
        HashMap<String, Double> valHM = new HashMap<String, Double>();
        double c = 3.456e3;
        valHM.put("c", c);
        
        double val = dev.evalD(valHM);
      
        double res = Math.log(c);

        if (Math.abs(res - val) > 1.e-6) {
        	ok = false;
        	E.error("Evaluation failed, got " + val + " but should be " + res + " for " + src);
        } else {
        	E.info("Eval OK for " + src + " " + res + "," + val);
        }
    	} catch (Exception ex) {
    		ok = false;
    		ex.printStackTrace();
    	}
    }



    public void conditionChecks() {
        Parser p = new Parser();
      
        HashMap<String, Double> valHM = new HashMap<String, Double>();
        valHM.put("a", 4.);
        valHM.put("b", 5.);
        
        String[] conditions = {"3 + 4^a .lt. 5 * b",
        						"a .gt. b",
        						"b .leq. 33",
        						"b .geq. 33 .and. a .leq. b",
        						"b .geq. 33 .or. a .leq. b"};
        
        boolean[] results = {false, false, true, false, true};
        
        E.info("testing conditions with a=4., b= 5.");
        for (int i = 0; i < conditions.length; i++) {
        	String src = conditions[i];
        	try {
        	BooleanEvaluable bev = p.parseCondition(src); 
        	boolean res = bev.evalB(valHM);
        	if (res == results[i]) {
        		E.info("OK " + src + " " + res + " as expected");
        	} else {
        		ok = false;
        		E.error("Condition teest failed for " + src + " (got " + res + ")");
        	}
        	}catch (Exception ex) {
        		ok = false;
        		ex.printStackTrace();
        	}
        }
    }     
   
      
         
    public void reportResult() {
    	if (!ok) {
    		E.error("Failed parser checks");
    	} else {
    		E.info("OK - parser checks passed");
    	}
    }

  

    public static void main(String[] args) {
        ParserCheck pc = new ParserCheck();
        pc.checkExamples();
        pc.reportResult();
    }

}