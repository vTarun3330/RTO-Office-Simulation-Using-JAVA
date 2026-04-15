## State Diagram 1: License Lifecycle — Detailed Workflow

**Object Being Tracked:** `License` object (status field)

### States & Transitions:

1. **[●] → NotApplied**
   - The citizen exists in the system but has not applied for any license.
   - No `License` record exists in the database for this user.

2. **NotApplied → Pending** — `submitApplication()`
   - **Trigger:** Citizen fills the license application form with personal details (name, email, address, blood group) and uploads identity documents.
   - **Action:** System generates a unique License ID (`LIC + timestamp`), sets `status = "PENDING"`, stores the record in the DB.
   - **Entry Activity:** `notifyOfficer()` — the RTO Officer is alerted that a new application is in the review queue.

3. **Pending → Rejected** — `rejectLicense()`
   - **Trigger:** RTO Officer reviews the application and finds discrepancies (invalid documents, age ineligibility, incomplete information).
   - **Action:** `status` is set to `"REJECTED"`.
   - **Entry Activity:** `notifyCitizen(reason)` — the citizen receives a notification with the specific rejection reason.

4. **Rejected → Pending** — `reSubmit()`
   - **Trigger:** Citizen corrects the errors (re-uploads documents, fixes details) and resubmits.
   - **Action:** Documents are updated, `status` is reset to `"PENDING"`.
   - The application re-enters the officer's review queue.

5. **Pending → LL_Active** — `approveLearnerLicense()`
   - **Trigger:** RTO Officer verifies all documents and approves the Learner's License.
   - **Action:** `status = "ACTIVE"`, `issueDate = today`, `expiryDate = today + 6 months`.
   - The system enters the **LL_Active composite state**.

6. **Inside LL_Active (Composite State):**
   - **[●] → LL_Valid** — License is valid; citizen can legally practice driving.
   - **LL_Valid → LL_Expired** — Guard: `[currentDate > expiryDate]` — the LL has passed its 6-month validity.
     - **Entry Activity:** `notifyCitizen("LL expired")` — citizen is alerted.
   - **LL_Expired → LL_Valid** — `renewLL()` — citizen renews, `expiryDate` extended by another 6 months.

7. **LL_Active → DL_Pending** — `applyForDL()`
   - **Guard:** `[daysSinceLL >= 30]` — citizen can only apply after 30 days of LL issuance.
   - **Trigger:** Citizen applies for a permanent Driving License.
   - **Action:** A new DL application is submitted.

8. **DL_Pending → Rejected** — `rejectDL()`
   - **Trigger:** Officer rejects the DL application (failed driving test, incomplete docs).
   - **Action:** `status = "REJECTED"`. Citizen can resubmit (goes back to Pending via `reSubmit()`).

9. **DL_Pending → DL_Active** — `approveDrivingLicense()`
   - **Trigger:** Citizen passes the driving test and officer approves.
   - **Action:** `status = "ACTIVE"`, `expiryDate = today + 10 years`.
   - The system enters the **DL_Active composite state**.

10. **Inside DL_Active (Composite State):**
    - **[●] → DL_Valid** — License is valid; citizen can drive legally.
    - **DL_Valid → DL_NearExpiry** — Guard: `[expiryDate - currentDate <= 90 days]` — within 3 months of expiry.
      - **Entry Activity:** `sendRenewalReminder()` — system proactively alerts the citizen.
    - **DL_NearExpiry → DL_Expired** — Guard: `[currentDate > expiryDate]` — the DL has expired.
    - **DL_Valid → DL_Expired** — Direct expiry if citizen ignores near-expiry warnings.
      - **Entry Activity:** `notifyCitizen("DL expired")`.
    - **DL_Expired → DL_Valid** — `renewDL()` — `expiryDate` extended by 10 years.

11. **DL_Active → Suspended** — `suspendLicense()`
    - **Trigger:** License is temporarily suspended due to serious traffic violations.
    - **Action:** `status = "SUSPENDED"`.

12. **Suspended → DL_Active** — `reinstateLicense()`
    - **Trigger:** Suspension period is over, or citizen completes corrective actions.
    - **Action:** `status = "ACTIVE"`.

13. **Suspended → Revoked** — `revokeLicense()`
    - **Trigger:** Repeat offenses or court order leads to permanent revocation.
    - **Action:** `status = "REVOKED"`.

14. **Revoked → [⊙]** — Terminal state. License is permanently revoked and cannot be reinstated.

15. **DL_Active → [⊙]** — `terminateLicense()` — Citizen deceased or voluntarily surrenders the license.

---

## State Diagram 2: Vehicle Registration Request Lifecycle — Detailed Workflow

**Object Being Tracked:** `VehicleRequest` object (status field)

### States & Transitions:

1. **[●] → Draft**
   - The citizen has opened the Vehicle Registration page but hasn't submitted yet.
   - **Do Activity:** `enterVehicleDetails()` — citizen is actively filling in vehicle type, model, and specifications.

