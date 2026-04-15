# RTO Office Simulation - UML State Machine Diagrams

## UML State Diagram Compliance

Each diagram follows standard UML State Machine notation:

| Component | Symbol | Present in All 4? |
|---|---|:---:|
| **Initial Pseudo-State** | Filled black circle `[*] -->` | ✅ |
| **Final State** | Bullseye circle `--> [*]` | ✅ |
| **States** | Rounded rectangles | ✅ |
| **Transitions** | Arrows with `event [guard] / action` | ✅ |
| **Guard Conditions** | In square brackets `[condition]` | ✅ |
| **Composite States** | Nested state regions | ✅ (D1, D2) |
| **Entry/Do Activities** | Inside state body | ✅ |
| **Self-Transitions** | Arrow from/to same state | ✅ (D3) |
| **All states reachable** | From initial pseudo-state | ✅ |
| **No orphan states** | Every state has ≥1 incoming transition | ✅ |

---

## 1. License Lifecycle

This state diagram models the complete lifecycle of a license — from application to expiry/revocation. It uses **composite states** to show that both LL and DL have internal validity tracking.

**Maps to:** `License.java` (status field: PENDING, ACTIVE, REJECTED, EXPIRED), `LicenseService.java` (applyForLicense, approveLicense, rejectLicense, renewLicense)

```plantuml
@startuml License_State_Diagram
title License Lifecycle State Diagram

skinparam state {
    BackgroundColor #FEFEFE
    BorderColor #333333
    FontSize 12
}

[*] --> NotApplied

state NotApplied : Citizen has not applied\nfor any license yet.

NotApplied --> Pending : submitApplication() /\nGenerate License ID,\nSet status = "PENDING"
note on link
  Citizen fills form with
  personal details & uploads
  identity documents
end note

state Pending : Application awaiting review\nby RTO Officer.
Pending : entry / notifyOfficer()

Pending --> Rejected : rejectLicense() /\nSet status = "REJECTED"
note on link
  Officer finds document
  discrepancies or
  ineligibility
end note

state Rejected : Application denied.\nCitizen can correct & resubmit.
Rejected : entry / notifyCitizen(reason)

Rejected --> Pending : reSubmit() /\nUpdate documents,\nReset status = "PENDING"

Pending --> LL_Active : approveLearnerLicense() /\nSet status = "ACTIVE",\nSet issueDate = today,\nSet expiryDate = today + 6 months

state LL_Active : Learner's License Issued.
note right of LL_Active
  Composite State:
  LL has its own
  validity tracking.
end note

state LL_Active {
    [*] --> LL_Valid
    state LL_Valid : License is valid.\nCitizen can practice driving.
    LL_Valid --> LL_Expired : [currentDate > expiryDate]
    state LL_Expired : LL has expired.\nMust renew to continue.
    LL_Expired : entry / notifyCitizen("LL expired")
    LL_Expired --> LL_Valid : renewLL() /\nExtend expiryDate by 6 months
}

LL_Active --> DL_Pending : applyForDL()\n[daysSinceLL >= 30] /\nSubmit DL application
note on link
  Citizen applies for
  permanent DL after
  30 days of LL issuance
end note

state DL_Pending : Awaiting driving test\nresult and officer approval.

DL_Pending --> Rejected : rejectDL() /\nSet status = "REJECTED"

DL_Pending --> DL_Active : approveDrivingLicense() /\nSet status = "ACTIVE",\nSet expiryDate = today + 10 years

state DL_Active : Full Driving License Issued.
note right of DL_Active
  Composite State:
  DL validity tracked
  independently.
end note

state DL_Active {
    [*] --> DL_Valid
    state DL_Valid : License is valid.\nCitizen can drive legally.
    DL_Valid --> DL_NearExpiry : [expiryDate - currentDate <= 90 days]
    state DL_NearExpiry : Approaching expiry.\nRenewal reminder sent.
    DL_NearExpiry : entry / sendRenewalReminder()
    DL_NearExpiry --> DL_Expired : [currentDate > expiryDate]
    DL_Valid --> DL_Expired : [currentDate > expiryDate]
    state DL_Expired : DL has expired.\nCannot drive legally.
    DL_Expired : entry / notifyCitizen("DL expired")
    DL_Expired --> DL_Valid : renewDL() /\nExtend expiryDate by 10 years
}

DL_Active --> Suspended : suspendLicense() /\nSet status = "SUSPENDED"
state Suspended : License temporarily suspended\ndue to violations.
Suspended --> DL_Active : reinstateLicense() /\nSet status = "ACTIVE"
Suspended --> Revoked : revokeLicense() /\nSet status = "REVOKED"

state Revoked : License permanently revoked.
Revoked --> [*]

DL_Active --> [*] : terminateLicense()\n(Citizen deceased / voluntary surrender)

@enduml
```

