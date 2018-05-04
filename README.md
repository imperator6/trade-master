# trade-master



### Start the ui


```
cd tradingmaster-client
npm install (run as Administrator make sure npm proxy is configured!)
npm start
```

### Executable Jar....

// build the react js app (UI)
cd tradingmaster-client
npm run build -> produces the bundle.js file and copies it over to the server side (server/src/main/webapp/resources/js)

// build the spring boot backend
// make sure a proper application.yml is in place (server/src/main/resources)
cd..
cd server
gradlew clean build -> produces the tradingmaster-1.0-SNAPSHOT.jar  (server\build\libs)


* Other requirements

A My-Sql or Maria DB needs to be in place.

To run the application you need valid *application.yml* which is not part of git.
Contact me if you need an example file.

# deploy

- stop java
-- find process id -> ´ps auy | grep -i 'java'´
-- kill ´sudo kill pid´



# Stop on Server

- find java process
-- ps aux | grep -i 'java'

- kill the process
-- sudo kill #processid








