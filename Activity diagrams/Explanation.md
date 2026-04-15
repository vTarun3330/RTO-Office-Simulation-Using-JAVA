Here are the detailed workflows for all 4 activity diagrams:

---

## Workflow 1: CBT Test Process (Learner's License)

**Actors:** Citizen, System

### Step-by-Step Flow:

1. **Citizen** logs into the RTO System.
2. **Citizen** opens the "Take LL Test" page.
3. **System** loads the citizen's previous test history from the database.
4. **System** displays the welcome screen with test instructions (10 questions, 10 minutes, 60% to pass).
5. **Citizen** clicks the "Start Test" button.
6. **System** fetches 10 random questions from the question bank in the DB.
7. **[FORK — Parallel Activities Begin]**
   - **System** starts a 10-minute countdown timer.
   - **System** displays the questions to the citizen.
8. **[JOIN — Both activities started]**
9. **[LOOP]** While there are more questions to answer:
   - **Citizen** selects an answer (A/B/C/D) for the current question.
   - **Citizen** navigates to the next or previous question.
10. **[LOOP ENDS]** when all questions are visited.
11. **[DECISION]** Submit Button Clicked?
    - **Yes →** Go to Step 12.
    - **No (Timer Expired) →** **System** shows "Time's Up!" alert → **System** auto-submits the test → Go to Step 14.
12. **[DECISION]** All Questions Answered?
    - **Yes →** Citizen confirms submission → Go to Step 14.
    - **No →** **System** shows "Incomplete" warning dialog → Go to Step 13.
13. **[DECISION]** Confirm Submit Anyway?
    - **Yes →** Citizen confirms submission → Go to Step 14.
    - **No →** Citizen continues answering → Clicks submit again → Returns to Step 11.
14. **System** stops the timer.
15. **System** collects all selected answers.
16. **System** grades answers against the correct answer keys.
17. **System** calculates the score (X out of 10).
18. **[DECISION]** Score ≥ 6/10 (60%)?
    - **Pass →** System sets result = "PASSED" → Auto-issues Learner's License → Updates license record in DB → **Citizen** views congratulations screen with score & LL confirmation.
    - **Fail →** System sets result = "FAILED" → **Citizen** views "Test Failed" screen with score & option to retake later.
19. **System** saves the test result to history.
20. **Citizen** closes the test window / returns to dashboard.
21. **[END]**

---

## Workflow 2: Vehicle Registration Process

**Actors:** Citizen, System, Admin

### Step-by-Step Flow:

1. **Citizen** logs in as a Citizen.
2. **Citizen** navigates to the "Vehicle Registration" page.
3. **Citizen** selects the vehicle type (Car / Bike / Truck).
4. **Citizen** enters the model name and specifications.
5. **Citizen** enters payment card details (card number, CVV).
6. **Citizen** clicks "Submit".
7. **System** validates the form inputs.
8. **[DECISION]** Form Valid?
   - **No →** System displays validation error → **Citizen** corrects input errors → **[END]**
   - **Yes →** Go to Step 9.
9. **System** processes the registration fee via the `PaymentGatewayAdapter` (Adapter Pattern).
10. **[DECISION]** Payment Successful?
    - **No →** System displays payment error message → **Citizen** views error & retries with a different card → **[END]**
    - **Yes →** Go to Step 11.
11. **System** stores the vehicle request in the DB with Status = PENDING.
12. **System** sends a notification to the Admin about the new request.
13. **Admin** logs in as Admin.
14. **Admin** opens the "Review Applications" panel.
15. **Admin** selects the pending vehicle request.
16. **Admin** verifies the submitted documents and specifications.
17. **[DECISION]** Documents Valid & Compliant?
    - **Yes →** Go to Step 18.
    - **No →** Go to Step 23.
