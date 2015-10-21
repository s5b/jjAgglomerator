(ns jjAgglomerator.classifier-test
  (:require [clojure.test :refer :all]
            [jjAgglomerator.classifier :refer :all :as jja]))

(def negative-amount (BigDecimal. "-67.23"))
(def positive-amount (BigDecimal. "+413.87"))

(deftest classify-transactions-from-savings
  (testing "Given the account is savings, the amount is negative, and the description mentions transfer and interest"
    (is (= ["admin" ["repayment"]] (jja/classify "savings" "Transfer to xx0721 NetBank Viridian interest" negative-amount))))

  (testing "Given the account is savings, the amount is positive, and the description mentions 'Transfer from xx2181 NetBank'"
    (is (= ["stuart" ["contribution"]] (jja/classify "savings" "Transfer from xx2181 NetBank" positive-amount))))
  (testing "Given the account is savings, the amount is positive, and the description mentions Alanna"
    (is (= ["alanna" ["contribution"]] (jja/classify "savings" "Direct Credit 165074 BENDIGO BANK ALANNA" positive-amount))))
  (testing "Given the account is savings, the amount is positive, and the description mentions Rob, RJB, or robert"
    (is (= ["robert" ["contribution"]] (jja/classify "savings" "Transfer from ROBERT BEGG NetBank RJB" positive-amount))))
  (testing "Given the account is savings, the amount is positive, and the description contains a partion rob"
    (is (= ["-unknown-" []] (jja/classify "savings" "Transfer from ROBBERY" positive-amount))))
  (testing "Given the account is savings, the amount is positive, and the description contains the word 'belmont'"
    (is (= ["mark" ["contribution"]] (jja/classify "savings" "Cash Dep Branch Belmont" positive-amount))))
  (testing "Given the account is savings, the amount is positive, and the description contains the word 'mark'"
    (is (= ["mark" ["contribution"]] (jja/classify "savings" "Cash Dep Branch Torquay CLASIC - MARK" positive-amount))))
  (testing "Given the account is savings, the amount is positive, and the description is exactly 'Credit Interest'"
    (is (= ["bank" ["interest"]] (jja/classify "savings" "Credit Interest" positive-amount))))
  )

(deftest classify-transactions-from-viridian
  (testing "Given the account is viridian, the amount is negative, and the description matches 'DEBIT INT TO nn MONTH'"
    (is (= ["bank" ["interest"]] (jja/classify "viridian" "DEBIT INT TO 30 JAN" negative-amount))))
  (testing "Given the account is viridian, the amount is negative, and the description matches 'Debit Interest'"
    (is (= ["bank" ["interest"]] (jja/classify "viridian" "Debit Interest" negative-amount))))
  (testing "Given the account is viridian, the amount is negative, and the description matches 'BL BCR MANAGEMEN CRM MANAGEM'"
    (is (= ["classic" ["bodycorp"]] (jja/classify "viridian" "BL BCR MANAGEMEN BL BCR MAANGEMENT" negative-amount))))
  (testing "Given the account is viridian, the amount is negative, and the description contains 'BL BCR MANAGEMEN CRM MANAGEM'"
    (is (= ["classic" ["bodycorp"]] (jja/classify "viridian" "Direct Debit 126817 BL BCR MANAGEMEN BL BCR MAANGEMENT" negative-amount))))

  (testing "Given the account is viridian, the amount is positive, and the description starts with 'NETBANK TFR' and contains 'interest'"
    (is (= ["admin" ["repayment"]] (jja/classify "viridian" "NETBANK TFR Viridian interest" positive-amount))))
  (testing "Given the account is viridian, the amount is positive, and the description starts with 'Transfer from xx1826' and contains 'interest'"
    (is (= ["admin" ["repayment"]] (jja/classify "viridian" "Transfer from xx1826 NetBank Interest Payment" positive-amount))))
  )