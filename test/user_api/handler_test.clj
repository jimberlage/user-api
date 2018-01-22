(ns user-api.handler-test
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [user-api.handler :refer :all]
            [user-api.records :refer [parse-record]]
            [user-api.test-helpers :refer [records]]))

(deftest test-app
  (let [db (atom {:records records})
        handler (app db)]

    (testing "POST /records"
      (let [;; We use a new db atom for this one, so that it doesn't pollute the read-only API route tests.
            db (atom {:records records})
            handler (app db)
            new-record ["Myself" "Me" "M" "deep lavender" "11/09/1987"]
            new-record* (parse-record new-record)
            response (handler (-> (mock/request :post "/records")
                                  (mock/json-body {:line (str/join "|" new-record)
                                                   :separator "pipe"})))]
        (is (= (:status response) 201))
        (is (= (get-in response [:headers "content-type"]) "application/json"))
        (is (= (:body response) "[{\"last-name\":\"Myself\",\"first-name\":\"Me\",\"gender\":\"M\",\"favorite-color\":\"deep lavender\",\"date-of-birth\":\"11\\/09\\/1987\"}]"))
        (is (= (:records @db) (conj records new-record*)))))

    (testing "GET /records/gender"
      (let [response (handler (mock/request :get "/records/gender"))]
        (is (= (:status response) 200))
        (is (= (get-in response [:headers "content-type"]) "application/json"))
        (is (= (:body response) "[{\"last-name\":\"Cobb\",\"first-name\":\"Thalia\",\"gender\":\"F\",\"favorite-color\":\"red\",\"date-of-birth\":\"08\\/30\\/1990\"},{\"last-name\":\"Henderson\",\"first-name\":\"Justine\",\"gender\":\"F\",\"favorite-color\":\"green\",\"date-of-birth\":\"10\\/21\\/1976\"},{\"last-name\":\"Shaffer\",\"first-name\":\"Joanna\",\"gender\":\"F\",\"favorite-color\":\"purple\",\"date-of-birth\":\"10\\/31\\/1980\"},{\"last-name\":\"Shelton\",\"first-name\":\"Lia\",\"gender\":\"F\",\"favorite-color\":\"yellow\",\"date-of-birth\":\"09\\/15\\/1964\"},{\"last-name\":\"Walker\",\"first-name\":\"Angela\",\"gender\":\"F\",\"favorite-color\":\"blue\",\"date-of-birth\":\"06\\/16\\/1973\"},{\"last-name\":\"Cruz\",\"first-name\":\"Marcus\",\"gender\":\"M\",\"favorite-color\":\"red\",\"date-of-birth\":\"01\\/14\\/1965\"},{\"last-name\":\"Dalton\",\"first-name\":\"Waylon\",\"gender\":\"M\",\"favorite-color\":\"red\",\"date-of-birth\":\"07\\/09\\/1989\"},{\"last-name\":\"Lang\",\"first-name\":\"Abdullah\",\"gender\":\"M\",\"favorite-color\":\"blue\",\"date-of-birth\":\"12\\/14\\/1982\"},{\"last-name\":\"Little\",\"first-name\":\"Mathias\",\"gender\":\"M\",\"favorite-color\":\"yellow\",\"date-of-birth\":\"02\\/02\\/1981\"},{\"last-name\":\"Randolph\",\"first-name\":\"Eddie\",\"gender\":\"M\",\"favorite-color\":\"orange\",\"date-of-birth\":\"06\\/10\\/1989\"}]"))))

    (testing "GET /records/birthdate"
      (let [response (handler (mock/request :get "/records/birthdate"))]
        (is (= (:status response) 200))
        (is (= (get-in response [:headers "content-type"]) "application/json"))
        (is (= (:body response) "[{\"last-name\":\"Shelton\",\"first-name\":\"Lia\",\"gender\":\"F\",\"favorite-color\":\"yellow\",\"date-of-birth\":\"09\\/15\\/1964\"},{\"last-name\":\"Cruz\",\"first-name\":\"Marcus\",\"gender\":\"M\",\"favorite-color\":\"red\",\"date-of-birth\":\"01\\/14\\/1965\"},{\"last-name\":\"Walker\",\"first-name\":\"Angela\",\"gender\":\"F\",\"favorite-color\":\"blue\",\"date-of-birth\":\"06\\/16\\/1973\"},{\"last-name\":\"Henderson\",\"first-name\":\"Justine\",\"gender\":\"F\",\"favorite-color\":\"green\",\"date-of-birth\":\"10\\/21\\/1976\"},{\"last-name\":\"Shaffer\",\"first-name\":\"Joanna\",\"gender\":\"F\",\"favorite-color\":\"purple\",\"date-of-birth\":\"10\\/31\\/1980\"},{\"last-name\":\"Little\",\"first-name\":\"Mathias\",\"gender\":\"M\",\"favorite-color\":\"yellow\",\"date-of-birth\":\"02\\/02\\/1981\"},{\"last-name\":\"Lang\",\"first-name\":\"Abdullah\",\"gender\":\"M\",\"favorite-color\":\"blue\",\"date-of-birth\":\"12\\/14\\/1982\"},{\"last-name\":\"Randolph\",\"first-name\":\"Eddie\",\"gender\":\"M\",\"favorite-color\":\"orange\",\"date-of-birth\":\"06\\/10\\/1989\"},{\"last-name\":\"Dalton\",\"first-name\":\"Waylon\",\"gender\":\"M\",\"favorite-color\":\"red\",\"date-of-birth\":\"07\\/09\\/1989\"},{\"last-name\":\"Cobb\",\"first-name\":\"Thalia\",\"gender\":\"F\",\"favorite-color\":\"red\",\"date-of-birth\":\"08\\/30\\/1990\"}]"))))

    (testing "GET /records/name"
      (let [response (handler (mock/request :get "/records/name"))]
        (is (= (:status response) 200))
        (is (= (get-in response [:headers "content-type"]) "application/json"))
        (is (= (:body response) "[{\"last-name\":\"Walker\",\"first-name\":\"Angela\",\"gender\":\"F\",\"favorite-color\":\"blue\",\"date-of-birth\":\"06\\/16\\/1973\"},{\"last-name\":\"Shelton\",\"first-name\":\"Lia\",\"gender\":\"F\",\"favorite-color\":\"yellow\",\"date-of-birth\":\"09\\/15\\/1964\"},{\"last-name\":\"Shaffer\",\"first-name\":\"Joanna\",\"gender\":\"F\",\"favorite-color\":\"purple\",\"date-of-birth\":\"10\\/31\\/1980\"},{\"last-name\":\"Randolph\",\"first-name\":\"Eddie\",\"gender\":\"M\",\"favorite-color\":\"orange\",\"date-of-birth\":\"06\\/10\\/1989\"},{\"last-name\":\"Little\",\"first-name\":\"Mathias\",\"gender\":\"M\",\"favorite-color\":\"yellow\",\"date-of-birth\":\"02\\/02\\/1981\"},{\"last-name\":\"Lang\",\"first-name\":\"Abdullah\",\"gender\":\"M\",\"favorite-color\":\"blue\",\"date-of-birth\":\"12\\/14\\/1982\"},{\"last-name\":\"Henderson\",\"first-name\":\"Justine\",\"gender\":\"F\",\"favorite-color\":\"green\",\"date-of-birth\":\"10\\/21\\/1976\"},{\"last-name\":\"Dalton\",\"first-name\":\"Waylon\",\"gender\":\"M\",\"favorite-color\":\"red\",\"date-of-birth\":\"07\\/09\\/1989\"},{\"last-name\":\"Cruz\",\"first-name\":\"Marcus\",\"gender\":\"M\",\"favorite-color\":\"red\",\"date-of-birth\":\"01\\/14\\/1965\"},{\"last-name\":\"Cobb\",\"first-name\":\"Thalia\",\"gender\":\"F\",\"favorite-color\":\"red\",\"date-of-birth\":\"08\\/30\\/1990\"}]"))))

    (testing "not-found route"
      (let [response (handler (mock/request :get "/invalid"))]
        (is (= (:status response) 404))))))
