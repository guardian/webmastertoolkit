# Webmastertoolkit

A simple set of tools for us to work on the Google Webmaster Tools reports.

## Installation instructions
1. Create a ~/.gu/webmastertoolkit.properties file and add the following into it:

    gdata.username=yourGoogleWebmasterToolkitUsername

    gdata.password=yourGoogleWebmasterToolkitPassword


1. Clone this project
1. $ sbt run
1. The app will output a sqlite3 db file in the output folder. Open it with [sqlite3] (http://www.sqlite.org/) 

The current errors output data to the *errors* table. 

