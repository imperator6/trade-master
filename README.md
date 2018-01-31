# trade-master



### Start the ui


```
cd client
npm install (run as Administrator make sure npm proxy is configured!)
npm start
```

open http://localhost:8080/

### Start the storybook

```
cd client
npm install (run as Administrator make sure npm proxy is configured!)
npm run storybook
```

open http://localhost:9001


### Executable Jar....

// build the react js app (UI)
cd client
npm run build -> produces the bundle.js file and copies it over to the server side (server/src/main/webapp/resources/js)

// build the spring boot backend
// make sure a proper application.yml is in place (server/src/main/resources)
cd..
cd server
gradlew clean build










