(defproject sampleapi "0.1.0-SNAPSHOT"
  :description "Sample hybrid CLI/HTTP application"
  :url "https://github.com/emidln/sampleapi"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/spec.alpha "0.2.168"]
                 [org.clojure/data.csv "0.1.4"]
                 [clj-time "0.14.4"]
                 [metosin/compojure-api "2.0.0-alpha21"]
                 [metosin/spec-tools "0.7.1"]
                 [spec.settings "0.1.0"]
                 [org.immutant/web "2.1.10"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.10.0-alpha3"]
                                  [orchestra "2017.11.12-1"]
                                  [com.gfredericks/test.chuck "0.2.9"]
                                  [expound "0.7.1"]]}})

