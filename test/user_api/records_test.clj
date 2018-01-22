(ns user-api.records-test
  (:require [clj-time.format :as f]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [user-api.records :refer :all]))

(def records
  [{:last-name "Dalton",    :first-name "Waylon",   :gender :M, :favorite-color "red",    :date-of-birth (f/parse date-format "07/09/1989")}
   {:last-name "Henderson", :first-name "Justine",  :gender :F, :favorite-color "green",  :date-of-birth (f/parse date-format "10/21/1976")}
   {:last-name "Lang",      :first-name "Abdullah", :gender :M, :favorite-color "blue",   :date-of-birth (f/parse date-format "12/14/1982")}
   {:last-name "Cruz",      :first-name "Marcus",   :gender :M, :favorite-color "red",    :date-of-birth (f/parse date-format "01/14/1965")}
   {:last-name "Cobb",      :first-name "Thalia",   :gender :F, :favorite-color "red",    :date-of-birth (f/parse date-format "08/30/1990")}
   {:last-name "Little",    :first-name "Mathias",  :gender :M, :favorite-color "yellow", :date-of-birth (f/parse date-format "02/02/1981")}
   {:last-name "Randolph",  :first-name "Eddie",    :gender :M, :favorite-color "orange", :date-of-birth (f/parse date-format "06/10/1989")}
   {:last-name "Walker",    :first-name "Angela",   :gender :F, :favorite-color "blue",   :date-of-birth (f/parse date-format "06/16/1973")}
   {:last-name "Shelton",   :first-name "Lia",      :gender :F, :favorite-color "yellow", :date-of-birth (f/parse date-format "09/15/1964")}
   {:last-name "Shaffer",   :first-name "Joanna",   :gender :F, :favorite-color "purple", :date-of-birth (f/parse date-format "10/31/1980")}])

(defn records->string
  "records->string turns the records in our test into lines of input."
  [records sep]
  (let [field-order [:last-name :first-name :gender :favorite-color :date-of-birth]]
    (->> records
         (map (fn [record]
                (->> (map (fn [field]
                            (case field
                              :gender        (name (get record field))
                              :date-of-birth (f/unparse date-format (get record field))
                                             (get record field)))
                          field-order)
                     (str/join sep))))
         (str/join "\n"))))

(def tmp-file-id
  "tmp-file-id is a unique id used to distinguish between files belonging to different tests."
  (atom 0))

(defn get-tmp-file-id
  "get-tmp-file-id atomically gets & increments the current tmp-file-id."
  []
  (let [current (atom nil)]
    (swap! tmp-file-id (fn [id]
                         (reset! current id)
                         (inc id)))
    @current))

(defn with-tmp-file
  "with-tmp-file creates a temporary file with some lines of content and passes a handle to the file to the given function.
  
  Args:
    f: A function taking the file handle.
    contents: The contents of the tmpfile."
  [f contents]
  (let [filename (str (fs/tmpdir) "/user-api-records-" (get-tmp-file-id))]
    (spit filename contents)
    (try
      (with-open [file (io/reader filename)]
        (f file))
      (finally
        (io/delete-file filename)))))

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
