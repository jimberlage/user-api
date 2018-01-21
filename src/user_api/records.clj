(ns user-api.records
  (:require [clj-time.core :as time]
            [schema.core :as schema]))

(def Record
  "A Record contains some basic information about a person."
  {:last-name schema/Str
   :first-name schema/Str
   :gender (schema/enum :male :female)
   :favorite-color schema/Str
   :date-of-birth schema/Any})

(defn comp-gender
  "comp-gender is a comparator defining how to sort genders.
  See https://clojure.org/guides/comparators for some details."
  [first-gender second-gender]
  (case [first-gender second-gender]
    [:male :female] 1
    [:female :male] -1
                    0))

(defn comp-time
  "comp-time is a comparator defining how to sort times.
  See https://clojure.org/guides/comparators for some details.
  first-time and second-time are assumed to be clj-time.core/DateTime objects."
  [first-time second-time]
  (cond
    (time/after? first-time second-time)  1
    (time/before? first-time second-time) -1
    :default                              0))

(def SortOrder
  "SortOrder is a datatype defining how two records should be sorted."
  {;; :key is the particular key in the record we're comparing while sorting.
   :key schema/Keyword
   ;; :comp references a comparator function, as described here: https://clojure.org/guides/comparators
   ;; If this is absent, clojure.core/compare will be used.
   (schema/optional-key :comp) schema/Symbol
   ;; :order determines whether values should be sorted lowest to highest (:asc), or highest to lowest (:desc)
   ;; If this is absent, :asc will be used.
   (schema/optional-key :order) (schema/enum :asc :desc)})

(defmacro sort-orders->comp
  "sort-orders->comp provides some boilerplate for writing a comparator function for sorting records.
  See https://clojure.org/guides/comparators for some details.
  The idea is to mimic something like SQL's ORDER BY function, where you can write 'ORDER BY gender ASC, last_name DESC' to sort by gender and then last name within genders.
  We may have to specify a comparison function in addition to the field name and order, since we don't have a default comparison function for fields like gender.
  
  Args:
    sort-orders: A sequence of SortOrder objects indicating how to sort the two records.
    first-record: The first Record object in the sequence.
    second-record: The second Record object in the sequence."
  [sort-orders first-record second-record]
  (if (empty? sort-orders)
    ;; No sorting necessary.
    true
    (let [;; Parse the SortOrder objects; we default to compare because that's the default for clojure.core/sort.
          ;; https://clojure.org/guides/comparators
          {:keys [key comp order] :or {comp compare, order :asc}} (first sort-orders)
          ;; A comparator will return true if first-record should come before second-record.
          ;; If we're sorting in decending order, we want to invert that logic.
          return-val (= order :asc)]
      ;; Basically, we use our comparison function to compare the values in the records.
      ;; If the values are equal, the next sort-order clause defines how the records are sorted; if they are equal, then we use the sort-order clause after that, and so on and so on.
      `(let [x# (~comp (get ~first-record ~key) (get ~second-record ~key))]
         (cond
           (pos? x#) ~(not return-val)
           (neg? x#) ~return-val
           :default (sort-orders->comp ~(rest sort-orders) ~first-record ~second-record))))))

(defmacro defsort
  "defsort provides some boilerplate for writing a custom sorting function for a sequence of records.
  The function this macro defines takes a sequence of records, and returns the sorted sequence of records.
  
  Args:
    name: The name of the function.
    docstring: The function's docstring, indicating how the sort is performed.
    sort-orders: A sequence of SortOrder objects indicating how to sort the sequence of records."
  [name docstring sort-orders]
  `(defn ~name
     ~docstring
     [records#]
     (sort (fn [first-record# second-record#]
             (sort-orders->comp ~sort-orders first-record# second-record#))
           records#)))

(defsort sort-by-gender
  "sort-by-gender sorts records by gender, ascending (females before males), and then by last name within genders.
  
  Args:
    records: A sequence of Records.
  
  Returns:
    The sorted sequence."
  [{:key :gender, :comp comp-gender}
   {:key :last-name}])

(defsort sort-by-date-of-birth
  "sort-by-date-of-birth sorts records by date of birth, ascending (earlier dates before later dates).
  
  Args:
    records: A sequence of Records.
  
  Returns:
    The sorted sequence."
  [{:key :date-of-birth, :comp comp-time}])

(defsort sort-by-last-name
  "sort-by-last-name sorts records by last name, descending (Z to A).
  
  Args:
    records: A sequence of Records.
  
  Returns:
    The sorted sequence."
  [{:key :last-name, :order :desc}])
