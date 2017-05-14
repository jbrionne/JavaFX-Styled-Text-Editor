package fr.next.pa.fx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * HighLight
 */
class HighLight {

	private static final String STYLE_SEPARATOR = "-";
	private Map<Integer, Set<String>> highlightsOffset = new HashMap<>();

	HighLight() {
	}

	/**
	 * Register a style.
	 * 
	 * @param offset
	 *            the offset in the text
	 * @param length
	 *            the length
	 * @param type
	 *            the css class
	 */
	void add(int offset, int length, String type) {
		for (int i = offset; i < offset + length; i++) {
			Set<String> types = highlightsOffset.get(i);
			if (types == null) {
				types = new HashSet<>();
			}
			types.add(type);
			highlightsOffset.put(i, types);
		}
	}

	/**
	 * Return the current highlight for the position.
	 * 
	 * @param position
	 *            the position
	 * @return collection of styles
	 */
	Set<String> retrieveHighlight(int position) {
		return highlightsOffset.get(position);
	}

	/**
	 * Remove registered style.
	 */
	void clear() {
		highlightsOffset.clear();
	}

	/**
	 * Organize highlights with the offset/css type/priority
	 * 
	 * @return highlights sections
	 */
	List<HighLightSection> computeHighlights() {

		List<HighLightSection> highLightSections = new ArrayList<>();
		String[] styleDefault = Style.DEFAULT.split(STYLE_SEPARATOR);
		String typeDefault = styleDefault[1];

		SortedSet<Integer> keys = new TreeSet<Integer>(highlightsOffset.keySet());
		Set<String> lastMainStyle = null;
		int lastIndex = -2;
		int firstIndex = -2;
		int index = 0;
		int size = keys.size();
		for (Integer key : keys) {
			Set<String> value = highlightsOffset.get(key);
			Set<String> mainStyle = new HashSet<>();
			Map<String, String> styleByTypeWithHigherPriority = new HashMap<>();
			styleByTypeWithHigherPriority.put(typeDefault, Style.DEFAULT);
			for (String styleClass : value) {
				String[] styleInfo = styleClass.split(STYLE_SEPARATOR);
				String type = styleInfo[1];
				int priority = Integer.valueOf(styleInfo[2]).intValue();

				String currentStyle = styleByTypeWithHigherPriority.get(type);
				if (currentStyle != null) {
					String[] currentStyleInfo = currentStyle.split(STYLE_SEPARATOR);
					int currentPriority = Integer.valueOf(currentStyleInfo[2]).intValue();
					if (currentPriority == priority) {
						throw new AssertionError(
								"same priority for the same type of style " + styleClass + " " + currentStyle);
					}
					if (currentPriority > priority) {
						styleByTypeWithHigherPriority.put(type, currentStyle);
					}
				} else {
					styleByTypeWithHigherPriority.put(type, styleClass);
				}

			}
			mainStyle.addAll(styleByTypeWithHigherPriority.values());

			if (lastMainStyle == null) {
				firstIndex = key;
			}
			boolean isDiff = lastIndex != key - 1 || !isEquals(mainStyle, lastMainStyle);
			if (lastMainStyle != null && (isDiff || size == index + 1)) {
				if (size == index + 1) {
					if (isDiff) {
						HighLightSection h = new HighLightSection(firstIndex, lastIndex + 1, lastMainStyle);
						highLightSections.add(h);
						HighLightSection hEnd = new HighLightSection(key, key + 1, mainStyle);
						highLightSections.add(hEnd);
					} else {
						HighLightSection h = new HighLightSection(firstIndex, key + 1, lastMainStyle);
						highLightSections.add(h);
					}
				} else {
					HighLightSection h = new HighLightSection(firstIndex, lastIndex + 1, lastMainStyle);
					highLightSections.add(h);
					firstIndex = key;
				}
			}

			if (keys.size() == 1) {
				HighLightSection hEnd = new HighLightSection(key, key + 1, mainStyle);
				highLightSections.add(hEnd);
			}

			lastMainStyle = mainStyle;
			lastIndex = key;
			index++;
		}
		return highLightSections;
	}

	private boolean isEquals(Set<String> firstSet, Set<String> secondSet) {
		if (firstSet.size() != secondSet.size()) {
			return false;
		}
		if (!firstSet.containsAll(secondSet)) {
			return false;
		}
		if (!secondSet.containsAll(firstSet)) {
			return false;
		}
		return true;
	}

}
