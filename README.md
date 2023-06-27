# Branch Status 

This intends to update Play 2.3.8 to Play 2.4 for the [`PlayRunHook` feature](https://www.playframework.com/documentation/2.4.0/SBTCookbook#Hook-actions-around-play-run) so that CSS postprocessing can be done outside of the application.

---

## Dependencies
* homebrew
* sdkman
* java
* mysql [5.7]
* sbt [0.13.x]
* sass
* nodejs

---

## Preparation
1. `npm install node-sass`
1. On Mac:
  1. `brew install mysql`
  1. `brew tap homebrew/services`
  1. `brew services start mysql`
1. On WSL/Ubuntu: 
  1. See *"Using MySQL 5.7.."* section below.
1. `sdk install sbt` 
  * ([See note under Homebrew installation command](https://www.scala-sbt.org/download.html) as to the issues with using Homebrew for `sbt`)

---

## Using MySQL@5.7 on Ubuntu 20.04 
**For Future Reference**

> **WSL Note:** If using WSL, you will need to enable `systemd` in `/etc/wsl.conf` using [these instructions](https://learn.microsoft.com/en-us/windows/wsl/wsl-config#systemd-support) which requires a `wsl --shutdown` and restart.

1. Go to https://dev.mysql.com/downloads/mysql/5.7.html
1. Click on the Download button for the DEB Bundle.
  * We're using the tarballed version to mitigate issues I ran into with `--fix-broken`.
1. Underneath the Oracle Account messaging, **right click** on *"No thanks, just start my download."* and copy the link.
1. `mkdir` a temporary folder somewhere and `cd` into it.
1. Download the file from copied URL with `wget https://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-server_5.7.42-1ubuntu18.04_amd64.deb-bundle.tar`
1. Extract the .tar with `tar -xfv mysql-server_5.7.42-1ubuntu18.04_amd64.deb-bundle.tar`
1. Install the packages with `sudo dpkg -i *.deb`
1. Run `sudo apt-get install --fix-broken` to resolve any broken packages.
1. Run `sudo dpkg -i *.deb` *again* to install the broken packages.
1. From here MySQL5.7 is installed, but you may need to grant permissions to root to access it:
  1. `sudo mysql -uroot` 
  1. `mysql> USE mysql;` 
  1. `mysql> UPDATE user SET plugin='mysql_native_password' WHERE User='root';`
  1. `mysql> FLUSH PRIVILEGES;`
  1. `mysql> ALTER USER 'root'@'localhost';`
    - Add `IDENTIFIED BY 'your_password_here';` if you want to assign a password
  1. `mysql> exit;`
  1. `sudo systemctl restart mysql`
  1. `sudo systemctl start mysql`

---

## Create the Database
1. `mysql -uroot`
    * `CREATE DATABASE cw;`
    * `GRANT ALL PRIVILEGES ON cw.* TO 'cw'@'localhost' IDENTIFIED BY 'sterlingarcher';`
    * `exit;`

---

## Run the Application
1. Go to the project directory (i.e. `website`).
1. If the database isn't yet running: `mysql.server start`.
1. In your Terminal: `sbt run`.

Application will be running on [http://localhost:9000](http://localhost:9000)

**Note:** To access the admin interface, you will need to grant administrative permissions to your personal user account. The first account must be bootstrapped with permissions to access the permissions administrative interface. This can be accomplished by running the following commands AFTER you have logged in and been denied permissions at [http://localhost:9000/admin](http://localhost:9000/admin):

1. `mysql -u root`
1. ``INSERT INTO `users_security_role` (`users_id`, `security_role_id`) VALUES (1, 2), (1, 5), (1, 6), (1, 7), (1, 8);``
1. `exit;`


