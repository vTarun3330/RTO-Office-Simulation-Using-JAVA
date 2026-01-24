package com.rto.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Document Service - Handles document upload and verification
 * Supports file storage and clerk verification workflow
 */
public class DocumentService implements IService {
    private DatabaseService db;
    private static final String UPLOAD_DIR = "uploads/";

    public DocumentService() {
        this.db = DatabaseService.getInstance();
        initializeUploadDirectory();
    }

    @Override
    public void initialize() {
        System.out.println("DocumentService initialized");
    }

    /**
     * Create uploads directory if it doesn't exist
     */
    private void initializeUploadDirectory() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ Created uploads directory: " + UPLOAD_DIR);
            }
        } catch (IOException e) {
            System.err.println("❌ ERROR: Failed to create uploads directory: " + e.getMessage());
        }
    }

    /**
     * Upload a document file
     */
    public String uploadDocument(String applicationId, String applicationType, 
                                 String documentType, File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            System.err.println("❌ ERROR: Source file does not exist");
            return null;
        }

        try {
            // Generate unique filename
            String documentId = "DOC-" + System.currentTimeMillis();
            String extension = getFileExtension(sourceFile.getName());
            String fileName = documentId + extension;
            String filePath = UPLOAD_DIR + fileName;

            // Copy file to uploads directory
            Path source = sourceFile.toPath();
            Path destination = Paths.get(filePath);
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

            // Save document record to database
            String sql = """
                INSERT INTO documents 
                (document_id, application_id, application_type, document_type, file_path, status)
                VALUES (?, ?, ?, ?, ?, 'PENDING')
                """;

            boolean saved = db.executeUpdate(sql, documentId, applicationId, applicationType, 
                                            documentType, filePath);

            if (saved) {
                System.out.println("✅ Document uploaded: " + fileName);
                return documentId;
            }

        } catch (IOException e) {
            System.err.println("❌ ERROR: File upload failed: " + e.getMessage());
        }

        return null;
    }

    /**
     * Verify document (approved by clerk)
     */
    public boolean verifyDocument(String documentId, String clerkId, boolean approved, String reason) {
        String status = approved ? "APPROVED" : "REJECTED";
        String sql = """
            UPDATE documents 
            SET status = ?, verified_by = ?, verification_date = CURRENT_TIMESTAMP, rejection_reason = ?
            WHERE document_id = ?
            """;

        boolean updated = db.executeUpdate(sql, status, clerkId, reason, documentId);

        if (updated) {
            System.out.println(approved ? 
                "✅ Document APPROVED: " + documentId : 
                "⚠️ Document REJECTED: " + documentId);
        }

        return updated;
    }

    /**
     * Get all documents for an application
     */
    public List<Document> getDocumentsByApplication(String applicationId) {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE application_id = ? ORDER BY upload_date DESC";

        try (ResultSet rs = db.executeQuery(sql, applicationId)) {
            while (rs != null && rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get documents: " + e.getMessage());
        }

        return documents;
    }

    /**
     * Get all pending documents (for clerk dashboard)
     */
    public List<Document> getPendingDocuments() {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE status = 'PENDING' ORDER BY upload_date ASC";

        try (ResultSet rs = db.executeQuery(sql)) {
            while (rs != null && rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get pending documents: " + e.getMessage());
        }

        return documents;
    }

    /**
     * Check if all required documents are approved for an application
     */
    public boolean areAllDocumentsApproved(String applicationId, String[] requiredDocTypes) {
        for (String docType : requiredDocTypes) {
            String sql = "SELECT status FROM documents WHERE application_id = ? AND document_type = ?";
            try (ResultSet rs = db.executeQuery(sql, applicationId, docType)) {
                if (rs == null || !rs.next()) {
                    return false; // Document not uploaded
                }
                String status = rs.getString("status");
                if (!"APPROVED".equals(status)) {
                    return false; // Document not approved
                }
            } catch (SQLException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Delete a document
     */
    public boolean deleteDocument(String documentId) {
        // Get file path first
        String getPathSql = "SELECT file_path FROM documents WHERE document_id = ?";
        String filePath = null;

        try (ResultSet rs = db.executeQuery(getPathSql, documentId)) {
            if (rs != null && rs.next()) {
                filePath = rs.getString("file_path");
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get file path");
            return false;
        }

        // Delete from database
        String deleteSql = "DELETE FROM documents WHERE document_id = ?";
        boolean deleted = db.executeUpdate(deleteSql, documentId);

        // Delete file from disk
        if (deleted && filePath != null) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
                System.out.println("✅ Document deleted: " + documentId);
            } catch (IOException e) {
                System.err.println("⚠️ WARNING: Failed to delete file from disk");
            }
        }

        return deleted;
    }

    // Helper Methods

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }

    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        Document doc = new Document();
        doc.setDocumentId(rs.getString("document_id"));
        doc.setApplicationId(rs.getString("application_id"));
        doc.setApplicationType(rs.getString("application_type"));
        doc.setDocumentType(rs.getString("document_type"));
        doc.setFilePath(rs.getString("file_path"));
        doc.setStatus(rs.getString("status"));
        doc.setVerifiedBy(rs.getString("verified_by"));
        doc.setRejectionReason(rs.getString("rejection_reason"));

        Timestamp uploadDate = rs.getTimestamp("upload_date");
        if (uploadDate != null) {
            doc.setUploadDate(uploadDate.toLocalDateTime());
        }

        Timestamp verificationDate = rs.getTimestamp("verification_date");
        if (verificationDate != null) {
            doc.setVerificationDate(verificationDate.toLocalDateTime());
        }

        return doc;
    }

    // Inner Class

    public static class Document {
        private String documentId;
        private String applicationId;
        private String applicationType;
        private String documentType;
        private String filePath;
        private String status;
        private String verifiedBy;
        private String rejectionReason;
        private LocalDateTime uploadDate;
        private LocalDateTime verificationDate;

        // Getters and Setters
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }

        public String getApplicationId() { return applicationId; }
        public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

        public String getApplicationType() { return applicationType; }
        public void setApplicationType(String applicationType) { this.applicationType = applicationType; }

        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getVerifiedBy() { return verifiedBy; }
        public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

        public LocalDateTime getUploadDate() { return uploadDate; }
        public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

        public LocalDateTime getVerificationDate() { return verificationDate; }
        public void setVerificationDate(LocalDateTime verificationDate) { this.verificationDate = verificationDate; }
    }
}
