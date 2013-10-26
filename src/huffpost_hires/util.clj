(ns huffpost-hires.util)


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