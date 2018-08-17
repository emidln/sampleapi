(ns sampleapi.core
  "Shared data manipulation for sampleapi. Separator detection, csv parsing, and output sorting is implemented
   along with a dummy data model that uses an atom."
  (:require [clojure.data.csv :as csv]
            [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [sampleapi.models :as models]
            [clojure.spec.alpha :as s]))

(defn detect-separator
  "Returns the character to use for a separator."
  [line]
  (->> (map #(some #{%} line) [\| \, \space])
       (drop-while nil?)
       first))

(s/fdef detect-separator
  :args (s/cat :line string?)
  :ret (s/or :matched char?
             :failed nil?))

(comment

  (= \| (detect-separator "LastName | FirstName | Gender | FavoriteColor | DateOfBirth"))

  (= \,(detect-separator "LastName, FirstName, Gender, FavoriteColor, DateOfBirth"))

  (= \space (detect-separator "LastName FirstName Gender FavoriteColor DateOfBirth"))

  )

(defn csv-data->maps
  "Transforms csv/parse-csv output from a sequence of vectors with the header at the front
  into a sequence of maps with each column keyed by the value at its position in the header."
  [csv-data]
  (map zipmap
       (->> (first csv-data)
            (map keyword)
            repeat)
       (rest csv-data)))

(s/fdef csv-data->maps
  :args (s/cat :csv-data (s/coll-of (s/coll-of string?)))
  :ret (s/coll-of (s/map-of keyword? string?)))

(def trim-csv-fields
  "Remove unnecessary whitespare around individual fields in csv/parse-csv output"
  (partial map #(mapv (partial str/trim) %)))

(def output-1
  "Sorts by gender (female first), last name asc"
  (partial sort-by (juxt :Gender :LastName)))

(def output-2
  "Sorts by Date of Birth ascending"
  (partial sort-by (comp (partial tf/parse models/formatter) :DateOfBirth) t/before?))

(def output-3
  "Sorts by Last Name descending"
  (partial sort-by :LastName #(compare %2 %1)))

(defn load-record
  "Loads a record into our datastore (an atom containing a vector)"
  [db record]
  (swap! db (fnil conj []) record))

(defn load-char-separated-value-string
  "Loads each record in the char-separated-value string into db."
  [db csv-string]
  (let [csv-header (first (str/split-lines csv-string))
        separator (detect-separator csv-header)]
    (->> (csv/read-csv csv-string :separator separator)
         trim-csv-fields
         csv-data->maps
         (map (partial load-record db))
         doall)))
