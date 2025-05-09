# Java Class to Object Decorator

This project demonstrates a lightweight, annotation-driven framework for dynamically decorating Java objects—especially EJB and JMS resources—at runtime. It is primarily intended for use in testing environments where dynamic behaviour injection is needed without altering the core codebase.

## Features

- Decorate Java objects at runtime using custom annotations
- Extendable architecture for different resource types (e.g., EJB, JMS)
- Minimal dependencies, using reflection and Java EE interfaces
- Modular and testable

## Project Structure


## Getting Started

### Prerequisites

- Java 8 or higher
- Gradle

### Build and Run

```bash
# Clone the repository
git clone https://github.com/your-org/java-class-to-object-decorator.git
cd java-class-to-object-decorator

# Build the project
./gradlew build

# Run the demo (if HelloWorld.java has a main method)
./gradlew run

### Example Usage
Annotate a field with @TestRuntimeConfig, and the corresponding decorator class will configure the object during runtime:

@TestRuntimeConfig(resourceType = "EJB")
private MyEjbService ejbService;

At runtime, EJBTestResourceDecorator will decorate the ejbService field.

### Extending the Decorator
- To support a new type of resource:
- Create a new decorator class extending TestResourceDecoratorBase
- Implement the decoration logic

Update the resolution logic to apply it based on the resourceType in the annotation


