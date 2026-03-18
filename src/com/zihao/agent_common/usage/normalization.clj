(ns com.zihao.agent-common.usage.normalization
  "Provider-agnostic usage normalization.
   Single source of truth for mapping provider usage payloads into internal
   keys used by thread aggregation/UI.")

(def ^:private base-empty-usage
  {:input_tokens 0
   :output_tokens 0
   :cache_creation_tokens 0
   :cache_read_tokens 0})

(defn normalize-usage
  "Normalize provider usage map to internal keys:
   {:input_tokens :output_tokens :cache_creation_tokens :cache_read_tokens ...extra}

   backend is accepted for call-site readability and future migration, but does
   not alter current behavior."
  ([usage]
   (normalize-usage nil usage))
  ([backend usage]
   (let [_ backend]
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
                preserved-extra))))))