2. **Draft → PaymentProcessing** — `submitForm()`
   - **Trigger:** Citizen clicks "Submit" after filling the form.
   - **Action:** System validates inputs (model not empty, type selected).
   - **Entry Activity:** `processPayment()` — the system initiates fee processing via `PaymentGatewayAdapter`.

3. **PaymentProcessing → Draft** — `paymentFailed`
   - **Trigger:** Payment is declined (invalid card, insufficient funds).
   - **Action:** Error message is displayed. Citizen returns to the form to retry with different card details.

4. **PaymentProcessing → PendingApproval** — `paymentSuccess`
   - **Trigger:** Payment is confirmed by the gateway.
   - **Action:** `VehicleRequest` is stored in DB with `status = "PENDING"`.
   - **Entry Activity:** `notifyAdmin()` — admin is alerted about the new pending request.

5. **Inside PendingApproval (Composite State):**
   - **[●] → UnderReview** — Admin is actively verifying the documents and vehicle specifications.
   - **UnderReview → NeedsClarification** — Guard: `[documents incomplete or specs unclear]` — admin finds issues.
     - **Entry Activity:** `notifyCitizen("Provide more info")` — citizen gets a request for additional information.
   - **NeedsClarification → UnderReview** — `citizenUpdatesDetails()` — citizen resubmits corrected/additional documents. Admin re-reviews.

6. **PendingApproval → Approved** — `approveRequest()`
   - **Trigger:** Admin verifies everything is valid and compliant.
   - **Action:** `status = "APPROVED"`, `approvedBy = adminId`, `approvalDate = now`.

7. **PendingApproval → Rejected** — `rejectRequest()`
   - **Trigger:** Invalid documents, illegal specifications, or non-compliance.
   - **Action:** `status = "REJECTED"`.
   - **Entry Activity:** `notifyCitizen(reason)` — citizen is informed with the rejection reason.

8. **Rejected → PendingApproval** — `resubmit()`
   - **Trigger:** Citizen corrects the details and resubmits.
   - **Action:** Status reset to `"PENDING"`, request re-enters the admin queue.

9. **Rejected → [⊙]** — `abandonRequest()` — Citizen chooses not to resubmit. Request is closed.

10. **Approved → Registered** — `autoMigrateToVehicle()`
    - **Trigger:** System automatically processes the approved request.
    - **Action:** Generates a unique Registration Number, calculates road tax using `TaxCalculationStrategy` (Strategy Pattern), creates a full `Vehicle` record in the DB.
    - **Entry Activities:** `issueRC()` — digital Registration Certificate is issued. `notifyCitizen("RC issued")` — citizen is notified.

11. **Registered → [⊙]** — Terminal state. The vehicle is officially registered and active in the system. The `VehicleRequest` lifecycle ends here; the `Vehicle` object takes over.

---

## State Diagram 3: Challan (Traffic Violation) Lifecycle — Detailed Workflow

**Object Being Tracked:** `Challan` object (isPaid boolean + time-based checks)

### States & Transitions:

1. **[●] → Issued**
   - **Trigger:** Admin identifies a traffic violation and clicks "Issue Challan".
   - **Entry Activities:**
     - `generateChallanId()` — unique ID created (`CHN- + timestamp`).
     - `setIssueDate(today)` — records when the challan was issued.
     - `notifyOwner()` — SMS/email sent to the vehicle owner via Observer Pattern.
   - `isPaid = false` at creation.

2. **Issued → Unpaid** — Guard: `[challan created successfully]`
   - **Trigger:** Automatic transition once the challan is stored in DB.
   - **Do Activity:** `displayInCitizenDashboard()` — the challan appears in the citizen's "My Challans" section.
   - The citizen is within the **30-day grace period** for payment.

3. **Unpaid → Paid** — `processPayment(amount)`
   - **Trigger:** Citizen pays the exact fine amount within 30 days via the RTO portal.
   - **Action:** `markAsPaid(transactionId)` — sets `isPaid = true` and links the payment transaction.
   - **Happy path** — no penalty applied.

4. **Unpaid → Overdue** — Guard: `[currentDate > issueDate + 30 days] [isPaid == false]`
   - **Trigger:** Automatic time-based transition. The fine remains unpaid for over 30 days.
   - This maps to `isOverdue()` returning `true` in the code.
   - **Entry Activities:**
     - `addPenalty(10%)` — a 10% late fee is added. `calculatePenalty()` returns `amount * 0.10`.
     - `sendOverdueNotification()` — citizen is alerted about the overdue status and penalty.
   - `getTotalDue()` now returns `amount + calculatePenalty()`.

5. **Overdue → Paid** — `processPayment(totalDue)`
   - **Trigger:** Citizen pays the fine **plus** the 10% late fee penalty.
   - **Action:** `markAsPaid(transactionId)`. The total paid = original fine + penalty.

6. **Overdue → Escalated** — Guard: `[currentDate > issueDate + 90 days] [isPaid == false]`
   - **Trigger:** Automatic time-based transition. Fine remains unpaid for over 90 days.
   - **Entry Activity:** `notifyAuthorities()` — the matter is escalated to higher authority or court.

