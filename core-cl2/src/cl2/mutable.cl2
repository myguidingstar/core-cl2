(defmacro inc! [arg] `(set! ~arg (+* ~arg 1)))

(defmacro dec! [arg] `(set! ~arg (-* ~arg 1)))

(defn assoc!
  "Mutable version of assoc."
  [m & kvs]
  (loop [kv-tail kvs]
    (if kv-tail
      (do (let* [k v] kv-tail)
          (set! (get* m k) v)
          (recur (nnext kv-tail)))
      m)))

(defn dissoc!
  "Mutable version of dissoc."
  [m & ks]
  (doseq [k ks]
    (delete (get* m k)))
  m)

(defn reverse!
  "Mutable version of reverse."
  [x]
  (.reverse x))

(defn conj!
  "Mutable version of conj."
  [coll & xs]
  (.apply coll.push coll xs)
  coll)

(defn cons!
  "Mutable version of cons."
  [coll & xs]
  (.apply coll.unshift coll xs)
  coll)

(defn merge!
  "Mutable version of merge."
  [m0 & ms]
  (doseq [m ms]
    (doseq [[k v] m]
      (set! (get* m0 k) v)))
  m0)