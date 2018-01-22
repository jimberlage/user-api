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

TODO: Write this.

## Tests

Run `lein test`.

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2018 FIXME
