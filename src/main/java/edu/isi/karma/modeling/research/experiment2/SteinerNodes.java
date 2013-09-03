/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.modeling.research.experiment2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.util.RandomGUID;

public class SteinerNodes implements Comparable<SteinerNodes> {

	private static final double MIN_CONFIDENCE = 1E-6;
	
	private Set<Node> nodes;
	private List<Double> confidenceList;
	private List<CoherenceItem> coherenceList;
	private double confidence;
	private double coherence;
	private int frequency;
	
//	class ValueComparator implements Comparator<String> {
//
//	    Map<String, Set<Node>> base;
//	    public ValueComparator(Map<String, Set<Node>> base) {
//	        this.base = base;
//	    }
//
//	    public int compare(String a, String b) {
//	        if (base.get(a).size() > base.get(b).size()) 
//	            return 1;
//	        else if (base.get(a).size() < base.get(b).size()) 
//		            return -1;
//	        else
//	            return 0;
//	    }
//	}
	
	public SteinerNodes() {
		this.nodes = new HashSet<Node>();
		this.confidenceList = new Vector<Double>();
		this.coherenceList = new ArrayList<CoherenceItem>();
		this.frequency = 0;
		this.confidence = 1.0;
		this.coherence = 0.0;
	}
	
	public SteinerNodes(SteinerNodes steinerNodes) {
		this.nodes = new HashSet<Node>(steinerNodes.getNodes());
		this.confidenceList = new Vector<Double>(steinerNodes.getConfidenceVector());
		this.coherenceList = new ArrayList<CoherenceItem>(steinerNodes.getCoherenceList());
		this.frequency = steinerNodes.getFrequency();
		this.confidence = steinerNodes.getConfidence();
		this.coherence = steinerNodes.getCoherence();
	}
	
	public Set<Node> getNodes() {
		return Collections.unmodifiableSet(this.nodes);
	}
	
	public boolean addNode(Node n, double confidence) {
		if (this.nodes.contains(n))
			return false;
		
		this.nodes.add(n);
		
		if (confidence <= 0 || confidence > 1)
			confidence = MIN_CONFIDENCE;
		
		this.confidenceList.add(confidence);
		this.confidence *= confidence;
		
		this.frequency += n.getPatternIds().size();
		
		this.computeCoherenceList();
		this.computeCoherenceValue();
		
		return true;
		
	}
	
	public List<Double> getConfidenceVector() {
		return Collections.unmodifiableList(this.confidenceList);
	}
	
	public int getNodeCount() {
		return this.nodes.size();
	}
	
	public List<CoherenceItem> getCoherenceList() {
		return Collections.unmodifiableList(this.coherenceList);
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public double getConfidence() {
		return confidence;
	}

	public double getCoherence() {
		return coherence;
	}
	
	
//	private int computeFrequency() {
//		int frequency = 0;
//		for (Node n : this.nodes)
//			frequency += n.getPatternIds().size();
//		return frequency;
//	}

//	private double computeConfidenceValue() {
//		
//		if (this.confidenceList.size() == 1)
//			return 1e-10;
//		
//		double confidence = 1.0;
//		
//		for (double d : this.confidenceList) {
//			if (d == 0)
//				confidence *= 1e-10;
//			else
//				confidence *= d;
//		}
//		
//		return confidence;
//	}
	
	private void computeCoherenceList() {
		
		if (nodes == null || nodes.size() == 0)
			return;
		  
		
//		Map<String, Set<Node>> patternToNodeMap = new HashMap<String, Set<Node>>();
//		ValueComparator valueComparator = new ValueComparator(patternToNodeMap);
//		SortedMap<String, Set<Node>> sortedPatternToNodeMap = new TreeMap<String, Set<Node>>(valueComparator);
//		
//		for (Node n : nodes) {
//			for (String p : n.getPatternIds()) {
//				Set<Node> nodes = patternToNodeMap.get(p);
//				if (nodes == null) {
//					nodes = new HashSet<Node>();
//					patternToNodeMap.put(p, nodes);
//				}
//				nodes.add(n);
//			}
//		}
//		
//		sortedPatternToNodeMap.putAll(patternToNodeMap);

		Map<String, Integer> patternSize = new HashMap<String, Integer>();
		Map<String, String> patternGuid = new HashMap<String, String>();
		int guidSize = new RandomGUID().toString().length();
		
		for (Node n : nodes) {
			for (String p : n.getPatternIds()) {
				
				Integer size = patternSize.get(p);
				if (size == null) 
					patternSize.put(p, 1);
				else
					patternSize.put(p, ++size);
				
				if (!patternGuid.containsKey(p)) {
					String guid = new RandomGUID().toString();
					patternGuid.put(p, guid);
				}
			}
		}
		
		// find the maximum pattern size
		int maxPatternSize = 0;
		for (Entry<String, Integer> entry : patternSize.entrySet()) {
			if (entry.getValue().intValue() > maxPatternSize)
				maxPatternSize = entry.getValue().intValue();
		}
		
		List<String> listOfNodesLargestPatterns = new ArrayList<String>();
		
		for (Node n : nodes) {
			List<String> patternIds = new ArrayList<String>(n.getPatternIds());
			Collections.sort(patternIds);
			
			String[] nodeMaxPatterns = new String[maxPatternSize];
			Arrays.fill(nodeMaxPatterns, "");
			
			for (String p : patternIds) {
				int size = patternSize.get(p).intValue();
				nodeMaxPatterns[size - 1] += patternGuid.get(p);
			}
			for (int i = maxPatternSize - 1; i >= 0; i--) {
				if (nodeMaxPatterns[i] != null) {
					listOfNodesLargestPatterns.add(nodeMaxPatterns[i]);
					break;
				}
			}
		}	
		
		Function<String, String> stringEqualiy = new Function<String, String>() {
			  @Override public String apply(final String s) {
			    return s;
			  }
			};
				
		Multimap<String, String> index =
			Multimaps.index(listOfNodesLargestPatterns, stringEqualiy);
		
		this.coherenceList.clear();
		int x, y;
		for (String s : index.keySet()) {
			x = index.get(s).size();
			y = index.get(s).size() / guidSize; 
			CoherenceItem ci = new CoherenceItem(x, y);
			this.coherenceList.add(ci);
		}
		
		Collections.sort(this.coherenceList);
		
	}
	
	private void computeCoherenceValue() {
		
		BigDecimal value = BigDecimal.ZERO;
		
		BigDecimal denominator = BigDecimal.ONE;
		BigDecimal factor = new BigDecimal(100);
		BigDecimal b;
		
		for (CoherenceItem ci : this.coherenceList) {
			denominator.multiply(factor);
			b = new BigDecimal(ci.getDouble());
			b.divide(denominator);
			value.add(b);
		}
		
		this.coherence = value.doubleValue();
	}

		
	public double computeScore() {
		
		double score = 0.0;
		
		double confidence = this.getCoherence();
		int nodeCount = this.getNodeCount();
		double coherence = this.getCoherence();
		int frequency = this.getFrequency();
		
		score = coherence;
		return score;
	}

	@Override
	public int compareTo(SteinerNodes target) {
		
		double score1 = this.computeScore();
		double score2 = target.computeScore();
		
		if (score1 < score2)
			return -1;
		else if (score1 > score2)
			return 1;
		else return 0;
	}
		
}
