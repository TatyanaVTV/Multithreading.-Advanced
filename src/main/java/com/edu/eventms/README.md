# Event Management System (Multithreading Demo)

Простой backend-сервис на Spring Boot для параллельной обработки регистрации участников мероприятий.
Демонстрирует использование ExecutorService (ThreadPoolExecutor) для конкурентного выполнения задач: регистрация, отправка приглашений и уведомлений.

## Технологии

- Java 17+
- Spring Boot 2.7 / 3.x
- Maven
- Lombok
- Executor Framework (Executors.newFixedThreadPool)

## Структура проекта

```
com.edu.eventms
├── config
│   └── ThreadPoolConfig.java         // бин ExecutorService (10 потоков)
├── controller
│   └── EventController.java          // REST эндпоинт /api/events/{eventId}/register
├── model
│   ├── Event.java
│   ├── Participant.java
│   └── RegistrationResult.java
└── service
    ├── EventService.java             // интерфейс
    └── EventServiceImpl.java         // реализация с параллельной обработкой
    └── EventManagementApplication.java
```

## Сборка и запуск
```
mvn clean package
java -jar target/event-management-0.0.1-SNAPSHOT.jar
```

Или через Maven:
```
mvn spring-boot:run
```

Сервер стартует на порту 8080.

## Пример запроса с замером времени (20 участников)

Ожидаемое время выполнения будет около 2–5 секунд, что доказывает параллельную обработку (последовательный подход занял бы ~50 секунд).

```bash
time curl -X POST http://localhost:8080/api/events/20/register \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '[
    {"id":30,"name":"U1","email":"u1@ex.com"},
    {"id":31,"name":"U2","email":"u2@ex.com"},
    {"id":32,"name":"U3","email":"u3@ex.com"},
    {"id":33,"name":"U4","email":"u4@ex.com"},
    {"id":34,"name":"U5","email":"u5@ex.com"},
    {"id":35,"name":"U6","email":"u6@ex.com"},
    {"id":36,"name":"U7","email":"u7@ex.com"},
    {"id":37,"name":"U8","email":"u8@ex.com"},
    {"id":38,"name":"U9","email":"u9@ex.com"},
    {"id":39,"name":"U10","email":"u10@ex.com"},
    {"id":40,"name":"U11","email":"u11@ex.com"},
    {"id":41,"name":"U12","email":"u12@ex.com"},
    {"id":42,"name":"U13","email":"u13@ex.com"},
    {"id":43,"name":"U14","email":"u14@ex.com"},
    {"id":44,"name":"U15","email":"u15@ex.com"},
    {"id":45,"name":"U16","email":"u16@ex.com"},
    {"id":46,"name":"U17","email":"u17@ex.com"},
    {"id":47,"name":"U18","email":"u18@ex.com"},
    {"id":48,"name":"U19","email":"u19@ex.com"},
    {"id":49,"name":"U20","email":"u20@ex.com"}
  ]'
```

Ожидаемый ответ (сокращённо):
```json
[
  {"participantId":30,"registered":true,"invitationSent":true,"notificationSent":true,"message":"Участник U1 обработан"},
  ...
  {"participantId":49,"registered":true,"invitationSent":true,"notificationSent":true,"message":"Участник U20 обработан"}
]
```

После выполнения вы увидите время, например:
```bash
real    0m5,082s
user    0m0,000s
sys     0m0,000s
```

## Лицензия

Проект создан в учебных целях.