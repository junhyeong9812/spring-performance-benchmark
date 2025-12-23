package com.benchmark.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CpuIntensiveUtil {

  private CpuIntensiveUtil() {}

  public static String hashIterations(int iterations) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] data = "benchmark-data".getBytes();

      for (int i = 0; i < iterations; i++) {
        data = md.digest(data);
      }

      return bytesToHex(data);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static int countPrimes(int limit) {
    int count = 0;
    for (int i = 2; i <= limit; i++) {
      if (isPrime(i)) count++;
    }
    return count;
  }

  private static boolean isPrime(int n) {
    if (n < 2) return false;
    for (int i = 2; i * i <= n; i++) {
      if (n % i == 0) return false;
    }

    return true;
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
