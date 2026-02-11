(ns bug.tuple
  (:require
   [nano-id.core :refer [nano-id]]
   [datahike.api :as d]
   [bug.db :refer [start-asset-db add-update-list get-list ]]
   ))


(def dbconn (start-asset-db "./db"))

(repeatedly 5 #(nano-id 8))

(defn make-list [name c]
           {:lists/name name
            :lists/asset (->> (repeatedly c #(nano-id 8))
                               (into []))
            })

(add-update-list dbconn (make-list "small" 5))

(-> (get-list dbconn "small")
    :lists/asset
    count) 
;; 5

(add-update-list dbconn (make-list "medium" 900))

(-> (get-list dbconn "medium")
    :lists/asset
    count) 
;; 900 

(add-update-list dbconn (make-list "big" 3150))

(-> (get-list dbconn "big")
    :lists/asset
    count)
;; 1000
;; ERROR - this should be 3150