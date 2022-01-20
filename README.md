# Shopify Logistics Backend

For the Shopify Backend Internship Challenge 2022.

## How to use?
**Head over to: [My Heroku Deployment](https://theriake-shopify-backend-chall.herokuapp.com/)**
--- On first load it may take awhile due to being served on Heroku and getting low traffic.  Shouldn't take more than 20s if server was sleeping ---

The application is fully functional there for all CRUD options.
Handles deletion with comments and undeletion with comments.  
Comments only show when viewing the 'graveyard' (ie. all deleted items.)

Alternatively you can build this using mvn package and have an executable .jar file which will contain all required files include Tomcat servlet allowing the application to run. I didn't include this because you would still require local JAVA runtime and SQLite drivers for it to work and the challenge said to 'not assume any technology exist in environment' So I chose to deploy to Heroku so you can see code here and view deployed project there.

## Tech Stack
- JAVA
- Spring Boot with JPA using Hibernate
- Lombok
- SQLite
- Thymeleaf
- Jackson (JSON Handling + Filtering)
- Deploy on Heroku using autodeploy.

## Explanation of stack choices

I chose to use Java with Spring boot because the challenge requested ensuring that the app could scale.  This gives very easy scalability only requiring entities be added.  And authorization/security can be easily added either custom written or easily via any popular auth provider using Spring security.  The database is updated with new entities automatically.

SQLite was chosen just because it's lightweight and this is a tiny project.  However could easily be changed to any other required database.  application.properties would just be changed to handle the connection.

I like using Lombok just for clean code.

I'm using Jackson (which is included in Spring Boot) to handle JSON response entities.  I didn't want comments showing for all items so used this to filter them out.  The default filter I created filters out "comments" and "enabled".  In graveyard I have no filters running so comments and enabled status is shown.

## Required Features:
- Create inventory items
- Edit them
- Delete them
- View a list of them

## Optional Additional features
- Deletion comments
- Can restore delete items, also with comments.
- Comments are only visible in graveyard or with direct queries.

I added a 'graveyard' which shows deleted items just to demonstrate the way I was handling deletes.  It is not necessary as an endpoint.  Items can NOT be viewed after being deleted.

There is a 'permanent delete' end point.  This end point however is only accessible by directly hitting DELETE on /delete/harddelete/id (ie. using postman).  I only included this functionality incase it was required to demonstrate knowledge for the challenge but I don't want it accessible via the front-end.
