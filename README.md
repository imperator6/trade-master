# trade-master



### Start the ui


```
cd client
npm install (run as Administrator make sure npm proxy is configured!)
npm start
```



### Executable Jar....

// build the react js app (UI)
cd client
npm run build -> produces the bundle.js file and copies it over to the server side (server/src/main/webapp/resources/js)

// build the spring boot backend
// make sure a proper application.yml is in place (server/src/main/resources)
cd..
cd server
gradlew clean build -> produces the tradingmaster-1.0-SNAPSHOT.jar  (server\build\libs)










