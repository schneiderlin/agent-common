(ns com.zihao.agent-common.content-block
  "Content Block 统一处理：创建、提取、转换。
   纯函数，可直接 unit test。"
  (:require [clojure.string :as str]))

;; --- 构造函数 ---

(defn text-block [s]
  {:type "text" :text s})

(defn thinking-block [s]
  {:type "thinking" :thinking s})

(defn tool-use-block [id name input]
  {:type "tool_use" :id id :name name :input input})

(defn tool-result-block [tool-use-id content-blocks]
  {:type "tool_result" :tool_use_id tool-use-id :content (vec content-blocks)})

;; --- 提取函数 ---

(defn extract-text
  "从 content blocks 中提取所有 text block 的文本，拼接成字符串。"
  [blocks]
  (->> blocks
       (filter #(= "text" (:type %)))
       (map :text)
       (str/join "")))

(defn extract-thinking
  "从 content blocks 中提取所有 thinking block 的内容，拼接成字符串。"
  [blocks]
  (->> blocks
       (filter #(= "thinking" (:type %)))
       (map :thinking)
       (str/join "")))

(defn extract-tool-uses
  "从 content blocks 中提取所有 tool_use block。"
  [blocks]
  (filterv #(= "tool_use" (:type %)) blocks))

(defn extract-tool-results
  "从 content blocks 中提取所有 tool_result block。"
  [blocks]
  (filterv #(= "tool_result" (:type %)) blocks))

;; --- 转换函数 ---

(defn normalize-content
  "统一 content 格式：string -> [{:type text :text s}]，blocks -> blocks。"
  [content]
  (cond
    (string? content) [(text-block content)]
    (sequential? content) content
    :else [(text-block (str content))]))

(defn filter-for-api
  "过滤 content blocks，只保留 API 支持的类型（text, image）。
   用于 tool_result 的 content 字段。"
  [blocks]
  (filterv #(contains? #{"text" "image"} (:type %)) blocks))

;; --- 辅助函数 ---

(defn has-tool-uses?
  "检查 content blocks 中是否有 tool_use。"
  [blocks]
  (boolean (some #(= "tool_use" (:type %)) blocks)))

(defn has-thinking?
  "检查 content blocks 中是否有 thinking。"
  [blocks]
  (boolean (some #(= "thinking" (:type %)) blocks)))
