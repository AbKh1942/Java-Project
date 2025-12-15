# Real-Time Traffic Simulation with Java

## Author
John Grosch  
Abdulrehman Saqib Khan  
Oleksandr Bahno

---

## Project Overview
The **goal** of this project is to develop a Java-based real-time traffic simulation.

**Core functionalities** of the application include connecting to a running SUMO simulation, enabling real-time interaction between the IDE and SUMO.

The system visualizes the road network, vehicles, and traffic lights through a graphical user interface. It provides a map overview of the simulation. The platform allows control of features such as vehicle injection on specific edges, adjustment of vehicle parameters, and switching of traffic light phases. Additionally, the system collects and displays essential traffic stats like average speed and vehicle density, ensuring users can monitor and analyze traffic flow.

### Motivation for the Project
The **motivation** behind this project is to apply object-oriented programming and software engineering principles in a practical, real-life scenario.

### Milestone One
**Milestone 1** lays the foundation of the project by focusing on system design and initial prototyping.  
The team defines the overall architecture, prepares class designs for the TraaS wrapper (e.g., Vehicle, TrafficLight), and creates GUI mockups to illustrate the planned user interface.  
A first connection to SUMO is demonstrated, showing basic functionality such as listing traffic lights and stepping through the simulation.  
Additionally, the technology stack is summarized, a Git repository is set up, and team roles are distributed to ensure clear responsibilities and efficient collaboration.

---

## Technology Stack Summary
**The technologies used in this project are:**

- **Programming language**:  
  *Java*

- **Framework/Libraries**:
    * SUMO (traffic simulation)
    * TraaS API (interface between IDE and SUMO)
    * JavaFX (GUI visualization)

- **Development Environment**:  
  *IntelliJ IDEA*

- **Version Control**:  
  *Git*

- **External libraries**:  
  *[Library name, if any]* (optional)

---

## Team Roles

**Milestone 1:**  
Everyone decides which tasks they want to work on.

The GitHub repository was set up by **Abdulrehman Saqib Khan**.  
The basic class structure of the Java project was created by **John Grosch** and **Oleksandr Bahno**.

---

### _John Grosch_ is responsible for:
- UML design of the project
- Program controller (link between GUI and logic)  
  *Milestone 2 → completed*

### _Abdulrehman Saqib Khan_ is responsible for:
- README
    - Project Overview
    - Technology Stack Summary
    - Time Plan

### _Oleksandr Bahno_ is responsible for:
- GUI Mockup