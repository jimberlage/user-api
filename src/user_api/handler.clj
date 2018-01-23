(ns user-api.handler
  (:require [clojure.data.csv :as csv]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-body]]
            [schema.core :as schema]
            [user-api.records :refer [check-record
                                      date-aware-writer
                                      parse-record
                                      sort-by-gender
                                      sort-by-date-of-birth
                                      sort-by-last-name]]))

(def separator-mapping
  "This is the canonical source for what separators we accept in lines of input."
  {"pipe"  \|
   "comma" \,
   "space" \space})

(defn records->json
  "records->json transforms a collection of records into a string representing valid JSON."
  [records]
  (json/write-str records
                  ;; org.joda.time.DateTime objects need special handling.
                  :value-fn date-aware-writer))

(defn sort-handler
  "sort-handler is a generic handler for GET requests which return a sorted list of records from the DB.
  
  Args:
    req: The ring request map.
    sort-fn: A function which takes a collection of records and sorts it, returning the sorted collection.
  
  Returns:
    A ring response map."
  [req sort-fn]
  {:status 200
   :body (records->json (sort-fn (:records @(:db req))))})

(defn ensure-json-content-type
  "ensure-json-content-type is middleware to ensure that only POST requests with a content-type of application/json are allowed."
  [handler]
  (fn [req]
    (if (= (get-in req [:headers "content-type"]) "application/json")
      (handler req)
      {:status 400
       :body (json/write-str {:error-type :validation
                              :message "Content-Type must be application/json"})})))

(def PostRecordsParameters
  {(schema/required-key "line") schema/Str
   (schema/required-key "separator") (apply schema/enum (keys separator-mapping))})

(def check-post-record-parameters
  (schema/checker PostRecordsParameters))

(defn post-record-handler
  "post-record-handler is a handler for adding a new record to the DB.

  It assumes that the body of the request has already been parsed.

  Args:
    req: The ring request map.

  Returns:
    A ring response map."
  [req]
  (if-let [validation-errors (check-post-record-parameters (:body req))]
    {:status 400
     :body (json/write-str {:error-type :validation
                            :message (pr-str validation-errors)})}
    (let [separator (get separator-mapping (get-in req [:body "separator"]))
          new-record (-> (get-in req [:body "line"])
                         (csv/read-csv :separator separator)
                         (first)
                         (parse-record))]
      (if (check-record new-record)
        (let [error new-record]
          (if (= :validation (:error-type error))
            ;; Return validation errors to the user, so they know what went wrong and how to fix it.
            {:status 400, :body (json/write-str error)}
            (do
              ;; Only log the real error, so we don't leak internal data to the API consumer.
              (println error)
              {:status 500, :body (json/write-str {:error "An unknown error occurred"})})))
        (do
          ;; Update the database.
          (swap! (:db req) update :records conj new-record)
          {:status 201, :body (records->json [new-record])})))))

(defroutes app-routes
  (context "/records" req
    (POST "/" req
      (-> post-record-handler
          wrap-json-body
          ensure-json-content-type))
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
  #(assoc-in (handler %) [:headers "content-type"] "application/json"))

(defn app-handler
  "app-handler is the main handler for our API."
  [db]
  (-> app-routes
      (wrap-response-content-type)
      (wrap-db db)))
