package framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lfo.agents.Agent;
import lfo.agents.matlab.DiscreteBNetOrderKAgent;
import lfo.agents.matlab.DiscreteDBNAgent;
import lfo.agents.matlab.DiscreteNNetOrderKAgent;
import util.ReadCsv;
import lfo.matlab.BNetRemoteDiscrete;
import lfo.matlab.BNetRemoteOrderKDiscrete;
import lfo.matlab.NNetRemote;
import lfo.matlab.NNetRemoteOrderKDiscrete;

public class TestingModule {

	public int[][] trainTest(String learnerType, List <String> trainData, int NumPerception, String[] actions, String splitBy) throws IOException{
		//create new reading object
		ReadCsv read = new ReadCsv();
		Learner l = new Learner();

		//output matrix
		int [][] conMat = new int [actions.length][actions.length];

		System.out.println("Loading Data from : " + trainData);

		// load all the learning traces for probabilistic
		List <String> traces = trainData;

		//creating matrix
		HashMap<String, HashMap<String, Integer>> matrix = new HashMap<String, HashMap<String, Integer>>();

		for(int i=0;i<traces.size();i++){
			//time
			double t = System.currentTimeMillis();	

			//gets new learner
			Agent agent = l.newAgent(i,learnerType, trainData,NumPerception, actions, splitBy);

			//calculate training time and convert into min
			printTime(t,false);

			//get start testing time
			t = System.currentTimeMillis();

			//get the test data for probabilistic
			String[][] test = read.readCsv(traces.get(i), splitBy); 

			//run through the trace line by line and predict actions and create confusion matrix
			for(int time = 0;time<test.length;time++) {

				//action definitions
				String actionLearner = "";
				String actionExpert ="";

				//list of perception
				List <Double> perceptionNew = new ArrayList<Double>();

				//collecting perception from the trace and adding them to the list
				for (int count = 0; count<NumPerception;count++){
					double placeholder = Double.parseDouble(test[time][count]);
					perceptionNew.add(placeholder);
				}
				//get the prediction			
				actionLearner = agent.cycleNew(actions, perceptionNew, time);

				//accounts for the actions being in a different format for NN and NNK2
				if(learnerType.equals("NN")|| learnerType.equals("NNk2")){
					int actionLen = actions.length-1;
					for(int k = actionLen;k>=0;k--){
						if(test[time][test[0].length-1-k].equals("1")) actionExpert = actions[actionLen-k];
					}

				}else{
					//get the action from the expert
					actionExpert = test[time][NumPerception];
				}

				//providing the expert action to the learner 
				if (agent instanceof DiscreteNNetOrderKAgent) ((DiscreteNNetOrderKAgent)agent).replaceLastActionNew(actions, actionExpert);
				if (agent instanceof DiscreteBNetOrderKAgent) ((DiscreteBNetOrderKAgent)agent).replaceLastActionNew(actions, actionExpert);
				if (agent instanceof DiscreteDBNAgent & learnerType.equals("DBN")) ((DiscreteDBNAgent)agent).replaceLastAction(actions, actionExpert);

				//comparing the actions to generate the confusionMatrix
				String correct = (actionExpert.equals("0")) ? "NOACTION" : actionExpert;
				String guess = (actionLearner.equals("0")) ? "NOACTION" : actionLearner;
				if (!matrix.containsKey(correct)){
					matrix.put(correct, new HashMap<String, Integer>());
				}
				if (!matrix.get(correct).containsKey(guess)){
					matrix.get(correct).put(guess, 0);
				}
				int value = matrix.get(correct).get(guess).intValue();
				matrix.get(correct).put(guess, value + 1);
			}

			//calculate testing time and convert into minutes
			printTime(t,true);
		}
		//get the output
		conMat = GetConfusionMatrix(matrix);

		if (DiscreteDBNAgent.proxy != null){
			DiscreteDBNAgent.proxy.disconnect();
		}
		if (BNetRemoteDiscrete.proxy != null){
			BNetRemoteDiscrete.proxy.disconnect();
		}
		if (NNetRemote.proxy != null){
			NNetRemote.proxy.disconnect();
		}
		if (BNetRemoteOrderKDiscrete.proxy != null){
			BNetRemoteOrderKDiscrete.proxy.disconnect();
		}
		if (NNetRemoteOrderKDiscrete.proxy != null){
			NNetRemoteOrderKDiscrete.proxy.disconnect();
		}
		//return output
		return conMat;
	}
	
	public int[][] GetConfusionMatrix(HashMap<String, HashMap<String, Integer>> confusionMatrix){
		HashSet<String> titles = new HashSet<String>();


		titles.addAll(confusionMatrix.keySet());
		for (String s1 : confusionMatrix.keySet()){
			titles.addAll(confusionMatrix.get(s1).keySet());
		}

		int [][] matrix = new int [titles.size()][titles.size()];
		int col = 0;
		int row = 0;
		for (String s1 :titles){
			for(String s2 : titles){
				if (!confusionMatrix.containsKey(s1) || !confusionMatrix.get(s1).containsKey(s2)){
					matrix[row][col] = 0;
				}else{
					matrix[row][col] = confusionMatrix.get(s1).get(s2);
				}
				row++;
			}
			col++;
			row = 0;
		}
		return matrix;
	}

	public void printTime(double t, boolean test){
		double t2;
		t2 = (System.currentTimeMillis() - t);
		t2 = t2/60000; //convert to minutes
		if(!test)System.out.print("Training complete in: "+ t+ " min");
		else System.out.print("Testing complete in: "+ t+ " min");
		System.out.print("\r\n");
	}
	
}
