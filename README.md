CW.com Website
=============

A work in progress.

Dependencies
-------

* mysql
* typesafe-activator
* sass

Preparation
-------

* gem install sass
* brew install mysql
* brew services start mysql
* mysql -uroot
 	* CREATE DATABASE cw;
	* GRANT ALL PRIVILEGES ON cw.* TO cw@localhost IDENTIFIED BY ‘sterlingarcher';
	* exit;
* brew install typesafe-activator

Running
-------

from the project directory (in a terminal) run 'activator run'.

Application will be running on http://localhost:9000
