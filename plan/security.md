# Security configuration

More information about how I've configured application security will appear here soon.

## Roles & Authorities

The application has a kind of SaaS design where multiple businesses may each have their own domain with their own data, multiple users, etc., yet from our point of view as owners/providers of the software we can see and administer all of these domains.  Thus, a role like "admin" would be confusing: is this for the customer to administer his domain (adding accounts for employees, etc.) or for the developers to administer the whole domain (adding new customers/domains)?

To try to reduce this ambiguity, I have named the roles thusly:

- **SUPER**: A super-user, representing the owner/developer of the SaaS, who can create new customers/domains.

- **AGENT**: Each customer domain will be referred to as an "Agency", so the customers will be referred to as "Agents".

At this point I do not see a reason to create a third role for subordinates (like the AGENT's employees).  These too can have the AGENT role, and we can use fine-grained "authorities" to grant them powers to within their agencies.  Example authorities might include:

- CAN_ADMINISTER_AGENCY
- CAN_GRANT_ADMINISTER_AGENCY
- CAN_CREATE_NEW_AGENTS
- CAN_DELETE_AGENTS
- CAN_VIEW_SENSITIVE_REPORTS
- etc...

When an agent account is deleted or is stripped of "CAN_ADMINISTER_AGENCY" we'll have to do a check to make sure that at least one privileged account exists for the agency; a fair trade for the flexibility this gives us.

## API Security

This project has two security configurations: one for REST API endpoints beginning with "`/api/`" and one for all other endpoints, assumed to be accessed with a web browser.  (In a real business project, these would likely be two separate projects/codebases, but this is a learning project...)

Web endpoint security is fairly standard and basically uses Spring Boot's defaults.  Users authenticate by a form, and if they try to access protected endpoints before logging in, they'll be redirected to a login form.  The app sets a session cookie which keeps track of the user's session.

REST API security is different.  The API is stateless, and we're using JWTs (javascript web tokens) for authentication and authorization.  The client POSTs to `/api/login` with a JSON body like the following:

    {"username": "joe", "password": "pass"}
    
The server should respond with a bearer token in its "Authorization" header.  The client can then access API endpoints with the same token (a string beginning with "Bearer ") in the Authorization header of each request.  The token tells the server that the client is authenticated and which authorities/roles he has.  The JWT has an expiration date (currently 60 seconds after authentication) so a stolen JWT is of limited value to a hacker.