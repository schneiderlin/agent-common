(ns com.zihao.agent-common.logger
  "Logging utility for the application.
   Logs are written to ./.debug.log file in the current working directory."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def log-file-path
  "Path to the log file relative to the current working directory."
  ".debug.log")

(def ^:private log-levels
  "Priority order for log levels: lower number = higher priority"
  {:debug 0
   :info 1
   :warn 2
   :error 3})

(def ^:dynamic *current-log-level*
  "Current log level. Messages at or above this level will be logged."
  :debug)

(defn enabled?
  "Return true when messages at `level` would be emitted under the current log level."
  [level]
  (let [message-priority (get log-levels level)
        current-priority (get log-levels *current-log-level*)]
    (and (some? message-priority)
         (some? current-priority)
         (>= message-priority current-priority))))

(defn- format-log-entry
  "Format a log entry with timestamp and level."
  [level message]
  (let [timestamp (java.time.LocalDateTime/now)
        formatted-time (.format timestamp
                               (java.time.format.DateTimeFormatter/ofPattern
                                "yyyy-MM-dd HH:mm:ss.SSS"))]
    (str formatted-time " [" (str/upper-case (name level)) "] " message "\n")))

(defn- ensure-log-file
  "Ensure the log file exists and can be written to."
  []
  (let [file (io/file log-file-path)]
    (when-not (.exists file)
      (.createNewFile file))
    file))

(defn write-log
  "Write a log entry to the log file.
   Parameters:
   - level: :debug, :info, :warn, or :error
   - message: the log message (can be any value, will be converted to string)"
  [level message]
  (try
    (let [entry (format-log-entry level (str message))
          file (ensure-log-file)]
      ;; Append to the log file with UTF-8 encoding
      (with-open [^java.io.BufferedWriter writer (io/writer file :append true :encoding "UTF-8")]
        (.write writer ^String entry))
      entry)
    (catch Exception e
      ;; If logging fails, fall back to stdout
      (println "[LOGGER ERROR]" (.getMessage e))
      (println (format-log-entry level (str message))))))

(defn log
  "Log a message if the level is at or above the current log level.
   Parameters:
   - level: :debug, :info, :warn, or :error
   - message: the log message (can be any value, will be converted to string)"
  [level message]
  (when (enabled? level)
    (write-log level message)))

(defn debug
  "Log a debug message."
  [message]
  (log :debug message))

(defn debug-enabled?
  "Return true when debug messages would be emitted."
  []
  (enabled? :debug))

(defn info
  "Log an info message."
  [message]
  (log :info message))

(defn warn
  "Log a warning message."
  [message]
  (log :warn message))

(defn error
  "Log an error message."
  [message]
  (log :error message))

(defn set-log-level!
  "Set the current log level.
   level: :debug, :info, :warn, or :error"
  [level]
  (when (contains? log-levels level)
    (alter-var-root #'*current-log-level* (constantly level))))

(defn clear-log
  "Clear the log file by deleting and recreating it."
  []
  (let [file (io/file log-file-path)]
    (when (.exists file)
      (.delete file)
      (.createNewFile file))
    (info "Log file cleared")))

(defn read-log
  "Read the entire contents of the log file.
   Returns nil if the file doesn't exist."
  []
  (let [file (io/file log-file-path)]
    (when (.exists file)
      (slurp file))))

(comment
  ;; Test logging
  (debug "This is a debug message")
  (info "This is an info message")
  (warn "This is a warning message")
  (error "This is an error message")

  ;; Test with different data types
  (debug {:key "value" :number 42})
  (info [1 2 3 4 5])
  (error {:error "Something went wrong" :code 500})

  ;; Read the log file
  (read-log)

  ;; Clear the log file
  (clear-log)

  ;; Set log level
  (set-log-level! :info)
  (debug "This won't be logged")  ; Won't appear because log level is :info
  (info "This will be logged")    ; Will appear
  :rcf)
