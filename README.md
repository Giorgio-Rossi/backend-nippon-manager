# Nippon Manager — Backend

Spring Boot REST API per la gestione di atleti e certificati medici. Funziona come applicazione desktop standalone: avvia un JAR con database H2 embedded e apre automaticamente il browser.

## Stack

- **Java 21** / Spring Boot 3.5.4
- **Spring Security** con autenticazione JWT stateless
- **H2** (database file-based, persistente)
- **Liquibase** per le migration del database
- **Lombok** per ridurre il boilerplate

## Avvio rapido

```bash
mvn spring-boot:run
```

L'applicazione si avvia su `http://localhost:8080` e apre automaticamente il browser.

## Credenziali di default

| Username | Password | Ruolo |
|----------|----------|-------|
| `admin`  | `admin123` | ADMIN |
| `user`   | `user123`  | USER  |

Le credenziali vengono create al primo avvio tramite `DataInitializer`.

## Database

H2 file-based, il database viene salvato in:

```
%USERPROFILE%/nippon-manager/data.mv.db
```

Le tabelle vengono gestite da Liquibase (migrations in `src/main/resources/db/changelog/`).

## Struttura del progetto

```
src/main/java/com/projectstarter/starter/
├── Config/
│   ├── SecurityConfig.java          # Configurazione Spring Security + JWT
│   ├── DataInitializer.java         # Creazione utenti di default
│   ├── BrowserLauncher.java         # Apertura browser all'avvio
│   └── FrontendController.java      # Fallback React Router → index.html
├── Controller/
│   ├── AuthController.java          # POST /api/auth/login, /register
│   ├── AtletaController.java        # CRUD /api/athletes
│   ├── UserController.java          # GET /api/users/me
│   └── TestController.java          # Endpoint di test
├── Dto/
│   ├── Request/                     # LoginRequest, RegisterRequest, AtletaRequest
│   └── Response/                    # JwtResponse, AtletaResponse, UserResponse
├── Entity/
│   ├── User.java                    # Utente (UserDetails), ruoli USER/ADMIN
│   └── Atleta.java                  # Atleta con dati anagrafici e certificato medico
├── Filter/
│   └── JwtAuthenticationFilter.java # Validazione JWT su ogni richiesta
├── Repository/
│   ├── UserRepository.java
│   └── AtletaRepository.java
├── Service/
│   ├── AtletaService.java           # Logica CRUD atleti + soft delete
│   └── CustomUserDetailsService.java
└── Util/
    └── JwtUtil.java                 # Generazione e validazione token
```

## API Endpoints

### Autenticazione

| Metodo | Endpoint | Accesso | Descrizione |
|--------|----------|---------|-------------|
| POST | `/api/auth/login` | Pubblico | Login, restituisce JWT |
| POST | `/api/auth/register` | Pubblico | Registrazione nuovo utente |

### Utenti

| Metodo | Endpoint | Accesso | Descrizione |
|--------|----------|---------|-------------|
| GET | `/api/users/me` | Autenticato | Dati utente corrente |

### Atleti

| Metodo | Endpoint | Accesso | Descrizione |
|--------|----------|---------|-------------|
| GET | `/api/athletes` | Autenticato | Lista atleti (filtro: `?attivo=true/false`) |
| POST | `/api/athletes` | Autenticato | Crea nuovo atleta |
| GET | `/api/athletes/{id}` | Autenticato | Dettaglio atleta |
| PUT | `/api/athletes/{id}` | Autenticato | Aggiorna atleta |
| DELETE | `/api/athletes/{id}` | Autenticato | Disattiva atleta (soft delete) |
| PUT | `/api/athletes/{id}/activate` | Autenticato | Riattiva atleta |
| GET | `/api/athletes/search?q=` | Autenticato | Ricerca per nome/cognome |
| GET | `/api/athletes/expiring-certificates?days=30` | Autenticato | Certificati in scadenza |

### Endpoint di test

| Metodo | Endpoint | Accesso |
|--------|----------|---------|
| GET | `/api/test/all` | Pubblico |
| GET | `/api/test/user` | USER / ADMIN |
| GET | `/api/test/admin` | ADMIN |

## Autenticazione

Tutte le richieste protette richiedono l'header:

```
Authorization: Bearer <jwt-token>
```

Il token si ottiene dalla risposta di `POST /api/auth/login`:

```json
{
  "token": "eyJhbGci...",
  "type": "Bearer",
  "username": "admin",
  "superuser": true
}
```

## Build con frontend incluso

Il frontend React viene compilato e incluso automaticamente nel JAR tramite Maven:

```bash
mvn clean package
java -jar target/starter-0.0.1-SNAPSHOT.jar
```

Il plugin `frontend-maven-plugin` esegue `npm install && npm run build` e `maven-resources-plugin` copia il risultato in `classpath:/static/`.

## Sicurezza

- Cambia `app.jwt.secret` in produzione con una chiave di almeno 256 bit
- Usa HTTPS in produzione
- Configura CORS per il dominio di produzione in `SecurityConfig.java`
