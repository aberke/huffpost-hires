(ns huffpost-hires.util
	(require [clojure.string :as string-helper]))


(defn string->number-or-0
	"Convert n to a number if its not already a number, or return 0 if has non numeric characters"
	[n]
	(let [n (if (string? n) (read-string n) n)]
		(if (number? n) n 0)))

(defn string->number
	"Convert n to a number if its not already a number, or return nil if has non numeric characters"
	[n]
	(let [n (if (string? n) (read-string n) n)]
		(if (number? n) n nil)))


(defn handle-exception
	"Helper to print out stack trace upon exception"
	[function exception]
	(println (str "EXCEPTION in function " function ": " exception))
		(.printStackTrace (.getCause exception))
		false)

(defn string->sql-safe
	"Helper to use before inserting item in a SQL statement
	Escapes special characters, namely, ' "
	[string]
	(string-helper/replace string #"'" "''"))
