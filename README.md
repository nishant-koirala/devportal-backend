# Fonepay Developer Documentation Portal — Backend API

**Context:** This is the backend API for the Phase 1 Fonepay Developer Documentation Portal (part of F1Soft Group). It serves as the foundation for the public docs portal, a CMS for internal content editors/admins, and developer account management.

*(Note: Live sandbox, API keys, and certification workflows are planned for Phase 2).*

## 🚀 Tech Stack
- **Language**: Java 26
- **Framework**: Spring Boot 4.1.0
- **Build Tool**: Maven
- **Database**: MongoDB (via MongoDB Atlas)

## 🏗️ Architecture & Structure
This project uses a **Modular Monolith** architecture following a **Package by Feature** (Domain-Driven) directory structure rather than traditional layering. 

### Core Modules
- `common/`: Global configurations, exceptions, utilities, security, constants, and audit trails.
- `auth/`: Authentication, authorization, and developer account management.
- `product/`: Product details and overviews.
- `cms/`: Core content management system (drafts, review, publish workflow, schemas).
- `apidoc/`: API endpoints and parameter documentation.
- `search/`: Search capabilities and indexing.
- `admin/`: Administrative controls and user management.
- `notification/`: Alerts and email communications.
- `developer/`: Developer profile and dashboard details.

Each module encapsulates its domain logic entirely, typically containing `controller`, `service`, `repository`, `document` (MongoDB entities), and `dto` folders.

## 📝 CMS Content Model Highlights
- **Schema-Driven**: Page types (API Endpoint, Product Overview, FAQ) are defined by JSON schemas rather than hardcoded forms.
- **Content Storage**: Content is stored as nested JSON natively in MongoDB, aligning perfectly with the schema-driven frontend editor.
- **Draft Isolation**: Multiple editors can work on the same page simultaneously via editor-specific drafts in `page_versions`, complete with optimistic locking (`row_version`) to prevent overwrite conflicts. Rich text is stored as Markdown.

## ⚙️ Local Setup
1. **Prerequisites**: Ensure you have Java 26 and Maven installed.
2. **Database**: You need access to the MongoDB Atlas cluster. Create an `application-dev.properties` or `application-dev.yml` file (make sure it's gitignored!) with your connection credentials.
3. **Run**: Use the Maven wrapper to run the application locally:
   ```bash
   ./mvnw spring-boot:run
   ```
   (Or on Windows: `mvnw.cmd spring-boot:run`)
