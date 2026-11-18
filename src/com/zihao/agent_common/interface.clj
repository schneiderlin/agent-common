(ns com.zihao.agent-common.interface
  "Shared primitives for every agent brick: content blocks, the debug log and
   provider usage normalization."
  (:require [com.zihao.agent-common.content-block :as content-block]
            [com.zihao.agent-common.logger :as logger]
            [com.zihao.agent-common.usage.normalization :as usage-normalization]))

(defn text-block
  "Builds one Anthropic-style text content block.

  Parameters:
  - `s` — the text; stored as given, any type.

  Returns: `{:type \"text\" :text s}`."
  [s]
  (content-block/text-block s))

(defn thinking-block
  "Builds one extended-thinking content block.

  Parameters:
  - `s` — the thinking text.

  Returns: `{:type \"thinking\" :thinking s}`."
  [s]
  (content-block/thinking-block s))

(defn tool-use-block
  "Builds one assistant tool-call content block.

  Parameters:
  - `id` — provider tool-use id, echoed back on the matching result.
  - `name` — tool name the caller dispatches on.
  - `input` — tool arguments map.

  Returns: `{:type \"tool_use\" :id id :name name :input input}`."
  [id name input]
  (content-block/tool-use-block id name input))

(defn tool-result-block
  "Builds one tool result content block.

  Parameters:
  - `tool-use-id` — id of the `tool-use-block` this answers.
  - `content-blocks` — resulting blocks; stored as a vector so a single block
    and a seq produce the same shape.

  Returns: `{:type \"tool_result\" :tool_use_id tool-use-id :content [...]}`."
  [tool-use-id content-blocks]
  (content-block/tool-result-block tool-use-id content-blocks))

(defn normalize-content
  "Coerces a message's content into a vector of content blocks.

  Parameters:
  - `content` — a string, a sequential collection of blocks, or any other
    value.

  Returns: `[{:type \"text\" :text content}]` for a string, the collection
  unchanged (not vectorized) when sequential, and one text block of the printed
  value otherwise."
  [content]
  (content-block/normalize-content content))

(defn extract-text
  "Concatenates the text of every text block.

  Parameters:
  - `blocks` — seq of content blocks.

  Returns: the joined text, without separators; `\"\"` when there are no text
  blocks."
  [blocks]
  (content-block/extract-text blocks))

(defn extract-thinking
  "Concatenates the thinking of every thinking block.

  Parameters:
  - `blocks` — seq of content blocks.

  Returns: the joined thinking text; `\"\"` when there are no thinking blocks."
  [blocks]
  (content-block/extract-thinking blocks))

(defn extract-tool-uses
  "Keeps only the tool-call blocks.

  Parameters:
  - `blocks` — seq of content blocks.

  Returns: vector of `tool_use` blocks in input order."
  [blocks]
  (content-block/extract-tool-uses blocks))

(defn extract-tool-results
  "Keeps only the tool-result blocks.

  Parameters:
  - `blocks` — seq of content blocks.

  Returns: vector of `tool_result` blocks in input order."
  [blocks]
  (content-block/extract-tool-results blocks))

(defn filter-for-api
  "Keeps only the block types an API accepts inside a tool result.

  Parameters:
  - `blocks` — seq of content blocks.

  Returns: vector of the `text` and `image` blocks, in input order."
  [blocks]
  (content-block/filter-for-api blocks))

(defn has-tool-uses?
  "Tests whether a content collection contains a tool call.

  Parameters:
  - `blocks` — seq of content blocks.

  Returns: true when at least one block has `:type \"tool_use\"`."
  [blocks]
  (content-block/has-tool-uses? blocks))

(defn has-thinking?
  "Tests whether a content collection contains thinking.

  Parameters:
  - `blocks` — seq of content blocks.

  Returns: true when at least one block has `:type \"thinking\"`."
  [blocks]
  (content-block/has-thinking? blocks))

