(ns com.zihao.agent-common.usage.normalization-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.zihao.agent-common.usage.normalization :as usage-normalization]))

(defn run-tests []
  (clojure.test/run-tests 'com.zihao.agent-common.usage.normalization-test))

(deftest normalize-usage-openai-chat-completions
  (testing "maps prompt/completion tokens and cached prompt details"
    (is (= {:input_tokens 217
            :output_tokens 75
            :cache_creation_tokens 0
            :cache_read_tokens 3200
            :provider-meta {:region "test"}}
           (usage-normalization/normalize-usage
            :openai
            {:prompt_tokens 217
             :completion_tokens 75
             :prompt_tokens_details {:cached_tokens 3200}
             :provider-meta {:region "test"}})))))

(deftest normalize-usage-openai-responses
  (testing "maps input/output and cached input details"
    (is (= {:input_tokens 20
            :output_tokens 5
            :cache_creation_tokens 0
            :cache_read_tokens 300}
           (usage-normalization/normalize-usage
            :openai-codex53
            {:input_tokens 20
             :output_tokens 5
             :input_tokens_details {:cached_tokens 300}})))))

(deftest normalize-usage-anthropic-cache-fields
  (testing "maps anthropic cache *_input_tokens keys"
    (is (= {:input_tokens 100
            :output_tokens 20
            :cache_creation_tokens 30
            :cache_read_tokens 400
            :provider-meta {:region "test"}}
           (usage-normalization/normalize-usage
            :anthropic
            {:input_tokens 100
             :output_tokens 20
             :cache_creation_input_tokens 30
             :cache_read_input_tokens 400
             :provider-meta {:region "test"}})))))

(deftest normalize-usage-without-backend-preserves-extra-fields
  (testing "supports generic normalization callsite with nil backend"
    (is (= {:input_tokens 9
            :output_tokens 0
            :cache_creation_tokens 0
            :cache_read_tokens 0
            :provider-meta {:region "test"}}
           (usage-normalization/normalize-usage
            {:input_tokens 9
             :provider-meta {:region "test"}})))))

(deftest normalize-usage-non-map-input
  (testing "returns nil for nil or non-map usage"
    (is (nil? (usage-normalization/normalize-usage nil)))
    (is (nil? (usage-normalization/normalize-usage :openai nil)))
    (is (nil? (usage-normalization/normalize-usage :openai [])))))
