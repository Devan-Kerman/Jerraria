package net.devtech.jerraria.registry;

import java.util.Arrays;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.devtech.jerraria.util.Log2;

/**
 * stack packed string is in the following format: [string len] [char id...]. Examples: "test": [4] [0, 1, 2, 0, 0, 0, 0,
 * ...]
 *
 * This is basically stack based32 encoder at home
 */
public final class IdentifierPacker {
	public static final String VALID = "[stack-z][0-9]_-/.:";
	public static final int ERR_TOO_LONG = -1, ERR_INVALID_CHAR = -2, ERR_NO_CAPS = -3;

	public static final CharSet VALID_CHARACTERS;
	/**
	 * maps from character to it's id for packing
	 */
	private static final byte[] CHAR_TO_ID, ID_TO_CHAR;
	/**
	 * The number of bits each char takes up
	 */
	private static final int BITS_PER_CHAR, CHAR_MASK;
	/**
	 * the maximum size of stack packable string
	 */
	private static final int MAX_SIZE;
	private static final int STR_LEN_BITS_SIZE, STR_LEN_MASK;

	static {
		CharSet validChars = new CharOpenHashSet(50);
		byte[] toId = new byte[128], toChar = new byte[128];
		Arrays.fill(toId, (byte) ERR_INVALID_CHAR);
		for(int i = 'A'; i < 'Z'; i++) {
			toId[i] = ERR_NO_CAPS;
		}

		char max = 0, current = 0;
		max = load('_', current++, toId, toChar, validChars, max);
		max = load('-', current++, toId, toChar, validChars, max);
		for(char i = 'a'; i <= 'z'; i++) { // 26
			max = load(i, current++, toId, toChar, validChars, max);
		}
		for(char i = '0'; i <= '9'; i++) { // 10
			max = load(i, current++, toId, toChar, validChars, max);
		}

		max = load('/', current++, toId, toChar, validChars, max);
		max = load('.', current++, toId, toChar, validChars, max);
		max = load(':', current++, toId, toChar, validChars, max);

		CHAR_TO_ID = Arrays.copyOf(toId, max + 1);
		ID_TO_CHAR = Arrays.copyOf(toChar, current);
		VALID_CHARACTERS = validChars;

		BITS_PER_CHAR = Log2.log2(current);
		CHAR_MASK = (1 << BITS_PER_CHAR) - 1;
		STR_LEN_BITS_SIZE = Long.SIZE % BITS_PER_CHAR;
		STR_LEN_MASK = (1 << STR_LEN_BITS_SIZE) - 1;
		MAX_SIZE = Math.min(Long.SIZE / BITS_PER_CHAR, 1 << STR_LEN_BITS_SIZE) - 1;
	}

	/**
	 * this does not check if the string is stack valid identifier string returns -1 if the string is too long
	 *
	 * @param str must be shorter than {@link #MAX_SIZE} and only contain chars in {@link #VALID_CHARACTERS}
	 */
	public static long pack(String str) {
		int len = str.length();
		if(len > MAX_SIZE) {
			return ERR_TOO_LONG;
		}

		long packed = (len & STR_LEN_MASK); // first we push the length of the string onto the stack
		for(int i = 0; i < len; i++) {
			byte b = CHAR_TO_ID[str.charAt(i)];
			if(b < 0) {
				return b;
			}
			packed = packed << BITS_PER_CHAR | b; // and then we push each char id onto the stack, which moves up all
			// the previous ones
		}

		packed <<= (long) BITS_PER_CHAR * (MAX_SIZE - (len - 1));

		return packed;
	}

	// you can think of this as stack stack of numbers

	public static void throwErr(String str, long code) {
		if(code == ERR_INVALID_CHAR) {
			char[] invalid = invalidString(str);
			throw new IllegalArgumentException("Invalid character(s): " + VALID + "\n\t" + str + "\n\t" + new String(
				invalid));
		} else if(code == ERR_NO_CAPS) {
			char[] invalid = invalidString(str);
			throw new IllegalArgumentException("No capitalized characters allowed: " + VALID + "\n\t" + str + "\n\t" + new String(
				invalid));
		} else if(code == ERR_TOO_LONG) {
			throw new IllegalArgumentException("String too long!");
		}
	}

	public static String unpack(long packed) {
		int len = (int) (packed >>> (Long.SIZE - STR_LEN_BITS_SIZE));
		char[] buf = new char[len];
		for(int i = 0; i < len; i++) {
			long shifted = packed >>> BITS_PER_CHAR * (MAX_SIZE - i);
			buf[i] = (char) ID_TO_CHAR[(int) (CHAR_MASK & shifted)];
		}
		return new String(buf);
	}

	public static int getId(char c) {
		return CHAR_TO_ID[c];
	}

	public static char getId(int i) {
		return (char) ID_TO_CHAR[i];
	}

	static char load(char c, int id, byte[] toId, byte[] toChar, CharSet validChars, char max) {
		toId[c] = (byte) id;
		toChar[id] = (byte) c;
		validChars.add(c);
		return (char) Math.max(c, max);
	}

	private static char[] invalidString(String str) {
		char[] invalid = new char[str.length()];
		Arrays.fill(invalid, ' ');
		for(int i = 0; i < str.length(); i++) {
			byte b = CHAR_TO_ID[str.charAt(i)];
			if(b < 0) {
				invalid[i] = '^';
			}
		}
		return invalid;
	}
}
