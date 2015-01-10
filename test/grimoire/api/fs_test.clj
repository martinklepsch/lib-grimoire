(ns grimoire.api.fs-test
  (:require [grimoire.api :as api]
            [grimoire.things :as t]
            [grimoire.either :refer [result]]
            [grimoire.api.fs.read]
            [grimoire.api.fs.write]
            [clojure.test :refer :all]))

(def test-config
  {:datastore
   {:docs  "resources/test/docs/"
    :notes "resources/test/notes/"
    :mode  :filesystem}})

;; Listing tests
;;------------------------------------------------------------------------------

(deftest list-groups-test
  (let [groups (-> test-config
                  api/list-groups
                  result)]
    (is (= ["org.bar" "org.foo"]
           (sort (map :name groups))))))

(deftest list-artifacts-test
  (let [g       (t/->Group "org.foo")
        members (-> test-config
                   (api/list-artifacts g)
                   result)]
    (is (= ["a" "b"]
           (sort (map :name members))))))

(deftest list-versions-test
  (let [g        (t/->Group "org.foo")
        a        (t/->Artifact g "a")
        versions (-> test-config
                    (api/list-versions a)
                    result)]
    (is (= ["0.1.0" "0.1.0-SNAPSHOT" "1.0.0" "1.0.1" "1.1.0"]
           (sort (map :name versions))))))

(deftest list-ns-test
  (let [v   (t/->Version "org.foo" "a" "1.0.0")
        nss (-> test-config
               (api/list-namespaces v)
               result)]
    (is (= ["a.core" "a.impl.clj" "a.impl.cljs"]
           (sort (map :name nss))))))

(deftest list-prior-versions-test
  (let [ns   (t/->Ns "org.foo" "a" "1.0.0" "a.core")
        defs (-> test-config
                (api/thing->prior-versions ns)
                result)]
    (is (= ["org.foo/a/0.1.0-SNAPSHOT/a.core"
            "org.foo/a/0.1.0/a.core"
            "org.foo/a/1.0.0/a.core"]
           (sort (map :uri defs))))))

(deftest list-def-test
  (let [ns   (t/->Ns "org.foo" "a" "1.0.0" "a.core")
        defs (-> test-config
                (api/list-defs ns)
                result)]
    (is (= ["foo" "qux"]
           (sort (map :name defs))))))

;; Reading/Writing tests
;;------------------------------------------------------------------------------

(def p  ["org.bar" "b" "1.0.0" "not-qux" "c"])

(deftest read-write-meta-test
  (let [n  (rand-int Integer/MAX_VALUE)
        ps (take 5 (iterate butlast p))]
    (doseq [p ps]
      (let [path  (apply str (interpose "/" p))
            thing (t/path->thing path)
            meta  {:val n}]
        (api/write-meta test-config thing meta)
        (is (= (-> test-config
                  (api/read-meta thing)
                  result)
               meta))))))

(deftest read-write-notes-test
  (let [n  "Some test notes"
        ps (take 3 (iterate butlast p))]
    (doseq [p ps]
      (let [path  (apply str (interpose "/" p))
            thing (t/path->thing path)]
        (api/write-notes test-config thing n)
        (is (= n
               (-> test-config
                  (api/read-notes thing)
                  result
                  first
                  second)))))))