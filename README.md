# [Commonwealth Skateboarding](http://commonwealthskateboarding.com) Website & Administrative Backend

#### Built for tracking member information, camps, events and publishing a blog. Integrations with Slack, Square, Stripe, Google, and other tools. It's becoming very purpose specific, but it may be of use for other businesses that have membership models (gyms, clubs, hacker spaces, etc).

**Happily accepting bug reports.**

## Dependencies
* homebrew
* sdkman
* java
* mysql
* sbt
* sass
* nodejs

## Preparation
1. `npm install node-sass`
1. `brew install mysql`
1. `brew tap homebrew/services`
1. `brew services start mysql` or `mysql.server start`
1. `mysql -u root`
    * `CREATE DATABASE cw;`
    * `GRANT ALL PRIVILEGES ON cw.* TO cw@localhost IDENTIFIED BY 'sterlingarcher';`
    * `exit;`
1. `sdk install sbt` ([See note under Homebrew installation command](https://www.scala-sbt.org/download.html) as to the issues with using Homebrew for `sbt`)




## Running
From within the project directory, in Terminal, type `sbt run`.

Application will be running on [http://localhost:9000](http://localhost:9000)

**Note:** To access the admin interface, you will need to grant administrative permissions to your personal user account. The first account must be bootstrapped with permissions to access the permissions administrative interface. This can be accomplished by running the following commands AFTER you have logged in and been denied permissions at [http://localhost:9000/admin](http://localhost:9000/admin):

1. `mysql -u root`

2. ``INSERT INTO `users_security_role` (`users_id`, `security_role_id`) VALUES (1, 2), (1, 5), (1, 6), (1, 7), (1, 8);``

3. `exit;`