package dev.lvstrng.muam3r.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class EncryptedString implements CharSequence {
	char[] key, value;

	public EncryptedString(String s) {
		int len = s.length();
		char[] real = new char[Math.min(len, 64)];
		ThreadLocalRandom current = ThreadLocalRandom.current();
		for (int i = 0; i < real.length; i++) {
			real[i] = (char) current.nextInt(0xFFFF);
		}
		char[] ca = s.toCharArray();
		doIter(ca, real, 0, ca.length);
		this.key = real;
		this.value = ca;
	}

	public static EncryptedString of(String s) {
		return new EncryptedString(s);
	}

	public static EncryptedString of(String encrypted, String key) {
		return new EncryptedString(encrypted.toCharArray(), key.toCharArray());
	}

	private static void doIter(char[] c, char[] v, int offset, int length) {
		for (int i = offset; i < offset + length; i++) {
			c[i] = (char) (c[i] ^ v[i % v.length]);
		}
	}

	public EncryptedString(char[] value, char[] key) {
		this.key = key;
		this.value = value;
	}

	@Override
	public int length() {
		return value.length;
	}

	@Override
	public char charAt(int index) {
		return (char) (value[index] ^ key[index % key.length]);
	}

	@Override
	public @NotNull String toString() {
		char[] copied = Arrays.copyOf(value, value.length);
		doIter(copied, key, 0, copied.length);
		return new String(copied).intern();
	}

	@Override
	public @NotNull CharSequence subSequence(int start, int end) {
		int length = end - start;
		char[] newValue = new char[length];
		char[] newKey = new char[length];
		System.arraycopy(value, start, newValue, 0, length);
		for (int i = 0; i < length; i++) {
			newKey[i] = key[(start + i) % key.length];
		}
		return new EncryptedString(newValue, newKey);
	}
}