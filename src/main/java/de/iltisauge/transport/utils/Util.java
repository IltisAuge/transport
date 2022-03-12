package de.iltisauge.transport.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.iltisauge.transport.client.NetworkClient;
import de.iltisauge.transport.network.Session;
import de.iltisauge.transport.server.NetworkServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Util {

	private final static SecureRandom RANDOM = new SecureRandom();;
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception exception) {
		}
	}

	public static String[] toLowerCase(String... strings) {
		Stream.of(strings).forEach(String::toLowerCase);
		return strings;
	}

	public static String randomHashString() {
		return new BigInteger(130, RANDOM).toString(32);
	}

	public static String getShortThrowable(Throwable throwable) {
		if (throwable.getMessage() == null) {
			return throwable.getClass().getName();
		}
		return throwable.getClass().getName() + ": " + throwable.getMessage();
	}

	/**
	 * <p>
	 * Tests if the array contains the given element using
	 * {@link Objects#equals(Object, Object)}.
	 * </p>
	 * 
	 * @return <code>true</code> if search is in the array.
	 */
	public static <T> boolean contains(T[] array, T search) {
		for (T e : array) {
			if (Objects.equals(e, search)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Tests if the array contains the given element using
	 * {@link String#equalsIgnoreCase(String)}
	 * </p>
	 * 
	 * @return <code>true</code> if search is in the array.
	 */
	public static boolean containsIgnoreCase(String[] array, String search) {
		for (String e : array) {
			if (e == null && search == null) {
				return true;
			}
			if (e != null && e.equalsIgnoreCase(search)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * <p>
	 * Converts the given {@link String} to lowercase and uppers the first
	 * {@link Character} of it.
	 * </p>
	 * <p>
	 * Examples:
	 * <table>
	 * <tr>
	 * <th>Input</th>
	 * <th>Output</th>
	 * </tr>
	 * <tr>
	 * <td><code>"FUU"</code></td>
	 * <td><code>"Fuu"</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>"bar"</code></td>
	 * <td><code>"Bar"</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>null</code></td>
	 * <td><code>null</code></td>
	 * </tr>
	 * </table>
	 * </p>
	 * 
	 * @return
	 *         <ul>
	 *         <li><code>null</code> when <code>null</code> is given</li>
	 *         <li>the same {@link String} when length zero</li>
	 *         <li>the converted {@link String} when length is greater then
	 *         zero</li>
	 *         </ul>
	 */
	public static String firstCharToUpper(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		if (s.length() == 1) {
			return s.toUpperCase();
		}
		String remainder = s.substring(1).toLowerCase();
		char first = Character.toUpperCase(s.charAt(0));
		return first + remainder;
	}

	/**
	 * Sets the {@link UUID#version()} to the specified number.
	 * 
	 * @param uuid
	 * @param targetVersion
	 * 
	 * @return a new {@link UUID} or the same if not modified.
	 */
	public static UUID setUUIDVersion(UUID uniqueId, short targetVersion) {
		if (targetVersion <= 0 || targetVersion > 5) {
			throw new IllegalArgumentException("Illegal target uuid version: " + targetVersion);
		}
		if (uniqueId.version() == targetVersion) {
			return uniqueId;
		}
		long msb = uniqueId.getMostSignificantBits();
		msb &= ~(uniqueId.version() << 12);
		msb |= targetVersion << 12;
		return new UUID(msb, uniqueId.getLeastSignificantBits());
	}
	
	public static String shortStr(String str, int wantLength) {
		if (str.length() > wantLength) {
			str = str.substring(0, wantLength);
		}
		return str;
	}
	
	public static boolean containsInArray(String[] args, String name) {
		for (String s : args) {
			if (s.contains(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param array to convert
	 * @param color if you want to replace all & to §
	 * @return each array in a sane line.
	 */
	public static String arrayToLine(String[] array) {
		return arrayToLine(array, false);
	}
	
	/**
	 * If you want to color your array, just use <code>true</code> as second parameter.
	 */
	public static String arrayToLine(String[] array, boolean color) {
		return handleArray(array, color, " ");
	}
	
	/**
	 * If you want to color your array, just use <code>true</code> as second parameter.
	 */
	public static String arrayToString(String[] array) {
		return arrayToString(array, false);
	}
	
	/**
	 * @param array to convert
	 * @param color if you want to replace all & to §
	 * @return each array in a new line.
	 */
	public static String arrayToString(String[] array, boolean color) {
		return handleArray(array, color, "\n");
	}
	
	private static String handleArray(String[] array, boolean color, String afterEachArray) {
		String s = "";
		for (int i = 0; i < array.length; i++) {
			s += array[i] + (i == array.length -1 ? "" : afterEachArray);
		}
		return color ? colorString(s) : s;
	}
	
	public static String colorString(String input) {
		return input.replace("\u0026", "\u00A7");
	}
	
	public static String decolorString(String input) {
		return input.replace("\u00A7", "\u0026");
	}
	
	public static boolean isNumberic(String number) {
		try {
			Double.valueOf(number);
		} catch (NumberFormatException exception) {
			return false;
		}
		return true;
	}
	
	/**
	 * Creates a String of random alphabetics<br>
	 * mixed with the current time millis.<br>
	 * e.g: 16W1V2y6T4M9N813186
	 */
	public static String randomObjectId() {
		final String randomString = RandomStringUtils.randomAlphabetic(6);
		final StringBuilder stringBuilder = new StringBuilder("" + System.currentTimeMillis());
		int j = 0;
		boolean set = true;
		for (int i = 2; i <= randomString.length() * 2; i++) {
			if (set) {
				stringBuilder.insert(i, randomString.charAt(j));
				j++;
			}
			set = !set;
		}
		return stringBuilder.toString();
	}
	
	public static String readContentFromFile(File file) {
		try {
			return new String(Files.readAllBytes(Paths.get(file.getPath())));
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return null;
	}
	
	public static void writeJson(File file, JSONObject json) {
		try {
			final FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(GSON.toJson(json));
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException exception) {
			Logger.getGlobal().log(Level.WARNING, "Error while creating config file:", exception);
		}
	}
	
	public static Session getSession(ChannelHandlerContext ctx, Object networkDevice) {
		final Channel channel = ctx.channel();
		SocketAddress clientAddress = null;
		SocketAddress serverAddress = null;
		if (networkDevice instanceof NetworkServer) {
			clientAddress = channel.remoteAddress();
			serverAddress = channel.localAddress();
		} else if (networkDevice instanceof NetworkClient) {
			clientAddress = channel.localAddress();
			serverAddress = channel.remoteAddress();
		}
		return new Session(channel, clientAddress, serverAddress);
	}
}
