# factions-plugin
Factions is a plugin made for [Spigot](https://www.spigotmc.org) used by the J76-Factions server. It adds factions, pvp wrappers
to prevent players from logging out while in combat, an economy system and much more. To use it, you will need [SQLite](https://www.sqlite.org/index.html)
as a library (put inside plugins/libs/ folder) and Spigot or Bukkit version 1.13.2+.

# Instructions on how to compile
This is a Maven project. You will need a maven environment to compile this project successfully, as well as an Internet connection
to download the required libraries. Once you have compiled it, you can find the compiled .jar inside target/, which you can then
copy into plugins/ on your server directory. The plugin will automatically setup the database for you the first time.
