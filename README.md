# MyGithub
My GitHub is an Android app for exploring GitHub users and repositories, as well as user notifications and followers/following.
It interacts with the GitHub API by sending either unathenticated or authenticated requests using a personal access token.

On app startup the user's GitHub profile is shown, listing profile details and navigation options 
to explore their repositories, followers, and following.
The user may also globally search for repositories or users by name via a search bar at the top of the app.

# Loading your profile
To load your GitHub profile on app startup, modify [`login.txt`](app/src/main/resources/login.txt), 
located in the [`app/src/main/resources/`](app/src/main/resources/) folder.
Replace the first line with your GitHub username,
and the second line with a 
[personal access token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/)
for that account. If you'd prefer not to use a personal access token, delete the entire second line.
Providing an invalid access token will cause an API exception 401.
It is encouraged to use an access token, as the API request limit per hour is much greater for authenticated requests
(see [https://developer.github.com/v3/#rate-limiting](https://developer.github.com/v3/#rate-limiting)).

If [`login.txt`](app/src/main/resources/login.txt) is invalid, unchanged or deleted,
the app will load the default profile (smwolfskill) and display a popup message on startup. 
All GitHub navigation and search functionality will still be present, albeit with a smaller allowance of API requests per hour.
Some actions, such as viewing user notifications, are not possible without authentication.
