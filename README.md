# Shopify Logistics Backend

For the Shopify Backend Internship Challenge 2022.

## How to use?
**Head over to: [My Heroku Deployment](https://theriake-shopify-backend-chall.herokuapp.com/)**

The application is fully functional there for all CRUD options.
Handles deletion with comments and undeletion with comments.  
Comments only show when viewing the 'graveyard' (ie. all deleted items.)

## Tech Stack
- JAVA
- Spring Boot with JPA using Hibernate
- Lombok
- SQLite
- Thymeleaf
- Jackson (JSON Handling + Filtering)
- Deploy on Heroku using autodeploy.

## Required Features:
- Create inventory items
- Edit them
- Delete them
- View a list of them

## Optional Additional features
- Deletion comments
- Can restore delete items, also with comments.
- Comments are only visible in graveyard or with direct queries.
- 
I added a 'graveyard' which shows deleted items just to demonstrate the way I was handling deletes.  It is not necessary as an endpoint.  Items can NOT be viewed after being deleted.

There is a 'permanent delete' end point.  This end point however is only accessible by directly hitting DELETE on /delete/harddelete/id (ie. using postman).  I only included this functionality incase it was required to demonstrate knowledge for the challenge but I don't want it accessible via the front-end.
