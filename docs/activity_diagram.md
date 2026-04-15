# RTO Office Simulation - UML Activity Diagrams

## 1. CBT Test Process (Learner's License)

This activity diagram demonstrates the operational workflow of the Computer Based Test (CBT) for obtaining a Learner's License.

```plantuml
@startuml CBT_Activity_Diagram
title CBT Test Process - Learner's License

|Citizen|
start
:Login to RTO System;
:Open "Take LL Test" Page;

|#F0F8FF|System|
:Load Previous Test History;
:Display Welcome Screen & Instructions;

|Citizen|
:Click "Start Test" Button;

|#F0F8FF|System|
:Fetch 10 Random Questions from DB;

fork
  :Start 10-Minute Countdown Timer;
fork again
  :Display Questions to Citizen;
end fork

|Citizen|
while (More Questions to Answer?) is (Yes)
  :Select Answer (A/B/C/D);
  :Navigate to Next/Previous Question;
endwhile (No)

if (Submit Button Clicked?) then (Yes)
  if (All Questions Answered?) then (Yes)
    :Confirm Submission;
  else (No)
    |#F0F8FF|System|
    :Show "Incomplete" Warning Dialog;
    |Citizen|
    if (Confirm Submit Anyway?) then (Yes)
      :Confirm Submission;
    else (No)
      :Continue Answering;
      :Click Submit Again;
    endif
  endif
else (No - Timer Expired)
  |#F0F8FF|System|
  :Show "Time's Up!" Alert;
  :Auto-Submit Test;
endif

|#F0F8FF|System|
:Stop Timer;
:Collect Selected Answers;
:Grade Answers Against Correct Keys;
:Calculate Score (X/10);

if (Score >= 6/10 (60%)?) then (Pass)
  :Set Result = "PASSED";
  :Auto-Issue Learner's License;
  :Update License Record in DB;
  |Citizen|
  :View "CONGRATULATIONS! You PASSED!" Screen;
  :View Score & LL Issuance Confirmation;
else (Fail)
  :Set Result = "FAILED";
  |Citizen|
  :View "Test Failed" Screen;
  :View Score & Option to Retake Later;
endif

|#F0F8FF|System|
:Save Test Result to History;

|Citizen|
:Close Test Window / Return to Dashboard;
stop

@enduml
```

### Key Logic Points:
1. **Fork/Join**: Timer and question display run concurrently (parallel activities shown with fork bar).
2. **Incomplete Submission**: If not all questions are answered, a confirmation dialog asks whether to submit anyway.
3. **Auto-Submit**: When timer expires, the system force-submits regardless of completion.
4. **Pass Threshold**: Score >= 6/10 triggers automatic Learner's License issuance.
5. **All paths converge** to the final node — no dangling branches.

---

## 2. Vehicle Registration Process

This diagram shows the end-to-end workflow of registering a vehicle, covering Citizen submission, payment, and Admin approval.

```plantuml
@startuml Registration_Activity_Diagram
title Vehicle Registration Process

|Citizen|
start
:Login as Citizen;
:Navigate to "Vehicle Registration";
:Select Vehicle Type (Car/Bike/Truck);
:Enter Model & Specifications;
:Enter Payment Card Details;
:Click "Submit";

|#F0F8FF|System|
:Validate Form Inputs;

if (Form Valid?) then (Yes)
  :Process Fee via PaymentGatewayAdapter;
  
  if (Payment Successful?) then (Yes)
    :Store Vehicle Request in DB\n(Status = PENDING);
    :Send Notification to Admin;
    
    |#FFE4E1|Admin|
    :Login as Admin;
    :Open "Review Applications";
    :Select Pending Vehicle Request;
    :Verify Documents & Specifications;
    
    if (Documents Valid & Compliant?) then (Yes)
      :Approve Application;
      
      |#F0F8FF|System|
      :Generate Registration Number;
      :Calculate Road Tax (via Strategy);
      :Create Vehicle Record (Status = ACTIVE);
      :Issue Digital Registration Certificate;
      :Send Approval Notification;
      
      |Citizen|
      :Receive Success Notification;
      :View Registered Vehicle in Dashboard;
    else (No)
      :Reject Application with Reason;
      
      |#F0F8FF|System|
      :Update Request Status to "REJECTED";
      :Log Rejection Reason in DB;
      :Send Rejection Notification;
      
      |Citizen|
      :Receive Rejection Notification;
      :View Rejection Reason;
    endif
  else (No)
    :Display Payment Error Message;
    
    |Citizen|
    :View Error & Retry with Different Card;
  endif
else (No)
  :Display Validation Error;
  
  |Citizen|
  :Correct Input Errors;
endif

stop

@enduml
```

### Process Highlights:
1. **Input Validation**: The system validates form inputs before processing payment.
2. **Payment via Adapter**: Uses the `PaymentGatewayAdapter` (Adapter Pattern) for fee processing.
3. **Admin Approval**: Citizens cannot self-register vehicles; an admin must approve pending requests.
4. **Strategy Pattern**: Tax calculation uses the `TaxCalculationStrategy` interface.
5. **All branches converge** to the final node.

---

## 3. License Application Workflow

This diagram models the process of applying for a license (LL or DL) and the administrative review cycle.

