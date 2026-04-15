# RTO Office Simulation - UML Component Diagram

This diagram shows the high-level software components and their physical/logical organization following the MVC and Service-Layer architecture.

```plantuml
@startuml RTO_Component_Diagram

package "User Interface (View)" {
  [FXML Views] as Views
}

package "Presentation Logic (Controller)" {
  [JavaFX Controllers] as Controllers
}

package "Business Logic (Service)" {
  [RTOSystemFacade] as Facade
  [Core Services] as Services
  [Design Patterns] as Patterns
}

package "Data Access (Persistence)" {
  [DatabaseService] as DBService
}

database "H2 Database" {
  [rto_db.mv.db] as DBFile
}

package "External Systems" {
  [Third-Party Payment API] as PaymentAPI
}

Views ..> Controllers : interacts
Controllers ..> Facade : uses
Facade ..> Services : delegates
Services ..> Patterns : implements
Services ..> DBService : queries
DBService ..> DBFile : persists
Services ..> PaymentAPI : adapts (via Adapter Pattern)

@enduml
```

---

# RTO Office Simulation - UML Deployment Diagram

This diagram illustrates the physical deployment structure of the application.

```plantuml
@startuml RTO_Deployment_Diagram

node "User's Workstation" as Workstation {
  node "Java Runtime Environment (JRE 17+)" {
    artifact "RTOSystem.jar" <<executable>>
  }
  
  node "Local Storage" {
    file "rto_db.mv.db" <<H2 Database File>>
    file "logs/" <<Log Directory>>
    file "documents/" <<Uploaded Docs>>
  }
}

node "External Payment Gateway" as PaymentNode <<Server>> {
  [Payment Processing Service]
}

Workstation -- PaymentNode : HTTPS (via Adapter)

@enduml
```
