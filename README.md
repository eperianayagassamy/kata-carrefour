# Seat Booking Service - Kata
Ce service Spring Boot permet de gérer la réservation de places pour des événements en temps réel. Il utilise une approche hybride pour garantir l'intégrité des données tout en offrant une expérience utilisateur fluide (verrouillage temporaire des places).

## Stack Technique
* **Java 21**
* **Framework** : Spring Boot 3.5.x
* **Base de données** : H2 (En mémoire) avec Spring Data JPA
* **Documentation** : Swagger / OpenAPI
* **Tests** : JUnit 5, Mockito, AssertJ, MockMvc
* **Lombok** : Pour réduire le code boilerplate

---
## Architecture
L'application repose sur deux couches de validation :
1. **Le Cache (En mémoire)** : Lorsqu'un utilisateur sélectionne un siège, un verrou est créé dans une Map synchronisée pour 10 minutes. Cela empêche d'autres utilisateurs de voir ou de prendre le siège durant le processus de paiement.
2. **La Base de données** : Une fois le paiement confirmé, le statut du siège passe définitivement à SOLD dans la base H2.
---
## Installation et Démarrage
1. Lancer l'application :
```Bash
mvn spring-boot:run
```
2. Accéder à la documentation API : http://localhost:8080/swagger-ui.html
3. Accéder à la base de données (Console H2) : http://localhost:8080/h2-console
    * JDBC URL : `jdbc:h2:mem:bookingdb`
    * User : `sa | Password : (vide)`

---
## Endpoints API
### Events
* `GET /api/v1/events/{eventId}/seats` : Récupère la liste des sièges disponibles (Statut `AVAILABLE` en base ET non verrouillés en cache).

### Bookings
* `POST /api/v1/bookings` : Pose un verrou temporaire sur un siège.
    * Body : `{ "seatId": 1, "userId": 100 }`
* `PATCH /api/v1/bookings/{seatId}?userId=100` : Finalise la vente et met à jour la base de données.
---
## Tests
Le projet suit une stratégie de tests rigoureuse :
* **Tests Unitaires** : Logique métier des services avec Mockito.
* **Tests de Slice Web** : Validation des contrôleurs et du mapping JSON avec `MockMvc`.
* **Tests de Slice JPA** : Intégrité des données et contraintes SQL (Not Null, Cascade) avec `@DataJpaTest`.

Exécuter les tests:
```bash
mvn test
```
---
## Points d'amélioration
Bien que fonctionnel pour un Kata, voici les axes d'évolution pour une mise en production :

### Robustesse du Cache
Actuellement, le cache est une `Map` en mémoire. Si le serveur redémarre, tous les verrous temporaires sont perdus.
* **Solution** : Utiliser Redis pour un cache distribué et persistant.

### Gestion du Temps (Expiration)
Le nettoyage des verrous expirés se fait actuellement de manière réactive lors de la lecture.
* **Solution** : Implémenter un `@Scheduled` qui nettoie périodiquement les verrous expirés du cache.

### Multi profil & Persistance de Production
Actuellement configurée pour le développement, l'application doit être capable de basculer sur un environnement de production robuste.
* **Solution** : Implémenter des profils Spring (application-dev.yml, application-prod.yml).
    * Dev : Garder H2 pour la rapidité et les tests.
    * Prod : Migrer vers PostgreSQL ou MySQL.

### Conteneurisation
L'application doit être portable et isolée de l'environnement de la machine hôte.
- **Solution** : Créer un Dockerfile et un fichier docker-compose.yml. Cela permet de lancer l'application, la base de données PostgreSQL et le cache Redis en une seule commande, garantissant un comportement identique sur n'importe quel poste ou serveur cloud.

### Gestion des migrations avec Flyway
Actuellement, le schéma est géré par des fichiers schema.sql et data.sql (ou par Hibernate). C'est limité pour le suivi des versions en équipe.
- **Solution** : Intégrer Flyway. Cela permet de versionner chaque modification de la base de données (ex: V1__init_schema.sql, V2__add_index_on_seats.sql). Flyway garantit que chaque environnement (Dev, Test, Prod) est exactement sur la même version du schéma, facilitant les montées de version sans perte de données.