```plantuml
@startuml License_Application_Activity
title License Application Workflow

|Citizen|
start
:Login to RTO Dashboard;
:Select "Apply for License";
:Choose License Type (LL / DL);
:Enter Personal Details & Address;
:Upload Identity Proof Documents;
:Click "Submit Application";

|#F0F8FF|System|
:Validate Form Completeness;

if (Form Valid?) then (Yes)
  :Generate Unique License Application ID;
  :Store Application (Status = PENDING);
  :Send "Application Received" Notification\n(via Observer Pattern);
  
  |#FFE4E1|RTO Officer|
  :Login to Review Panel;
  :View Pending Applications Queue;
  :Select Application to Review;
  :Verify Submitted Documents;
  
  if (Documents Verified & Eligible?) then (Yes)
    :Approve Application;
    
    |#F0F8FF|System|
    :Update License Status to "ACTIVE";
    :Set Issue Date = Today;
    :Calculate Expiry Date;
    :Send Approval Notification to Citizen;
    
    |Citizen|
    :Receive Approval Notification;
    :View Active License in Dashboard;
  else (No)
    :Reject Application with Remarks;
    
    |#F0F8FF|System|
    :Update Status to "REJECTED";
    :Log Rejection Remarks;
    :Send Rejection Notification to Citizen;
    
    |Citizen|
    :Receive Rejection Notification;
    :View Rejection Reason;
  endif
else (No)
  :Display Validation Errors to Citizen;
  
  |Citizen|
  :Correct Form Errors;
  :Re-submit Application;
endif

stop

@enduml
```

### Key Corrections:
1. **Removed invalid `goto`** — PlantUML does not support `goto` labels; replaced with a proper branch that ends cleanly.
2. **Fixed Yes/No swap** — "Documents Verified? → Yes" now correctly leads to **Approve**, and "No" leads to **Reject**.
3. **Observer Pattern** — The notification step explicitly references the Observer design pattern used in code.
4. **All branches converge** to the final node.

---

## 4. Challan Issuance & Payment Workflow

This diagram describes the end-to-end flow of issuing a traffic challan and the citizen's payment process.

```plantuml
@startuml Challan_Workflow_Activity
title Challan Issuance & Payment Workflow

|#FFE4E1|Admin|
start
:Identify Traffic Violation;
:Enter Vehicle Registration Number;
:Select Violation Type from List;
:Set Fine Amount (₹);
:Click "Issue Challan";

|#F0F8FF|System|
:Validate Vehicle Registration Number;

if (Vehicle Found in DB?) then (Yes)
  :Look Up Owner Details;
  :Create Challan Record\n(Status = UNPAID);
  :Generate Unique Challan ID;
  :Send SMS & Email Notification\n(via Observer Pattern);
  
  |Citizen|
  :Receive Challan Notification;
  :Login to RTO Portal;
  :Navigate to "My Challans" Section;
  :View Pending/Unpaid Challans;
  :Select Challan & Click "Pay Now";
  :Enter Payment Details;
  
  |#F0F8FF|System|
  :Process Payment via PaymentGatewayAdapter;
  
  if (Transaction Successful?) then (Yes)
    :Update Challan Status to "PAID";
    :Create Transaction Record;
    :Generate Payment Receipt;
    :Clear Challan Liability from Profile;
    :Send Payment Confirmation Notification;
    
    |Citizen|
    :Receive Payment Confirmation;
    :View/Download Receipt;
  else (No)
    :Display Payment Failure Message;
    :Log Failed Transaction;
    
    |Citizen|
    :View Error Message;
    :Retry Payment or Contact Support;
  endif
else (No)
  :Display "Vehicle Not Found" Error;
  
  |#FFE4E1|Admin|
  :Verify Registration Number & Retry;
endif

stop

@enduml
```

### Key Improvements:
1. **Added vehicle validation** — The system first checks if the vehicle exists in the DB before issuing, matching the actual code's validation logic.
2. **Observer Pattern** — Notification step explicitly references the pattern.
3. **Transaction logging** — Added transaction record creation on success and failure logging.
4. **All branches converge** to the final node — no dangling paths.

---

## Summary of Corrections Made

| Diagram | Issue Fixed | UML Rule Enforced |
|---|---|---|
| **CBT Test** | Replaced `detach` with proper merge | Every path must reach a final node |
| **CBT Test** | Added `fork/join` for timer concurrency | Parallel activities require fork bars |
| **CBT Test** | Fixed loop from `repeat/while` to `while/endwhile` | Cleaner rendering of iteration |
| **Vehicle Reg** | Added form validation decision | Complete flow before payment |
| **Vehicle Reg** | Fixed retry/rejection paths to reach `stop` | No dangling branches |
| **License App** | Removed invalid `goto` syntax | PlantUML doesn't support `goto` |
| **License App** | Fixed **Yes/No label swap** on Documents check | Guard conditions must be logically correct |
| **License App** | Fixed re-submit path to end properly | All branches converge |
| **Challan** | Added vehicle existence validation | Matches actual code logic |
| **Challan** | Fixed retry path to reach `stop` | No dangling branches |
| **All 4** | Added `title` to each diagram | Better presentation |
| **All 4** | Used consistent swimlane coloring | Visual clarity |
