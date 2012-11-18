(ns rhyme-finder.core)

(defn parse-lines [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    ((comp doall line-seq) rdr)))

(defn rhyme-list "Loads all of the rhyme list into memory and returns it" []
  (parse-lines "Documents/dev/rhyme-finder/cmudict.txt"))

(defn phone-list "Loads all of the phones into memory" []
  (parse-lines "Documents/dev/rhyme-finder/cmudict-phones.txt"))

(def phones (phone-list))

(defn get-phones []
  (clojure.walk/keywordize-keys
   (reduce
    (fn [m [v k]] (update-in m [k] #(conj % v)))
    {}
    (map #(clojure.string/split % #"\s") phones))))
