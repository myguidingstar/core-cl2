(fn not [x] (! x))

(defn contains?
  "Returns true if key is present in the given collection, otherwise
  returns false. Note that for numerically indexed collections like
  vectors, this tests if the numeric key is within the range of indexes;
  for maps and sets, this tests if key is within the keys. Also, keys must
  be of javascript primitive types.
  See also 'some'."
  [m k]
  (in k m))

(defn boolean
  "Coerces to boolean"
  [x]
  (and (!= null x)
       (!== false x)))

(defn get
  "Returns the value mapped to key, not-found or nil if key not present."
  [m k not-found]
  (or (or (and (string? m) (get* m k))
          not-found)
      (or (and (contains? m k)
               (get* m k))
          not-found)))

(defn true? [expr] (=== true expr))

(defn false? [expr] (=== false expr))

(defn nil? [expr] (=== nil expr))

(defn first
  "Returns the first item in the collection. Doesn't work on maps.
  If coll is nil, returns nil."
  [x]
  (if (nil? x) nil (get* x 0)))

(defn second
  "Returns the second item in the collection. Doesn't work on maps.
  If coll is nil, returns nil."
  [x]
  (if (nil? x) nil (get* x 1)))

(defn last
  "Returns the last item in the collection. Doesn't work on maps.
  If coll is nil, returns nil."
  [x]
  (if (nil? x) nil (get* x (dec (count x)))))

(defn next
  "Returns a vector of the items after the first. Doesn't work on maps.
  If there are no more items, returns nil."
  [x]
  (if (empty? x) nil (if (< 1 (count x)) (.slice x 1))))

(defn rest
  "Returns a possibly empty vector of the items after the first.
  Doesn't work on maps."
  [x]
  (if (nil? x) [] (.slice x 1)))

(defn nnext
  "Same as (next (next x))"
  [x]
  (next (next x)))

(defn vector?
  "Return true if x is a Vector."
  [x] (isa? x "Array"))

(defn string?
  "Returns true if x is a String"
  [x]
  (or (=== "string" (typeof x))
      (isa? x "String")))

(defn number?
  "Return true if x is a Number"
  [n]
  (=== "number" (typeof n)))

(defn zero? [x] (=== 0 x))

(defn fn?
  "Return true if x is a Function"
  [f]
  (=== "function" (typeof f)))

(defn inc
  "Returns a number one greater than num."
  [arg]
  (+ 1 arg))

(defn dec
  "Returns a number one less than num."
  [arg]
  (- arg 1))

(defn count
  "Returns the number of items in the collection. (count nil) returns
  0."
  [x]
  (if (or (vector? x) (string? x))
    (. x -length)
    (. (keys x) -length)))

(defn empty?
  "Returns true if coll has no items."
  [coll]
  (or (=== coll "") (nil? coll) (= {} coll) (= [] coll)))

(defn reduce*
  "Standard version of reduce."
  [f val coll]
  (let [c (count coll)]
    (loop [i 0
           r val]
      (if (< i c)
        (recur (+ i 1) (f r (get* coll i)))
        r))))

(defn reduce
  "f should be a function of 2 arguments. If val is not supplied,
  returns the result of applying f to the first 2 items in coll, then
  applying f to that result and the 3rd item, etc. If coll contains no
  items, f must accept no arguments as well, and reduce returns the
  result of calling f with no arguments.  If coll has only 1 item, it
  is returned and f is not called.  If val is supplied, returns the
  result of applying f to val and the first item in coll, then
  applying f to that result and the 2nd item, etc. If coll contains no
  items, returns val and f is not called."
  ([f val coll]
    (reduce* f val coll))
  ([f coll]
    (reduce* f (first coll) coll)))

(defn reductions*
  "Standard version of reductions"
  [f init coll]
  (let [c (count coll)
        ret []]
    (loop [i 0
           r init]
      (if (< i c)
        (recur (+ i 1) (f (do (.push ret r) r)
                          (get* coll i)))
        (.push ret r)))
    ret))

