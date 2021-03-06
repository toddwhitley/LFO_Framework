package framework;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lfo.agents.Agent;
import lfo.agents.cbr.TBAgent;
import lfo.agents.matlab.DiscreteBNetAgent;
import lfo.agents.matlab.DiscreteBNetOrderKAgent;
import lfo.agents.matlab.DiscreteDBNAgent;
import lfo.agents.matlab.DiscreteNNetAgent;
import lfo.agents.matlab.DiscreteNNetOrderKAgent;
import util.Config;


public class Learner extends Agent {
	
	public Agent newAgent(int ignoreTrace, String learnerType, List<String> trainData, int NumPerception, String[] actions, String splitBy ) throws FileNotFoundException, IOException{
		
		//declare and initialize agent
		int i = ignoreTrace;
		Agent agent = null;

		List <String> lt = new ArrayList<String>();

		switch(learnerType){
		case "BN":
			lt.addAll(trainData);
			lt.remove(i);
			agent = new DiscreteBNetAgent(lt,NumPerception);
			break;
		case "BNk2":
			lt.addAll(trainData);
			lt.remove(i);
			agent = new DiscreteBNetOrderKAgent(lt,NumPerception,2);
			break;
		case "NN":
			lt.addAll(trainData);
			lt.remove(i);
			agent = new DiscreteNNetAgent(lt,NumPerception);
			break;
		case "NNk2":
			lt.addAll(trainData);
			lt.remove(i);
			agent = new DiscreteNNetOrderKAgent(lt,NumPerception,actions.length, 2);
			break;
		case "IOHMM":
			lt.addAll(trainData);
			lt.remove(i);
			agent = DiscreteDBNAgent.getIOHMMFromMATLAB(lt,Config.LOCAL_DATA + "Matlab/LfODBN-EVAL_IOHMM",NumPerception, (NumPerception+2)*2);
			break;
		case "DBN":
			lt.addAll(trainData);
			lt.remove(i);
			agent = DiscreteDBNAgent.getLfODBNFromMATLAB(lt,Config.LOCAL_DATA + "Matlab/LfODBN-EVAL_DBN",NumPerception,(NumPerception+2)*2);
			break;
		case "TB":
			lt.addAll(trainData);
			agent = new TBAgent(lt,splitBy,i);
			break;
		}
		
		return agent;
		
	}

}
