(ns jjAgglomerator.classifier)

; An attempt at classifying the transactions by applying rules.


(def unknown ["-unknown-" []])
(def source-admin "admin")
(def source-bank "bank")
(def source-classic "classic")


(defn- classify-savings-credit [description]
  (condp re-matches description
    #"(?i)Transfer from xx2181 NetBank.*" ["stuart" ["contribution"]]
    #"(?i).*\balanna\b.*"                 ["alanna" ["contribution"]]
    #"(?i).*\b(rob(ert)?|rjb)\b.*"        ["robert" ["contribution"]]
    #"(?i)cash dep.*\b(belmont|mark)\b.*" ["mark" ["contribution"]]
    #"(?i)Credit Interest"                [source-bank ["interest"]]
    unknown))

(defn- classify-savings-debit [description]
  (condp re-matches description
    #"(?i)Transfer to xx0721 NetBank.*interest.*" [source-admin ["repayment"]]
    unknown))

(defn- classify-savings [description amount]
  (condp = (.signum amount)
      -1 (classify-savings-debit description)
      0  unknown
      1  (classify-savings-credit description)
      unknown))

(defn- classify-varidian-credit [description]
  (condp re-matches description
    #"(?i)NETBANK TFR .*Interest.*" [source-admin ["repayment"]]
    #"(?i)Transfer from xx1826 .*Interest.*" [source-admin ["repayment"]]
    unknown))

(defn- classify-varidian-debit [description]
  (condp re-matches description
    #"DEBIT INT TO \d+ (JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)" [source-bank ["interest"]]
    #"(?i)Debit Interest"                                                 [source-bank ["interest"]]
    #".*BL BCR MANAGEMEN .*"                                              [source-classic ["bodycorp"]]
    unknown))

(defn- classify-viridian [description amount]
  (condp = (.signum amount)
      -1 (classify-varidian-debit description)
      0  unknown
      1  (classify-varidian-credit description)
      unknown))

(defn classify [^String account ^String description ^BigDecimal amount]
    (condp = account
      "savings"  (classify-savings description amount)
      "viridian" (classify-viridian description amount)
      unknown))
