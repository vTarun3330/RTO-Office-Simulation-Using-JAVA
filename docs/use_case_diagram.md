# RTO Office Simulation - Use Case Diagram

## Comprehensive System Use Case Diagram

This is the **complete use case diagram** for the RTO Management System, showing all actors, use cases, and their relationships (`<<include>>` and `<<extend>>`).

### UML Use Case Diagram Compliance

| Component | Rule | Status |
|---|---|:---:|
| **Actors** | Stick figures placed OUTSIDE system boundary | ✅ |
| **Use Cases** | Ovals/ellipses INSIDE system boundary | ✅ |
| **System Boundary** | Single rectangle enclosing all use cases | ✅ |
| **Associations** | Solid lines connecting actors to use cases | ✅ |
| **`<<include>>`** | Dashed arrow FROM base → TO included (mandatory sub-behavior) | ✅ |
| **`<<extend>>`** | Dashed arrow FROM extending → TO base (optional/conditional) | ✅ |
| **Generalization** | Solid arrow with hollow triangle for actor/use case inheritance | ✅ |
| **No implementation detail** | Use cases describe WHAT, not HOW | ✅ |
| **Each use case = meaningful goal** | Complete goal for at least one actor | ✅ |

### Actors

| Actor | Type | Description |
|---|---|---|
| **Citizen** | Primary | End-user who registers, applies, pays, and takes tests |
| **Admin / RTO Officer** | Primary | Administrator who reviews, approves, issues challans |
| **Payment Gateway** | External System | Third-party payment processor (SimulatedThirdPartyPaymentAPI) |

---

```plantuml
@startuml RTO_System_Use_Case_Diagram
title RTO Management System - Use Case Diagram

left to right direction

skinparam actorStyle awesome
skinparam packageStyle rectangle
skinparam usecase {
    BackgroundColor #FEFEFE
    BorderColor #333333
    ArrowColor #555555
    FontSize 11
}

' ===== ACTORS =====
actor "Citizen" as Citizen
actor "Admin /\nRTO Officer" as Admin
actor "Payment\nGateway" as PG <<External>>

' ===== SYSTEM BOUNDARY =====
rectangle "RTO Management System" {

    ' --- Authentication ---
    usecase "Login" as UC_Login
    usecase "Register Account" as UC_Register
    usecase "Validate Credentials" as UC_ValidateCred

    ' --- Vehicle Management ---
    usecase "Register Vehicle" as UC_RegVehicle
    usecase "Calculate Road Tax" as UC_CalcTax
    usecase "Generate Registration\nNumber" as UC_GenReg

    ' --- License Management ---
    usecase "Apply for License\n(LL / DL)" as UC_ApplyLicense
    usecase "Take LL Test (CBT)" as UC_TakeCBT
    usecase "Start Timed Quiz\n(10 min, 10 Qs)" as UC_StartQuiz
    usecase "Evaluate Answers\n& Calculate Score" as UC_EvalAnswers
    usecase "Issue Learner\nLicense" as UC_IssueLL
    usecase "View Test History" as UC_TestHistory
    usecase "Retake Test" as UC_Retake

    ' --- Ownership Transfer ---
    usecase "Transfer Vehicle\nOwnership" as UC_Transfer
    usecase "Verify Owner\n& Buyer" as UC_VerifyParties

    ' --- Challan Management ---
    usecase "Issue Challan" as UC_IssueChallan
    usecase "Search Vehicle\nby Reg. Number" as UC_SearchVehicle
    usecase "Send Notification\nto Owner" as UC_Notify

    ' --- Payment ---
    usecase "Pay Fees / Challan" as UC_Pay
    usecase "Process Payment\nvia Adapter" as UC_ProcessPay
    usecase "Generate Payment\nReceipt" as UC_Receipt

    ' --- Admin Functions ---
    usecase "Manage Users" as UC_ManageUsers
    usecase "View / Search\nUser List" as UC_ViewUsers
    usecase "Edit User\nInformation" as UC_EditUser
    usecase "Review Pending\nApplications" as UC_ReviewApps
    usecase "Approve / Reject\nApplication" as UC_Decision

    ' --- Citizen Dashboard ---
    usecase "View My Requests\n& Status" as UC_MyRequests

    ' --- Logout ---
    usecase "Logout" as UC_Logout
}

' ===== ACTOR ASSOCIATIONS =====

' -- Citizen Actions --
Citizen --> UC_Login
Citizen --> UC_Register
Citizen --> UC_RegVehicle
Citizen --> UC_ApplyLicense
Citizen --> UC_TakeCBT
Citizen --> UC_Transfer
Citizen --> UC_Pay
Citizen --> UC_MyRequests
Citizen --> UC_Logout

' -- Admin Actions --
Admin --> UC_Login
Admin --> UC_ManageUsers
Admin --> UC_IssueChallan
Admin --> UC_ReviewApps
Admin --> UC_RegVehicle : (direct\nregistration)
Admin --> UC_Logout

' -- External System --
PG --> UC_ProcessPay

' ===== <<include>> RELATIONSHIPS =====
' (Base use case ----> Included use case)
' Meaning: the base ALWAYS invokes the included

UC_Login ..> UC_ValidateCred : <<include>>

UC_RegVehicle ..> UC_CalcTax : <<include>>
UC_RegVehicle ..> UC_GenReg : <<include>>
UC_RegVehicle ..> UC_Pay : <<include>>

UC_TakeCBT ..> UC_TestHistory : <<include>>
UC_TakeCBT ..> UC_StartQuiz : <<include>>
UC_TakeCBT ..> UC_EvalAnswers : <<include>>

UC_Transfer ..> UC_VerifyParties : <<include>>
UC_Transfer ..> UC_Pay : <<include>>

UC_IssueChallan ..> UC_SearchVehicle : <<include>>
UC_IssueChallan ..> UC_Notify : <<include>>

UC_Pay ..> UC_ProcessPay : <<include>>
UC_Pay ..> UC_Receipt : <<include>>

UC_ManageUsers ..> UC_ViewUsers : <<include>>

UC_ReviewApps ..> UC_Decision : <<include>>

' ===== <<extend>> RELATIONSHIPS =====
' (Extending use case ----> Base use case)
' Meaning: extending OPTIONALLY occurs under a condition

UC_IssueLL ..> UC_TakeCBT : <<extend>>\n[score >= 60%]
UC_Retake ..> UC_TakeCBT : <<extend>>\n[score < 60%]
UC_EditUser ..> UC_ManageUsers : <<extend>>\n[admin modifies data]

@enduml
```

