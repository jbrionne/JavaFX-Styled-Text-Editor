package fr.next.pa.fx;

import java.util.Set;

/**
 * HighLight Section
 */
class HighLightSection {

	/** start offset **/
	private int start;

	/** end offset **/
	private int end;

	/** style class **/
	private Set<String> type;

	HighLightSection(int start, int end, Set<String> set) {
		super();
		this.start = start;
		this.end = end;
		this.type = set;
	}

	int getStart() {
		return start;
	}

	int getEnd() {
		return end;
	}

	Set<String> getType() {
		return type;
	}

}
