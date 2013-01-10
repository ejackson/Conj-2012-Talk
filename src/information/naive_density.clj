(ns information.naive-density
  (:require
   [incanter.core   :as i]
   [incanter.charts :as c]))

;; Note:  This ns is mildly hacked to make it easy to demo.  It will only work
;; distributions whos domain is in the range *min-value* -> *max-value*, which is
;; kinda gross, but its a start.

;; Doing everything with boxed numerics to keep the intent clear.  The path ahead
;; is to profile, find the hotspots and convert to natives, if the copy cost of
;; moving to an array is not prohibitive.

;; -----------------------------------------------------------------------------
;;  Density Functions.

;; Define the support of my density.  Dodgy for easy demo.
(def *max-value* 100)
(def *min-value* 0)

(defn- support
  "Return a density with entries from 0 to 100, initialised to epsilon"
  []
  (zipmap (range *max-value*) (repeat 1e-20)))

(defn- normalise
  "Return a density density from a counts density"
  [hist]
  (let [c (apply + (vals hist))]
    (reduce (fn [m [k v]] (assoc m k (/ v c))) {} hist)))

(defn- round-down [x]
  (int (Math/floor x)))

;; -------------------------------------------------------------------------------
;; Interface

(defn- calculate-density
  "Take a seq of numbers between 0.0 and 100.0 and return a density of the integers
   over that range"
  [xs]
  {:pre [(>= (apply min xs) *min-value*)
         (<= (apply max xs) *max-value*)]}
  (->> xs
       (map round-down)
       frequencies
       normalise
       (merge (support))
       (into (sorted-map))))

;; One might be tempted to get flashy with Protocols here.... resist that.
(defn density [xs]
  (if (map? xs) xs
      (calculate-density xs)))

(defn view
  "Quick and dirty view on the density"
  [x]
  (do
    (i/view (c/line-chart (keys x) (vals x)))
    x))


(comment
  (use 'clojure.pprint)
  (pprint (density [1 1 1 3 3 3]))
  (view (density [1 1 1 4 4 4]))

  (use 'incanter.stats)
  (-> (sample-normal 1e5 :mean 50 :sd 10)
      density
      view)
  )
