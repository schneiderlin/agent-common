(ns com.zihao.agent-common.usage.normalization
  "Provider field normalization.

   This namespace maps provider usage payload fields into stable internal keys
   used by raw thread usage aggregation. It does not define context-window
   accounting; backend/model-aware context semantics live in agent-runtime.")

(def ^:private base-empty-usage
  {:input_tokens 0
   :output_tokens 0
   :cache_creation_tokens 0
   :cache_read_tokens 0})

(defn normalize-provider-usage
  "Normalize provider usage map to internal keys:
   {:input_tokens :output_tokens :cache_creation_tokens :cache_read_tokens ...extra}

   This is field normalization only. It preserves provider cache fields under
   stable names without deciding whether those fields are extra input tokens or
   metadata for context-window accounting."
  [usage]
  (when (map? usage)
    (let [input-tokens (or (:input_tokens usage)
                           (:prompt_tokens usage)
                           0)
          output-tokens (or (:output_tokens usage)
                            (:completion_tokens usage)
                            0)
          cache-creation-tokens (or (:cache_creation_tokens usage)
                                    (:cache_creation_input_tokens usage)
                                    0)
          cache-read-tokens (or (:cache_read_tokens usage)
                                (:cache_read_input_tokens usage)
                                (get-in usage [:input_tokens_details :cached_tokens])
                                (get-in usage [:prompt_tokens_details :cached_tokens])
                                0)
          preserved-extra (apply dissoc usage
                                 [:input_tokens :prompt_tokens
                                  :output_tokens :completion_tokens
                                  :cache_creation_tokens :cache_creation_input_tokens
                                  :cache_read_tokens :cache_read_input_tokens
                                  :input_tokens_details :prompt_tokens_details :completion_tokens_details])]
      (merge base-empty-usage
             {:input_tokens input-tokens
              :output_tokens output-tokens
              :cache_creation_tokens cache-creation-tokens
              :cache_read_tokens cache-read-tokens}
             preserved-extra))))

(defn normalize-usage
  "Backward-compatible alias for provider field normalization.

   The two-arg arity accepts backend for call-site readability only. Backend
   must not be interpreted here as context-window accounting policy."
  ([usage]
   (normalize-provider-usage usage))
  ([backend usage]
   (let [_ backend]
     (normalize-provider-usage usage))))
