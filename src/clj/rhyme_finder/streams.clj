(ns rhyme-finder.streams
  (:require [rhyme-finder.core :as core]))

(defn indices [pred coll]
  (keep-indexed #(when (pred %2) %1) coll))

(defn combos
  "34 4 = 1234343412343434 => [[2, 4, 6], [10, 12, 14]]"
  [val dist match?-fn coll]
  (let [indexes (indices (partial match?-fn val) coll)]
    (loop [rem (rest indexes)
           curr (first indexes)
           ret [[curr]]]
      (if (seq rem)
        (let [new (first rem)]
          (recur (rest rem)
                 new
                 (if (> (- new curr) dist)
                   (conj ret [new])
                   (update-in ret [(dec (count ret))] conj new))))
        ret))))

(defn get-streams
  "1234343412343434 1 3 => [
   	{:value 1 :streams []}
	{:value 2 :streams []}
	{:value 3 :streams [[2, 4, 6], [10, 12, 14]]}
	{:value 4 :streams [[3, 5, 7], [11, 13, 15]]}
   ]

   1234343412343434 2 3 => [
	{:value 12 :streams []}
	{:value 23 :streams []}
	{:value 34 :streams [[2, 4, 6], [10, 12, 14]]}
	{:value 43 :streams [[3, 5, 7], [11, 13, 15]]}
   ]
   Takes a collection, the length of your combination, and the distance to
   search the collection and returns the streams."
  [clen dist coll]
  (let [colls (partition clen 1 coll)
        append-fn (fn [ret val]
                    (conj ret {:value val :streams (combos val dist = colls)}))]
    (reduce append-fn [] (set colls))))
