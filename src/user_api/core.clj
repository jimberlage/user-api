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

(def order-mapping
  {"gender"        sort-by-gender
   "date-of-birth" sort-by-date-of-birth
   "last-name"     sort-by-last-name})

(def empty-filename-msg
  "May not be empty")

(defn display-strings
  "display-strings takes a sequence of strings and formats them for display in the cli.
  
  For example, (display-strings [\"a\" \"b\" \"c\"]) is \"\\\"a\\\", \\\"b\\\", \\\"c\\\"\""
  [values]
  (->> values
       (map (fn [input]
              (str \" input \")))
       (string/join ", ")))

(def cli-options
  (let [display-separator-inputs (display-strings (keys separator-mapping))
        display-order-inputs (display-strings (keys order-mapping))]
    [["-h" "--help"]
     ["-s" "--separator SEPARATOR" (str "The character used to separate fields. May be one of " display-separator-inputs)
      :default \,
      :parse-fn #(get separator-mapping %)
      :validate [#(contains? (set (vals separator-mapping)) %) (str "Must be one of " display-separator-inputs)]]
     ["-f" "--filename FILENAME" "The file we will read records from.  If the --api option is specified, this file will be used to seed the database."
      :validate [not-empty empty-filename-msg]]
     ["-o" "--order ORDER" (str "The order records will be output in. May be one of " display-order-inputs)
      :default sort-by-last-name
      :parse-fn #(get order-mapping %)
      :validate [#(contains? (set (vals order-mapping)) %) (str "Must be one of " display-order-inputs)]]]))

(def valid-actions
  [["cli" "Ingest records from a file and sort them"]
   ["api" "Start an API server"]])

(defn usage
  [summary]
  (->> ["This parses a set of user records from a file."
        ""
        "Usage: user-api [options] action"
        ""
        "Options:"
        summary
        ""
        "Actions:"
        (->> valid-actions
             (map (fn [[action description]]
                    (str "  " action "    " description)))
             (string/join \newline))
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

(defn as-cli
  "as-cli runs the app as a command-line tool.
  
  Args:
    options: The options returned by clojure.tools.cli/parse-opts"
  [options]
  (let [{:keys [records errors]} (parse-records-from-file (:filename options) (:separator options))
        sort-fn (:order options)
        formatted (json/write-str (sort-fn records)
                                  ;; org.joda.time.DateTime objects need special handling.
                                  :value-fn date-aware-writer)]
    (if (not-empty errors)
      (let [formatted-errors (json/write-str errors)]
        (.println *err* formatted-errors)
        (exit formatted false))
      (exit formatted true))))

(defn as-api
  "as-api runs the app as a webserver.
  
  Args:
    options: The options returned by clojure.tools.cli/parse-opts"
  [options]
  (exit "TODO: this" true))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary] :as opts} (cli/parse-opts args cli-options)
        action (or (first arguments) "cli")
        unspecified-file? (and (= "cli" action) (not (contains? options :filename)))]
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

      :else
      (case action
        "cli" (as-cli options)
        "api" (as-api options)
        :else (exit (error-msg [(str "Action must be one of " (display-strings (map first valid-actions)))] summary) false)))))
