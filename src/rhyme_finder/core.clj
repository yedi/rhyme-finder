(ns rhyme-finder.core)

(defn parse-lines [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    ((comp doall line-seq) rdr)))

(defn pronunciation-list "Loads all of the rhyme list into memory and returns it" []
  (parse-lines "Documents/dev/rhyme-finder/cmudict.txt"))

(def pronunciations (pronunciation-list))

(defn phone-list "Loads all of the phones into memory" []
  (parse-lines "Documents/dev/rhyme-finder/cmudict-phones.txt"))

(def phones (phone-list))

(defn get-phones []
  (clojure.walk/keywordize-keys
   (reduce
    (fn [m [v k]] (update-in m [k] #(conj % v)))
    {}
    (map #(clojure.string/split % #"\s") phones))))

(defn get-poem [filename]
  (parse-lines filename))

(defn to-words [string-list]
  (clojure.string/split
   (clojure.string/join " " string-list)
   #"\s"))

(defn check-line [line words]
  "checks the line to see if it matches a word in the word-list. If so, returns the a map, {word <word's pronunciation>}. Else returns nil"
  (let [word-pronunciation (clojure.string/split (clojure.string/lower-case line) #"\s" 2)]
    (if (some #{(first word-pronunciation)} (map clojure.string/lower-case words))
      {(first word-pronunciation) (rest word-pronunciation)}
      nil)))

(defn load-pronunciations [words]
  "Takes a list of words and returns a mapping of those words to their pronunctiation"
  (reduce merge (map #(check-line % words) pronunciations)))

; (load-pronunciations (to-words (get-poem "Documents/dev/rhyme-finder/poems/test.txt")))
;{"outperforms" (" aw1 t p er0 f ao1 r m z"), "fest" (" f eh1 s t"), "a" (" ah0"), "but" (" b ah1 t"), "best" (" b eh1 s t"), "for" (" f ao1 r"), "is" (" ih1 z"), "it" (" ih1 t"), "enough" (" ih0 n ah1 f"), "the" (" dh ah0"), "test" (" t eh1 s t"), "not" (" n aa1 t"), "rest" (" r eh1 s t"), "quite" (" k w ay1 t"), "this" (" dh ih1 s")}