# user-api

This utility provides a command-line tool & an API for dealing with user records.

**Note:** the examples for running the code assume a Unix environment.  I'd test on Windows, but I don't have access to a Windows machine right now.

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.  It's also helpful to have [jq][], to prettify the output.

[leiningen]: https://github.com/technomancy/leiningen
[jq]: https://stedolan.github.io/jq/

## Quick start (CLI):

To build, run `lein bin`.  This will create a file, `user-api`, in `bin/`.

You can check the output with the following commands.  The last command should give some errors:

1. `./bin/user-api --help`
2. `./bin/user-api -s comma -f test/records-comma.csv -o gender cli | jq`
3. `./bin/user-api -s pipe -f test/records-pipe.csv -o date-of-birth cli | jq`
4. `./bin/user-api -s space -f test/records-space.csv -o last-name cli | jq`
5. `./bin/user-api -s comma -f test/invalid-records-comma.csv cli 2>&1 >/dev/null | jq`

## Quick start (server):

To build, run `lein bin`.  This will create a file, `user-api`, in `bin/`.  Then try something like:

`./bin/user-api -s comma -f test/records-comma.csv api`

This will start a webserver, initialized with the set of records in `test/records-comma.csv`.

Some useful test commands:

1. `curl http://localhost:4567/records/gender | jq`
2. `curl http://localhost:4567/records/birthdate | jq`
3. `curl http://localhost:4567/records/name | jq`
4. `curl -H 'Content-Type: application/json' -d '{"line":"Berlage|Jim|M|blue|02/02/1982","separator":"pipe"}' http://localhost:4567/records | jq`

## Quick start (REPL):

```
jim@goliath$ lein repl
2018-01-22 21:23:46.561:INFO::main: Logging initialized @1270ms
nREPL server started on port 52847 on host 127.0.0.1 - nrepl://127.0.0.1:52847
REPL-y 0.3.7, nREPL 0.2.12
Clojure 1.9.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_112-b16
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

user-api.core=> (require '[user-api.records :refer :all])
nil
user-api.core=> (def records
           #_=>   (:records (parse-records-from-file "test/records-comma.csv" \,)))
#'user-api.core/records
user-api.core=> (map #(str (:first-name %) " " (:last-name %)) records)
("Waylon Dalton" "Justine Henderson" "Abdullah Lang" "Marcus Cruz" "Thalia Cobb" "Mathias Little" "Eddie Randolph" "Angela Walker" "Lia Shelton" "Joanna Shaffer")
user-api.core=> (map #(str (:first-name %) " " (:last-name %)) (sort-by-gender records))
("Thalia Cobb" "Justine Henderson" "Joanna Shaffer" "Lia Shelton" "Angela Walker" "Marcus Cruz" "Waylon Dalton" "Abdullah Lang" "Mathias Little" "Eddie Randolph")
```

## Tests

Run `lein test`.

## License

Copyright © 2018 Jim Berlage
