# BiDirectionalMapper

[![Clojars Project](https://img.shields.io/clojars/v/bdm.svg)](https://clojars.org/bdm)
![ci](https://github.com/aitem/bdm/workflows/CI/badge.svg)


BDM is super simple, fully declarative bi directional mapper that allow convert your data from `A` to `B` and back.

## Usage

``` clj
(ns user
  (:require [bdm.core as bdm]))
  
(def person
  {:name "John"
   :lastname "Smith"
   :contacts [{:type "email"
               :value "john@smith.com"}
              {:type "phone"
               :value "122233344"}]})

(def mapping
  [[[:name]]
   [[:lastname]]
   [[:emails] [:contacts {:get [:= :type "email"]} :#]]
   [[:phones] [:contacts {:get [:= :type "phone"]} :#]]])

(bdm/import person mapping)

;; Will return 
;; {:name "John",
;;  :lastname "Smith",
;;  :emails [{:type "email", :value "john@smith.com"}],
;;  :phones [{:type "phone", :use "home", :value "122233344"}
;;           {:type "phone", :use "vork", :value "122233344"}]}
  
  
;; We can do import/export roundtrip

(= person (bdm/export (bdm/import person mapping) mapping)) ;; => true

```

## Concepts

### Short alias
...

### Getters and setters
...

### Predicates 
...

### Submapping 
...


## Develop

```
# run repl
$ make repl

# run test 
$ make test 
```


MIT License Copyright (c) 2020 Marat Surmashev
