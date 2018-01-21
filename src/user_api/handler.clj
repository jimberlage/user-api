(ns user-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (context "/records" req
    (POST "/" req)
    (GET "/gender" req)
    (GET "/birthdate" req)
    (GET "/name" req))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
