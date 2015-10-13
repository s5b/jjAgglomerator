(ns jjAgglomerator.core
  (:gen-class)
  (:use [digest] :reload-all)
  (:import (java.time.format DateTimeFormatter)
           (java.time LocalDate ZoneId)))

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
  (re-pattern (str "(.*/)?(" (str/join "|" (map name account-keys)) ")-\\d{4}-\\d{4}-\\d{8}.csv")))



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

(def empty-accounts [])
(def empty-hashes (reduce #(assoc %1 (name %2) #{}) {} account-keys))

(defn validate-accounts [candidate-accounts]
  ; Add code here to validate the contents of the account file.
  candidate-accounts)

(defn x-group-by
  ([f g coll]
   (persistent!
     (reduce
       (fn [ret x]
         (let [k (f x)]
           (assoc! ret k (conj (get ret k #{}) (g x)))))
       (transient {}) coll)))
  ([f coll]
   (x-group-by f identity coll)))

(defn read-accounts [pathname]
  (if (.exists (io/as-file pathname))
    (with-open [rdr (io/reader pathname)]
      (let [candidate-accounts (json/read rdr :eof-error? false :eof-value empty-accounts :key-fn keyword)]
        (validate-accounts candidate-accounts)))
    empty-accounts))

(defn init-accounts [pathname]
  (let [serialised-accounts (read-accounts pathname)
        existing-hashes (x-group-by :account :hash serialised-accounts)
        keyed-hashes (merge empty-hashes existing-hashes)]
    {:serialised-accounts serialised-accounts, :keyed-hashes keyed-hashes}))



;; Process the input files.

(def index-when 0)
(def index-amount 1)
(def index-description 2)
(def index-balance 3)

(def when-format (DateTimeFormatter/ofPattern "dd/MM/yyyy"))
(def when-timezone (ZoneId/systemDefault))

(def prefix-1 " #--> ")
(def prefix-2 "    > ")

(defn to-epoch [simple-local-date]
  (let [local-date (LocalDate/parse simple-local-date when-format)
        local-date-time (.atTime local-date 12 0)
        offset-date-time (.atZone local-date-time when-timezone)
        instant (.toInstant offset-date-time)]
    (.toEpochMilli instant)))

(defn process-line [line line-hash account accounts]
  (let [seq-columns (csv/read-csv line)
        columns (nth seq-columns 0)
        when-human (nth columns index-when)
        when-epoch (to-epoch when-human)
        origin "-unknown-"
        amount (BigDecimal. (nth columns index-amount))
        expected-balance (BigDecimal. (nth columns index-balance))
        description (nth columns index-description)
        entry {:account          (name account)
               :when-epoch       when-epoch
               :when-human       when-human
               :origin           origin
               :tags             []
               :amount           amount
               :expected-balance expected-balance
               :hash             line-hash
               :description      description
               :verified         false
               :raw              line}
        added-entry (update-in accounts [:serialised-accounts] conj entry)
        added-key (update-in added-entry [:keyed-hashes account] conj line-hash)]
    added-key))

(defn digest-line [line]
  (digest/sha-256 (str/lower-case (str/replace (str/trim line) #"\s+" " "))))

(defn process-file [accounts filename account]
  (with-open [rdr (io/reader filename)]
    (loop [all-lines (line-seq rdr)
           accumulated-accounts accounts]
      (if (empty? all-lines)
        accumulated-accounts
        (do
          (let [line (first all-lines)
                line-hash (digest-line line)]
            (println prefix-1 line)
            (recur (rest all-lines)
                   (if (contains? (get-in accounts [:keyed-hashes account]) line-hash)
                     (do
                       (println prefix-2 "**** Ignoring duplicate line ***")
                       accumulated-accounts)
                     (process-line line line-hash account accumulated-accounts)))))))))


  (defn process-filename [accounts filename]
    (println (str "Candidate filename: " filename))
    (if-let [parts (re-matches input-filename-pattern filename)]
      (process-file accounts filename (get parts 2))
      (println " -- Unrecognised filename: IGNORED!")))



  ;; Run the whole sh'bang-a-bang.

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
        (println (str "Initial accounts: " accounts))
        (loop [filenames arguments
               accumulated-accounts accounts]
          (when (seq filenames)
            (recur (rest filenames) (process-filename accumulated-accounts (first filenames)))))
      (println "---------------------------")
      (println options))))
