Commonwealth Skateboarding Website
=============

http://commonwealthskateboarding.com

Built for tracking member information, camps, events and publishing a blog. Integrations with Slack, Square, Stripe, Google, and other tools. It's becoming very purpose specific, but it may be of use for other businesses that have membership models (gyms, clubs, hacker spaces, etc).

Happily accepting bug reports.

Dependencies
-------

* homebrew
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