(defn debug
  "Appends one debug message to the process log.

  Parameters:
  - `message` — the message; any value, printed with `str`.

  Returns: the formatted log entry when the message was written, nil when it was
  filtered by the current log level. A side effect on `./.debug.log`; callers
  ignore the return."
  [message]
  (logger/debug message))

(defn debug-enabled?
  "Tests whether debug messages are currently emitted.

  Parameters: none.

  Returns: true when the current log level is `:debug`."
  []
  (logger/debug-enabled?))

(defn info
  "Appends one info message to the process log.

  Parameters:
  - `message` — the message; any value, printed with `str`.

  Returns: the formatted log entry when written, nil when filtered by the
  current log level. A side effect on `./.debug.log`; callers ignore the return."
  [message]
  (logger/info message))

(defn warn
  "Appends one warning to the process log.

  Parameters:
  - `message` — the message; any value, printed with `str`.

  Returns: the formatted log entry when written, nil when filtered by the
  current log level. A side effect on `./.debug.log`; callers ignore the return."
  [message]
  (logger/warn message))

(defn error
  "Appends one error to the process log.

  Parameters:
  - `message` — the message; any value, printed with `str`.

  Returns: the formatted log entry when written, nil when filtered by the
  current log level. A side effect on `./.debug.log`; callers ignore the return."
  [message]
  (logger/error message))

(defn set-log-level!
  "Sets the process log level for the rest of the process.

  Parameters:
  - `level` — `:debug`, `:info`, `:warn`, or `:error`; messages below it are
    dropped.

  Returns: the new level when it was accepted, nil when `level` is not one of the
  four (the previous level then stays in force)."
  [level]
  (logger/set-log-level! level))

(defn clear-log
  "Empties the process log file and records that it was cleared.

  Parameters: none.

  Returns: the formatted `\"Log file cleared\"` entry, or nil when the info level
  is filtered. Deleting and recreating the file is the side effect callers rely
  on."
  []
  (logger/clear-log))

(defn read-log
  "Reads the whole process log file.

  Parameters: none.

  Returns: the file contents as a string, or nil when the file does not exist."
  []
  (logger/read-log))

(defn normalize-usage
  "Normalizes one provider usage payload to the stable internal keys.

  Field normalization only: it preserves cache fields under stable names without
  deciding whether they count as extra input tokens or as context-window
  accounting metadata.

  Parameters:
  - `backend` — backend label accepted for call-site readability; it selects no
    behavior and must not be read as an accounting policy.
  - `usage` — provider usage map, keyword keys:
    - `:input_tokens` / `:prompt_tokens` — defaults to 0.
    - `:output_tokens` / `:completion_tokens` — defaults to 0.
    - `:cache_creation_tokens` / `:cache_creation_input_tokens` — defaults to 0.
    - `:cache_read_tokens` / `:cache_read_input_tokens` /
      `[:input_tokens_details :cached_tokens]` /
      `[:prompt_tokens_details :cached_tokens]` — defaults to 0.
    - any other key — preserved under its own name.

  Returns: `{:input_tokens :output_tokens :cache_creation_tokens
  :cache_read_tokens ...preserved extras}`, or nil when `usage` is not a map."
  ([usage] (usage-normalization/normalize-usage usage))
  ([backend usage] (usage-normalization/normalize-usage backend usage)))

(defn normalize-provider-usage
  "Normalizes one provider usage payload to the stable internal keys.

  Parameters:
  - `usage` — provider usage map; the recognized keys are those listed on
    `normalize-usage`, and unrecognized keys are preserved.

  Returns: `{:input_tokens :output_tokens :cache_creation_tokens
  :cache_read_tokens ...preserved extras}`, or nil when `usage` is not a map.
  Prefer this over the two-argument `normalize-usage` at new call sites."
  [usage]
  (usage-normalization/normalize-provider-usage usage))
