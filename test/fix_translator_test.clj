(ns fix-translator-test
  (:require [fix-translator :as fix]
            [clojure.test :refer [deftest is] :as t]))

(t/use-fixtures :once
  (fn [f]
    (fix/load-spec :test-market)
    (f)))

(deftest test-invert-map
  (let [m {:a 1 :b 2 :c 3}]
    (is (= {1 :a 2 :b 3 :c} (fix/invert-map m)))))

(deftest test-load-spec
  (is (= true (fix/load-spec :test-market)))
  (is (= nil (fix/load-spec :non-market))))

(deftest test-gen-transformations
  (let [transform-by-value (fix/gen-transformations {:tag "35"
                                                     :transform-by "by-value"
                                                     :values {:heartbeat "0"
                                                              :test-request "1"}}
                                                    :test-market)]
    (is (= "0" ((:outbound transform-by-value) :heartbeat)))
    (is (= :heartbeat ((:inbound transform-by-value) "0")))

    (is (= "1" ((:outbound transform-by-value) :test-request)))
    (is (= :test-request ((:inbound transform-by-value) "1")))

    (let [transform-to-int (fix/gen-transformations {:tag "38" :transform-by "to-int"} :test-market)]
      (is (= "100" ((:outbound transform-to-int) 100)))
      (is (= 100 ((:inbound transform-to-int) "100")))

      (is (= "100" ((:outbound transform-to-int) 100.0)))
      (is (= "100" ((:outbound transform-to-int) 100.1)))

      (is (thrown? Exception ((:inbound transform-to-int) "100.0"))))

    (let [transform-to-double (fix/gen-transformations {:tag "44" :transform-by "to-double"} :test-market)]
      (is (= "1.0" ((:outbound transform-to-double) 1.0)))
      (is (= "1.0" ((:outbound transform-to-double) 1.00)))
      (is (= 1.0 ((:inbound transform-to-double) "1.00")))

      (is (= "1.01" ((:outbound transform-to-double) 1.01)))
      (is (= 1.01 ((:inbound transform-to-double) "1.01")))

      (is (= "1.0" ((:outbound transform-to-double) 1)))
      (is (= 1.0 ((:inbound transform-to-double) "1"))))

    (let [transform-to-string (fix/gen-transformations {:tag "55" :transform-by "to-string"} :test-market)]
      (is (= "NESNz" ((:outbound transform-to-string) "NESNz")))
      (is (= "NESNz" ((:inbound transform-to-string) "NESNz"))))

    (let [invalid-transform {:tag "00" :transform-by "to-nothing"}]
      (is (thrown? Exception (fix/gen-transformations invalid-transform))))

    (let [no-transform-fn {:tag "00"}]
      (is (thrown? Exception (fix/gen-transformations no-transform-fn))))

    (let [no-values {:tag "00" :transform-by "by-value"}]
      (is (thrown? Exception (fix/gen-transformations no-values))))))

(deftest test-gen-codec
  (let [fix-tag-name :exec-inst
        tag-spec {:tag "18"
                  :transform-by "by-value"
                  :values {:market-peg "P"
                           :primary-peg "R"
                           :mid-price-peg "M"}}
        codec (fix/gen-codec fix-tag-name tag-spec :test-market)]
    (is (= "18" (fix/tag-number (get-in codec [:encoder :exec-inst]))))
    (is (= "P" ((fix/translation-fn (get-in codec [:encoder fix-tag-name])) :market-peg)))
    (is (= "R" ((fix/translation-fn (get-in codec [:encoder fix-tag-name])) :primary-peg)))
    (is (= "M" ((fix/translation-fn (get-in codec [:encoder fix-tag-name])) :mid-price-peg)))

    (is (= (fix/tag-name (get-in codec [:decoder "18"])) fix-tag-name))
    (is (= :market-peg ((fix/translation-fn (get-in codec [:decoder "18"])) "P")))
    (is (= :primary-peg ((fix/translation-fn (get-in codec [:decoder "18"])) "R")))
    (is (= :mid-price-peg ((fix/translation-fn (get-in codec [:decoder "18"])) "M")))))



(deftest test-get-encoder
  (let [encoder (fix/get-encoder :test-market)]
    (is (= "35" (fix/tag-number (encoder :msg-type))))
    (is (= "0" ((fix/translation-fn (encoder :msg-type)) :heartbeat)))

    (is (thrown? Exception (fix/get-encoder :invalid-market)))))

(deftest test-translate-to-fix
  (let [encoder (fix/get-encoder :test-market)]
    (is (= (str "35=0" fix/tag-delimiter) (fix/translate-to-fix encoder [:msg-type :heartbeat])))
    (is (not= (str "35=0") (fix/translate-to-fix encoder [:msg-type :heartbeat])))
    (is (thrown? Exception
                 ((fix/translate-to-fix encoder [:invalid-tag :heartbeat]))))
    (is (thrown? Exception
                 ((fix/translate-to-fix encoder [:msg-type :invalid-value]))))))

