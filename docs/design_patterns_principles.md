# design_patterns_principles.md
# RTO Management System - Design Patterns and Principles

This document elaborates on the Architectural Patterns, Design Patterns, and Design Principles used in the RTO Management System, as per the UE23CS352B Mini-Project Guidelines.

---

## 1. Architectural Pattern: MVC (Model-View-Controller)

The system follows the **MVC Architecture** to ensure separation of concerns, making the codebase maintainable and scalable.

- **Model**: Located in `com.rto.model`. Classes like `User`, `Vehicle`, `License`, and `Challan` represent the data and business logic. They are independent of the UI.
- **View**: Located in `src/main/resources/com/rto/view`. FXML files define the layout and look of the application using JavaFX components.
- **Controller**: Located in `com.rto.controller`. Controllers like `LoginController` and `RegistrationController` handle user input, update the Model, and refresh the View.

---

## 2. Design Patterns (Categorized)

As required, the project implements **Creational, Structural, and Behavioral patterns**.

### A. Creational Patterns
#### 1. Singleton Pattern
- **Implementation**: `DatabaseService`, `SessionManager`.
- **Application**: Ensures only one connection pool exists for the H2 database and one global session persists for the logged-in user, preventing resource leaks.

#### 2. Builder Pattern
- **Implementation**: `VehicleBuilder`.
- **Application**: Simplifies the creation of complex `Vehicle` objects with many optional attributes (engine number, chassis number, color, features) using a fluent interface.

### B. Structural Patterns
#### 3. Adapter Pattern
- **Implementation**: `PaymentGatewayAdapter`.
- **Application**: Acts as a bridge between our system's `IPaymentProcessor` interface and a simulated third-party Payment API, allowing us to swap payment providers without changing core logic.

#### 4. Facade Pattern
- **Implementation**: `RTOSystemFacade`.
- **Application**: Provides a unified, simplified interface to the various complex services (`UserService`, `VehicleService`, `LicenseService`), reducing the coupling between the UI Controllers and the Service layer.

### C. Behavioral Patterns
#### 5. Strategy Pattern
- **Implementation**: `TaxCalculationStrategy` interface.
- **Application**: Defines a family of tax calculation algorithms (Standard, Premium, Commercial) and makes them interchangeable at runtime based on the vehicle type.

#### 6. Observer Pattern
- **Implementation**: `NotificationSubject` and `NotificationObserver`.
- **Application**: Implementing an event-driven system where `LicenseService` notifies multiple observers (Email, SMS, System Notification) whenever an application status changes.

---

## 3. Design Principles (SOLID)

The system is developed adhering to the following **Design Principles**:

1. **Single Responsibility Principle (SRP)**:
   - Each service (e.g., `UserService`) is responsible for only one domain of the RTO system, and each Controller handles exactly one FXML view.
2. **Open-Closed Principle (OCP)**:
   - The `TaxCalculationStrategy` allows new tax rules to be added by adding new strategy classes without modifying the existing `Vehicle` or `Service` code.
3. **Liskov Substitution Principle (LSP)**:
   - Subclasses like `Car`, `Bike`, and `Truck` can be used wherever the base `Vehicle` class is expected, ensuring polymorphically correct calculations.
4. **Dependency Inversion Principle (DIP)**:
   - High-level modules (Controllers) depend on Abstractions (Interfaces like `IService`, `IPaymentProcessor`) rather than low-level concrete implementations, facilitated by the `ServiceFactory` and Facade.

---

## 4. Design Pattern Justification for 4-Member Team

| Member | Pattern Owned | Domain Application |
|---|---|---|
| **Member 1** | **Singleton** | Secure Session & DB management |
| **Member 2** | **Factory/Builder** | Flexible Object creation (Vehicles/Services) |
| **Member 3** | **Adapter/Facade** | Third-party integrations & System simplification |
| **Member 4** | **Strategy/Observer** | Dynamic logic branching & Real-time notifications |

