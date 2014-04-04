(ns rhyme-finder.streams)

(defn indices [pred coll]
  (keep-indexed #(when (pred %2) %1) coll))

(defn combos
  "[3 4] 4 = 2 1234343412343434 => [[2, 4, 6], [10, 12, 14]]"
  [val dist match?-fn min-combo-len coll]
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
        (filterv #(<= min-combo-len (count %)) ret)))))

(defn trim-empty [streams]
  (filter (fn [s] (seq (:streams s))) streams))

(defn get-streams
  "[1 2 3 4] 3 = 2 1234343412343434 => [
   	{:value 1 :streams []}
	{:value 2 :streams []}
	{:value 3 :streams [[2, 4, 6], [10, 12, 14]]}
	{:value 4 :streams [[3, 5, 7], [11, 13, 15]]}
   ]

    [12, 23, 34, 43] 3 = 2 1234343412343434=> [
	{:value 12 :streams []}
	{:value 23 :streams []}
	{:value 34 :streams [[2, 4, 6], [10, 12, 14]]}
	{:value 43 :streams [[3, 5], [11, 13]]}
   ]
   Takes the vals to check, the max distance between matching values, a matching fn,
   the minimum # of streams a collection has and returns the streams."
  [vals dist match?-fn min-len coll]
  (let [append-fn (fn [ret val]
                    (conj ret {:value val
                               :streams (combos val dist match?-fn min-len coll)}))]
    (trim-empty (reduce append-fn [] vals))))


(defn find-streams
  [clen dist match?-fn min-len coll]
  (let [coll (partition clen 1 coll)]
    (get-streams (set coll) dist match?-fn min-len coll)))

