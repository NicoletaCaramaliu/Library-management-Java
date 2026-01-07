# 📚 Library Management System — Spring Boot Project

## 🔎 Descriere generală

Acest proiect reprezintă un **Library Management System** (aplicație de bibliotecă) dezvoltat în **Spring Boot**.

Aplicația permite:

- gestionarea utilizatorilor  
- gestionarea cărților și categoriilor  
- împrumuturi de cărți (loans)  
- recenzii (reviews)  
- notificări pentru întârzieri (notifications)

Sistemul respectă principiile **REST**, validează datele și persista informațiile într-o bază de date.

---

## 🏗️ Tehnologii folosite

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **Spring Security**
- **Hibernate**
- **MySQL / H2**
- **JUnit + Mockito**
- **Swagger / OpenAPI**

---

## ▶️ Cum se pornește aplicația

### 1️⃣ Clonare repo

```bash
git clone <link-repository>
```

### 2️⃣ Configurare baza de date (MySQL)

În `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library
spring.datasource.username=root
spring.datasource.password=parola-ta
spring.jpa.hibernate.ddl-auto=update
```

### 3️⃣ Pornire aplicație

Din IntelliJ:

```
Run → LibraryApplication
```

sau din terminal:

```bash
mvn spring-boot:run
```

---

## 📘 Documentație API (Swagger)

Accesezi:

```
http://localhost:8080/swagger-ui/index.html
```

➡️ De aici se pot testa toate endpoint-urile.

> 🔓 **Swagger este accesibil fără autentificare.**

---

## 🔐 Autentificare & Roluri

Aplicația folosește **Spring Security**.

Roluri:

- `USER`
- `LIBRARIAN`
- `ADMIN`

### 🧩 Permisiuni principale

| Funcționalitate                        | USER | LIBRARIAN | ADMIN |
|---------------------------------------|:----:|:---------:|:-----:|
Vedi cărți                              | ✅   | ✅        | ✅    |
Adaugă / editează cărți                  | ❌   | ✅        | ✅    |
Gestionează categorii                    | ❌   | ✅        | ✅    |
Împrumută cărți pentru sine             | ✅   | ❌        | ❌    |
Creaiăîmprumut pentru alt utilizator   | ❌   | ✅        | ✅    |
Gestionează utilizatori                  | ❌   | ❌        | ✅    |
Vede propriile notificări               | ✅   | ✅        | ✅    |
Notifică librarian loans overdue              | ❌   | ❌        | ✅    |

> 📝 **Înregistrarea unui cont nou (`POST /api/users`) este permisă fără autentificare.**

---

## 🗄️ Structura aplicației

```text
src/main/java/com/example/library
 ├── config
 ├── controller
 ├── service
 ├── repository
 ├── model
 └── LibraryApplication.java
```

---

## 🗃️ Baza de date & ERD

Entități principale:

- User  
- Book  
- Category  
- Loan  
- Review  
- Notification  

### 🔗 Relații principale

- **User — Loan (1:N)**
- **Book — Loan (1:N)**
- **Book — Review (1:N)**
- **User — Notification (1:N)**

> Diagrama ERD a fost generată cu **MySQL Workbench** și este inclusă în [documentația proiectului](docs).

---

## 🧪 Teste

Proiectul conține:

- teste pentru controllere (MockMvc)  
- teste pentru servicii (Mockito + JUnit)

Se rulează cu:

```bash
mvn test
```
---

## 📨 Endpoints principale (exemple)

### 👤 Users
```
GET   /api/users/me
POST  /api/users
PUT   /api/users/me
DELETE /api/users/{id}
```

### 📚 Books
```
GET   /api/books
POST  /api/books
GET   /api/books/search
```

### 📦 Loans
```
POST /api/loans/borrow/{bookId}
GET  /api/loans/me
```

### ⭐ Reviews
```
POST /api/reviews
GET  /api/reviews/book/{id}
```

### 🔔 Notifications
```
GET /api/notifications/me
```

---

## 🎬 Demonstrarea aplicației

Aplicația poate fi demonstrată folosind:

- **Swagger UI**
- **Postman**
- sau o interfață GUI (dacă este implementată)

În documentație sunt incluse capturi de ecran pentru scenariile principale.

---

## ✅ Concluzie

Acest proiect reprezintă un **MVP funcțional** pentru un sistem de management al bibliotecii:

✔ REST API complet  
✔ validări  
✔ securitate  
✔ 6+ entități și relații  
✔ persistare în DB  MySQL
✔ documentație Swagger  
✔ teste unitare  
✔ demonstrație funcțională
