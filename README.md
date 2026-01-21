# RTO Management System

A comprehensive Desktop Application built with JavaFX demonstrating OOAD principles and Design Patterns.

## Quick Start

### Prerequisites
- Java JDK 17 or higher
- Maven (or use your IDE's built-in Maven)

### Running the Application

**Option 1: Using IntelliJ IDEA or Eclipse**
1. Open the project folder in your IDE
2. Wait for Maven to download dependencies
3. Run the `com.rto.Main` class

**Option 2: Using Maven Command Line**
```bash
mvn clean javafx:run
```

### Login Credentials
- **Admin**: Username: `admin`, Password: `admin`
- **Citizen**: Any other username/password combination

## Design Patterns Implemented

| Pattern | Implementation | File |
|---------|---------------|------|
| **Singleton** | Session Manager | `SessionManager.java` |
| **Factory** | Service Factory | `ServiceFactory.java` |
| **Builder** | Vehicle Builder | `VehicleBuilder.java` |
| **Adapter** | Payment Gateway | `PaymentGatewayAdapter.java` |
| **Facade** | RTO System Facade | `RTOSystemFacade.java` |

## Features
- ✅ Role-based Login (Admin/Citizen)
- ✅ Vehicle Registration with Builder Pattern
- ✅ Simulated Payment Processing with Adapter Pattern
- ✅ Tax Calculation using Polymorphism
- ✅ Session Management with Singleton Pattern

## Project Structure
```
src/main/java/com/rto/
├── Main.java                 # Application Entry Point
├── model/                    # Domain Models
│   ├── User.java
│   ├── Admin.java
│   ├── RTOOfficer.java
│   ├── Citizen.java
│   ├── Vehicle.java
│   ├── Car.java
│   └── Bike.java
├── service/                  # Business Logic
│   ├── SessionManager.java
│   ├── RTOSystemFacade.java
│   ├── ServiceFactory.java
│   ├── IService.java
│   ├── VehicleService.java
│   └── LicenseService.java
├── patterns/                 # Design Patterns
│   ├── VehicleBuilder.java
│   ├── PaymentGatewayAdapter.java
│   ├── IPaymentProcessor.java
│   └── SimulatedThirdPartyPaymentAPI.java
├── controller/               # JavaFX Controllers
│   ├── LoginController.java
│   ├── DashboardController.java
│   └── RegistrationController.java
└── view/                     # FXML Views (in resources)
```
