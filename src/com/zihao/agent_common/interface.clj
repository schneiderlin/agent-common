(ns com.zihao.agent-common.interface
  (:require [com.zihao.agent-common.content-block :as content-block]
            [com.zihao.agent-common.logger :as logger]
            [com.zihao.agent-common.usage.normalization :as usage-normalization]))

(defn text-block
  [& args]
  (apply content-block/text-block args))
(defn thinking-block
  [& args]
  (apply content-block/thinking-block args))
(defn tool-use-block
  [& args]
  (apply content-block/tool-use-block args))
(defn tool-result-block
  [& args]
  (apply content-block/tool-result-block args))
(defn normalize-content
  [& args]
  (apply content-block/normalize-content args))
(defn extract-text
  [& args]
  (apply content-block/extract-text args))
(defn extract-thinking
  [& args]
  (apply content-block/extract-thinking args))
(defn extract-tool-uses
  [& args]
  (apply content-block/extract-tool-uses args))
(defn extract-tool-results
  [& args]
  (apply content-block/extract-tool-results args))
(defn filter-for-api
  [& args]
  (apply content-block/filter-for-api args))
(defn has-tool-uses?
  [& args]
  (apply content-block/has-tool-uses? args))
(defn has-thinking?
  [& args]
  (apply content-block/has-thinking? args))
(defn debug
  [& args]
  (apply logger/debug args))
(defn info
  [& args]
  (apply logger/info args))
(defn warn
  [& args]
  (apply logger/warn args))
(defn error
  [& args]
  (apply logger/error args))
(defn set-log-level!
  [& args]
  (apply logger/set-log-level! args))
(defn clear-log
  [& args]
  (apply logger/clear-log args))
(defn read-log
  [& args]
  (apply logger/read-log args))
(defn normalize-usage
  [& args]
  (apply usage-normalization/normalize-usage args))
