(ns information.entropy
  (:require
   [clatrix.core              :as m]
   [information.naive-density :as d]
   [clojure.core.reducers     :as r]))

;; ---------------------------------------------------------
;;  The actual functions that calculate entropy. These are
;;  defined over maps representing Densities.
(defn- inner-entropy [p]
  (* p (Math/log p)))

(defn- bits
  "Convert from nats to bits"
  [x]
  (/ x (Math/log 2)))

;; Do this bit with reducers
(defn entropy
  "Entropy of an empirical distribution"
  [d]
  {:pre  [(every? number? d)]
   :post [(>= % 0)]}
  (bits
   (* -1
      (r/fold + (r/map inner-entropy (vals (d/density d)))))))

;; Do this with jBLAS, just for giggles
(defn relative-entropy
  "Cross entropy between two dbns p and q"
  [p q]
  {:pre  [(every? number? p)
          (every? number? q)]
   :post [(>= % 0)]}
  (let [ip (m/column (map double (vals (d/density p))))
        iq (m/column (map double (vals (d/density q))))]
    (bits
     (m/dot ip (m/log (m/div ip iq))))))






















(comment

  ;; Draw pics for preso
  (let [p (map (partial * 1E-3) (range 1 1001))]
    (save
     (line-chart p (map #(+ (* (* -1 %) (* (/ (Math/log %) (Math/log 2))))
                            (* (- % 1)  (* (/ (Math/log (- 1 %)) (Math/log 2)))))
                        p)
                 :title "Entropy (Unpredictability)"
                 :x-label "p"
                 :y-label "-p*log(p)")
     "/home/edmund/Resources/entropy.png"))

  (let [p (map (partial * 1E-3) (range 1 1001))]
    (save
     (line-chart p (map #(* -1 (* (/ (Math/log %) (Math/log 2))))
                        p)
                 :title "Self Information (Unexpectedness)"
                 :x-label "p"
                 :y-label "-p*log(p)")
     "/home/edmund/Resources/sinfo.png"))

  )
