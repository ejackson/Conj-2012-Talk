(ns information.dtmc
  (:use [datomic.api :only [q db] :as d]
        [clojure.pprint])
  (:require [information.entropy :as e]
            [information.naive-density :as density]))

;; -------------------------------------------------------------------
;; store database uri
(def uri "datomic:mem://entropy1")
(d/create-database uri)
(def conn (d/connect uri))


;; -------------------------------------------------------------------
;; Some closed questions.  Using aggregate functions
(q '[:find ?server-id (avg ?r)
     :where
     [?e :response/server ?server-id]
     [?e :response/time   ?r]]
   (db conn))

(q '[:find ?server-id (variance ?r)
     :where
     [?e :response/server ?server-id]
     [?e :response/time   ?r]]
   (db conn))


;; -------------------------------------------------------------------
(defn response-times
  ([]
     (flatten
      (seq
       (q '[:find ?r
            :where
            [_ :response/time ?r]]
          (db conn))))))

(defn to-baseline
  "Returns a function returning the relative entropy with respect to ps"
  [ps]
  (partial e/relative-entropy ps))

;; Somewhat nasty, but I need to take a name to pass to datomic
(def relative-to-population
  (to-baseline (response-times)))

;; Ask the *open* question
(q '[:find ?server-id (information.dtmc/relative-to-population ?r)
     :where
     [?e :response/server ?server-id]
     [?e :response/time   ?r]]
   (db conn))



;; Look at the distributions
(defn server-response-times
  "Query the response times for a particular any subset of servers, pass
   no args for all, 1 for that server, or multi for all of those."
  ([]
      (q '[:find ?r
           :where
           [_ :response/time   ?r]]
         (db conn)))
  ([server-id]
     (q '[:find ?r
          :in $ ?server-id
          :where
          [?e :response/server ?server-id]
          [?e :response/time   ?r]]
        (db conn) server-id))
  ([server-id & server-ids]
     (q '[:find ?r
          :in $ [?server-id ...]
          :where
          [?e :response/server ?server-id]
          [?e :response/time   ?r]]
        (db conn) (cons server-id server-ids))))

(comment
  (-> (server-response-times 3)
      seq
      flatten
      density/density
      density/view))
