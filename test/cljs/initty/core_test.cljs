(ns initty.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [initty.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
