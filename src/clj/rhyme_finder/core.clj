(ns rhyme-finder.core
  (:require [clojure.string :as str]))

(defn parse-lines [filename]
  (map str/lower-case (str/split-lines (slurp filename))))

(defn parse-words [filename]
  (map str/lower-case (str/split (slurp filename) #"\s+")))

(def pronunciations (parse-lines "cmudict.txt"))

(defn update-values [f m]
  (zipmap (keys m) (map f (vals m))))

(defn parse-phones [filename]
  (let [phone-lines (map str/lower-case (parse-lines filename))
        phone-list (map #(str/split % #"\s") phone-lines)
        add-to-list-fn (fn [m [v k]] (update-in m [k] #(conj % v)))]
    (clojure.walk/keywordize-keys
     (reduce add-to-list-fn {} phone-list))))

(def phones (parse-phones "cmudict-phones.txt"))

(defn get-poem [filename]
  (remove str/blank?
          (map str/lower-case (parse-lines filename))))

(defn to-words [string-list]
  (str/split (str/join " " string-list) #"\s"))

(defn check-line 
  "checks the line to see if it matches a word in the word-list. If so, returns
   a map, {word <word's pronunciation>}. Else returns nil"
  [line words]
  (let [word-pronunciation (str/split (str/lower-case line) #"\s" 2)]
    (if (some #{(first word-pronunciation)} (map str/lower-case words))
      {(first word-pronunciation) (first (rest word-pronunciation))}
      nil)))

(defn remove-numbers-from-string [a-string]
  (apply str (filter #(not (Character/isDigit %)) a-string)))

(defn load-pronunciations
  "Takes a list of words and returns a mapping of those words to their pronunctiation"
  [words]
  (update-values (comp
                  #(str/split % #"\s")
                  remove-numbers-from-string
                  #(str/trim %))
                 (reduce merge (map #(check-line % words) pronunciations))))

(defn get-pronunctiations-from-file [filename]
  "Takes a file and return a mapping of the words in the file to their pronunctiations"
  (load-pronunciations (to-words (get-poem filename))))

(defn get-wp-from-mapping [mapping line]
  (into [] (apply concat (map mapping (str/split line #"\s")))))

(defn remove-numbers [string-list]
  (map remove-numbers-from-string string-list))

(defn is-vowel? [phone]
  (some #{phone} (:vowel phones)))

(defn vowels-only [wp]
  "returns only the vowel phones of the pronunciation"
  (filter is-vowel? wp))

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

(defn settize [vec]
  (reduce
   (fn [vec-set item]
     (if (some #{item} vec-set)
       vec-set
       (conj vec-set item)))
   []
   vec))
 
(defn rhyme-scheme
  "returns the rhyme scheme of a poem"
  [poem]
  (let [wp-mapping (load-pronunciations (to-words poem))
        end-rhymes (map #(get-end-rhyme (get-wp-from-mapping wp-mapping %)) poem)]
    (reduce
     (fn [scheme end-rhyme]
       (let [end-rhyme-set (settize end-rhymes)]
         (str scheme (.indexOf end-rhyme-set end-rhyme))))
     ""
     end-rhymes)))

(defn indexed-phones
  "i'm writing a poem today => [
     {:phone 'ay' :index 0},
     {:phone 'm' :index 0},
     {:phone 'r' :index 1},
     {:phone 'ay' :index 1},
     {:phone 't' :index 1},
     {:phone 'ih' :index 1},
     {:phone 'ng' :index 1},
     {:phone 'ah' :index 2},
     {:phone 'p' :index 3},
     ...
  ]"
  [poem]
  (let [wp-mapping (load-pronunciations (to-words poem))
        poem-words (str/split (str/join " " poem) #"\s")]
    (loop [i 0 rem poem-words ret []]
      (if (seq rem)
        (recur (inc i) (rest rem)
               (concat ret (map (fn [phone] {:index i :phone phone})
                                (get wp-map (first rem)))))
        ret))))





;====
;rhyme-finder.core> (classify-lines (get-poem "poems/abab.txt"))
;{("ey") ["i'm writing a poem today" "i don't care what you say"],
;("eh" "l") ["i hope it turns out swell" "because we're all under a
                                        ;spell"]}

;rhyme-finder.core> (rhyme-scheme (get-poem "poems/abab.txt"))
                                        ;"0101"
