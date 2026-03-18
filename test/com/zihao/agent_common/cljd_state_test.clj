(ns com.zihao.agent-common.cljd-state-test
  "json->state-delta 纯函数单元测试"
  (:require [com.zihao.agent-common.cljd-state :as cljd-state]
            [clojure.test :refer [deftest is testing]]))

(defn run-tests []
  (clojure.test/run-tests 'com.zihao.agent-common.cljd-state-test))

;; ==================== json->state-delta ====================

(deftest full-state-update-test
  (testing "full core state maps all keys correctly"
    (let [json-map {"type" "state-update"
                    "core" {"conversation_items" [{"type" "user" "content" "hello"}]
                            "llm_running" true
                            "system_prompt" "You are helpful."
                            "view" "chat"
                            "thread_index" [{"id" "t1" "title" "test"}]
                            "thread_detail" {"id" "t1"}}}
          delta (cljd-state/json->state-delta json-map)]
      (is (= [{"type" "user" "content" "hello"}] (:conversation-items delta)))
      (is (= true (:llm-running delta)))
      (is (= "You are helpful." (:system-prompt delta)))
      (is (= "chat" (:view delta)))
      (is (= [{"id" "t1" "title" "test"}] (:thread-index delta)))
      (is (= {"id" "t1"} (:thread-detail delta))))))

(deftest no-core-key-returns-nil-test
  (testing "returns nil when 'core' key is missing"
    (is (nil? (cljd-state/json->state-delta {"type" "connected"})))))

(deftest empty-core-returns-empty-map-test
  (testing "empty core returns empty delta"
    (is (= {} (cljd-state/json->state-delta {"core" {}})))))

(deftest partial-core-only-maps-present-keys-test
  (testing "partial core maps only the keys present"
    (let [delta (cljd-state/json->state-delta
                  {"core" {"conversation_items" [{"type" "user" "content" "hi"}]
                           "llm_running" false}})]
      (is (= [{"type" "user" "content" "hi"}] (:conversation-items delta)))
      (is (= false (:llm-running delta)))
      (is (not (contains? delta :system-prompt)))
      (is (not (contains? delta :view)))
      (is (not (contains? delta :thread-index)))
      (is (not (contains? delta :thread-detail))))))

(deftest nil-values-are-preserved-test
  (testing "nil values in core are preserved (not treated as missing)"
    (let [delta (cljd-state/json->state-delta
                  {"core" {"system_prompt" nil
                           "thread_detail" nil}})]
      (is (contains? delta :system-prompt))
      (is (nil? (:system-prompt delta)))
      (is (contains? delta :thread-detail))
      (is (nil? (:thread-detail delta))))))

(deftest empty-conversation-items-test
  (testing "empty conversation items list"
    (let [delta (cljd-state/json->state-delta
                  {"core" {"conversation_items" []}})]
      (is (= [] (:conversation-items delta))))))

(deftest multiple-conversation-items-test
  (testing "multiple conversation items with different types"
    (let [items [{"type" "user" "content" "hello"}
                 {"type" "assistant" "content" "hi there"}
                 {"type" "tool" "tool" "bash" "result" "ok"}]
          delta (cljd-state/json->state-delta {"core" {"conversation_items" items}})]
      (is (= 3 (count (:conversation-items delta))))
      (is (= "tool" (get (nth (:conversation-items delta) 2) "type"))))))
