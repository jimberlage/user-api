(ns user-api.core
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [user-api.records :refer [date-aware-writer
                                      parse-records-from-file
                                      sort-by-gender
                                      sort-by-date-of-birth
                                      sort-by-last-name]])
  (:gen-class))

(def separator-mapping
  {"pipe"  \|
   "comma" \,
   "space" \space})

(def empty-filename-msg
  "May not be empty")

(def cli-options
  (let [valid-separator-inputs (->> (keys separator-mapping)
                                    (map (fn [input]
                                           (str \" input \")))
                                    (string/join ", "))]
    [["-h" "--help"]
     ["-s" "--separator SEPARATOR" (str "The character used to separate fields. May be one of " valid-separator-inputs)
      :default "comma"
      :parse-fn #(get separator-mapping %)
      :validate [#(contains? (set (vals separator-mapping)) %) (str "Must be one of " valid-separator-inputs)]]
     ["-f" "--filename FILENAME" "The file we will read records from.  If the --api option is specified, this file will be used to seed the database."
      :validate [not-empty empty-filename-msg]]]))

(defn usage
  [summary]
  (->> ["This parses a set of user records from a file."
        ""
        "Usage: user-api [options]"
        ""
        "Options:"
        summary
        ""
        "Try user-api --help to see this menu again."]
       (string/join \newline)))

(defn error-msg
  [errors summary]
  (->> ["The following errors occurred while parsing your command:\n"
        (string/join \newline errors)
        "\n"
        (usage summary)]
       (string/join \newline)))

(defn exit
  [msg ok?]
  (println msg)
  (System/exit (if ok? 0 1)))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary] :as opts} (cli/parse-opts args cli-options)
        unspecified-file? (not (contains? options :filename))]
    (cond
      (:help options)
      (exit (usage summary) true)

      (or errors unspecified-file?)
      (let [;; Unfortunately, clojure.tools.cli doesn't allow you to specify that an option must always be set.
            ;; You can only validate options that have been passed in.
            errors (if unspecified-file?
                     (conj errors (str "Failed to validate \"--filename\": " empty-filename-msg))
                     errors)]
        (exit (error-msg errors summary) false))

      ;; Sort the records three ways and return them.
      :else
      (let [{:keys [records errors]} (parse-records-from-file (:filename options) (:separator options))
            formatted (json/write-str {:gender (sort-by-gender records)
                                       :date-of-birth (sort-by-date-of-birth records)
                                       :last-name (sort-by-last-name records)}
                                      ;; org.joda.time.DateTime objects need special handling.
                                      :value-fn date-aware-writer)]
        (if (not-empty errors)
          (let [formatted-errors (json/write-str errors)]
            (.println *err* formatted-errors)
            (exit formatted false))
          (exit formatted true))))))
