/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.pgcraft.spectatorplus.utils;

import java.util.TreeMap;


/**
 * Converts a number to roman.
 *
 * @author bhlangonijr
 */
public class RomanNumber
{
	private final static TreeMap<Integer, String> map = new TreeMap<>();

	static
	{
		map.put(1000, "M");
		map.put(900, "CM");
		map.put(500, "D");
		map.put(400, "CD");
		map.put(100, "C");
		map.put(90, "XC");
		map.put(50, "L");
		map.put(40, "XL");
		map.put(10, "X");
		map.put(9, "IX");
		map.put(5, "V");
		map.put(4, "IV");
		map.put(1, "I");
	}

	public static String toRoman(Integer number)
	{
		if (number == null)
			return "0";

		Integer l = map.floorKey(number);

		if (l == null)
			return String.valueOf(number);
		else if (number.equals(l))
			return map.get(number);

		return map.get(l) + toRoman(number - l);
	}
}
