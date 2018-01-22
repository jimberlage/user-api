(ns user-api.test-helpers
  (:require [clojure.string :as str]
            [clj-time.format :as f]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [user-api.records :refer [date-format]]))

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
