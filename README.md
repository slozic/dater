# dater
Spring app using JWT and postgres

Demo project of creating YADA (yet another dating app) that puts focus on creating dates, instead of profile swipes and matches. Premise is: users that want to meet someone through the date in a spontaneous way, could do so by creating dates and accepting one user out of many and then going out and meeting IRL.

## Tech used
- Springboot 3.0
- Java 17
- Authentication using Spring security and JWTs (on successfull login Bearer token is issued and returend through http headers)
- PostgresSQL for data storage
- REST API's exposed (register, login, create, view and send request to join date)

## Data model
Users, Dates, Date Attendees ->
- User needs to be registered to access the app and create, join dates etc.
- User can create one or many dates, Users -> Dates (1:M)
- User can request to join one or many dates, and vv, one date can be requested by many users, Users -> Date Attendees <- Dates, (M:M) relation through Date Attendees table

## Tests
- Controller tests using JUnit and Spring MockMvc library
- TODO: update after the API re-defintions

## Local setup
- Clone the repo
- Run docker compose to start the postgres instance
- Run the app through IDE e.g intellij or springboot mvn helper 

## Next features and updates
- Enable user profile updates, adding images
- Enable date updates, adding images
- Spring security update and refine authorisation layer
