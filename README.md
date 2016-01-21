# [Commonwealth Skateboarding](http://commonwealthskateboarding.com) Website & Administrative Backend

#### Built for tracking member information, camps, events and publishing a blog. Integrations with Slack, Square, Stripe, Google, and other tools. It's becoming very purpose specific, but it may be of use for other businesses that have membership models (gyms, clubs, hacker spaces, etc).

**Happily accepting bug reports.**

## Dependencies
* homebrew
* java
* mysql
* typesafe-activator
* sass
* nodejs
* gulp (npm install gulp)
* gulp-sass (npm install gulp-sass)
* gulp-autoprefixer (npm install gulp-autoprefixer)
* browser-sync (npm install browser-sync)

## Preparation
1. `gem install sass`*
2. `brew install mysql`
3. `brew tap homebrew/services`
4. `brew services start mysql`
5. `mysql -uroot`
    * `CREATE DATABASE cw;`
    * `GRANT ALL PRIVILEGES ON cw.* TO cw@localhost IDENTIFIED BY ‘sterlingarcher';`
    * `exit;`

6. `brew install typesafe-activator`

*If you're getting permissions issues, add the following to your `~/.bashrc` file:
```
export GEM_HOME=~/.gem
export GEM_PATH=~/.gem
```

## Running
From within the project directory, in Terminal, type `activator run`.

Application will be running on [http://localhost:9000](http://localhost:9000)

**Note:** To access the admin interface, you will need to grant administrative permissions to your personal user account. The first account must be bootstrapped with permissions to access the permissions administrative interface. This can be accomplished by running the following commands AFTER you have logged in and been denied permissions at [http://localhost:9000/admin](http://localhost:9000/admin):

1. `mysql -uroot`

2. ``INSERT INTO `users_security_role` (`users_id`, `security_role_id`) VALUES (1, 2), (1, 5), (1, 6), (1, 7), (1, 8);``

3. `exit;`