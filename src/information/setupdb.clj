(ns information.setupdb
  (:use [datomic.api :only [q db] :as d]))

;; -------------------------------------------------------------------
;;  Setup up the database
;; store database uri
(def uri "datomic:mem://entropy1")

;; create database
(d/create-database uri)

;; connect to database
(def conn (d/connect uri))

;; Define a ludicrously simple database of response times
(def schema-trxn
  [{:db/id #db/id[:db.part/db]
    :db/ident :response/server
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Which server produced this response"
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :response/time
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "How many milliseconds did it take"
    :db.install/_attribute :db.part/db}])

;; Store the schema transaction
(d/transact conn schema-trxn)

;; Seed the data
(def data-tx (read-string (slurp "test.dtm")))
(def a @(d/transact conn data-tx))
