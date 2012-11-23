(ns rhyme-finder.core)

(defn parse-lines [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    ((comp doall line-seq) rdr)))

(defn pronunciation-list "Loads all of the rhyme list into memory and returns it" []
  (parse-lines "cmudict.txt"))

(def pronunciations (pronunciation-list))

(defn update-values [f m]
  (zipmap (keys m) (map f (vals m))))

(defn phone-list "Loads all of the phones into memory" []
  (parse-lines "cmudict-phones.txt"))

(def phones (phone-list))

(defn get-phones []
 (update-values #(map clojure.string/lower-case %)
   (clojure.walk/keywordize-keys
    (reduce
     (fn [m [v k]] (update-in m [k] #(conj % v)))
     {}
     (map #(clojure.string/split % #"\s") phones)))))

(defn get-poem [filename]
  (remove clojure.string/blank?
          (map clojure.string/lower-case (parse-lines filename))))

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

(defn remove-numbers-from-string [a-string]
  (apply str (filter #(not (Character/isDigit %)) a-string)))

(defn load-pronunciations [words]
  "Takes a list of words and returns a mapping of those words to their pronunctiation"
  (update-values (comp
                  #(clojure.string/split % #"\s")
                  remove-numbers-from-string
                  #(clojure.string/trim %))
                 (reduce merge (map #(check-line % words) pronunciations))))

(defn get-pronunctiations-from-file [filename]
  "Takes a file and return a mapping of the words in the file to their pronunctiations"
  (load-pronunciations (to-words (get-poem "filename"))))

(defn get-wp-from-mapping[mapping line]
  (into [] (apply concat (map mapping (clojure.string/split line #"\s")))))

(defn remove-numbers [string-list]
  (map remove-numbers-from-string string-list))

(defn is-vowel? [phone]
  (some #{phone} (:vowel (get-phones))))

(defn vowels-only [wp]
  "returns only the vowel phones of the pronunciation"
  (filter
   (fn [phone]
     (if (is-vowel? phone)
       true
       false)) wp))

; wp* denotes the word-pronunciation of a word
(defn pure-rhyme? [wp1 wp2]
  "Returns true if both words(strings?) have a pure rhyme with each other"
  (and
   (= (vowels-only wp1) (vowels-only wp2))
   (= (last wp1) (last wp2))))

(defn pure-end-rhyme? [wp1 wp2]
  (and
   (= (last (vowels-only wp1)) (last (vowels-only wp2)))
   (= (last wp1) (last wp2))))

(defn indices [pred coll]
  "http://stackoverflow.com/questions/8641305/how-do-i-find-the-index-of-an-element-that-matches-a-predicate-in-clojure#answer-8642069"
  (keep-indexed #(when (pred %2) %1) coll))

(defn get-end-rhyme [wp1]
  (nth 
   (split-at
    (reduce max (seq (indices is-vowel? wp1)))
    wp1)
   1))
  
(defn classify-lines [poem]
  "takes a poem and returns a mapping of each end rhyme to vector of the lines that have that end rhyme"
  (let [wp-mapping (load-pronunciations (to-words poem))]
    (group-by #(get-end-rhyme (get-wp-from-mapping wp-mapping %)) poem)))

;rhyme-finder.core> (classify-lines (get-poem "poems/abab.txt"))
;{("ey") ["i'm writing a poem today" "i don't care what you say"], ("eh" "l") ["i hope it turns out swell" "because we're all under a spell"]}