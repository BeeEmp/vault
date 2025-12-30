# VaultV1 Snippet Vault

A secure, encrypted code snippet sharing platform built with Java Spring Boot and Vanilla JS.

## Prerequisites
- Java 17+
- **IntelliJ IDEA**, **Eclipse**, or **VS Code** (with Java Extension Pack)
- *Note: If Maven is not installed in your terminal, please use one of the IDEs above which have Maven built-in.*

## How to Run
1. Open this `VaultV1` folder in your IDE.
2. Allow the IDE to import the Maven project (it will download dependencies like Spring Boot, H2, etc.).
3. Run the `VaultV1Application` main class.
4. Open your browser to `http://localhost:8080`.

## Features
- **Login**: default credentials `admin` / `vibe`.
- **Dashboard**: Create snippets with auto-encryption.
- **Expiry**: Snippets auto-delete after 6 hours.
- **Secure View**: Read-only view checks expiry and decrypts on the fly.

## Project Structure
- `src/main/java`: Backend (Controllers, Services, Models).
- `src/main/resources/static`: Frontend (HTML, CSS, JS).
- `data/`: Local H2 database file.

## License
This project is licensed under the terms of the MIT license.
