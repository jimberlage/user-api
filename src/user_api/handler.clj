(ns user-api.handler
  (:require [clojure.data.json :as json]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [user-api.records :refer [date-aware-writer
                                      sort-by-gender
                                      sort-by-date-of-birth
                                      sort-by-last-name]]))

(defn records->json
  ""
  [records]
  (json/write-str records
                  ;; org.joda.time.DateTime objects need special handling.
                  :value-fn date-aware-writer))

(defn sort-handler
  ""
  [req sort-fn]
  {:status 200
   :body (records->json (sort-fn (:records @(:db req))))})

(defroutes app-routes
  (context "/records" req
    (POST "/" req)
    (GET "/gender" req (sort-handler req sort-by-gender))
    (GET "/birthdate" req (sort-handler req sort-by-date-of-birth))
    (GET "/name" req (sort-handler req sort-by-last-name)))
  (route/not-found "{\"error\":\"not found\"}"))

(defn wrap-db
  "wrap-db is some middleware to put the current handle to the database in each request."
  [handler db]
  #(handler (assoc % :db db)))

(defn wrap-response-content-type
  "wrap-response-content-type is some middleware to add Content-Type: application/json to our API responses."
  [handler]
  #(assoc-in (handler %) [:headers "Content-Type"] "application/json"))

(defn app
  [db]
  (-> app-routes
      (wrap-response-content-type)
      (wrap-db db)
      (wrap-defaults site-defaults)))