### State Descriptions:
| State | Description | Code Mapping |
|---|---|---|
| **NotApplied** | Citizen hasn't applied yet | No License record exists |
| **Pending** | Application submitted, awaiting officer review | `status = "PENDING"` |
| **Rejected** | Application denied; citizen may resubmit | `status = "REJECTED"` via `reject()` |
| **LL_Active** | Learner's License issued (composite: Valid/Expired) | `status = "ACTIVE"`, `approve()` |
| **DL_Pending** | Applied for permanent DL after CBT pass | `status = "PENDING"` (DL application) |
| **DL_Active** | Full Driving License issued (composite: Valid/NearExpiry/Expired) | `status = "ACTIVE"` |
| **Suspended** | Temporarily suspended due to violations | Extended state |
| **Revoked** | Permanently revoked | Terminal state |

---

## 2. Vehicle Registration Request Lifecycle

This diagram tracks the states of a `VehicleRequest` object from citizen submission through admin review to final registration.

**Maps to:** `VehicleRequest.java` (status: PENDING, APPROVED, REJECTED), `VehicleService.java`, `RTOSystemFacade.java`

```plantuml
@startuml Vehicle_Request_State_Diagram
title Vehicle Registration Request Lifecycle

skinparam state {
    BackgroundColor #FEFEFE
    BorderColor #333333
    FontSize 12
}

[*] --> Draft

state Draft : Citizen is filling\nthe registration form.
Draft : do / enterVehicleDetails()

Draft --> PaymentProcessing : submitForm() /\nValidate inputs

state PaymentProcessing : Processing registration\nfee via PaymentGatewayAdapter.
PaymentProcessing : entry / processPayment()

PaymentProcessing --> Draft : paymentFailed /\nShow error message

PaymentProcessing --> PendingApproval : paymentSuccess /\nStore request in DB,\nSet status = "PENDING"

state PendingApproval : Request stored in DB.\nAwaiting admin review.
PendingApproval : entry / notifyAdmin()

note right of PendingApproval
  Composite State:
  Admin reviews and may
  request clarification.
end note

state PendingApproval {
    [*] --> UnderReview
    state UnderReview : Admin is verifying\ndocuments and specs.
    UnderReview --> NeedsClarification : [documents incomplete\nor specs unclear]
    state NeedsClarification : Admin requested\nadditional info from citizen.
    NeedsClarification : entry / notifyCitizen("Provide more info")
    NeedsClarification --> UnderReview : citizenUpdatesDetails() /\nResubmit documents
}

PendingApproval --> Approved : approveRequest() /\nSet status = "APPROVED",\nSet approvedBy = adminId,\nSet approvalDate = now

state Approved : Admin approved.\nSystem auto-registers.

PendingApproval --> Rejected : rejectRequest() /\nSet status = "REJECTED"

state Rejected : Request denied by admin.
Rejected : entry / notifyCitizen(reason)

Rejected --> PendingApproval : resubmit() /\nCorrect details,\nReset status = "PENDING"
Rejected --> [*] : abandonRequest()

Approved --> Registered : autoMigrateToVehicle() /\nGenerate Registration Number,\nCalculate Tax (Strategy Pattern),\nCreate Vehicle record

state Registered : Vehicle officially\nregistered in system.
Registered : entry / issueRC()
Registered : entry / notifyCitizen("RC issued")

Registered --> [*]

@enduml
```

