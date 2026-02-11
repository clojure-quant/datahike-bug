(ns bug.db
  (:require
   [datahike.api :as d]))

(def schema
  [{:db/ident :lists/name
    :db/unique :db.unique/identity ; name of list is our id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :lists/asset
    :db/valueType :db.type/tuple
    :db/tupleTypes [:db.type/long :db.type/string] ;; [idx value]
    :db/cardinality :db.cardinality/many}])

(defn start-asset-db [db-dir]
  (let [cfg {:store {:backend :file ; backends: in-memory, file-based, LevelDB, PostgreSQL
                     :path db-dir}
             :keep-history? false
             :schema-flexibility :write  ;default - strict value types need to be defined in advance. 
                  ;:schema-flexibility :read ; transact any  kind of data into the database you can set :schema-flexibility to read
             :initial-tx schema ; commit a schema
             }]
  ; create when not existing
  ; (.exists (io/file db-filename))
    (when-not (d/database-exists? cfg)
      (println "creating datahike db..")
    ;(d/delete-database cfg)
      (d/create-database cfg))
  ; connect
    (d/connect cfg)))







(defn tupelize-list [data]
  (update data :lists/asset #(into []
                                   (map-indexed (fn [idx asset]
                                                  [idx asset]) %))))

(defn untupelize-list [data]
  (update data :lists/asset #(into []
                                   (map (fn [[idx asset]]
                                          asset) %))))

(defn add-update-list
  [dbconn data]
  (if (map? data)
    (d/transact dbconn [(tupelize-list data)])
    (d/transact dbconn (tupelize-list data))))

(defn get-list [dbconn list-name]
  (-> '[:find [(pull ?id [*]) ...]
        :in $ ?list-name
        :where
        [?id :lists/name ?list-name]]
      (d/q @dbconn list-name)
      first
      untupelize-list))
