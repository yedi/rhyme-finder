# rhyme-finder

A Clojure library for performing rhyme analysis on poems

## Usage
Add the following dependency to your `project.clj`

```clojure
[rhyme-finder "0.1.0-SNAPSHOT"]
```

To start playing with the various rhyme analyzing functions you can

```clojure
(:require [rhyme-finder.core :refer :all])
```

**Note:** This library is super pre-alpha and still being developed so some of the function signatures may change without notice

### Using it
First you need the poem or song in the correct format

```clojure
(format-as-poem some-text)  ;; converts any string to the correct poem format
(get-poem filename)  ;; pulls text from any textual file on your system and converts it to the correct poem format
```

Return the rhyme scheme of a poem: 
```clojure
(rhyme-scheme poem)
```
     
Return the found rhyme-combinations in a poem:
```clojure
(rhyme-combos (rhyme-streams poem 2 15 2))
```

## License

Copyright © 2014 yedi

Distributed under the Eclipse Public License, the same as Clojure.
