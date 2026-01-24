package com.rto.service;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Slot Service - Manages test slot booking and driving test scheduling
 * Implements 7-day cooloff period after failed tests
 */
public class SlotService implements IService {
    private DatabaseService db;
    private static final int COOLOFF_DAYS = 7;
    private static final int DEFAULT_SLOT_CAPACITY = 5;

    public SlotService() {
        this.db = DatabaseService.getInstance();
    }

    @Override
    public void initialize() {
        System.out.println("SlotService initialized");
    }

    /**
     * Get available slots for a date range
     */
    public List<TestSlot> getAvailableSlots(LocalDate fromDate, LocalDate toDate, String testType) {
        List<TestSlot> slots = new ArrayList<>();
        
        String sql = """
            SELECT * FROM test_slots 
            WHERE slot_date BETWEEN ? AND ? 
            AND test_type = ? 
            AND booked_count < capacity 
            ORDER BY slot_date, slot_time
            """;
        
        try (ResultSet rs = db.executeQuery(sql, java.sql.Date.valueOf(fromDate), java.sql.Date.valueOf(toDate), testType)) {
            while (rs != null && rs.next()) {
                slots.add(mapResultSetToSlot(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get available slots: " + e.getMessage());
        }
        
        return slots;
    }

    /**
     * Book a slot for a user
     */
    public boolean bookSlot(String slotId, String userId, String licenseId) {
        // Check cooloff period
        if (isInCooloffPeriod(userId)) {
            LocalDate cooloffEnd = getCooloffEndDate(userId);
            System.err.println("❌ BLOCKED: You are in cooloff period until " + cooloffEnd);
            return false;
        }

        // Check slot availability
        TestSlot slot = getSlotById(slotId);
        if (slot == null) {
            System.err.println("❌ ERROR: Slot not found");
            return false;
        }

        if (slot.getBookedCount() >= slot.getCapacity()) {
            System.err.println("❌ ERROR: Slot is full");
            return false;
        }

        try {
            // Create booking
            String bookingId = "BK-" + System.currentTimeMillis();
            String insertSql = """
                INSERT INTO slot_bookings 
                (booking_id, slot_id, user_id, license_id, status, test_result)
                VALUES (?, ?, ?, ?, 'BOOKED', NULL)
                """;
            
            boolean inserted = db.executeUpdate(insertSql, bookingId, slotId, userId, licenseId);
            
            if (inserted) {
                // Increment booked count
                String updateSql = "UPDATE test_slots SET booked_count = booked_count + 1 WHERE slot_id = ?";
                db.executeUpdate(updateSql, slotId);
                
                System.out.println("✅ Slot booked successfully: " + bookingId);
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: Booking failed: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Check if user is in cooloff period (7 days after failed test)
     */
    public boolean isInCooloffPeriod(String userId) {
        String sql = "SELECT cooloff_until FROM slot_bookings WHERE user_id = ? AND cooloff_until > CURRENT_DATE ORDER BY cooloff_until DESC LIMIT 1";
        
        try (ResultSet rs = db.executeQuery(sql, userId)) {
            return rs != null && rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get cooloff end date
     */
    public LocalDate getCooloffEndDate(String userId) {
        String sql = "SELECT cooloff_until FROM slot_bookings WHERE user_id = ? AND cooloff_until > CURRENT_DATE ORDER BY cooloff_until DESC LIMIT 1";
        
        try (ResultSet rs = db.executeQuery(sql, userId)) {
            if (rs != null && rs.next()) {
                java.sql.Date cooloffDate = rs.getDate("cooloff_until");
                return cooloffDate != null ? cooloffDate.toLocalDate() : null;
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get cooloff date");
        }
        
        return null;
    }

    /**
     * Mark test result (for MVI)
     */
    public boolean markTestResult(String bookingId, String result, String mviId) {
        if (!"PASS".equals(result) && !"FAIL".equals(result)) {
            System.err.println("❌ ERROR: Invalid result. Must be PASS or FAIL");
            return false;
        }

        String sql = "UPDATE slot_bookings SET status = 'COMPLETED', test_result = ? WHERE booking_id = ?";
        boolean updated = db.executeUpdate(sql, result, bookingId);

        if (updated && "FAIL".equals(result)) {
            // Apply 7-day cooloff
            LocalDate cooloffEnd = LocalDate.now().plusDays(COOLOFF_DAYS);
            String cooloffSql = "UPDATE slot_bookings SET cooloff_until = ? WHERE booking_id = ?";
            db.executeUpdate(cooloffSql, java.sql.Date.valueOf(cooloffEnd), bookingId);
            
            System.out.println("⚠️ Test FAILED. Cooloff period applied until: " + cooloffEnd);
        } else if (updated && "PASS".equals(result)) {
            System.out.println("✅ Test PASSED!");
            
            // Upgrade to DL
            String getUserSql = "SELECT user_id FROM slot_bookings WHERE booking_id = ?";
            try (ResultSet rs = db.executeQuery(getUserSql, bookingId)) {
                if (rs != null && rs.next()) {
                    String userId = rs.getString("user_id");
                    LicenseService licenseService = new LicenseService();
                    licenseService.upgradeToDL(userId);
                }
            } catch (SQLException e) {
                System.err.println("❌ ERROR: Failed to upgrade license");
            }
        }

        return updated;
    }

    /**
     * Get bookings for a specific date (for MVI dashboard)
     */
    public List<SlotBooking> getBookingsForDate(LocalDate date, String mviId) {
        List<SlotBooking> bookings = new ArrayList<>();
        
        String sql = """
            SELECT sb.* FROM slot_bookings sb
            JOIN test_slots ts ON sb.slot_id = ts.slot_id
            WHERE ts.slot_date = ? AND ts.mvi_officer_id = ? AND sb.status = 'BOOKED'
            ORDER BY ts.slot_time
            """;
        
        try (ResultSet rs = db.executeQuery(sql, java.sql.Date.valueOf(date), mviId)) {
            while (rs != null && rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get bookings: " + e.getMessage());
        }
        
        return bookings;
    }

    // Helper Methods

    private TestSlot getSlotById(String slotId) {
        String sql = "SELECT * FROM test_slots WHERE slot_id = ?";
        try (ResultSet rs = db.executeQuery(sql, slotId)) {
            if (rs != null && rs.next()) {
                return mapResultSetToSlot(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get slot");
        }
        return null;
    }

    private TestSlot mapResultSetToSlot(ResultSet rs) throws SQLException {
        TestSlot slot = new TestSlot();
        slot.setSlotId(rs.getString("slot_id"));
        slot.setSlotDate(rs.getDate("slot_date").toLocalDate());
        slot.setSlotTime(rs.getTime("slot_time").toLocalTime());
        slot.setTestType(rs.getString("test_type"));
        slot.setMviOfficerId(rs.getString("mvi_officer_id"));
        slot.setCapacity(rs.getInt("capacity"));
        slot.setBookedCount(rs.getInt("booked_count"));
        return slot;
    }

    private SlotBooking mapResultSetToBooking(ResultSet rs) throws SQLException {
        SlotBooking booking = new SlotBooking();
        booking.setBookingId(rs.getString("booking_id"));
        booking.setSlotId(rs.getString("slot_id"));
        booking.setUserId(rs.getString("user_id"));
        booking.setLicenseId(rs.getString("license_id"));
        booking.setStatus(rs.getString("status"));
        booking.setTestResult(rs.getString("test_result"));
        
        java.sql.Date cooloff = rs.getDate("cooloff_until");
        if (cooloff != null) {
            booking.setCooloffUntil(cooloff.toLocalDate());
        }
        
        return booking;
    }

    // Inner Classes

    public static class TestSlot {
        private String slotId;
        private LocalDate slotDate;
        private LocalTime slotTime;
        private String testType;
        private String mviOfficerId;
        private int capacity;
        private int bookedCount;

        // Getters and Setters
        public String getSlotId() { return slotId; }
        public void setSlotId(String slotId) { this.slotId = slotId; }
        
        public LocalDate getSlotDate() { return slotDate; }
        public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }
        
        public LocalTime getSlotTime() { return slotTime; }
        public void setSlotTime(LocalTime slotTime) { this.slotTime = slotTime; }
        
        public String getTestType() { return testType; }
        public void setTestType(String testType) { this.testType = testType; }
        
        public String getMviOfficerId() { return mviOfficerId; }
        public void setMviOfficerId(String mviOfficerId) { this.mviOfficerId = mviOfficerId; }
        
        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
        
        public int getBookedCount() { return bookedCount; }
        public void setBookedCount(int bookedCount) { this.bookedCount = bookedCount; }
        
        public boolean isAvailable() { return bookedCount < capacity; }
    }

    public static class SlotBooking {
        private String bookingId;
        private String slotId;
        private String userId;
        private String licenseId;
        private String status;
        private String testResult;
        private LocalDate cooloffUntil;

        // Getters and Setters
        public String getBookingId() { return bookingId; }
        public void setBookingId(String bookingId) { this.bookingId = bookingId; }
        
        public String getSlotId() { return slotId; }
        public void setSlotId(String slotId) { this.slotId = slotId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getLicenseId() { return licenseId; }
        public void setLicenseId(String licenseId) { this.licenseId = licenseId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getTestResult() { return testResult; }
        public void setTestResult(String testResult) { this.testResult = testResult; }
        
        public LocalDate getCooloffUntil() { return cooloffUntil; }
        public void setCooloffUntil(LocalDate cooloffUntil) { this.cooloffUntil = cooloffUntil; }
    }
}
