(ns callerid.server-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [callerid.data :as data]
            [callerid.server :refer :all]))

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

(deftest test-server
  ;; data-set is the injected dependency from component.
  (let [handler (make-handler {:data-set test-data})]
    (testing "/query route with an existing valid number"
      (let [response (handler (mock/request :get "/query" {:number "+16575044762"}))]
        (is (= (:status response) 200))
        (is (= (:body response) "{\"results\":[{\"name\":\"Briley Hunterstone\",\"number\":\"+16575044762\",\"context\":\"desk.com\"}]}"))))

    (testing "/query route with a non-existing number"
      (let [response (handler (mock/request :get "/query" {:number "GARBAGE"}))]
        (is (= (:status response) 404))
        (is (= (:body response) "Not Found"))))

    (testing "number route with good input and a new number"
      (let [response (handler (mock/request
                               :post "/number",
                               {:number "+12345669876"
                                :name "Joe Bloggs"
                                :context "work"}))]
        (is (= (:status response) 200))
        (is (= (:body response) "Success"))))

    (testing "number route with good input and an existing number"
      (let [response (handler (mock/request
                               :post "/number",
                               {:number "+12345669876"
                                :name "Joe Bloggs"
                                :context "work"}))]
        (is (= (:status response) 409))
        (is (= (:body response) "This number and context were already entered"))))

    (testing "number route with bad input"
      (let [response (handler (mock/request
                               :post "/number",
                               {:number ""
                                :name ""
                                :context ""}))]
        (is (= (:status response) 400))
        (is (= (:body response) "Bad Request"))))

    (testing "number route with invalid phone number"
      (let [response (handler (mock/request
                               :post "/number",
                               {:number "(616) 555 1212"
                                :name "Test"
                                :context "test"}))]
        (is (= (:status response) 400))
        (is (= (:body response) "Bad Request"))))

    (testing "not-found route"
      (let [response (handler (mock/request :get "/invalid"))]
        (is (= (:status response) 404))))))
