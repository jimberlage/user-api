(ns user-api.records-test
  (:require [clj-time.format :as f]
            [clojure.test :refer :all]
            [user-api.records :refer :all]))

(def date-format
  (f/formatter "MM/dd/yyyy"))

(def records
  [{:last-name "Dalton",    :first-name "Waylon",   :gender :male,   :favorite-color "red",    :date-of-birth (f/parse date-format "07/09/1989")}
   {:last-name "Henderson", :first-name "Justine",  :gender :female, :favorite-color "green",  :date-of-birth (f/parse date-format "10/21/1976")}
   {:last-name "Lang",      :first-name "Abdullah", :gender :male,   :favorite-color "blue",   :date-of-birth (f/parse date-format "12/14/1982")}
   {:last-name "Cruz",      :first-name "Marcus",   :gender :male,   :favorite-color "red",    :date-of-birth (f/parse date-format "01/14/1965")}
   {:last-name "Cobb",      :first-name "Thalia",   :gender :female, :favorite-color "red",    :date-of-birth (f/parse date-format "08/30/1990")}
   {:last-name "Little",    :first-name "Mathias",  :gender :male,   :favorite-color "yellow", :date-of-birth (f/parse date-format "02/02/1981")}
   {:last-name "Randolph",  :first-name "Eddie",    :gender :male,   :favorite-color "orange", :date-of-birth (f/parse date-format "06/10/1989")}
   {:last-name "Walker",    :first-name "Angela",   :gender :female, :favorite-color "blue",   :date-of-birth (f/parse date-format "06/16/1973")}
   {:last-name "Shelton",   :first-name "Lia",      :gender :female, :favorite-color "yellow", :date-of-birth (f/parse date-format "09/15/1964")}
   {:last-name "Shaffer",   :first-name "Joanna",   :gender :female, :favorite-color "purple", :date-of-birth (f/parse date-format "10/31/1980")}])

(deftest test-sort-by-gender
  (let [expected-order ["Cobb" "Henderson" "Shaffer" "Shelton" "Walker" "Cruz" "Dalton" "Lang" "Little" "Randolph"]]
    (testing "It sorts the records by gender ascending, then last name ascending."
      (is (= (map :last-name (sort-by-gender records)) expected-order)))))

(deftest test-sort-by-date-of-birth
  (let [expected-order ["Shelton" "Cruz" "Walker" "Henderson" "Shaffer" "Little" "Lang" "Randolph" "Dalton" "Cobb"]]
    (testing "It sorts the records by date of birth ascending."
      (is (= (map :last-name (sort-by-date-of-birth records)) expected-order)))))

(deftest test-sort-by-last-name
  (let [expected-order ["Walker" "Shelton" "Shaffer" "Randolph" "Little" "Lang" "Henderson" "Dalton" "Cruz" "Cobb"]]
    (testing "It sorts the records by last name descending."
      (is (= (map :last-name (sort-by-last-name records)) expected-order)))))
