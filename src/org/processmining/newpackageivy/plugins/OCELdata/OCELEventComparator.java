package org.processmining.newpackageivy.plugins.OCELdata;

import java.util.Comparator; 

public class OCELEventComparator implements Comparator<OCELEvent>{
	
	public int compare(OCELEvent o1, OCELEvent o2) {

		if (o1.timestamp.getTime() < o2.timestamp.getTime()) {
			return -1;
		}
		else if (o1.timestamp.getTime() > o2.timestamp.getTime()) {
			return 1;
		}
		return o1.id.compareTo(o2.id);
	}

}