### State Descriptions:
| State | Description | Code Mapping |
|---|---|---|
| **Draft** | Citizen filling the form, not yet submitted | UI state in `RegistrationController` |
| **PaymentProcessing** | Fee being processed via Adapter Pattern | `rtoFacade.processPayment()` |
| **PendingApproval** | Stored in DB, awaiting admin (composite: UnderReview / NeedsClarification) | `status = "PENDING"` |
| **Approved** | Admin verified and approved | `status = "APPROVED"` |
| **Rejected** | Admin denied; citizen can resubmit or abandon | `status = "REJECTED"` |
| **Registered** | Vehicle record created with registration number | Vehicle record in DB |

---

## 3. Challan (Traffic Violation) Lifecycle

This diagram models the complete lifecycle of a traffic challan from issuance to payment resolution.

**Maps to:** `Challan.java` (isPaid, isOverdue(), calculatePenalty(), getTotalDue()), `ChallanService.java` (issueChallan, payChallan)

```plantuml
@startuml Challan_State_Diagram
title Challan (Traffic Violation) Lifecycle

skinparam state {
    BackgroundColor #FEFEFE
    BorderColor #333333
    FontSize 12
}

[*] --> Issued

state Issued : Admin issues challan.\nStored in DB with isPaid = false.
Issued : entry / generateChallanId()
Issued : entry / setIssueDate(today)
Issued : entry / notifyOwner()

Issued --> Unpaid : [challan created successfully]

state Unpaid : Fine is pending payment.\nWithin 30-day grace period.
Unpaid : do / displayInCitizenDashboard()

Unpaid --> Paid : processPayment(amount) /\nmarkAsPaid(transactionId)
note on link
  Citizen pays the exact
  fine amount within
  30-day window
end note

Unpaid --> Overdue : [currentDate > issueDate + 30 days]\n[isPaid == false]
note on link
  Automatic transition
  triggered by time.
  isOverdue() returns true.
end note

state Overdue : Fine overdue. 10% penalty applied.\ngetTotalDue() = amount + calculatePenalty()
Overdue : entry / addPenalty(10%)
Overdue : entry / sendOverdueNotification()

Overdue --> Paid : processPayment(totalDue) /\nmarkAsPaid(transactionId)
note on link
  Citizen pays fine +
  10% late penalty.
  getTotalDue() used.
end note

Overdue --> Escalated : [currentDate > issueDate + 90 days]\n[isPaid == false]

state Escalated : Escalated to higher\nauthority / court.
Escalated : entry / notifyAuthorities()

Escalated --> Paid : resolveAndPay() /\nmarkAsPaid(transactionId)

state Paid : Fine fully settled.\nisPaid = true.
Paid : entry / generateReceipt()
Paid : entry / clearFromProfile()

Paid --> Closed : [after record retention period]

state Closed : Archived in system records.

Closed --> [*]
Paid --> [*]

@enduml
```

### State Descriptions:
| State | Description | Code Mapping |
|---|---|---|
| **Issued** | Challan created by admin | `new Challan()`, `issueChallan()` |
| **Unpaid** | Active challan within 30-day window | `isPaid = false`, `!isOverdue()` |
| **Overdue** | Past 30 days unpaid, penalty applied | `isOverdue() == true`, `calculatePenalty()` |
| **Escalated** | Past 90 days, referred to authority | Extended state |
| **Paid** | Fine settled | `markAsPaid(txnId)`, `isPaid = true` |
| **Closed** | Archived after retention | Terminal state |

---

## 4. Payment Transaction Lifecycle

This diagram models the internal states of a payment transaction processed through the `PaymentGatewayAdapter` and `SimulatedThirdPartyPaymentAPI`.

**Maps to:** `Transaction.java` (status: PENDING, SUCCESS, FAILED), `PaymentGatewayAdapter.java`, `SimulatedThirdPartyPaymentAPI.java` (makeTransaction, validateCard, luhnCheck)

