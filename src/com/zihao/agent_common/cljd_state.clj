(ns com.zihao.agent-common.cljd-state
  "Pure functions shared with ClojureDart state.cljd (JVM mirror for testing).
   Keep in sync with cljd-app/src/my_agent/state.cljd json->state-delta.")

(defn json->state-delta
  "Pure function: SSE JSON map -> state delta map (keyword keys).
   Returns nil if no 'core' key found.
   Supports both legacy flat fields and new per-thread thread_states."
  [json-map]
  (when-let [core (get json-map "core")]
    (let [sentinel ::not-found
          ci (get core "conversation_items" sentinel)
          lr (get core "llm_running" sentinel)
          sp (get core "system_prompt" sentinel)
          vw (get core "view" sentinel)
          ti (get core "thread_index" sentinel)
          td (get core "thread_detail" sentinel)
          at (get core "active_thread_id" sentinel)
          ts (get core "thread_states" sentinel)]
      (cond-> {}
        (not= ci sentinel) (assoc :conversation-items ci)
        (not= lr sentinel) (assoc :llm-running lr)
        (not= sp sentinel) (assoc :system-prompt sp)
        (not= vw sentinel) (assoc :view vw)
        (not= ti sentinel) (assoc :thread-index ti)
        (not= td sentinel) (assoc :thread-detail td)
        (not= at sentinel) (assoc :active-thread-id at)
        (not= ts sentinel) (assoc :thread-states ts)))))
