(ns user-api.records-test
  (:require [clojure.test :refer :all]
            [user-api.records :refer :all]
            [user-api.test-helpers :refer [records records->string with-tmp-file]]))

(deftest test-parse-records
  (let [field-order [:last-name :first-name :gender :favorite-color :date-of-birth]]
    (testing "with a pipe separator"
      (let [sep \|
            lines (records->string records sep)]

        (testing "it correctly parses the records from an input string"
          (is (= (parse-records lines sep) {:records records, :errors []})))

        (testing "it correctly parses the records from an input file"
          (with-tmp-file
            #(is (= (parse-records % sep) {:records records, :errors []}))
            lines))))

    (testing "with a comma separator"
      (let [sep \,
            lines (records->string records sep)]

        (testing "it correctly parses the records from an input string"
          (is (= (parse-records lines sep) {:records records, :errors []})))

        (testing "it correctly parses the records from an input file"
          (with-tmp-file
            #(is (= (parse-records % sep) {:records records, :errors []}))
            lines))))

    (testing "with a space separator"
      (let [sep \space
            lines (records->string records sep)]

        (testing "it correctly parses the records from an input string"
          (is (= (parse-records lines sep) {:records records, :errors []})))

        (testing "it correctly parses the records from an input file"
          (with-tmp-file
            #(is (= (parse-records % sep) {:records records, :errors []}))
            lines))))

    (testing "it handles errors in individual records"
      (let [sep \space
            lines (records->string records sep)
            lines (str lines "\nBerlage Jim C fuschia 08/21/1973\nBerlage Joey M orange 02451992")]
        (is (= (parse-records lines sep)
               {:records records
                :errors [{:error-type :validation, :message "[nil nil (named (not (#{\"M\" \"F\"} \"C\")) :gender) nil nil]"}
                         {:error-type :validation, :message "Cannot parse 02451992 as a date"}]}))))))

(deftest test-sort-by-gender
  (testing "it sorts the records by gender ascending, then last name ascending."
    (let [expected-order ["Cobb" "Henderson" "Shaffer" "Shelton" "Walker" "Cruz" "Dalton" "Lang" "Little" "Randolph"]]
      (is (= (map :last-name (sort-by-gender records)) expected-order)))))

(deftest test-sort-by-date-of-birth
  (testing "it sorts the records by date of birth ascending."
    (let [expected-order ["Shelton" "Cruz" "Walker" "Henderson" "Shaffer" "Little" "Lang" "Randolph" "Dalton" "Cobb"]]
      (is (= (map :last-name (sort-by-date-of-birth records)) expected-order)))))

(deftest test-sort-by-last-name
  (testing "it sorts the records by last name descending."
    (let [expected-order ["Walker" "Shelton" "Shaffer" "Randolph" "Little" "Lang" "Henderson" "Dalton" "Cruz" "Cobb"]]
      (is (= (map :last-name (sort-by-last-name records)) expected-order)))))
