# RTO Office Simulation - UML Sequence Diagram

## Vehicle Registration Flow

This sequence diagram illustrates the process of registering a vehicle, featuring the **Facade**, **Adapter**, and **Builder** design patterns. It handles two flows:
1. **Admin Flow**: Direct registration.
2. **Citizen Flow**: Request submission for admin approval.

```plantuml
@startuml Vehicle_Registration_Sequence_Diagram

autonumber
skinparam Style strictuml
skinparam SequenceMessageAlignment center

actor "User" as User
participant "RegistrationUI" as UI
participant "RegistrationController" as Ctrl
participant "RTOSystemFacade" as Facade
participant "PaymentGatewayAdapter" as Adapter
participant "SimulatedPaymentAPI" as API
participant "VehicleService" as Service
participant "VehicleBuilder" as Builder
database "H2 Database" as DB

User -> UI : Enter Vehicle Details (Type, Model, Spec)
User -> UI : Enter Payment Details (Card#, CVV)
User -> UI : Click Submit

UI -> Ctrl : handleSubmit()
activate Ctrl

Ctrl -> Facade : processPayment(card#, cvv, amount)
activate Facade

Facade -> Adapter : processPayment(amount)
activate Adapter
Adapter -> API : initiateTransaction(amount)
Adapter -> API : verifyReceipt()
Adapter --> Facade : paymentSuccess
deactivate Adapter

alt Payment Successful
    Facade --> Ctrl : true
    
    alt User is ADMIN
        Ctrl -> Facade : registerVehicle(type, model, spec)
        Facade -> Service : registerVehicle(type, model, spec)
        activate Service
        
        Service -> Builder : new VehicleBuilder()
        Service -> Builder : setType(type).setModel(model)...
        Service -> Builder : build()
        activate Builder
        Builder --> Service : vehicle instance
        deactivate Builder
        
        Service -> DB : INSERT INTO vehicles (...)
        Service -> Service : calculateTax()
        Service --> Facade : registrationResult (with Reg#)
        deactivate Service
        
        Facade --> Ctrl : registrationResult
        Ctrl -> UI : showSuccessMsg("Vehicle Registered! Reg#: [Number]")
    else User is Citizen
        Ctrl -> Facade : submitVehicleRequest(type, model, spec)
        Facade -> DB : INSERT INTO vehicle_requests (PENDING)
        Facade --> Ctrl : success
        Ctrl -> UI : showSuccessMsg("Request Submitted! Awaiting Admin Approval")
    end
else Payment Failed
    Facade --> Ctrl : false
    Ctrl -> UI : showErrMsg("Payment Failed!")
end

deactivate Facade
deactivate Ctrl

@enduml
```

### Key Interactions:
1. **Facade Pattern**: The `RTOSystemFacade` simplifies the complex interaction between UI, Payment, and Vehicle services.
2. **Adapter Pattern**: The `PaymentGatewayAdapter` bridges the system logic with a simulated external Payment API.
3. **Builder Pattern**: The `VehicleBuilder` is used by the `VehicleService` to construct complex vehicle objects fluently.
4. **Conditional Logic**: The flow branches based on the User's Role (Admin vs Citizen), demonstrating role-based access control.

---

## 2. User Authentication (Login) Flow

This diagram shows the singleton-based session management and service-oriented authentication.

```plantuml
@startuml User_Login_Sequence_Diagram

autonumber
skinparam Style strictuml

actor "User" as User
participant "LoginUI" as UI
participant "LoginController" as Ctrl
participant "RTOSystemFacade" as Facade
participant "UserService" as Service
participant "SessionManager" as Session
database "H2 Database" as DB

User -> UI : Enter Username & Password
User -> UI : Click Login

UI -> Ctrl : handleLogin()
activate Ctrl

Ctrl -> Facade : login(username, password)
activate Facade

Facade -> Service : authenticate(username, password)
activate Service
Service -> DB : SELECT * FROM users WHERE ...
Service --> Facade : user object / null
deactivate Service

alt Authentication Success
    Facade -> Session : getInstance()
    Facade -> Session : setCurrentUser(user)
    Facade --> Ctrl : true
    Ctrl -> UI : navigateToDashboard()
else Authentication Failed
    Facade --> Ctrl : false
    Ctrl -> UI : showErrMsg("Invalid Credentials")
end

deactivate Facade
deactivate Ctrl

@enduml
```

---

## 3. License Application Flow

Demonstrates the use of the **Observer Pattern** for notifications.

```plantuml
@startuml License_Application_Sequence_Diagram

autonumber
skinparam Style strictuml

actor "Citizen" as Citizen
participant "LicenseUI" as UI
participant "LicenseController" as Ctrl
participant "RTOSystemFacade" as Facade
participant "LicenseService" as Service
participant "NotificationSubject" as Observer
participant "EmailNotifier" as Email
database "H2 Database" as DB

Citizen -> UI : Click "Apply for License"
UI -> Ctrl : handleApplyLicense()
activate Ctrl

Ctrl -> Facade : applyForLicense(userId, type)
activate Facade

Facade -> Service : applyForLicense(userId, type)
activate Service

Service -> DB : INSERT INTO licenses (...)
Service -> Observer : attach(new EmailNotifier(email))
Service -> Observer : notifyObservers("Application Submitted")
activate Observer
Observer -> Email : update(msg)
Email -> Email : sendEmail()
Observer --> Service : done
deactivate Observer

Service --> Facade : true
deactivate Service

Facade --> Ctrl : true
Ctrl -> UI : showSuccessMsg("Applied Successfully!")

deactivate Facade
deactivate Ctrl

@enduml
```

---

## 4. Reviewing Pending Applications (Admin) Flow

```plantuml
@startuml Review_Applications_Sequence_Diagram

autonumber
skinparam Style strictuml

actor "Admin" as Admin
participant "DashboardUI" as UI
participant "DashboardController" as Ctrl
participant "RTOSystemFacade" as Facade
participant "VehicleService" as VService
participant "LicenseService" as LService
database "H2 Database" as DB

Admin -> UI : Open "Review Applications" Tab
UI -> Ctrl : loadPendingApplications()
activate Ctrl

Ctrl -> Facade : getPendingVehicleRequests()
Facade -> DB : SELECT * FROM vehicle_requests WHERE ...
Facade --> Ctrl : list of requests

Ctrl -> UI : Display Requests in Table

Admin -> UI : Select Request & Click "Approve"
UI -> Ctrl : handleApproveRequest()

Ctrl -> Facade : approveVehicleRequest(requestId)
activate Facade
Facade -> VService : approveRequest(id)
activate VService
VService -> VService : registerVehicle(details)
VService -> DB : UPDATE vehicle_requests SET ...
VService -> DB : INSERT INTO vehicles (...)
VService --> Facade : success
deactivate VService
Facade --> Ctrl : success
deactivate Facade

Ctrl -> UI : refreshTable()
Ctrl -> UI : showNotification("Request Approved")

deactivate Ctrl

@enduml
```

