# Real-Time Collaborative Document Editor

This project is a real-time collaborative document editor similar to Google Docs, allowing multiple users to view and edit documents simultaneously. It leverages WebSockets for real-time communication and Redis for efficient, in-memory data storage to synchronize document changes among all participants.

This project is built based on the architecture, discussed in and interview meeting, So the interview discussions can be found in the [session file](SESSION.md). 


## Architecture Overview

The application is built using a Spring Boot backend and a vanilla JavaScript frontend. It consists of several key components:

- **Document Service**: Handles CRUD operations for documents stored in a MySQL database.
- **Edit Session Service**: Manages real-time editing sessions using Redis to store document content and change events.
- **WebSocket Server**: Facilitates real-time communication between the client and server, broadcasting document changes to all connected clients.
- **Frontend Client**: Provides a user interface for listing documents, and creating, viewing, and editing documents in real-time.

### Data Flow

1. **Document Listing**: Users retrieve a list of available documents from the Document Service.
2. **Session Initiation**: Upon selecting a document, a WebSocket connection is established for the chosen document.
3. **Real-Time Editing**:
    - Editing actions in one client (insertions/deletions) are sent to the server via WebSocket.
    - The server updates the document's content in Redis and broadcasts the change events to all clients connected to that document's WebSocket channel.
    - Clients receive updates and apply them to the document view in real-time.
4. **Session Termination**: Editing sessions are closed gracefully, with the final document state saved back to the MySQL database.

## Setup and Running

### Prerequisites

- Java JDK 17 or later
- Maven 3.6 or later
- MySQL Server 8 or later
- Redis Server 6.0 or later

### Backend

1. **Configure the application**:
    - Copy `env.example.properties` to `env.properties`.
    - Update the MySQL and Redis connection settings in `env.properties`.

2. **Run the application**:
    ```sh
    mvn spring-boot:run
    ```

### Frontend

1. **Serve the static files**:
    - The Spring Boot application automatically serves the static files located in `src/main/resources/static`.

2. **Access the application**:
    - Open a web browser and navigate to `http://localhost:8080`.

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests to us.
