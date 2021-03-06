(ns core-test
  (:require [core :as sut]
            [matcho.core :refer [match]]
            [clojure.test :refer :all]))

(defmacro getin= [obj pth pattern]
  `(let [res# (sut/get-in ~obj ~pth)]
     (is (= res# ~pattern))
     res#))

(defmacro set-in-match [obj pth val pattern]
  `(let [res# (sut/set-in ~obj ~pth ~val)]
     (is (= res# ~pattern))
     res#))

(def data
  {:resourceType "Patient"
   :id "patient-id"
   :contact [{:telecom [{:system "fax"
                         :value "+78219090"}
                        {:system "phone"
                         :value "8800100200"}
                        {:system "sms"
                         :use "work"
                         :value "88888900000"}]}]})


(deftest getin-test
  (testing "Primitive getin"
    (testing "with keywordize path"
      (getin=
       {:foo {:bar {:tar "mar"}}}
       [:foo :bar :tar]
       "mar"))

    (testing "with string path"
      (getin=
       {:foo {:bar {:tar "mar"}}}
       ["foo" "bar" "tar"]
       "mar"))

    (testing "with aray idx"
      (getin=
       {:foo [0 1 {:bar {:tar "mar"}}]}
       ["foo" 2 "bar" "tar"]
       "mar")))

  (testing "Get value with"
    (testing "equal predicate"
      (getin=
       [{:key "orion"  :value "1"}
        {:key "venera" :value "2"}
        {:key "orion"  :value "3"}]

       [{:get [:= [:key] "orion"]}]

       [{:key "orion"  :value "1"}
        {:key "orion"  :value "3"}])

      (getin=
       [{:key "orion"  :value "1"}
        {:key "venera" :value "2"}]
       [{"get" ["=" [:key] "orion"]}]

       [{:key "orion"  :value "1"}])

      (getin=
       [{:key "orion"  :value "1"}
        {:key "venera" :value "2"}]
       [{"get" ["=" ["key"] "orion"]}]

       [{:key "orion"  :value "1"}])

      (getin=
       [{"key" "orion"  "value" "1"}
        {"key" "venera" "value" "2"}]
       [{"get" ["=" ["key"] "orion"]}]

       [{"key" "orion"  "value" "1"}])

      (testing "with deep path"
        (getin=
         [{"key" {"foo" "bar"}  "value" "1"}
          {"key" "venera" "value" "2"}]
         [{"get" ["=" ["key" "foo" ] "bar"]}]

         [{"key" {"foo" "bar"}  "value" "1"}])

        (getin=
         [{"key" {"foo" "bar"}  "value" "1"}
          {"key" {"tar" "mar"} "value" "2"}]
         [{"get" ["=" ["key" "tar" ] "mar"]}]

         [{"key" {"tar" "mar"}  "value" "2"}])

        )

      )

    (testing "undefined pridcate"
      (is (thrown? java.lang.AssertionError 
                   ((getin=
                     [{:key "orion"  :value "1"}]
                     [{"get" ["ups" [:key] "orion"]}]
                     nil)))))

    (match
     (sut/getin [{:foo  "bar"
                  :value [{:bar "tar"
                           :system "42"}]}]
                  [{:get [:= :foo "bar"]}
                   0 :value
                   {:get [:= :bar "tar"]}
                   0 :system])
     "42")

    #_(match
     (sut/getin [{:foo {:bar {:baz "keywords seq"}}
                  :value 12}]
                  [{:get [:= "keywords seq" [:foo :bar :baz]]} 0 :value])
     12)


    (match
     (sut/getin [{:foo 42 :value "42"}
                 {:value "10"}
                 {:foo -10 :value "-10"}]
                [{:get [:not :foo]} :# :value])
     ["10"])


    (match
     (sut/getin [{:foo 42 :value "42"}
                   {:foo 10 :value "10"}
                   {:foo -10 :value "-10"}]
                  [{:get [:> :foo 20]} :# :value])
     ["42"])
    (match
     (sut/getin [{:foo 42 :value "42"}
                 {:foo 10 :value "10"}
                 {:foo -10 :value "-10"}]
                [{:get [:< :foo 0]} 0 :value])
     "-10")

    (match
     (sut/getin data [:contact 0 :telecom {:get [:= :system "phone"]
                                           :set {:system "phone" :value ""}} 0 :value])
     "8800100200")
    (match
     (sut/getin data [:contact 0 :telecom {:get [:= :system "fax" ]
                                           :set {:system "phone" :value ""}} 0 :value])
     "+78219090")
    (match
     (sut/getin data [:contact 0 :telecom {:get [:and
                                                   [:= :system "sms"]
                                                   [:= :use "work" ]]
                                            :set {:system "sms"
                                                  :use "work"
                                                  :value ""}} 0 :value])
     "88888900000")

    (match
     (sut/getin data [:contact 0 :telecom {:get [:= "email" :system]
                                             :set {:system "email" :value "superman@batma.com"}} :value])
     "superman@batma.com")
    (match
     (sut/getin data [:contact 0 :telecom {:get [:and
                                                   [:= :system "sms"]
                                                   [:= "home" :use]]
                                             :set {:system "sms"
                                                   :use "home"
                                                   :value "home_sms"}} :value])
     "home_sms")

    (match
     (sut/getin data [:contact 0 :telecom {:get [:or
                                                   [:= :system "fake_system"]
                                                   [:=  :use "work"]]
                                             :set {:system "fake_system"
                                                   :use "work"
                                                   :value "home_sms"}} 0 :value])
     "88888900000"))

  (testing "Set value"

    (testing "Simple insert"
      (match
       (sut/setin {:telecom "12"}
                  [:telecom ]
                  "42")
       {:telecom "42"})

      (match
       (sut/setin {:telecom {:system "phone"}}
                  [:telecom :system]
                  "email")
       {:telecom {:system "email"}})

      (match
       (sut/setin {:telecom {:system ["phone" "email" "fax"]}}
                  [:telecom :system 1]
                  "postal")
       {:telecom {:system ["phone" "postal" "fax"]}})


      (match
       (sut/setin nil
                  [:telecom :system 1]
                  "postal")
       {:telecom {:system [nil "postal"]}}))

    (testing "Insert by search"

      (testing "Insert into nil"
        (match
         (sut/setin nil
                    [:telecom {:get [:= :system "phone"]
                               :set {:system "phone" :value nil}} :# :value]
                    ["+7(912)-123-45-67"])
         {:telecom vector? }))

      (testing "Insert exists item"
        (match
         (sut/setin {:telecom [{:system "phone" :use "work" :value "+7(911)-189-12-12"}]}
                    [:telecom {:get [:= :system "phone"]
                               :set {:system "phone" :value nil}} 0 :value]
                    "+7(912)-123-45-67")
         {:telecom [{:system "phone" :value "+7(912)-123-45-67"}]}))

      (testing "Insert new item item"
        (match
         (sut/setin {:telecom [{:system "phone" :use "work" :value "+7(911)-189-12-12"}]}
                    [:telecom {:get [:= :system "email"]
                               :set {:system "email" :rank 1 :value nil}} 0 :value]
                    "e@mail.com")
         {:telecom [{:system "phone" :use "work" :value "+7(911)-189-12-12"}
                    {:system "email" :rank 1 :value "e@mail.com"}
                    ]}))

      (testing "Merge with existing item"
        (match
         (sut/setin {:telecom [{:system "phone" :use "work" :rank 1 :value "+7(911)-189-12-12"}]}
                    [:telecom {:get [:= :system "phone"]
                               :set {:system "phone" :rank 2 :value nil}} 0 :value]
                    "8800100200")
         {:telecom [{:system "phone" :use "work" :rank 1 :value "8800100200"}]})))

    (testing "Arrays of objects"
      (def in [{:rt "user" :userName "marat" :email "foo@com" :password "Fooooooooo"}
               {:rt "role" :name "practitioner" :link "pr"}
               {:rt "role" :name "miac"}
               {:rt "role" :name "org-admin" :link "org"}])

      (def role-mapping
        [[[:link] [:link]]
         [[:name] [:name]]
         [[:resourceType] [:rt]]])

      (def user-mapping
        [[[:login] [:userName]]
         [[:rt] [:rt]]
         [[:contact] [:email]]
         [[:password] [:password]]])

      (def m
        [[[:user {:map user-mapping}]     [{:get [:= :rt "user" ]} 0]]
         [[:role :# {:map role-mapping}]  [{:get [:= :rt "role"]} :#]]])

      (match
       (sut/import in m)
       {:user {:login "marat" :password "Fooooooooo" :contact "foo@com"},
        :role [{:link "pr"   :name "practitioner" :resourceType "role"}
               {:name "miac" :resourceType "role"}
               {:link "org"  :name "org-admin" :resourceType "role"}]})

      (match
       (sut/export (sut/import in m) m)
       in)

      (match
       (sut/getin
        {:role [{:id 1} {:id 2}]}
        [:role :# {:map [[[:id] [:longId]]]}])
       [{:longId 1} {:longId 2}]
       )

      
      )

    ))



#_(deftest mapping-test
  #_(testing "get-in"
    (match
     (sut/get-in data [:contact 0 :telecom {:get [:= :system "phone"]} 0])
     {:system "phone"
      :value "8800100200"})
    (match
     (sut/get-in data [:contact 0 :telecom {:get [:= :system "email"]} 0])
     nil)
    (match
     (sut/get-in data [:contact 0 :telecom {:get [:= :system "email"]} 0] {:system "email", :value "no-email-found"})
     {:system "email", :value "no-email-found"})

    (testing "nested get-in"
      (match (sut/get-in {:foo [{:sys "foo1", :bar [{:sys "foo1bar1", :baz {:quux "test4"}}
                                                    {:sys "foo1bar2", :baz {:quux "test3"}}]}
                                {:sys "foo2", :bar [{:sys "foo2bar1", :baz {:quux "test2"}}
                                                    {:sys "foo2bar2", :baz {:quux "test"}}]}]}
                         [:foo {:get [:=  [:bar {:get [:=  [:baz :quux] "test"]} 0 :baz :quux] "test"]} 0
                          :bar {:get [:=  [:baz :quux] "test"]} 0 :baz])
             {:quux "test"})))

  (testing "default value for export"
    (def resource {:resourceType "Patient"
                   :id "id"})
    (def mapping [[[:rt] [:resourceType]]])
    (match
     (sut/import resource mapping)
      {:rt "Patient"})
    (match
     (sut/export {:rt "Patient"} mapping resource)
      resource)

    (match
     (sut/export {:rt "foo"} mapping resource)
      {:resourceType "foo"
       :id "id"}))

  (testing "primitive key-value"
    (def resource {:resourceType "Patient"})
    (def mapping [[[:rt] [:resourceType]]])
    (match
     (sut/import resource mapping)
      {:rt "Patient"})
    (match
     (sut/export {:rt "Patient"} mapping)
      resource)

    (match
     (sut/export (sut/import resource mapping) mapping)
      resource))

  (testing "deep keys and arrays"
    (def resource {:name [{:given ["Marat" "Razi"]
                           :family "Surm"}]
                   :gender "male"})
    (def mapping [[[:name]     [:name 0 :given 0]]
                  [[:family]   [:name 0 :family]]
                  [[:lastName] [:name 0 :given 1]]
                  [[:gender :test] [:gender]]])
    (def result  {:name "Marat"
                  :family "Surm"
                  :lastName "Razi"
                  :gender {:test "male"}})
    (match
     (sut/import resource mapping)
      result)
    (match
     (sut/export result mapping)
      resource)
    (match
     (sut/export (sut/import resource mapping) mapping)
      resource))

  (testing "identifier"
    (def resource {:identifier [{:system "pasport" :value "123"}
                                {:system "inn" :value "345"}
                                {:system "inn" :value "567"}]})
    (def mapping [[[:pasport] [:identifier {:get [:= :system "pasport"]
                                            :set {:system "pasport"}}     0 :value]]
                  [[:inn]     [:identifier {:get [:= :system "inn"]
                                            :set {:system "inn"}}         :# :value]]])

    (def result  {:inn ["345" "567"] :pasport "123"})

    (match
     (sut/import resource mapping)
      result)
    (match
     (sut/export result mapping)
      resource)
    (match
     (sut/export (sut/import resource mapping) mapping)
      resource))

  (testing "get whole item after get"
    (let [mappings [[[:patient-documents]    [:supportingInfo {:get [:= :resourceType "DocumentReference"]} :#]]
                    [[:patient-other]        [:supportingInfo {:get [:not-in :resourceType ["DocumentReference"]]} :#]]]
          data {:supportingInfo [{:resourceType "DocumentReference", :value "val"}
                                 {:resourceType "Other", :value "other-val"}]}]
      (match
       (-> data
           (sut/import mappings)
           (sut/export mappings))
       data)))

  (testing "in pred"
    (def resource  [{:system "inn"     :value "3"}
                    {:system "inn"     :value "4"}
                    {:system "inn-old" :value "5"}
                    {:system "pasport" :value "1"}
                    {:system "pasport" :value "2"}
                    {:system "snils"   :value "2"}])
    (def mapping [[[:inn]       [{:get [:in :system ["inn"]]
                                  :set {:system "inn"}}      :# :value]]
                  [[:inn-old]    [{:get [:= :system "inn-old"]
                                   :set {:system "inn-old"}} :# :value]  ]
                  [[:other]     [{:get [:not-in :system ["inn" "inn-old"]]}]]])

    (def result {:inn ["3" "4"]
                 :inn-old ["5"]
                 :other [{:system "pasport" :value "1"}
                         {:system "pasport" :value "2"}
                         {:system "snils"   :value "2"}]})

    (match
     (sut/import resource mapping)
      result)
    (match
     (sut/export result mapping)
      resource)
    (match
     (sut/export (sut/import resource mapping) mapping)
      resource)))
