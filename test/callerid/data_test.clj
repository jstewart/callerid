(ns callerid.data-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [callerid.data :as data]))

(def test-data (atom {}))

(defn load-test-data
  [test-fn]
  (reset! test-data (-> "test-data.csv"
                        io/resource
                        io/reader
                        data/load-seed-data))
  (test-fn)
  (reset! test-data {}))

(use-fixtures :each load-test-data)

(deftest test-to-e164
  (testing "inputs already in E.164 format pass through"
    (is (= "+15555551212" (data/to-e164 "+15555551212"))))

  (testing "inputs in (xxx) xxx-xxxx format get converted to E.164"
    (is (= "+15555551212" (data/to-e164 "(555) 555-1212")))))

(deftest test-is-e164?
  (testing "falsey when in (xxx) xxx xxxx format"
    (is (= nil (data/is-e164? "(555) 555 1212"))))

  (testing "truthy when in +1xxxxxxxxxx format"
    (is (= "+15555551212" (data/is-e164? "+15555551212")))))

(deftest test-load-seed-data
  (testing "loads a file of CSV seed data into data set"
    (let [expected {"+13058224036" {"zendesk" ["zendesk" "Nowlin Saul"]}
                    "+16094914267" {"home" ["home" "Rubino Lennoxlove"]}
                    "+17157767000" {"zendesk"["zendesk" "Deluna Mcginley"]}
                    "+15203611642" {"blah" ["blah" "Spearman Mccreary"] "soblah" ["soblah" "Spearman Mccreary"]}
                    "+16575044762" {"desk.com" ["desk.com" "Briley Hunterstone"]}}]
      (is (= expected @test-data))))

(deftest test-find-number
    (testing "when number doesn't exist"
      (is (= nil (data/find-number test-data "notanumber"))))

    (testing "when number exists with a single context"
      (is (= {:results [{:name "Nowlin Saul" :number "+13058224036" :context "zendesk"}]}
             (data/find-number test-data "+13058224036"))))
    (testing "when number exists with multiple contexts"
      (is (= {:results [{:name "Spearman Mccreary" :number "+15203611642" :context "blah"}
                        {:name "Spearman Mccreary" :number "+15203611642" :context "soblah"}]}
             (data/find-number test-data "+15203611642"))))))

(deftest test-insert-number
  (testing "when number is new to the app state"
    (is (= true (data/insert-number
                 test-data
                 "Jason Stewart"
                 "+13072111234"
                 "home"))))

  (testing "true when number exists in the app state but with different context"
    (data/insert-number test-data "Jason Stewart" "+13072111234" "home")
    (is (= true (data/insert-number
                 test-data
                 "Jason Stewart" "+13072111234"
                 "work"))))
  (testing "falsey when number exists in the app state with the same context"
    (is (= nil (data/insert-number
                 test-data
                 "Jason Stewart" "+13072111234"
                 "work")))))
