package com.rto.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * ValidationEngine - Central validation logic for the RTO system.
 * Implements business rules for age, VIN, email, and license checks.
 */
public class ValidationEngine {

  // Regex Patterns
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@gmail\\.com$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$"); // Indian mobile format
  private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$"); // Standard VIN format (17 chars,
                                                                                       // no I/O/Q)
  private static final Pattern REG_NUMBER_PATTERN = Pattern.compile("^[A-Z]{2}-\\d{2}-[A-Z]{1,2}-\\d{4}$"); // e.g.,
                                                                                                            // KA-01-AB-1234

  // Minimum age for license (18 years)
  private static final int MIN_LICENSE_AGE = 18;

  /**
   * Validates if a person is eligible for a license based on their DOB.
   * 
   * @param dateOfBirth Date of birth
   * @return true if age >= 18
   */
  public static boolean isEligibleForLicense(LocalDate dateOfBirth) {
    if (dateOfBirth == null)
      return false;
    int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
    return age >= MIN_LICENSE_AGE;
  }

  /**
   * Calculate age from DOB.
   */
  public static int calculateAge(LocalDate dateOfBirth) {
    if (dateOfBirth == null)
      return 0;
    return Period.between(dateOfBirth, LocalDate.now()).getYears();
  }

  /**
   * Validates email format.
   */
  public static boolean isValidEmail(String email) {
    if (email == null || email.isBlank())
      return false;
    return EMAIL_PATTERN.matcher(email).matches();
  }

  /**
   * Validates Indian mobile phone format.
   */
  public static boolean isValidPhone(String phone) {
    if (phone == null || phone.isBlank())
      return false;
    return PHONE_PATTERN.matcher(phone).matches();
  }

  /**
   * Validates standard 17-character VIN format.
   */
  public static boolean isValidVIN(String vin) {
    if (vin == null || vin.isBlank())
      return false;
    return VIN_PATTERN.matcher(vin.toUpperCase()).matches();
  }

  /**
   * Validates Indian vehicle registration number format (e.g., KA-01-AB-1234).
   */
  public static boolean isValidRegistrationNumber(String regNumber) {
    if (regNumber == null || regNumber.isBlank())
      return false;
    return REG_NUMBER_PATTERN.matcher(regNumber.toUpperCase()).matches();
  }

  /**
   * Validates username (alphanumeric, 3-20 chars).
   */
  public static boolean isValidUsername(String username) {
    if (username == null || username.isBlank())
      return false;
    return username.matches("^[a-zA-Z0-9_]{3,20}$");
  }

  /**
   * Validates password strength (min 6 chars).
   */
  public static boolean isValidPassword(String password) {
    if (password == null)
      return false;
    return password.length() >= 6;
  }

  /**
   * Validates that a string is not null or empty.
   */
  public static boolean isNotEmpty(String value) {
    return value != null && !value.trim().isEmpty();
  }

  /**
   * Validates manufacturing year is reasonable (1900 to current year).
   */
  public static boolean isValidManufacturingYear(int year) {
    int currentYear = LocalDate.now().getYear();
    return year >= 1900 && year <= currentYear;
  }

  /**
   * Simple password hash (for demo purposes - use BCrypt in production).
   */
  public static String hashPassword(String password) {
    // Simple hash for demonstration - in production use BCrypt or similar
    return Integer.toHexString(password.hashCode());
  }

  /**
   * Verify password hash.
   */
  public static boolean verifyPassword(String password, String hash) {
    return hashPassword(password).equals(hash);
  }

  /**
   * Validates Door Number (digits, -, / only).
   */
  public static boolean isValidDoorNo(String dNo) {
    if (dNo == null || dNo.isBlank())
      return false;
    return dNo.matches("^[0-9\\-/]+$");
  }

  /**
   * Validates Pincode (exactly 6 digits).
   */
  public static boolean isValidPincode(String pincode) {
    if (pincode == null || pincode.isBlank())
      return false;
    return pincode.matches("^\\d{6}$");
  }
}
