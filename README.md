Commonwealth Skateboarding Website
=============

http://commonwealthskateboarding.com

Built for tracking member information, camps, events and publishing a blog. Integrations with Slack, Square, Stripe, Google, and other tools. It's becoming very purpose specific, but it may be of use for other businesses that have membership models (gyms, clubs, hacker spaces, etc).

Happily accepting bug reports.

Dependencies
-------

* homebrew
* java
* mysql
* typesafe-activator
* sass
* nodejs

Preparation
-------

* gem install sass
* brew install mysql
* brew services start mysql (if not installed, install with 'brew tap homebrew/services')
* mysql -uroot
 	* CREATE DATABASE cw;
	* GRANT ALL PRIVILEGES ON cw.* TO cw@localhost IDENTIFIED BY ‘sterlingarcher';
	* exit;
* brew install typesafe-activator

Running
-------

from the project directory (in a terminal) run 'activator run'.

Application will be running on http://localhost:9000

Note: To access the admin interface, you will need to grant administrative permissions to your personal user account.
The first account must be bootstrapped with permissions to access the permissions administrative interface. This can be
accomplished by running the following commands AFTER you have logged in and been denied permissions at
http://localhost:9000/admin:

* mysql -uroot
    * INSERT INTO `users_security_role` (`users_id`, `security_role_id`) VALUES (1, 2), (1, 5), (1, 6), (1, 7), (1, 8);
    * exit;