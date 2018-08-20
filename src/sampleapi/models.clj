(ns sampleapi.models
  "Data models used by SampleAPI. Includes clojure.spec definitions and JodaTime formatter definitions."
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clj-time.format :as tf]
            [clj-time.spec :as ts]
            [clojure.data.csv :as csv])
  (:import org.joda.time.DateTime
           org.joda.time.DateTimeZone
           java.io.StringWriter))

(s/def ::LastName string?)
(s/def ::FirstName string?)
(s/def ::Gender (s/spec #{"Male" "Female"}))
(s/def ::FavoriteColor string?)

(def formatter
  "JodaTime formatter for the requested format"
  (tf/formatter "M/d/yyyy"))

(defn date-string?
  [s]
  (try
    (tf/parse formatter s)
    (catch Exception _)))

(s/def ::DateOfBirth (s/with-gen (s/and string? (comp boolean date-string?))
                       #(gen/fmap (fn [ms]
                                    (tf/unparse
                                     formatter
                                     (DateTime. ms DateTimeZone/UTC)))
                                  (ts/*period*))))

(s/def ::person (s/keys :req-un [::LastName
                                 ::FirstName
                                 ::Gender
                                 ::FavoriteColor
                                 ::DateOfBirth]))

(s/def ::persons (s/coll-of ::person))

(def atom? (partial instance? clojure.lang.Atom))

(s/def ::db
  (s/with-gen
    (s/and atom?
           (comp coll? deref))
    #(gen/fmap (fn [coll]
                 (atom coll))
               (s/gen ::persons))))

(defn maps->csv-prep
  "Turns a sequence of maps into a sequence of vectors appropriate for csv"
  [rows]
  (when (seq rows)
    (let [keys (->> (reduce (fn [s xs]
                              (into s xs))
                            #{}
                            (map keys rows))
                    (into []))
          lookups (apply juxt keys)]
      (cons (mapv name keys)
            (map lookups rows)))))

(s/fdef maps->csv-prep
  :args (s/cat :rows (s/coll-of (s/map-of keyword? any?)))
  :ret (s/or :happy (s/coll-of (s/coll-of any?))
             :empty nil?)
  :fn (fn [{:keys [args ret]}]
        (let [r (-> ret second)]
          (when (some? r)
            (= (count args) (count r))))))

(defn write-csv-data-to-string
  [data separator]
  (let [writer (StringWriter.)]
    (csv/write-csv writer data :separator separator)
    (str writer)))

(comment

  (require '[orchestra.spec.test :as stest])
  (stest/instrument)

  (require '[clojure.data.csv :as csv])
  (require '[clojure.java.io :as io])

  (->> (gen/sample (s/gen ::persons) 1)
       first
       maps->csv-prep)

  (defn generate-test-file
    [filename separator]
    (with-open [writer (io/writer filename)]
      (-> (gen/sample (s/gen ::person) 100)
          maps->csv-prep
          (as-> data (csv/write-csv writer data :separator separator)))))

  (generate-test-file "resources/test.csv" \,)

  (generate-test-file "resources/test.psv" \|)

  (generate-test-file "resources/test.ssv" \space)

  )

