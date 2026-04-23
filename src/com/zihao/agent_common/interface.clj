(ns com.zihao.agent-common.interface
  (:require [com.zihao.agent-common.content-block :as content-block]
            [com.zihao.agent-common.logger :as logger]
            [com.zihao.agent-common.usage.normalization :as usage-normalization]))

(defn text-block
  [s]
  (content-block/text-block s))
(defn thinking-block
  [s]
  (content-block/thinking-block s))
(defn tool-use-block
  [id name input]
  (content-block/tool-use-block id name input))
(defn tool-result-block
  [tool-use-id content-blocks]
  (content-block/tool-result-block tool-use-id content-blocks))
(defn normalize-content
  [content]
  (content-block/normalize-content content))
(defn extract-text
  [blocks]
  (content-block/extract-text blocks))
(defn extract-thinking
  [blocks]
  (content-block/extract-thinking blocks))
(defn extract-tool-uses
  [blocks]
  (content-block/extract-tool-uses blocks))
(defn extract-tool-results
  [blocks]
  (content-block/extract-tool-results blocks))
(defn filter-for-api
  [blocks]
  (content-block/filter-for-api blocks))
(defn has-tool-uses?
  [blocks]
  (content-block/has-tool-uses? blocks))
(defn has-thinking?
  [blocks]
  (content-block/has-thinking? blocks))
(defn debug
  [message]
  (logger/debug message))
(defn debug-enabled?
  []
  (logger/debug-enabled?))
(defn info
  [message]
  (logger/info message))
(defn warn
  [message]
  (logger/warn message))
(defn error
  [message]
  (logger/error message))
(defn set-log-level!
  [level]
  (logger/set-log-level! level))
(defn clear-log
  []
  (logger/clear-log))
(defn read-log
  []
  (logger/read-log))
(defn normalize-usage
  ([usage] (usage-normalization/normalize-usage usage))
  ([backend usage] (usage-normalization/normalize-usage backend usage)))
