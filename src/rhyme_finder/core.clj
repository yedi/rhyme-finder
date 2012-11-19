(ns rhyme-finder.core)

(defn parse-lines [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    ((comp doall line-seq) rdr)))

(defn pronunciation-list "Loads all of the rhyme list into memory and returns it" []
  (parse-lines "Documents/dev/rhyme-finder/cmudict.txt"))

(def pronunciations (pronunciation-list))

(defn update-values [f m]
  (zipmap (keys m) (map f (vals m))))

(defn phone-list "Loads all of the phones into memory" []
  (parse-lines "Documents/dev/rhyme-finder/cmudict-phones.txt"))

(def phones (phone-list))

(defn get-phones []
 (update-values #(map clojure.string/lower-case %)
   (clojure.walk/keywordize-keys
    (reduce
     (fn [m [v k]] (update-in m [k] #(conj % v)))
     {}
     (map #(clojure.string/split % #"\s") phones)))))

(defn get-poem [filename]
  (parse-lines filename))

(defn to-words [string-list]
  (clojure.string/split
   (clojure.string/join " " string-list)
   #"\s"))

(defn check-line [line words]
  "checks the line to see if it matches a word in the word-list. If so, returns
 the a map, {word <word's pronunciation>}. Else returns nil"
  (let [word-pronunciation (clojure.string/split (clojure.string/lower-case line) #"\s" 2)]
    (if (some #{(first word-pronunciation)} (map clojure.string/lower-case words))
      {(first word-pronunciation) (first (rest word-pronunciation))}
      nil)))

(defn load-pronunciations [words]
  "Takes a list of words and returns a mapping of those words to their pronunctiation"
  (update-values remove-numbers-from-string
                 (reduce merge (map #(check-line % words) pronunciations))))

(defn remove-numbers-from-string [a-string]
  (apply str (filter #(not (Character/isDigit %)) a-string)))

(defn remove-numbers [string-list]
  (map remove-numbers-from-string string-list))

(defn pure-rhyme? [word1 word2]
  "Returns true if both words(strings?) have a pure rhyme with each other"
  nil)

(defn vowels-only [wp]
  "returns only the vowel phones of the pronunciation"
  (filter
   (fn [word]
     (if (some #{word} (:vowel (get-phones)))
       true
       false))
   (clojure.string/split wp #"\s")))