(defn reductions
  "Returns a vector of the intermediate values of the reduction (as
  per reduce) of coll by f, starting with init."
  ([f init coll]
    (reductions* f init coll))
  ([f coll]
    (reductions* f (first coll) coll)))

(def *gensym* 999)
(defn gensym
  "Returns a new symbol with a unique name. If a prefix string is
  supplied, the name is prefix# where # is some unique number. If
  prefix is not supplied, the prefix is 'G__'."
  [prefix-string]
  (inc! *gensym*)
  (str (or prefix-string "G__") *gensym*))

(defn subvec
  "Returns a persistent vector of the items in vector from
  start (inclusive) to end (exclusive).  If end is not supplied,
  defaults to (count vector)."
  [v start end]
  (. v slice start end))

(defn map?
  "Returns true if m is a map. Note: all objects of non-primitive types
   are considered maps."
  [m]
  (and m
       (not (or (contains? #{'string 'number 'boolean 'function} (typeof m))
                (vector? m)
                (nil? m)
                (= null m)
                (fn? m)
                (isa? m "RegExp")))))

;; More info about type detection in javascript:
;; http://stackoverflow.com/questions/332422/how-do-i-get-the-name-of-an-objects-type-in-javascript

(defn type
  "Returns the type of x as a string. Returns 'map on non-primitive types.
  Won't try to determine Class name of x."
  [x]
  (cond (vector?     x)  'vector
        (string?     x)  'string
        (number?     x)  'number
        (nil?        x)  "nil"
        (=== null    x)  'null
        (=== "boolean"
             (typeof x)) 'boolean
        (fn?         x)  'function
        (isa? x
              "RegExp")  'regexp
        :else            'map))

(defn map*
  "Non-native map implementation for old browsers."
  [f arr]
  (let [c (count arr)]
    (loop [r []
           i 0]
      (if (< i c)
        (do
          (.push r (f (get* arr i)))
          (recur r (+ i 1)))
        r))))

(def ^{:doc "Creates a new vector with the results of calling a
provided function  on every element in this vector."}
  map)
(if (fn? Array.prototype.map)
  (set! map (fn [f coll] (.map coll f)))
  (set! map (fn [f coll] (map* f coll))))

(defn remove
  "Returns a vector of the items in coll for which
  (pred item) returns false. pred must be free of side-effects."
  [pred seq]
  (let [c (count seq)]
    (loop [r []
           i 0]
      (if (< i c)
        (do
          (when-not (pred (get* seq i))
            (.push r (get* seq i)))
          (recur r (+ 1 i)))
        r))))

(defn filter*
  "Non-native filter implementation for old browsers."
  [pred arr]
  (let [c (count arr)]
    (loop [r []
           i 0]
      (if (< i c)
        (do
          (if (pred (get* arr i)) (.push r (get* arr i)))
          (recur r (+ i 1)))
        r))))

(def ^{:doc "Returns a vector of the items in coll for which
  (pred item) returns true. pred must be free of side-effects."}
  filter)
(if (fn? Array.prototype.filter)
  (set! filter (fn [pred coll]
                 (.filter coll pred)))
  (set! filter (fn [pred coll]
                 (filter* pred coll))))

(defn merge
  "Returns a map that consists of the rest of the maps conj-ed onto
  the first.  If a key occurs in more than one map, the mapping from
  the latter (left-to-right) will be the mapping in the result."
  [& ms]
  (or (let [ret {}]
        (doseq [m ms]
          (doseq [[k v] m]
            (set! (get* ret k) v)))
        ret)
      {}))

(defn select-keys
  "Returns a map containing only those entries in map whose key is in keys"
  [m ks]
  (let [m1 {}]
    (doseq [k ks]
      (if (.hasOwnProperty m k)
        (set! (get* m1 k) (get* m k))))
    m1))

(defn keys
  "Returns a sequence of the map's keys."
  [m]
  (for [[k v] m] k))

(defn vals
  "Returns a sequence of the map's values."
  [m]
  (for [[k v] m] v))

(defn v=*
  "Checks if two vectors are equal."
  [x y]
  (if (=== (count x) (count y))
    (loop [a x b y c (count x)]
      (if (=== 0 c) ;;empty vectors
        true
        (if (=* (first a) (first b))
          (recur (rest a) (rest b) (dec c))
          false)))
    false))

(defn m=*
  "Checks if two maps are equal."
  [x y]
  (let [xkeys (.sort (keys x))]
    (if (=* xkeys (.sort (keys y)))
      ;;keys-equal
      (loop [ks xkeys c (count xkeys)]
        (if (=* (get* x (first ks))
                (get* y (first ks)))
          (if (=== 0 c)
            true
            (recur (rest ks) (dec c)))
          false))
      false)))

(defn =*
  "Two argument version of =."
  [x y]
  (if (=== x y)
    true
    (if (=== (type x) (type y))
      (cond
       (vector? x)
       (v=* x y)

       (map? x)
       (m=* x y)

       :else
       false)
      ;; non comparable types
      false)))

(defn =
  ([]    true)
  ([x]   true)
  ([x y] (=* x y))
  ([a b & c]
     (loop [x a y b more c]
       (if (=* x y)
         (if (next more)
           (recur y (first more) (next more))
           (=* y (first more)))
         false))))
(defn not=
  "Same as (not (= obj1 obj2))"
  ([x] false)
  ([x y] (not (= x y)))
  ([& args]
     (not (apply = args))))

(defn +
  "Returns the sum of nums. (+) returns 0."
  [& args]
  (reduce #(+ %1 %2) 0 args))

(defn -
  "If no ys are supplied, returns the negation of x, else subtracts
  the ys from x and returns the result."
  [& args]
  (reduce #(- %1 %2) 0 args))

(defn *
  "Returns the product of nums. (*) returns 1."
  [& args]
  (reduce #(* %1 %2) 1 args))

(fn identity
  [x] x)

(fn reverse [x] (.reverse (.slice x 0)))
(fn compare [x y]
  (cond
   (=== x y)
   0
   (> x y)
   1
   (< x y)
   -1))
(fn zero? [x] (=== 0 x))

(fn int [x]
  (if (number? x) (bit-or x 0)))

(def max Math.max)
(def min Math.min)
(fn pos? [x]
  (and (number? x) (> x 0)))
(fn neg? [x]
  (and (number? x) (< x 0)))
(fn rand
  ([]  (Math.random))
  ([n] (* n (Math.random))))
(fn quot [x y]
  (int (/ x y)))
(fn integer? [n]
  (=== n (int n)))
(fn even? [n]
  (= 0 (rem n 2)))
(fn odd? [n]
  (= 1 (rem n 2)))
(fn complement [f]
  (fn [& args] (not (apply f args))))
(fn constantly [x]
  (fn [] x))
(fn peek [x]
  (if (nil? x) nil (get* x 0)))
(fn pop [x]
  (if (nil? x) nil (.slice x 1)))
(fn conj [coll & xs]
  (if (empty? coll)
    xs
    (let [ret (.slice coll 0)]
      (.concat ret xs))))
(fn cons [x coll]
  (.concat [x] coll))
(fn assoc [m k v]
  (let [ret (merge m {})]
    (set! (get* ret k) v)
    ret))
(fn dissoc [m & ks]
  (let [ret (merge m {})]
    (doseq [k ks]
      (delete (get* ret k)))
    ret))
(fn find [m k]
  (if (contains? m k)
    [k (get* m k)]
    nil))
(fn every?
  [pred coll]
  (cond
   (empty? coll) true
   (pred (first coll)) (every? pred (next coll))
   :else false))
(fn some
  [pred coll]
  (when coll
    (or (pred (first coll))
        (some pred (next coll)))))

(fn concat
  ([] [])
  ([x] [x])
  ([x y] (.concat x y))
  ([x & xs] (concat x (apply concat xs))))

(fn mapcat
  [f coll]
  (apply concat (map f coll)))
(fn drop
  [n coll]
  (when (pos? n)
    (when-let [s coll]
      (.slice s n))))
(fn take
  [n coll]
  (when (pos? n)
    (when-let [s coll]
      (.slice s 0 n))))

(defn Cl2Set
  "Constructor for set data type."
  [coll]
  (let [that this]
    (doseq [k coll]
      (set! (get* that k) true))))

(defn hash-set
  "Returns a new hash set with supplied keys."
  [& ks]
  (Cl2Set. ks))

(defn set
  "Returns a set of the distinct elements of coll."
  [coll]
  (Cl2Set. coll))

(fn sort
  ([coll]
     (.sort (Array.prototype.slice.call coll 0)))
  ([comp x]
     (.sort (Array.prototype.slice.call x 0) comp)))
(fn take-while [pred coll]
  (when-let [s coll]
    (when (pred (first s))
      (conj (take-while pred (rest s)) (first s)))))

(fn drop-last [n coll]
  (.slice coll 0 (- coll.length n)))

(fn take-last [n coll]
  (.slice coll (- coll.length n)))

(fn drop-while [pred coll]
  [pred coll]
  (let [step (fn [pred coll]
               (let [s coll]
                 (if (and s (pred (first s)))
                   (step pred (rest s))
                   s)))]
    (step pred coll)))
(fn cycle [coll n]
  (loop [ret [] n n]
    (if (zero? n)
      ret
      (recur (.concat ret coll) (dec n)))))
(fn split-at [n coll]
  [(take n coll) (drop n coll)])
(fn repeat [n x]
  (loop [ret [] n n]
    (if (zero? n)
      ret
      (recur (conj ret x) (dec n)))))
(fn iterate [f x n]
  (def ret [])
  (cons x (loop [v x i (dec n)]
            (if (zero? i)
              ret
              (recur (let [val (f v)] (ret.push val) val)
                     (dec i))))))
(fn split-with [pred coll]
  [(take-while pred coll) (drop-while pred coll)])

(fn zipmap
  [keys vals]
  (def map {})
  (loop [ks keys
         vs vals]
    (if (and ks vs)
      (do (set! (get* map (first ks)) (first vs))
          (recur (next ks)
                 (next vs)))
      map)))

(fn nthnext
  [coll n]
  (loop [n n xs coll]
    (if (and xs (pos? n))
      (recur (dec n) (next xs))
      xs)))
(fn nthrest
  [coll n]
  (loop [n n xs coll]
    (if (and xs (pos? n))
      (recur (dec n) (rest xs))
      xs)))
(fn rand-int [n] (int (rand n)))
(fn rand-nth
  [coll]
  (nth coll (rand-int (count coll))))
(fn range*
  [start end step]
  (let [ret []
        comp (if (pos? step) #(< %1 %2) #(> %1 %2))]
    (loop [i start]
      (if (comp i end)
        (do
          (.push ret i)
          (recur (+ i step)))
        (if (comp i end)
          (cons ret (range* i end step))
          ret)))))
(fn range
  ([end] (range* 0 end 1))
  ([start end] (range* start end 1))
  ([start end step] (range* start end step)))
(fn partition
  ([n coll] (partion n n coll))
  ([n step coll]
     (when-let [s coll]
       (when-let [p (take n s)]
         (if (= n (count p))
           (cons p (partition n step (nthrest s step)))
           []))))
  ([n step pad coll]
     (when-let [s coll]
       (when-let [p (take n s)]
         (if (= n (count p))
           (cons p (partition n step pad (nthrest s step)))
           [(take n (concat p pad))])))))
(defn subs
  "Returns the substring of s beginning at start inclusive, and ending
  at end (defaults to length of string), exclusive."
  [s start end]
  (.slice s start end))

(def println console.log)

(defn trampoline
  "trampoline can be used to convert algorithms requiring mutual
  recursion without stack consumption. Calls f with supplied args, if
  any. If f returns a fn, calls that fn with no arguments, and
  continues to repeat, until the return value is not a fn, then
  returns that non-fn value. Note that if you want to return a fn as a
  final value, you must wrap it in some data structure and unpack it
  after trampoline returns."
  ([f]
     (loop [ret (f)] (if (fn? ret) (recur (ret)) ret)))
  ([f & args]
     (trampoline #(apply f args))))
