(ns com.zihao.agent-common.content-block-test
  (:require [com.zihao.agent-common.content-block :as cb]
            [clojure.test :refer [deftest is testing]]))

(defn run-tests []
  (clojure.test/run-tests 'com.zihao.agent-common.content-block-test))

;; --- 构造函数 ---

(deftest text-block-test
  (is (= {:type "text" :text "hello"} (cb/text-block "hello")))
  (is (= {:type "text" :text ""} (cb/text-block ""))))

(deftest thinking-block-test
  (is (= {:type "thinking" :thinking "let me think"} (cb/thinking-block "let me think"))))

(deftest tool-use-block-test
  (is (= {:type "tool_use" :id "tu-1" :name "bash" :input {}}
         (cb/tool-use-block "tu-1" "bash" {})))
  (is (= {:type "tool_use" :id "tu-2" :name "run" :input {:x 1}}
         (cb/tool-use-block "tu-2" "run" {:x 1}))))

(deftest tool-result-block-test
  (is (= {:type "tool_result" :tool_use_id "tu-1" :content [{:type "text" :text "ok"}]}
         (cb/tool-result-block "tu-1" [{:type "text" :text "ok"}])))
  (is (vector? (:content (cb/tool-result-block "x" '({:type "text" :text "a"}))))))

;; --- 提取函数 ---

(deftest extract-text-test
  (testing "仅 text blocks"
    (is (= "ab" (cb/extract-text [{:type "text" :text "a"} {:type "text" :text "b"}]))))
  (testing "忽略 thinking、tool_use"
    (is (= "hi" (cb/extract-text [{:type "thinking" :thinking "x"}
                                  {:type "text" :text "hi"}
                                  {:type "tool_use" :id "1" :name "n" :input {}}]))))
  (testing "空 blocks"
    (is (= "" (cb/extract-text []))))
  (testing "无 text block"
    (is (= "" (cb/extract-text [{:type "thinking" :thinking "only"}])))))

(deftest extract-thinking-test
  (testing "仅 thinking blocks"
    (is (= "t1t2" (cb/extract-thinking [{:type "thinking" :thinking "t1"}
                                         {:type "thinking" :thinking "t2"}]))))
  (testing "忽略 text、tool_use"
    (is (= "think" (cb/extract-thinking [{:type "text" :text "hi"}
                                         {:type "thinking" :thinking "think"}]))))
  (testing "空或无 thinking"
    (is (= "" (cb/extract-thinking [])))
    (is (= "" (cb/extract-thinking [{:type "text" :text "x"}])))))

(deftest extract-tool-uses-test
  (is (= [{:type "tool_use" :id "1" :name "a" :input {}}]
         (cb/extract-tool-uses [{:type "text" :text "x"}
                                {:type "tool_use" :id "1" :name "a" :input {}}])))
  (is (= [] (cb/extract-tool-uses [{:type "text" :text "x"}])))
  (is (vector? (cb/extract-tool-uses []))))

(deftest extract-tool-results-test
  (is (= [{:type "tool_result" :tool_use_id "tu-1" :content []}]
         (cb/extract-tool-results [{:type "tool_result" :tool_use_id "tu-1" :content []}
                                   {:type "text" :text "x"}])))
  (is (vector? (cb/extract-tool-results []))))

;; --- 转换函数 ---

(deftest normalize-content-test
  (testing "string -> 单元素 text block 数组"
    (is (= [{:type "text" :text "hello"}] (cb/normalize-content "hello")))
    (is (= [{:type "text" :text ""}] (cb/normalize-content ""))))
  (testing "sequential -> 原样"
    (let [blocks [{:type "text" :text "a"} {:type "thinking" :thinking "b"}]]
      (is (= blocks (cb/normalize-content blocks))))
    (is (= [{:type "text" :text "x"}] (cb/normalize-content '({:type "text" :text "x"})))))
  (testing "其他类型 -> 转 str 包成 text block"
    (is (= [{:type "text" :text "123"}] (cb/normalize-content 123)))
    (is (= [{:type "text" :text ""}] (cb/normalize-content nil)))))

(deftest filter-for-api-test
  (testing "保留 text 和 image"
    (is (= [{:type "text" :text "a"} {:type "image" :source {}}]
           (cb/filter-for-api [{:type "text" :text "a"}
                               {:type "image" :source {}}
                               {:type "diff" :old "o" :new "n" :available? true}
                               {:type "thinking" :thinking "x"}]))))
  (testing "空或全过滤"
    (is (= [] (cb/filter-for-api [{:type "diff" :old "o" :new "n"}])))
    (is (vector? (cb/filter-for-api [])))))

;; --- 辅助函数 ---

(deftest has-tool-uses-test
  (is (cb/has-tool-uses? [{:type "tool_use" :id "1" :name "n" :input {}}]))
  (is (cb/has-tool-uses? [{:type "text" :text "x"} {:type "tool_use" :id "1" :name "n" :input {}}]))
  (is (not (cb/has-tool-uses? [])))
  (is (not (cb/has-tool-uses? [{:type "text" :text "x"}]))))

(deftest has-thinking-test
  (is (cb/has-thinking? [{:type "thinking" :thinking "x"}]))
  (is (cb/has-thinking? [{:type "text" :text "x"} {:type "thinking" :thinking "y"}]))
  (is (not (cb/has-thinking? [])))
  (is (not (cb/has-thinking? [{:type "text" :text "x"}]))))
