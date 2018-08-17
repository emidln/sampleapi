(ns sampleapi.cli
  "CLI for SampleAPI. This loads a file specified as the first argument and then pretty prints a couple
   different sortings.

  Example:

    lein run -m sampleapi.cli my-input-file.csv

  "
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [sampleapi.core :as core])
  (:gen-class))

(defn -main [input-file]
  ;; load the data
  (let [db (atom [])]
    (with-open [f (io/input-stream input-file)]
      (core/load-char-separated-value-string db (slurp f)))
    ;; sort the data and display it
    (newline)
    (println "output 1: sorted by gender (females first), last name ascending")
    (pprint/print-table (core/output-1 @db))
    (newline)
    (println "output 2: sorted by DateOfBirth ascending")
    (pprint/print-table (core/output-2 @db))
    (newline)
    (println "output 3: sorted by LastName descending")
    (pprint/print-table (core/output-3 @db))))

(comment

  (let [db (atom [])]
    (newline)
    (println "csv")
    (let [db1 (do (-main "/home/bja/test.csv")
                  @db)]
      (reset! db [])
      (newline)
      (println "ssv")
      (let [db2 (do (-main "/home/bja/test.ssv")
                    @db)]
        (reset! db [])
        (newline)
        (println "psv")
        (let [db3 (do (-main "/home/bja/test.psv")
                      @db)]
          (= db1 db2 db3)))))

  )
