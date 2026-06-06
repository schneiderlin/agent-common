(ns com.zihao.agent-common.content-block
  "Unified Content Block handling: creation, extraction, and conversion.
   Pure functions that can be unit tested directly."
  (:require [clojure.string :as str]))

;; --- Constructors ---

(defn text-block [s]
  {:type "text" :text s})

(defn thinking-block [s]
  {:type "thinking" :thinking s})

(defn tool-use-block [id name input]
  {:type "tool_use" :id id :name name :input input})

(defn tool-result-block [tool-use-id content-blocks]
  {:type "tool_result" :tool_use_id tool-use-id :content (vec content-blocks)})

;; --- Extractors ---

(defn extract-text
  "Extracts text from all text blocks in content blocks and joins it into a string."
  [blocks]
  (->> blocks
       (filter #(= "text" (:type %)))
       (map :text)
       (str/join "")))

(defn extract-thinking
  "Extracts content from all thinking blocks in content blocks and joins it into a string."
  [blocks]
  (->> blocks
       (filter #(= "thinking" (:type %)))
       (map :thinking)
       (str/join "")))

(defn extract-tool-uses
  "Extracts all tool_use blocks from content blocks."
  [blocks]
  (filterv #(= "tool_use" (:type %)) blocks))

(defn extract-tool-results
  "Extracts all tool_result blocks from content blocks."
  [blocks]
  (filterv #(= "tool_result" (:type %)) blocks))

;; --- Converters ---

(defn normalize-content
  "Normalizes content format: string -> [{:type text :text s}], blocks -> blocks."
  [content]
  (cond
    (string? content) [(text-block content)]
    (sequential? content) content
    :else [(text-block (str content))]))

(defn filter-for-api
  "Filters content blocks, keeping only API-supported types: text and image.
   Used for the content field of tool_result."
  [blocks]
  (filterv #(contains? #{"text" "image"} (:type %)) blocks))

;; --- Helpers ---

(defn has-tool-uses?
  "Checks whether content blocks contain a tool_use."
  [blocks]
  (boolean (some #(= "tool_use" (:type %)) blocks)))

(defn has-thinking?
  "Checks whether content blocks contain thinking."
  [blocks]
  (boolean (some #(= "thinking" (:type %)) blocks)))
