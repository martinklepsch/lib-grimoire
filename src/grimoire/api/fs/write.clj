(ns grimoire.api.fs.write
  "Filesystem datastore implementation of the Grimoire API."
  (:refer-clojure :exclude [isa?])
  (:require [grimoire.things :as t]
            [grimoire.api :as api]
            [grimoire.api.fs.impl :as impl]))

;; Interacting with the datastore - writing
;;--------------------------------------------------------------------
(defmethod api/write-meta :filesystem [config thing data]
  (let [thing  (t/ensure-thing thing)
        _      (assert thing)
        handle (impl/thing->meta-handle config thing)
        _      (assert handle)]
    (.mkdirs (.getParentFile handle))
    (spit handle (pr-str data))
    nil))

(defmethod api/write-notes :filesystem [config thing data]
  {:pre [(string? data)
         thing
         config
         (-> config :datastore :notes)]}
  (let [thing  (t/ensure-thing thing)
        _      (assert thing)
        handle (impl/thing->notes-handle config thing)
        _      (assert thing)]
    (.mkdirs (.getParentFile handle))
    (spit handle data)))

;; FIXME: add write-example

(defmethod api/write-related :filesystem [config thing related-things]
  (let [thing  (t/ensure-thing thing)
        _      (assert thing)
        _      (assert (t/def? thing))
        handle (impl/thing->related-handle config thing)
        _      (assert thing)]
    (.mkdirs (.getParentFile handle))
    (doseq [thing related-things]
      (spit handle (str (t/thing->path thing) \newline)
            :append true))))