7. **Escalated → Paid** — `resolveAndPay()`
   - **Trigger:** The matter is resolved (court ruling, citizen settlement).
   - **Action:** `markAsPaid(transactionId)`.

8. **Paid** — Fine fully settled.
   - **Entry Activities:**
     - `generateReceipt()` — a payment receipt is created for the citizen.
     - `clearFromProfile()` — the challan liability is removed from the citizen's active profile.

9. **Paid → Closed** — Guard: `[after record retention period]`
   - **Trigger:** After the system's record retention period (e.g., 1 year), the challan is archived.

10. **Closed → [⊙]** — Terminal state. Challan is archived in system records.

11. **Paid → [⊙]** — Alternative direct termination (for simpler cases).

---

## State Diagram 4: Payment Transaction Lifecycle — Detailed Workflow

**Object Being Tracked:** `Transaction` object (status field: PENDING, SUCCESS, FAILED)

### States & Transitions:

1. **[●] → Created**
   - **Trigger:** A payment is initiated (vehicle registration fee, challan payment, etc.).
   - **Entry Activities:**
     - `generateTransactionId()` — unique ID created (`TXN + timestamp`).
     - `setTimestamp(now)` — records the creation time.
   - `status = "PENDING"`.

2. **Created → Validating** — `initiatePayment()`
   - **Trigger:** System sends the card details to the `PaymentGatewayAdapter`.
   - **Do Activities:**
     - `validateCard(cardNumber, cvv)` — checks card number length (13-19 digits), format (all digits), and CVV (3-4 digits).
     - `luhnCheck(cardNumber)` — runs the Luhn algorithm to verify the card number's checksum.

3. **Validating → CardInvalid** — Guard: `[card validation fails]`
   - **Trigger:** Card number is too short/long, contains non-digits, CVV is invalid, or Luhn check fails.
   - **Entry Activity:** `log("Invalid card details")`.

4. **CardInvalid → Failed** — `markFailed()`
   - **Trigger:** Automatic transition. Invalid card means the transaction cannot proceed.

5. **Validating → Processing** — Guard: `[card valid]`
   - **Trigger:** Card format, length, and Luhn check all pass.
   - **Do Activities:**
     - `simulateDelay()` — simulates network latency (500ms + 300ms + 200ms in code).
     - `contactBankServer()` — the API communicates with the (simulated) bank.

6. **Processing → Success** — Guard: `[bankApproves == true]` (95% probability)
   - **Trigger:** The simulated bank server approves the transaction. In code: `random.nextDouble() < 0.95`.
   - **Entry Activities:**
     - `generateReference()` — a transaction reference ID is created (`TXN + timestamp + random 4 digits`).
     - `log("Transaction SUCCESSFUL")`.

7. **Processing → Declined** — Guard: `[bankApproves == false]` (5% probability)
   - **Trigger:** Bank declines the transaction. In code: the 5% failure case.
   - **Entry Activity:** `log("Bank declined")`.

8. **Declined → Failed** — `markFailed()`
   - **Trigger:** Automatic transition. Bank decline means the payment cannot succeed.

9. **Processing → Timeout** — Guard: `[no response within timeout period]`
   - **Trigger:** The payment gateway does not respond within the expected time.

10. **Timeout → Processing** — `retry()`
    - **Trigger:** The system (or user) retries the payment. The API call is re-initiated.

11. **Timeout → Failed** — `cancel()`
    - **Trigger:** The user cancels the retry attempt.

12. **Success → Completed** — `updateRecords()`
    - **Trigger:** The gateway confirmed, now the system updates its own records.
    - **Action:** Links the transaction to the relevant challan/vehicle/license via `referenceId`.
    - **Entry Activities:**
      - `markSuccess()` — `status = "SUCCESS"`.
      - `updateSystemRecords()` — the challan is marked paid, or the vehicle is registered, etc.

13. **Failed** — Terminal failure state.
    - **Entry Activities:**
      - `markFailed()` — `status = "FAILED"`.
      - `notifyUser("Payment failed")` — user is shown an error message.

14. **Failed → [⊙]** — Terminal state.

15. **Completed → [⊙]** — Terminal state. Transaction is successfully completed and all records are synchronized.

---

## Summary Table

| Diagram | Object | Total States | Composite States | Terminal Paths | Code Files |
|---|---|:---:|:---:|:---:|---|
| 1. License Lifecycle | `License` | 9 | 2 (LL_Active, DL_Active) | 2 (Revoked, Terminated) | `License.java`, `LicenseService.java` |
| 2. Vehicle Request | `VehicleRequest` | 7 | 1 (PendingApproval) | 2 (Registered, Abandoned) | `VehicleRequest.java`, `VehicleService.java` |
| 3. Challan Lifecycle | `Challan` | 6 | 0 | 2 (Closed, Paid direct) | `Challan.java`, `ChallanService.java` |
| 4. Payment Transaction | `Transaction` | 9 | 0 | 2 (Failed, Completed) | `Transaction.java`, `PaymentGatewayAdapter.java` |