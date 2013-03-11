(defmacro inc! [arg] `(set! ~arg (+ ~arg 1)))
(defmacro dec! [arg] `(set! ~arg (- ~arg 1)))
(fn assoc*! [m k v]
  (set! (get m k) v)
  m)
(fn assoc!
  ([map key val] (assoc*! map key val))
  ([map key val & kvs]
     (let [ret (assoc*! map key val)]
       (if kvs
         (assoc! ret (first kvs) (second kvs) (nnext kvs))
         ret))))
(fn dissoc! [m & ks]
  (for [k ks]
    (delete (get m k)))
  m)
(fn reverse! [x] (.reverse x))
(fn conj! [coll & xs]
  (.apply coll.push coll xs)
  coll)
(fn cons! [coll & xs]
  (.apply coll.unshift coll xs)
  coll)
(fn merge!
  [m0 & ms]
  (for [m ms]
    (for [[k v] m]
      (set! (get m0 k) v)))
  m0)