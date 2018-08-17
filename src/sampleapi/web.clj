(ns sampleapi.web
  "HTTP Service for SampleAPI consisting of a Ring app and a -main to run an Undertow server with the app.

  Example:

    SAMPLEAPI_HTTP_HOST=localhost SAMPLEAPI_HTTP_PORT=8080 lein run -m sampleapi.web

  "
  (:require [compojure.api.sweet :refer [api GET POST]]
            [compojure.api.middleware :as mw]
            [immutant.web :as web]
            [ring.util.request :refer [body-string]]
            [sampleapi.core :as core]
            [sampleapi.models :as models]
            [sampleapi.settings :as settings])
  (:gen-class))

(def app (api
          ;; enable a swagger api console at /
          {:swagger {:ui "/"
                     :spec "/swagger.json"
                     :data {:info {:title "Sample People API"
                                   :description "A sample api for managing people."}
                            :tags []}}
           :coercion :spec}
          ;; no favicon; avoids a log message when the browser tries to use it
          (GET "/favicon.ico" []
            :no-doc true
            {:status 404 :body "Not Found"})
          ;; Records is a bit complicated since we need custom parsing of the request body
          ;; As such, we need to break out of compojure-api into ring-swagger directly
          (POST "/records" request
            :summary "Uploads a CSV, SSV, or PSV body containing person records"
            :swagger {:description (str "Body be a comma-, space-, or pipe, separated file with header "
                                        "and the following columns: LastName, FirstName, Gender, "
                                        "FavoriteColor, DateOfBirth. Column order does not matter, although "
                                        "must be consistent throughout the file.")
                      :consumes ["text/csv"
                                 "text/space-separated-values"
                                 "text/pipe-separated-values"]
                      :parameters {:body {:csv-data string?}}}
            ;; Use a 204 instead of a 201 since we don't have a good Location to link
            :responses {204 {:schema nil?}}
            :components [db]
            {:status 204
             :body (do (core/load-char-separated-value-string db (body-string request)) nil)})
          (GET "/records/gender" []
            :summary "Returns a list of person records ordered by Gender asc, then LastName asc"
            :responses {200 {:schema ::models/persons}}
            :components [db]
            {:status 200
             :body (core/output-1 @db)})
          (GET "/records/birthdate" []
            :summary "Returns a list of person records ordered by DateOfBirth asc"
            :responses {200 {:schema ::models/persons}}
            :components [db]
            {:status 200
             :body (core/output-2 @db)})
          (GET "/records/name"[]
            :summary "Returns a list of person records ordered by LastName desc"
            :responses {200 {:schema ::models/persons}}
            :components [db]
            {:status 200
             :body (core/output-3 @db)})))

(defn -main []
  ;; typically we'd have a SystemMap which would take care of finding the deps for the routes (like a db pool)
  ;; and injecting it into our handler using wrap-components. For simplicity sake, we'll use the idea that a
  ;; system is just a fancy map
  (let [settings (settings/validate-settings!)
        db (atom [])
        handler (-> app
                    (mw/wrap-components {:db db}))]
    (web/run handler {:host (:http-host settings)
                      :port (:http-port settings)})))
