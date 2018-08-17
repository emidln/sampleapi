(ns sampleapi.settings
  "Settings, specification, and defaults for SampleAPI"
  (:require [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]
            [spec.settings :as ss]))

(def settings-config {:http-host {:spec spec/string?
                                  :doc "Host to run our http service on (default: localhost)"
                                  :default "localhost"}
                      :http-port {:spec spec/integer?
                                  :doc "Port number to run our http service on (default: 8080)"
                                  :default 8080}})

(s/def ::settings (ss/settings-spec ::settings settings-config))

(defn validate-settings!
  "A thunk for easy settings resolution."
  []
  (ss/validate-settings! ::settings (ss/all-settings settings-config "sampleapi")))

