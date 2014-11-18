CW.com Website
=============

A work in progress.

Dependencies
-------

mysql
typesafe-activator

Preparation
-------

* brew install mysql
* brew services start mysql
* mysql -uroot
    * CREATE DATABASE cw;
	* GRANT ALL PRIVILEGES ON cw.* TO cw@localhost IDENTIFIED BY ‘sterlingarcher';
	* exit;
* brew install typesafe-activator

Building
-------

from the project directory (in a terminal) run 'activator run'.