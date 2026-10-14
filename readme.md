This repo contains the router and compiler services for KojoJS.

### Prerequisites

* In another terminal window, start the **kojojs-editor web-server**, see instructions here: https://github.com/litan/kojojs-editor 

### How to run in terminal:
  
* Start `sbt` i terminal.
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

### Operations endpoints

The router has two endpoints for monitoring:

* `GET /status?secret=<secret>` returns JSON with the number of registered, ready, compiling and initializing
  compilers, the compile queue, and one row per compiler (`state`, `lastActivitySeconds`, `lastSeenSeconds`).
  It needs the router secret (`SCALAFIDDLE_SECRET`, the same one compilers use to register).
  A compiler that is alive and pinging but stuck compiling shows as a high `lastActivitySeconds`, a low
  `lastSeenSeconds` and `state` = `Compiling`.
* `GET /health?min=N` answers with the status code only: `200` if at least `N` compilers are registered
  (default 1), `503` otherwise. It needs no secret and has no body, so it can be used directly as an HTTP
  health check. A compiler that is busy still counts as registered.

If `SCALAFIDDLE_SECRET` is not set, the router starts with the public default secret and logs an error.
