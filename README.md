# etl

This code is an exercise in learning something new. It is an experiment at creating 
a filter to combine a group of `.csv` files into a single file while eliminating duplicates.

It is being developed with JetBrains IntelliJ IDEA, using the Cursive plugin. A feature to note 
about Cursive is that it tries to be too helpful sometimes with its _structural editing_. This can 
be turned off in the status bar at the bottom of the screen.

## Installation

You need to have `leiningen` installed to work with the code 

## Usage

You can run the application by typing:

    lein run ./data/raw/savings-2012-2013-20130611.csv  ./data/raw/savings-2014-2015-20150826.csv
    
This will process the two `.csv` files, to `stdout`

FIXME: explanation

    $ java -jar etl-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2015 s5b

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
