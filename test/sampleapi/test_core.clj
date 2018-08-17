(ns sampleapi.test-core
  (:require [sampleapi.core :as core]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as st]
            [orchestra.spec.test :as stest]
            [clojure.test.check.clojure-test :refer [defspec]]
            [com.gfredericks.test.chuck.clojure-test :refer [for-all]]
            [sampleapi.models :as models]))

(deftest test-detect-separator
  (testing "positive detect-separator tests"
    (doseq [s [\| \, \space]]
      (is (= (core/detect-separator
              (->> (repeat 6 "foo")
                   (interpose s)
                   (apply str)))
             s))))
  (testing "negative detect-separator tests"
    (is (nil? (core/detect-separator "foo\tbar\baz")))))

(deftest test-csv-data->maps
  (testing "csv-data->maps happy path"
    (is (= (core/csv-data->maps [["foo" "bar" "baz"]
                                 ["quxf" "quxb" "quxz"]
                                 ["spam" "eggs" "bacon"]])
           [{:foo "quxf"
             :bar "quxb"
             :baz "quxz"}
            {:foo "spam"
             :bar "eggs"
             :baz "bacon"}])))
  (testing "csv-data->maps can be missing keys if not all data rows are long enough"
    (is (= (core/csv-data->maps [["foo" "bar" "baz"]
                                 ["quxf" "quxb"]
                                 ["spam" "eggs" "bacon"]])
           [{:foo "quxf"
             :bar "quxb"}
            {:foo "spam"
             :bar "eggs"
             :baz "bacon"}]))))

(deftest test-trim-csv-fields
  (testing "removes whitespace around cells"
    (is (= (core/trim-csv-fields [[" foo   " "bar    " " baz"]
                                  ["quxf    " "quxb     " "quxz    "]
                                  ["spam" " eggs  " "     bacon"]])
           [["foo" "bar" "baz"]
            ["quxf" "quxb" "quxz"]
            ["spam" "eggs" "bacon"]]))))

(deftest test-loading-records
  (testing "loading records against a nil db"
    (let [db (atom nil)
          _ (core/load-record db {:foo "spam" :bar "eggs"})]
      (is (= @db [{:foo "spam" :bar "eggs"}]))))
  (testing "loading records against an empty db"
    (let [db (atom [])
          _ (core/load-record db {:foo "spam" :bar "eggs"})]
      (is (= @db [{:foo "spam" :bar "eggs"}]))))
  (testing "loading records against a db with records"
    (let [db (atom [{:foo "spam" :bar "eggs"}])
          _ (core/load-record db {:foo "quxf" :bar "quxb"})]
      (is (= @db [{:foo "spam" :bar "eggs"}
                  {:foo "quxf" :bar "quxb"}])))))

(defspec round-trip-load-char-separated-value-string 100
  (testing "generating person models round trip to csv and back"
    (for-all [ps (s/gen ::models/persons)]
             (let [db (atom [])]
               (some-> ps
                       models/maps->csv-prep
                       (models/write-csv-data-to-string \,)
                       (->> (core/load-char-separated-value-string db)))
               (is (= @db ps))))))

(comment

  (require '[expound.alpha :as expound])
  (defn instrument [f]
    (set! s/*explain-out* expound/printer)
    (stest/instrument)
    (f))

  (use-fixtures :once instrument)

  )