18. **Admin** approves the application.
19. **System** generates a unique registration number.
20. **System** calculates road tax using the `TaxCalculationStrategy` (Strategy Pattern).
21. **System** creates the vehicle record with Status = ACTIVE.
22. **System** issues a digital Registration Certificate (RC) → Sends approval notification → **Citizen** receives success notification → Views registered vehicle in dashboard → **[END]**
23. **Admin** rejects the application with a reason.
24. **System** updates request status to "REJECTED".
25. **System** logs the rejection reason in the DB.
26. **System** sends a rejection notification.
27. **Citizen** receives rejection notification → Views rejection reason → **[END]**

---

## Workflow 3: License Application

**Actors:** Citizen, System, RTO Officer

### Step-by-Step Flow:

1. **Citizen** logs into the RTO Dashboard.
2. **Citizen** selects "Apply for License".
3. **Citizen** chooses the license type (Learner's License or Driving License).
4. **Citizen** enters personal details and address.
5. **Citizen** uploads identity proof documents.
6. **Citizen** clicks "Submit Application".
7. **System** validates form completeness.
8. **[DECISION]** Form Valid?
   - **No →** System displays validation errors to citizen → **Citizen** corrects form errors → **Citizen** re-submits application → **[END]**
   - **Yes →** Go to Step 9.
9. **System** generates a unique License Application ID.
10. **System** stores the application with Status = PENDING.
11. **System** sends an "Application Received" notification to the citizen (via Observer Pattern).
12. **RTO Officer** logs into the review panel.
13. **RTO Officer** views the pending applications queue.
14. **RTO Officer** selects the application to review.
15. **RTO Officer** verifies the submitted documents.
16. **[DECISION]** Documents Verified & Eligible?
    - **Yes →** Go to Step 17.
    - **No →** Go to Step 22.
17. **RTO Officer** approves the application.
18. **System** updates license status to "ACTIVE".
19. **System** sets the issue date to today.
20. **System** calculates the expiry date.
21. **System** sends approval notification to citizen → **Citizen** receives approval notification → Views active license in dashboard → **[END]**
22. **RTO Officer** rejects the application with remarks.
23. **System** updates status to "REJECTED".
24. **System** logs rejection remarks.
25. **System** sends rejection notification to citizen.
26. **Citizen** receives rejection notification → Views rejection reason → **[END]**

---

## Workflow 4: Challan Issuance & Payment

**Actors:** Admin, System, Citizen

### Step-by-Step Flow:

1. **Admin** identifies a traffic violation.
2. **Admin** enters the vehicle registration number.
3. **Admin** selects the violation type from the list (Speeding, Signal Violation, No Helmet, etc.).
4. **Admin** sets the fine amount (₹).
5. **Admin** clicks "Issue Challan".
6. **System** validates the vehicle registration number.
7. **[DECISION]** Vehicle Found in DB?
   - **No →** System displays "Vehicle Not Found" error → **Admin** verifies registration number & retries → **[END]**
   - **Yes →** Go to Step 8.
8. **System** looks up the vehicle owner's details.
9. **System** creates a challan record with Status = UNPAID.
10. **System** generates a unique Challan ID.
11. **System** sends SMS & email notification to the owner (via Observer Pattern).
12. **Citizen** receives the challan notification.
13. **Citizen** logs into the RTO Portal.
14. **Citizen** navigates to "My Challans" section.
15. **Citizen** views pending/unpaid challans.
16. **Citizen** selects the challan and clicks "Pay Now".
17. **Citizen** enters payment details.
18. **System** processes payment via `PaymentGatewayAdapter` (Adapter Pattern).
19. **[DECISION]** Transaction Successful?
    - **Yes →** Go to Step 20.
    - **No →** Go to Step 25.
20. **System** updates challan status to "PAID".
21. **System** creates a transaction record.
22. **System** generates a payment receipt.
23. **System** clears challan liability from citizen's profile.
24. **System** sends payment confirmation notification → **Citizen** receives confirmation → Views/downloads receipt → **[END]**
25. **System** displays payment failure message.
26. **System** logs the failed transaction.
27. **Citizen** views error message → Retries payment or contacts support → **[END]**

---
