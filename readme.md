This repo contains the router and compiler services for KojoJS.

### Prerequisites

* In another terminal window, start the **kojojs-editor web-server**, see instructions here: https://github.com/litan/kojojs-editor 

### How to run in terminal:
  
* Start `sbt` i terminal.
* Fork and clone this repo to your local machine.
* Execute the `compile` command inside sbt.
* Execute the `update` command inside sbt.
* Execute the `router/reStart` command inside sbt to get the **router** going.
* Execute the `compilerServer/reStart` command inside sbt to get the **compiler service** going
* Navigate to localhost:9000 using a browser to start using KojoJS

### Instructions for using with IntelliJ Idea:
* Import sbt project
* Open sbt shell
* Run `router/reStart` to get the router going. The kojojs-editor web-server should be running at this point.
* Run `compilerServer/reStart` to get the compiler service going
* Navigate to localhost:9000 to start using KojoJS
