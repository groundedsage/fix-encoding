(ns dev
  (:require [clojure.data.json :as json]
            [clojure.data.xml :as xml]
            [clojure.pprint :as pprint]
            [clojure.java.io :as io]
            [meander.epsilon :as m]
            [clojure.string :as str]))

 (defn sort-map [m]
  (into (sorted-map) (map (fn [[k v]] [k (if (map? v) (sort-map v) v)]) m)))
 
 (defn read-xml [filepath]
   (xml/parse (java.io.FileInputStream. filepath)))
 

  

(comment

  ;; Read test-market.spec json and create edn file
  (def spec (json/read-str (slurp "./resources/fix-specs/test-market.spec") :key-fn keyword))

  (with-open [writer (io/writer "./resources/fix-specs/test-market.edn")]
    (pprint/pprint (sort-map spec) writer))

  ;; Read ctrader



  (def c-trader-spec (read-xml "./resources/fix-specs/ctrader/FIX44-CSERVER.xml"))



  (m/rewrite {:name "entity1"
              :status :complete
              :history [{:value 100} {:value 300} {:value 700}]
              :future [{:value 1000} {:value 10000}]}
             {:name ?name
              :status ?status
              :history [{:value !values} ...]
              :future [{:value !values} ...]}
             [{:name ?name
               :status ?status
               :value !values} ...])
  
    (def header-content {:tag :header
                         :example :test
                         :attrs '({:tag :field, :attrs {:name "BeginString", :required "Y"}, :content '()}
                                  {:tag :field, :attrs {:name "BodyLength", :required "Y"}, :content '()}
                                  {:tag :field, :attrs {:name "MsgType", :required "Y"}, :content '()}
                                  {:tag :field, :attrs {:name "SenderCompID", :required "Y"}, :content '()}
                                  {:tag :field, :attrs {:name "TargetCompID", :required "Y"}, :content '()}
                                  {:tag :field, :attrs {:name "OnBehalfOfCompID", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "DeliverToCompID", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "SecureDataLen", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "SecureData", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "MsgSeqNum", :required "Y"}, :content '()}
                                  {:tag :field, :attrs {:name "SenderSubID", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "SenderLocationID", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "TargetSubID", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "TargetLocationID", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "OnBehalfOfSubID", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "OnBehalfOfLocationID", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "DeliverToSubID", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "DeliverToLocationID", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "PossDupFlag", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "PossResend", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "SendingTime", :required "Y"}, :content '()}
                                  {:tag :field, :attrs {:name "OrigSendingTime", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "MessageEncoding", :required "N"}, :content '()}
                                  {:tag :field, :attrs {:name "LastMsgSeqNumProcessed", :required "N"}, :content '()})})
  

  (def required? #(= "Y" %))

  (defn camel-to-kebab [s]
    (-> s
        (str/replace #"([a-z])([A-Z])" "$1-$2")
        (str/lower-case)
        keyword))
  
  (def merge-maps #(apply merge %))
  


  (m/rewrite c-trader-spec
             
             ;; Pattern
             {:tag :fix
              :attrs ?fix-version
              :content ({:tag :header
                         :content ({:tag :field
                                    :attrs {:name !names 
                                            :required (m/app required? !required)}} ..!header-section)}
                        {:tag :trailer 
                         :content ({:tag :field
                                    :attrs {:name !trailer-names
                                            :required (m/app required? !trailer-required)}} ..!trailer-section)} 
                        {:tag :messages 
                         :content ({:tag :message
                                    :attrs {:name !messages-names
                                            :msgtype !msg-type
                                            :msgcat !msgcat}
                                    :content !msg-content 
                                    
                                    #_({:tag :field 
                                               :attrs {:name !field-msg-names
                                                       :required (m/app required? !field-msg-required)}}
                                              {:tag :field 
                                               :attrs {:name !field-msg-names
                                                       :required (m/app required? !field-msg-required)}}
                                              {:tag :group
                                               :attrs {:name !group-msg-names
                                                       :required (m/app required? !group-msg-required)
                                                       :content !group-msg-content}} ..!msg-content)} ..!messages-section)} 
                        {:tag :components
                         :content ({:tag :component
                                    :attrs {:name !component-names}
                                    :content !component-contents} ..!components-section)}
                        
                        & ?rest)}
             
             ;; Output
             {:fix ?fix-version
              :content [{:header {:fields (m/app merge-maps [{!names {:required? !required}} ..!header-section])}
                         :trailer {:fields (m/app merge-maps [{!trailer-names {:required? !trailer-required}} ..!trailer-section])}
                         :messages (m/app merge-maps [{!messages-names {:type !msg-type
                                                                        :msgcat !msgcat
                                                                        :content (m/app #(set (map :tag %)) !msg-content)}} ..!messages-section])
                         :components [{!component-names !component-contents} ..!components-section]}]
              :rest (m/app #(map :tag %) ?rest)})
  

  
  









  )