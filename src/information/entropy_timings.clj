(ns information.entropy-timings
  (:require
   [clatrix.core :as m]
   [clojure.core.reducers :as r]
   [criterium.core :as crit]))

(m/rnorm 5 5)

;; Get a 1Mn random numbers to calculate entropy over
(def ps (vec (repeatedly 1e6 rand)))



;; -----------------------------------------------------------------------------
;; A basic inner fn to calc entropy
(defn entropy [p]
  (* p (Math/log p)))

(crit/bench (* -1 (reduce + (map entropy ps))))

;; Evaluation count : 1320 in 60 samples of 22 calls.
;; Execution time mean : 49.936724 ms
;; Execution time std-deviation : 3.136605 ms
;; Execution time lower quantile : 46.515181 ms ( 2.5%)
;; Execution time upper quantile : 58.345592 ms (97.5%)

;; Found 2 outliers in 60 samples (3.3333 %)
;; low-severe    2 (3.3333 %)
;; Variance from outliers : 46.7913 % Variance is moderately inflated by outliers



;; -----------------------------------------------------------------------------
;; Use native arrays
(defn array-entropy ^double [^doubles aps]
  (areduce aps i res 0.0 (+ res (entropy (aget aps i)))))

(let [pps (double-array ps)]
  (crit/bench (array-entropy pps)))

;; Evaluation count : 2460 in 60 samples of 41 calls.
;; Execution time mean : 24.764204 ms
;; Execution time std-deviation : 101.913508 us
;; Execution time lower quantile : 24.576306 ms ( 2.5%)
;; Execution time upper quantile : 24.969607 ms (97.5%)





;; -----------------------------------------------------------------------------
;;  Reducers FTW
(crit/bench (r/fold + (r/map entropy ps)))

;; Evaluation count : 7800 in 60 samples of 130 calls.
;; Execution time mean : 7.795341 ms
;; Execution time std-deviation : 130.344259 us
;; Execution time lower quantile : 7.614024 ms ( 2.5%)
;; Execution time upper quantile : 8.115075 ms (97.5%)

;; Found 3 outliers in 60 samples (5.0000 %)
;; low-severe    3 (5.0000 %)
;; Variance from outliers : 6.2527 % Variance is slightly inflated by outliers






;; -----------------------------------------------------------------------------
;; Using Clatrix
(def col-ps (m/column ps))
(crit/bench (* -1 (m/dot col-ps (m/log col-ps))))

;; Evaluation count : 2520 in 60 samples of 42 calls.
;; Execution time mean : 24.653773 ms
;; Execution time std-deviation : 1.278333 ms
;; Execution time lower quantile : 23.724101 ms ( 2.5%)
;; Execution time upper quantile : 27.400775 ms (97.5%)

;; Found 9 outliers in 60 samples (15.0000 %)
;; low-severe    8 (13.3333 %)
;; low-mild      1 (1.6667 %)
;; Variance from outliers : 36.9100 % Variance is moderately inflated by outliers




;; -----------------------------------------------------------------------------
(comment
  ;; Matlab -> 12ms
  p = rand(1e5,1);
  tic; p.*log(p); toc

  ;; R -> 60ms
  p <- runif(1e5, 0.1, 1);
  system.time(p %*% log(p))

  ;; Mathematica -> 7,600ms
  y = RandomVariate[UniformDistribution[], 10^5]
  AbsoluteTiming[Entropy[2, y] // N]


  )