```plantuml
@startuml Payment_State_Diagram
title Payment Transaction Lifecycle

skinparam state {
    BackgroundColor #FEFEFE
    BorderColor #333333
    FontSize 12
}

[*] --> Created

state Created : Transaction object created.\nstatus = "PENDING"
Created : entry / generateTransactionId()
Created : entry / setTimestamp(now)

Created --> Validating : initiatePayment() /\nSend card details to\nPaymentGatewayAdapter

state Validating : Adapter validates card\nvia SimulatedThirdPartyPaymentAPI.
Validating : do / validateCard(cardNumber, cvv)
Validating : do / luhnCheck(cardNumber)

Validating --> CardInvalid : [card validation fails] /\nInvalid format, length,\nor Luhn check failure

state CardInvalid : Card details rejected.\nTransaction cannot proceed.
CardInvalid : entry / log("Invalid card details")

CardInvalid --> Failed : markFailed()

Validating --> Processing : [card valid] /\nCard format and\nLuhn check passed

state Processing : API is processing the\nfunds transfer.
Processing : do / simulateDelay()
Processing : do / contactBankServer()

Processing --> Success : [bankApproves == true]\n(95% probability) /\nFunds transferred

state Success : Payment confirmed by\nexternal gateway.
Success : entry / generateReference()
Success : entry / log("Transaction SUCCESSFUL")

Processing --> Declined : [bankApproves == false]\n(5% probability) /\nBank declined transaction

state Declined : Bank declined the\ntransaction.
Declined : entry / log("Bank declined")

Declined --> Failed : markFailed()

Processing --> Timeout : [no response within\ntimeout period]

state Timeout : Gateway did not respond\nin time.

Timeout --> Processing : retry() /\nRe-initiate API call
Timeout --> Failed : cancel() /\nUser cancels retry

state Failed : Transaction failed.\nstatus = "FAILED"
Failed : entry / markFailed()
Failed : entry / notifyUser("Payment failed")

state Completed : Transaction completed.\nSystem records updated.\nstatus = "SUCCESS"
Completed : entry / markSuccess()
Completed : entry / updateSystemRecords()

Success --> Completed : updateRecords() /\nLink to challan/vehicle/license,\nSet referenceId

Failed --> [*]
Completed --> [*]

@enduml
```

### State Descriptions:
| State | Description | Code Mapping |
|---|---|---|
| **Created** | Transaction object instantiated | `new Transaction()`, `status = "PENDING"` |
| **Validating** | Card being validated (format, Luhn) | `validateCard()`, `luhnCheck()` in `SimulatedThirdPartyPaymentAPI` |
| **CardInvalid** | Card validation failed | `validateCard()` returns false |
| **Processing** | Funds transfer in progress | `makeTransaction()` in `SimulatedThirdPartyPaymentAPI` |
| **Success** | Bank approved the payment | `makeTransaction()` returns true |
| **Declined** | Bank declined (5% chance in simulation) | `makeTransaction()` returns false |
| **Timeout** | Gateway didn't respond | Extended state for robustness |
| **Failed** | Terminal failure state | `markFailed()`, `status = "FAILED"` |
| **Completed** | All records updated, transaction done | `markSuccess()`, `status = "SUCCESS"` |

---

## Summary

| # | Diagram | Object Modeled | States | Composite? | Code Files |
|---|---|---|:---:|:---:|---|
| 1 | License Lifecycle | `License` | 9 | Yes (LL, DL) | `License.java`, `LicenseService.java` |
| 2 | Vehicle Request | `VehicleRequest` | 7 | Yes (PendingApproval) | `VehicleRequest.java`, `VehicleService.java` |
| 3 | Challan Lifecycle | `Challan` | 6 | No | `Challan.java`, `ChallanService.java` |
| 4 | Payment Transaction | `Transaction` | 9 | No | `Transaction.java`, `PaymentGatewayAdapter.java` |