(deftest test-add-msg-cap
  (let [encoder (fix/get-encoder :test-market)]
    (is (= "8=FIX.4.2\u00019=8\u0001my-order" (fix/add-msg-cap encoder "my-order")))
    (is (= "8=FIX.4.2\u00019=9\u0001my-order\u0001" (fix/add-msg-cap encoder "my-order\u0001")))))

(deftest test-checksum
  (let [msg-a "checksum-without-delimiter"
        msg-b "checksum\u0001with\u0001delimiters\u0001"]
    (is (= "128" (fix/checksum msg-a)))
    (is (= "068" (fix/checksum msg-b)))))

(deftest test-add-checksum
  (let [encoder (fix/get-encoder :test-market)
        msg-a "checksum-without-delimiter"
        msg-b "checksum\u0001with\u0001delimiters\u0001"]
    (is (= "checksum-without-delimiter10=128\u0001" (fix/add-checksum encoder msg-a)))
    (is (= "checksum\u0001with\u0001delimiters\u000110=068\u0001" (fix/add-checksum encoder msg-b)))))

(deftest test-encode-msg
  (let [msg [:msg-type :new-order-single :side :buy :order-qty 100
             :symbol "NESNz" :price 1.00]]
    (is (= (str "8=FIX.4.2\u0001" "9=33\u0001" "35=D\u0001" "54=1\u0001"
                "38=100\u0001" "55=NESNz\u0001" "44=1.0\u0001" "10=131\u0001")
           (fix/encode-msg :test-market msg)))))

(deftest test-get-decoder
  (let [decoder (fix/get-decoder :test-market)]
    (is (= (fix/tag-name (decoder "35")) :msg-type))
    (is (= ((fix/translation-fn (decoder "35")) "0") :heartbeat))
    (is (thrown? Exception (fix/get-decoder :invalid-market)))))

(deftest test-get-tags-of-interest
  (is (= "45|58|371|372|373" (fix/get-tags-of-interest :test-market :reject)))
  (is (thrown? Exception (fix/get-tags-of-interest :invalid-market :reject)))
  (is (thrown? Exception (fix/get-tags-of-interest :test-market :invalid-tag))))

(deftest test-extract-tag-value
  (is (= "D" (fix/extract-tag-value "35" "35=D\u0001")))
  (is (= "D" (fix/extract-tag-value "35" "35=D\u000144=1.0\u000155=NESNz\u0001")))
  (is (= "1.0" (fix/extract-tag-value "44" "35=D\u000144=1.0\u000155=NESNz\u0001")))
  (is (= "NESNz" (fix/extract-tag-value "55" "35=D\u000144=1.0\u000155=NESNz\u0001")))
  (is (= nil (fix/extract-tag-value "35" "35=D")))
  (is (= nil (fix/extract-tag-value "35" "35=D44=1.0")))
  (is (= "1.0" (fix/extract-tag-value "44" "35=D44=1.0\u0001"))))

(deftest test-get-msg-type
  (is (= :logon (fix/get-msg-type :test-market "35=A\u0001")))
  (is (= :test-request (fix/get-msg-type :test-market "35=1\u0001")))
  (is (= :execution-report (fix/get-msg-type :test-market "35=8\u0001")))
  (is (= :unknown-msg-type (fix/get-msg-type :test-market "35=X\u0001"))))

(deftest test-translate-to-map
  (let [decoder (fix/get-decoder :test-market)]
    (is (= {:msg-type :execution-report} (fix/translate-to-map decoder ["35" "8"])))
    (is (thrown? Exception (fix/translate-to-map decoder ["00" "8"])))
    (is (thrown? Exception (fix/translate-to-map decoder ["35" "Z"])))))

(deftest test-decode-tag
  (let [msg "35=8\u000144=1.0\u000155=NESNz\u000139=0\u0001"]
    (is (= :execution-report (fix/decode-tag :test-market :msg-type msg)))
    (is (= 1.0 (fix/decode-tag :test-market :price msg)))
    (is (= "NESNz" (fix/decode-tag :test-market :symbol msg)))
    (is (= :new (fix/decode-tag :test-market :order-status msg)))))

(deftest test-decode-msg
  (is (= {:price 1.0 :symbol "NESNz" :order-status :new}
         (fix/decode-msg :test-market :execution-report
                         "35=8\u000144=1.0\u000155=NESNz\u000139=0\u0001")))
  (is (thrown? Exception (fix/decode-msg :test-market :execution-report
                                         "35=8\u000144=1.0\u000155=NESNz\u000139=Z\u0001"))))