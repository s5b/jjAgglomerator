(ns jjAgglomerator.core
  (:gen-class)
  (:use [digest] :reload-all))

(require '[clojure.string :as str]
         '[clojure.data.csv :as csv]
         '[clojure.data.json :as json]
         '[clojure.java.io :as io]
         '[digest :as digest]
         '[clojure.tools.cli :refer [parse-opts]])



;; Define the data keys.

(def account-keys #{:savings :viridian})



;; Define the input filename pattern based on the keys.

(def input-filename-pattern
  (re-pattern (str "(.*/)?(" (str/join "/" account-keys) ")-\\d{4}-\\d{4}-\\d{8}.csv")))



;; Define the command line options, and processing.

(def cli-options
  [["-d" "--data DATAFILE" "Cumulative data file"
    :id :data-file
    :default "./datafile.json"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["This program combines raw data files into the base data file."
        ""
        "Uasage: add-data [options] datafiles..."
        ""
        "Options:"
        options-summary
        ""
        "Definition of datafiles:"
        " The pathname of a download .csv files containg the "
        " raw bank transactions to be merged into the base "
        " data file. The format of the filename of the data "
        " file must follow the following pattern:"
        ""
        (str "    " input-filename-pattern)
        ""
        "Please refer to the documentation for more information."]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))



;; Initialise the accounts.

(def empty-accounts (reduce #(assoc %1 %2 {}) {} account-keys))

(defn validate-accounts [candidate-accounts]
  (doseq [expected-key (keys empty-accounts)]
    (if (not (candidate-accounts expected-key))
      (throw (IllegalStateException. (str "Data file must contain a " expected-key " map.")))))
  candidate-accounts)

(defn read-accounts [pathname]
  (if (.exists (io/as-file pathname))
    (with-open [rdr (io/reader pathname)]
      (let [candidate-accounts (json/read rdr :eof-error? false :eof-value empty-accounts :key-fn keyword)]
        (validate-accounts candidate-accounts)))
    empty-accounts))

(defn init-accounts [pathname]
  (let [serialised-accounts (read-accounts pathname)]
    (map )))



;; Process the input files.

(defn digest-line [line]
  (digest/sha-256 (str/lower-case (str/replace (str/trim line) #"\s+" " "))))

(defn process-file [accounts filename account]
  (with-open [rdr (io/reader filename)]
    (doseq [line (line-seq rdr)]
      (let [line-hash (digest-line line)]
        )
      (let [columns (csv/read-csv line)]
        )
      (println (str "   --> " line))
      (println (str "     > " (digest-line line))))))

(defn process-filename [accounts filename]
  (println (str "Candidate filename: " filename))
  (if-let [parts (re-matches input-filename-pattern filename)]
    (process-file accounts filename (get parts 2))
    (println " -- Unrecognised filename: IGNORED!")))



;; Run the whole she-bang-a-bang.

(defn -main
  "Combine raw transaction files. Example command: lein run -- `ls ./data/raw/*.csv` savings-2014-2015-20150607.csv"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (< (count arguments) 0) (exit 1 (usage summary))
      errors (exit 1 (str (error-msg errors) \newline \newline (usage summary))))
    ;; Let the processing begin ...
    (let [accounts (init-accounts (:data-file options))]
      (println accounts)
      (loop [filenames arguments]
        (when (seq filenames)
          (process-filename accounts (first filenames))
          (recur (rest filenames)))))
    (println "---------------------------")
    (println options)))