---

## Use Case Summary Table

| # | Use Case | Primary Actor | `<<include>>` | `<<extend>>` |
|---|---|---|---|---|
| 1 | Login | Citizen, Admin | Validate Credentials | — |
| 2 | Register Account | Citizen | — | — |
| 3 | Register Vehicle | Citizen, Admin | Calculate Tax, Generate Reg#, Pay Fees | — |
| 4 | Apply for License (LL/DL) | Citizen | — | — |
| 5 | Take LL Test (CBT) | Citizen | View History, Start Quiz, Evaluate Answers | Issue LL [pass], Retake [fail] |
| 6 | Transfer Vehicle Ownership | Citizen | Verify Parties, Pay Fees | — |
| 7 | Issue Challan | Admin | Search Vehicle, Send Notification | — |
| 8 | Pay Fees / Challan | Citizen | Process Payment, Generate Receipt | — |
| 9 | Manage Users | Admin | View/Search User List | Edit User [admin action] |
| 10 | Review Pending Applications | Admin | Approve/Reject | — |
| 11 | View My Requests & Status | Citizen | — | — |
| 12 | Logout | Citizen, Admin | — | — |

---

## Actor–Use Case Mapping

### Citizen Can:
1. **Login** — Authenticate with username and password
2. **Register Account** — Create a new citizen account
3. **Register Vehicle** — Submit vehicle registration (goes to admin approval queue)
4. **Apply for License** — Apply for Learner's or Driving License
5. **Take LL Test (CBT)** — Attempt the 10-question timed computer-based test
6. **Transfer Vehicle Ownership** — Initiate transfer to another registered user
7. **Pay Fees / Challan** — Pay registration fees, license fees, or traffic fines
8. **View My Requests** — Track status of submitted vehicle registration requests
9. **Logout** — End session

### Admin / RTO Officer Can:
1. **Login** — Authenticate with admin credentials
2. **Register Vehicle** — Directly register a vehicle (bypasses approval)
3. **Issue Challan** — Issue traffic violation challans against vehicles
4. **Review Applications** — Review, approve or reject pending citizen requests
5. **Manage Users** — View, search, and edit citizen accounts
6. **Logout** — End session

### Payment Gateway (External):
1. **Process Payment** — Handles actual fund transfer via `PaymentGatewayAdapter` (Adapter Pattern) using `SimulatedThirdPartyPaymentAPI`

---

## Code-to-Use Case Traceability

| Use Case | Controller(s) | Service(s) | Model(s) |
|---|---|---|---|
| Login | `LoginController` | `RTOSystemFacade.login()` | `User` |
| Register Account | `LoginController` | `RTOSystemFacade.registerUser()` | `User` |
| Register Vehicle | `RegistrationController` | `VehicleService`, `RTOSystemFacade` | `Vehicle`, `VehicleRequest` |
| Apply for License | `LicenseController` | `LicenseService`, `RTOSystemFacade` | `License` |
| Take LL Test | `CBTController` | `CBTService` | `CBTQuestion`, `CBTResult` |
| Transfer Ownership | `TransferController` | `TransferService` | `Vehicle` |
| Issue Challan | `DashboardController` | `ChallanService` | `Challan` |
| Pay Fees/Challan | `RegistrationController`, `DashboardController` | `RTOSystemFacade.processPayment()` | `Transaction` |
| Manage Users | `DashboardController` | `DatabaseService` | `User` |
| Review Applications | `DashboardController` | `VehicleService` | `VehicleRequest` |
| View My Requests | `DashboardController` | `VehicleService` | `VehicleRequest` |
| Logout | `DashboardController` | `SessionManager.logout()` | — |
